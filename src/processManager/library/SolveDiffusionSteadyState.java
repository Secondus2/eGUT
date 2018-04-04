package processManager.library;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import dataIO.ObjectFactory;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.Reaction;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.subvoxel.CoordinateMap;
import solver.PDEmultigrid;
import solver.PDEupdater;

/**
 * \brief Simulate the diffusion of solutes and their production/consumption by
 * reactions in a steady-state manner, in a spatial {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionSteadyState extends ProcessDiffusion
{
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * 
	 * Initiation from protocol file: 
	 * 
	 * TODO verify and finalise
	 */
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);

		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEmultigrid();

	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		/*
		 * Do the generic set up and solving.
		 */
		super.internalStep();
		/*
		 * Estimate the steady-state mass flows in or out of the well-mixed
		 * region, and distribute it among the relevant boundaries.
		 */
		this._environment.distributeWellMixedFlows();
		/*
		 * Estimate agent growth based on the steady-state solute 
		 * concentrations.
		 */
		for ( Agent agent : this._agents.getAllLocatedAgents() )
			this.applyAgentGrowth(agent);

		/* perform final clean-up and update agents to represent updated 
		 * situation. */
		this.postStep();
	}
	
	/* ***********************************************************************
	 * INTERNAL METHODS
	 * **********************************************************************/
	
	/**
	 * \brief The standard PDE updater method resets the solute
	 * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
	 * {@code Agent}s to grow.
	 * 
	 * @return PDE updater method.
	 */
	protected PDEupdater standardUpdater()
	{
		return new PDEupdater()
		{
			/*
			 * This is the updater method that the PDEsolver will use before
			 * each mini-timestep.
			 */
			@Override
			public void prestep(Collection<SpatialGrid> variables, double dt)
			{
				for ( SpatialGrid var : variables )
					var.newArray(PRODUCTIONRATE);
				applyEnvReactions(variables);
				for ( Agent agent : _agents.getAllLocatedAgents() )
					applyAgentReactions(agent, variables);
			}
		};
	}
	
	/**
	 * \brief Apply the reactions for a single agent.
	 * 
	 * <p><b>Note</b>: this method assumes that the volume distribution map
	 * of this agent has already been calculated. This is typically done just
	 * once per process manager step, rather than at every PDE solver
	 * relaxation.</p>
	 * 
	 * @param agent Agent assumed to have reactions (biomass will not be
	 * altered by this method).
	 * @param variables Collection of spatial grids assumed to be the solutes.
	 */
	private void applyAgentReactions(
			Agent agent, Collection<SpatialGrid> variables)
	{
		/*
		 * Get the agent's reactions: if it has none, then there is nothing
		 * more to do.
		 */
		@SuppressWarnings("unchecked")
		List<Reaction> reactions = 
				(List<Reaction>) agent.getValue(XmlRef.reactions);
		if ( reactions == null )
			return;
		/*
		 * Get the distribution map and scale it so that its contents sum up to
		 * one.
		 */
		Shape shape = variables.iterator().next().getShape();
		@SuppressWarnings("unchecked")
		Map<Shape, CoordinateMap> mapOfMaps = (Map<Shape, CoordinateMap>)
						agent.getValue(VOLUME_DISTRIBUTION_MAP);
		CoordinateMap distributionMap = mapOfMaps.get(shape);
		distributionMap.scale();
		/*
		 * Get the agent biomass kinds as a map. Copy it now so that we can
		 * use this copy to store the changes.
		 */
		Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
		/*
		 * Now look at all the voxels this agent covers.
		 */
		Map<String,Double> concns = new HashMap<String,Double>();
		Map<String,Double> stoichiometry;
		SpatialGrid solute;
		double concn, rate, productRate, volume, perVolume;
		for ( int[] coord : distributionMap.keySet() )
		{
			volume = shape.getVoxelVolume(coord);
			perVolume = 1.0/volume;
			for ( Reaction r : reactions )
			{
				/* 
				 * Build the dictionary of variable values. Note that these 
				 * will likely overlap with the names in the reaction 
				 * stoichiometry (handled after the reaction rate), but will 
				 * not always be the same. Here we are interested in those that
				 * affect the reaction, and not those that are affected by it.
				 */
				concns.clear();
				for ( String varName : r.getConstituentNames() )
				{
					solute = FindGrid(variables, varName);
					if ( solute != null )
						concn = solute.getValueAt(CONCN, coord);
					else if ( biomass.containsKey(varName) )
					{
						concn = biomass.get(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else if ( agent.isAspect(varName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						concn = agent.getDouble(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else
					{
						// TODO safety?
						concn = 0.0;
					}
					concns.put(varName, concn);
				}
				/*
				 * Calculate the reaction rate based on the variables just 
				 * retrieved.
				 */
				rate = r.getRate(concns);
				/* 
				 * Now that we have the reaction rate, we can distribute the 
				 * effects of the reaction. Note again that the names in the 
				 * stoichiometry may not be the same as those in the reaction
				 * variables (although there is likely to be a large overlap).
				 */
				stoichiometry = r.getStoichiometry();
				for ( String productName : stoichiometry.keySet() )
				{
					productRate = rate * stoichiometry.get(productName);
					solute = FindGrid(variables, productName);
					if ( solute != null )
						solute.addValueAt(PRODUCTIONRATE, coord, productRate);
					/* 
					 * Unlike in a transient solver, we do not update the agent
					 * mass here.
					 */
				}
			}
		}
	}
	
	private SpatialGrid FindGrid(Collection<SpatialGrid> grids, String name)
	{
		for ( SpatialGrid grid : grids )
			if ( grid.getName().equals(name) )
				return grid;
		return null;
	}
	
	private void applyAgentGrowth(Agent agent)
	{
		/*
		 * Get the agent's reactions: if it has none, then there is nothing
		 * more to do.
		 */
		@SuppressWarnings("unchecked")
		List<Reaction> reactions = 
				(List<Reaction>) agent.getValue(XmlRef.reactions);
		if ( reactions == null )
			return;
		/*
		 * Get the distribution map and scale it so that its contents sum up to
		 * one.
		 */
		@SuppressWarnings("unchecked")
		Map<Shape, CoordinateMap> mapOfMaps = (Map<Shape, CoordinateMap>)
						agent.getValue(VOLUME_DISTRIBUTION_MAP);
		CoordinateMap distributionMap = 
				mapOfMaps.get(agent.getCompartment().getShape());
		distributionMap.scale();
		/*
		 * Get the agent biomass kinds as a map. Copy it now so that we can
		 * use this copy to store the changes.
		 */
		Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
		@SuppressWarnings("unchecked")
		Map<String,Double> newBiomass = (HashMap<String,Double>)
				ObjectFactory.copy(biomass);
		/*
		 * Now look at all the voxels this agent covers.
		 */
		Map<String,Double> concns = new HashMap<String,Double>();
		Map<String,Double> stoichiometry;
		SpatialGrid solute;
		Shape shape = this._agents.getShape();
		double concn, rate, productRate, volume, perVolume;
		for ( int[] coord : distributionMap.keySet() )
		{
			volume = shape.getVoxelVolume(coord);
			perVolume = 1.0/volume;
			for ( Reaction r : reactions )
			{
				/* 
				 * Build the dictionary of variable values. Note that these 
				 * will likely overlap with the names in the reaction 
				 * stoichiometry (handled after the reaction rate), but will 
				 * not always be the same. Here we are interested in those that
				 * affect the reaction, and not those that are affected by it.
				 */
				concns.clear();
				for ( String varName : r.getConstituentNames() )
				{
					if ( this._environment.isSoluteName(varName) )
					{
						solute = this._environment.getSoluteGrid(varName);
						concn = solute.getValueAt(CONCN, coord);
					}
					else if ( biomass.containsKey(varName) )
					{
						concn = biomass.get(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else if ( agent.isAspect(varName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						concn = agent.getDouble(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else
					{
						// TODO safety?
						concn = 0.0;
					}
					concns.put(varName, concn);
				}
				/*
				 * Calculate the reaction rate based on the variables just 
				 * retrieved.
				 */
				rate = r.getRate(concns);
				/* 
				 * Now that we have the reaction rate, we can distribute the 
				 * effects of the reaction. Note again that the names in the 
				 * stoichiometry may not be the same as those in the reaction
				 * variables (although there is likely to be a large overlap).
				 */
				stoichiometry = r.getStoichiometry();
				for ( String productName : stoichiometry.keySet() )
				{
					productRate = rate * stoichiometry.get(productName);
					if ( agent.isAspect(productName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						newBiomass.put(productName, agent.getDouble(productName)
								+ (productRate * this._timeStepSize * volume));
					}
				}
			}
		}
		ProcessMethods.updateAgentMass(agent, newBiomass);
	}
}
