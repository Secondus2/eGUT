/**
 * 
 */
package processManager.library;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import bookkeeper.KeeperEntry.EventType;
import boundary.Boundary;
import boundary.SpatialBoundary;
import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.Global;
import idynomics.Idynomics;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.Reaction;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.ShapeLibrary.Dimensionless;
import shape.subvoxel.IntegerArray;
import solver.PDEexplicit;
import solver.PDEupdater;
import utility.Helper;

/**
 * \brief Simulate the diffusion of solutes and their production/consumption by
 * reactions in a time-dependent manner, in a spatial {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionTransient extends ProcessDiffusion
{
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);

		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEexplicit();

		this._solver.setUpdater(this);
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
		
		for ( SpatialGrid var : this._environment.getSolutes() )
		{
			var.applyTransportFlux();
		}
		
		/*
		 * If any mass has flowed in or out of the well-mixed region,
		 * distribute it among the relevant boundaries.
		 */
		this._environment.distributeWellMixedFlows(this._timeStepSize);

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
	public void prestep(Collection<SpatialGrid> variables, double dt)
	{
		for ( SpatialGrid var : variables )
			var.newArray(PRODUCTIONRATE);
		applyEnvReactions(variables);
		for ( Agent agent : _agents.getAllLocatedAndEpithelialAgents() )
			applyAgentReactions(agent, dt);
	}
	
	/**
	 * \brief Apply the reactions for a single agent.
	 * 
	 * <p><b>Note</b>: this method assumes that the volume distribution map
	 * of this agent has already been calculated. This is typically done just
	 * once per process manager step, rather than at every PDE solver
	 * mini-timestep.</p>
	 * 
	 * <p>Note also that here the solute grids PRODUCTIONRATE arrays are 
	 * updated, and the agent's biomass is updated immediately after all
	 * relevant voxels have been visited. This is a different approach to the
	 * one taken in SolveChemostat, where applyAgentReactions is split into two
	 * methods.</p>
	 * 
	 * @param agent Agent assumed to have reactions (biomass will be altered by
	 * this method).
	 * @param dt Length of the mini time-step to use.
	 */
	private void applyAgentReactions(Agent agent, double dt)
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
		Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>)
						agent.getValue(VOLUME_DISTRIBUTION_MAP);
		HashMap<IntegerArray,Double> distributionMap = 
				mapOfMaps.get(agent.getCompartment().getShape());
		ProcessDiffusion.scale(distributionMap, 1.0);
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

		SpatialGrid solute;
		Shape shape = this._agents.getShape();
		double concn, productRate, volume, perVolume;
		
		for ( IntegerArray coord : distributionMap.keySet() )
		{

			volume = shape.getVoxelVolume(coord.get());
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
					
					if (varName.contains("@"))
					{
						Collection<SpatialBoundary> collidingBoundaries = this._agents.
								boundarySearch(agent, Double.MIN_VALUE);
						String[] splitString = varName.split("@");
						String constituent = splitString[0];
						String compartmentName = splitString[1];
						if (!(Idynomics.simulator.getCompartment(
								compartmentName) == null))
						{
							Compartment compartment = Idynomics.simulator.
									getCompartment(compartmentName);
							
							EnvironmentContainer partnerEnvironment =
									compartment.environment;
							
							Shape partnerShape = compartment.getShape();
							
							/**
							 * Check that the compartment referenced is not this
							 * compartment. If it is, nothing is done as the
							 * solute concentrations have already been recorded.
							 */
							if (compartment != this.getParent()) 
							{
								
								/**
								 * Check whether the referenced compartment is
								 * connected to this compartment via a Boundary
								 */
								boolean connectedCompartment= false;
								for (Boundary b : collidingBoundaries)
								{
									if (!(Helper.isNullOrEmpty(b)))
									{
										if (b.getPartnerCompartmentName().
												contentEquals(compartmentName)) 
										{
											connectedCompartment = true;
										}
										
										else
										{
											if (Log.shouldWrite(Tier.DEBUG))
												Log.out(Tier.DEBUG,
													"Reaction requires boundary"
													+ "connection between "
													+ this._compartmentName +
													" and " + compartmentName +
													". No such connection "
													+ "exists.");
										}
									}
								}
								
								/**
								 * Check whether the referenced compartment is
								 * dimensionless (reactions between two spatial
								 * compartments is not possible as compartments
								 * are solved separately)
								 */
								boolean dimensionlessPartner = false;
								if (partnerShape instanceof Dimensionless)
								{
									dimensionlessPartner = true;
								}
										
								else
								{
									if (Log.shouldWrite(Tier.DEBUG))
										Log.out(Tier.DEBUG,
											"Transport reaction links spatial "
											+ "compartment to " + 
											compartmentName + ". Transport "
											+ "reactions between two spatial "
											+ "compartments are not supported.");
								}
								
								/**
								 * Check whether the compartment has a solute of
								 * the given name
								 */
								if (partnerEnvironment.isSoluteName(constituent)) 
								{
									if (dimensionlessPartner && 
											connectedCompartment) 
									{
										solute = partnerEnvironment.
											getSoluteGrid(constituent);
										
										concn = partnerEnvironment.
												getAverageConcentration(
														constituent);
										
										concns.put(varName, concn);
										
										
									}
								}

								else 
								{
									if (Log.shouldWrite(Tier.DEBUG))
										Log.out(Tier.DEBUG,
												"Reaction specifies " + 
										"non-existent solute, " + constituent);
								} 
							}
							
							else
							{
								if ( this._environment.isSoluteName(varName) )
								{
									solute = this._environment.getSoluteGrid(varName);
									concn = solute.getValueAt(CONCN, coord.get());
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
						}
						
						else
						{
							if( Log.shouldWrite(Tier.DEBUG) )
								Log.out(Tier.DEBUG, "Reaction specifies "
										+ "non-existent compartent" + 
										compartmentName);
						}
					}
					
					else
					{
					
						if ( this._environment.isSoluteName(varName) )
						{
							solute = this._environment.getSoluteGrid(varName);
							concn = solute.getValueAt(CONCN, coord.get());
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
				}
				
				/* 
				 * Now that we have the reaction rate, we can distribute the 
				 * effects of the reaction. Note again that the names in the 
				 * stoichiometry may not be the same as those in the reaction
				 * variables (although there is likely to be a large overlap).
				 */
				
				for ( String product : r.getReactantNames() )
				{
					double quantity;
					productRate = r.getProductionRate(concns,product);
					if (product.contains("@"))
					{
						Collection<SpatialBoundary> collidingBoundaries = this._agents.
								boundarySearch(agent, Double.MIN_VALUE);
						String[] splitString = product.split("@");
						String constituent = splitString[0];
						String compartmentName = splitString[1];
						if (!(Idynomics.simulator.getCompartment(
								compartmentName) == null))
						{
							Compartment compartment = Idynomics.simulator.
									getCompartment(compartmentName);
							
							EnvironmentContainer partnerEnvironment =
									compartment.environment;
							
							Shape partnerShape = compartment.getShape();
							
							/**
							 * Check that the compartment referenced is not this
							 * compartment. If it is, nothing is done as the
							 * solute concentrations have already been recorded.
							 */
							if (compartment != this.getParent()) 
							{
								
								/**
								 * Check whether the referenced compartment is
								 * connected to this compartment via a Boundary
								 */
								Boundary transportBoundary = null;
								for (Boundary b : collidingBoundaries)
								{
									if (!(Helper.isNullOrEmpty(b)))
									{
										if (b.getPartnerCompartmentName().
												contentEquals(compartmentName)) 
										{
											transportBoundary = b;
										}
										
										else
										{
											if (Log.shouldWrite(Tier.DEBUG))
												Log.out(Tier.DEBUG,
													"Reaction requires boundary"
													+ "connection between "
													+ this._compartmentName +
													" and " + compartmentName +
													". No such connection "
													+ "exists.");
										}
									}
								}
								
								/**
								 * Check whether the referenced compartment is
								 * dimensionless (reactions between two spatial
								 * compartments is not possible as compartments
								 * are solved separately)
								 */
								boolean dimensionlessPartner = false;
								if (partnerShape instanceof Dimensionless)
								{
									dimensionlessPartner = true;
								}
										
								else
								{
									if (Log.shouldWrite(Tier.DEBUG))
										Log.out(Tier.DEBUG,
											"Transport reaction links spatial "
											+ "compartment to " + 
											compartmentName + ". Transport "
											+ "reactions between two spatial "
											+ "compartments are not supported.");
								}
								
								/**
								 * Check whether the compartment has a solute of
								 * the given name
								 */
								if (partnerEnvironment.isSoluteName(constituent)) 
								{
									if (dimensionlessPartner && 
											transportBoundary != null) 
									{
										solute = this._environment.getSoluteGrid(constituent);
										
										solute.increaseTransportFlux(
												transportBoundary, volume * 
												productRate * this.getTimeStepSize());
									}
								}

								else 
								{
									if (Log.shouldWrite(Tier.DEBUG))
										Log.out(Tier.DEBUG,
												"Reaction specifies " + 
										"non-existent solute, " + constituent);
								} 
							}
							
							else
							{
								if ( this._environment.isSoluteName(constituent) )
								{
									solute = this._environment.getSoluteGrid(constituent);
									quantity = 
											productRate * volume * this.getTimeStepSize();
									solute.addValueAt(PRODUCTIONRATE, coord.get(), quantity
											);
								}
								else if ( newBiomass.containsKey(constituent) )
								{
									quantity = 
											productRate * this.getTimeStepSize() * volume;
									newBiomass.put(constituent, newBiomass.get(constituent)
											+ quantity );
								}
								/* FIXME this can create conflicts if users try to mix mass-
								 * maps and simple mass aspects	 */
								else if ( agent.isAspect(constituent) )
								{
									/*
									 * Check if the agent has other mass-like aspects
									 * (e.g. EPS).
									 */
									quantity = 
											productRate * this.getTimeStepSize() * volume;
									newBiomass.put(constituent, agent.getDouble(constituent)
											+ quantity);
								}
								else
								{
									quantity = 
											productRate * this.getTimeStepSize() * volume;
									//TODO quick fix If not defined elsewhere add it to the map
									newBiomass.put(constituent, quantity);
									System.out.println("agent reaction catched " + 
											constituent);
									// TODO safety?
			
								}
								if( Global.bookkeeping )
									agent.getCompartment().registerBook(
											EventType.REACTION, 
											constituent, 
											String.valueOf( agent.identity() ), 
											String.valueOf( quantity ), null );
							
							}
						}
						
						else
						{
							if( Log.shouldWrite(Tier.DEBUG) )
								Log.out(Tier.DEBUG, "Reaction specifies "
										+ "non-existent compartent" + 
										compartmentName);
						}
					}
					
					else
					{
						if ( this._environment.isSoluteName(product) )
						{
							solute = this._environment.getSoluteGrid(product);
							quantity = 
									productRate * volume * this.getTimeStepSize();
							solute.addValueAt(PRODUCTIONRATE, coord.get(), quantity
									);
						}
						else if ( newBiomass.containsKey(product) )
						{
							quantity = 
									productRate * this.getTimeStepSize() * volume;
							newBiomass.put(product, newBiomass.get(product)
									+ quantity );
						}
						/* FIXME this can create conflicts if users try to mix mass-
						 * maps and simple mass aspects	 */
						else if ( agent.isAspect(product) )
						{
							/*
							 * Check if the agent has other mass-like aspects
							 * (e.g. EPS).
							 */
							quantity = 
									productRate * this.getTimeStepSize() * volume;
							newBiomass.put(product, agent.getDouble(product)
									+ quantity);
						}
						else
						{
							quantity = 
									productRate * this.getTimeStepSize() * volume;
							//TODO quick fix If not defined elsewhere add it to the map
							newBiomass.put(product, quantity);
							System.out.println("agent reaction catched " + 
									product);
							// TODO safety?
	
						}
						if( Global.bookkeeping )
							agent.getCompartment().registerBook(
									EventType.REACTION, 
									product, 
									String.valueOf( agent.identity() ), 
									String.valueOf( quantity ), null );
					
					}
				}
			}
		
		}
		ProcessMethods.updateAgentMass(agent, newBiomass);
	}
}