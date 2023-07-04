package processManager.library;

import static grid.ArrayType.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.Agent;
import agent.Body;
import bookkeeper.KeeperEntry;
import boundary.Boundary;
import boundary.WellMixedBoundary;
import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.Log.Tier;
import idynomics.Global;

import org.w3c.dom.Element;

import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import grid.SpatialGrid;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.Reaction;
import reaction.RegularReaction;
import reaction.RegularReaction.ReactionType;
import reaction.SoluteAtSite;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.subvoxel.IntegerArray;
import solver.mgFas.*;
import utility.Helper;

/**
 * \brief wraps and runs PDE solver
 *
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class PDEWrapper extends ProcessDiffusion
{
    public static String ABS_TOLERANCE = AspectRef.solverAbsTolerance;

    public static String REL_TOLERANCE = AspectRef.solverRelTolerance;
	
	private static final String SD_TAG = AspectRef.agentSurfaceDistributionMap;
	
	private static final String EPITHELIAL = AspectRef.isEpithelial;
    
    private Multigrid multigrid;

    public double absTol;
    
    public double relTol;

    public double solverResidualRatioThreshold;

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

        /* TODO move values to global defaults */
        this.absTol = (double) this.getOr(ABS_TOLERANCE, 1.0e-12);
        this.relTol = (double) this.getOr(REL_TOLERANCE, 1.0e-6);

        this.solverResidualRatioThreshold = (double) this.getOr(
                AspectRef.solverResidualRatioThreshold, 1.0e-4);

        int vCycles = (int) this.getOr(AspectRef.vCycles, 15);
        int preSteps = (int) this.getOr(AspectRef.preSteps, 5);
        int coarseSteps = (int) this.getOr(AspectRef.coarseSteps, 5);
        int postSteps = (int) this.getOr(AspectRef.postSteps, 5);

        boolean autoVcycleAdjust = (boolean) this.getOr(AspectRef.autoVcycleAdjust, false);

        /* gets specific solutes from process manager aspect registry if they
         * are defined, if not, solve for all solutes.
         */
        this._soluteNames = (String[]) this.getOr(SOLUTES,
                Helper.collectionToArray(this._environment.getSoluteNames()));
        /*
         * Set up the relevant arrays in each of our solute grids: diffusivity
         * & well-mixed need only be done once each process manager time step,
         * but production rate must be reset every time the PDE updater method
         * is called.
         */
        for ( String soluteName : this._soluteNames )
        {
            SpatialGrid solute = this._environment.getSoluteGrid(soluteName);
            solute.updateDiffusivity(this._environment, this._agents);
        }

        Domain domain = new Domain(environment.getShape(), this._environment);
        this.multigrid = new Multigrid();
        multigrid.init(domain, environment, agents, this,
                vCycles, preSteps, coarseSteps, postSteps, autoVcycleAdjust);

        // TODO Let the user choose which ODEsolver to use.


    }

    public double fetchBulk(String solute)
    {
        for( Boundary b : this._environment.getShape().getAllBoundaries() )
        {
            if (b instanceof WellMixedBoundary )
                return ((WellMixedBoundary) b).getConcentration(solute);
        }
        return 0.0;
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
//        super.internalStep();
        for ( Boundary b : this._environment.getShape().getAllBoundaries() )
        {
            b.resetMassFlowRates();
        }
        /* gets specific solutes from process manager aspect registry if they
         * are defined, if not, solve for all solutes.
         */
        this._soluteNames = (String[]) this.getOr(SOLUTES,
                Helper.collectionToArray(this._environment.getSoluteNames()));

        prestep(this._environment.getSolutes(), 0.0);

        for ( SpatialGrid var : this._environment.getSolutes() )
        {
            var.reset(PRODUCTIONRATE);
        }
        multigrid.initAndSolve();
        /*
         * Estimate agent growth based on the steady-state solute
         * concentrations.
         */
        for ( Agent agent : this._agents.getAllAgents() )
            this.applyAgentGrowth(agent);
        
        for (Agent agent : this._agents.getAllAgents() )
        	this.applyTransferReactions(agent);

        for ( SpatialGrid var : this._environment.getSolutes() )
        {
            double massMove = var.getTotal(PRODUCTIONRATE);
            var.increaseWellMixedMassFlow(massMove);
        }

        /*
         * Estimate the steady-state mass flows in or out of the well-mixed
         * region, and distribute it among the relevant boundaries.
         */
        this._environment.distributeWellMixedFlows(this._timeStepSize);

        /* perform final clean-up and update agents to represent updated
         * situation. */
        this.postStep();
    }

    /**
     * \brief The standard PDE updater method resets the solute
     *
     * TODO this method would benefit from renaming
     *
     * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
     * {@code Agent}s to grow.
     *
     * @return PDE updater method.
     */
    public void prestep(Collection<SpatialGrid> variables, double dt)
    {
    /* TODO should env reactions be aplied here? */
        for ( SpatialGrid var : variables )
            var.newArray(PRODUCTIONRATE);
        applyEnvReactions(variables);

        setupAgentDistributionMaps(this._agents.getShape());
    }

    public void applyReactions(MultigridSolute[] sols, int resorder, SolverGrid[] reacGrid, double[] resolution,
                               double voxelVolume)
    {
        for( Agent agent : this._agents.getAllAgents() )
            applyAgentReactions(agent, sols, resorder, reacGrid, resolution, voxelVolume);
        
        for (Agent agent : this._agents.getAllAgents())
        	solveTransferReactions (agent, sols, resorder, reacGrid, resolution, voxelVolume);
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
     */
    private void applyAgentReactions(
            Agent agent, MultigridSolute[] concGrid, int resorder, SolverGrid[] reacGrid, double[] resolution,
            double voxelVolume)
    {
        /*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         */
        @SuppressWarnings("unchecked")
        List<Reaction> reactions =
                (List<Reaction>) agent.getValue(XmlRef.reactions);
        
        ArrayList<Reaction> volumeReactions =
        		new ArrayList<Reaction>();
        
        for (Reaction r : reactions)
        {
        	if (r instanceof RegularReaction)
        	{
        		if ( ((RegularReaction) r).getType()
        				== ReactionType.VOLUME)
        			volumeReactions.add((RegularReaction) r);
        	}
        	else
        		volumeReactions.add(r);
        }
        
        if ( Helper.listIsNullOrEmpty(volumeReactions) )
            return;
        /*
         * Get the distribution map and scale it so that its contents sum up to
         * one.
         */
        Shape shape = this._agents.getShape();

        /*
         * Get the agent biomass kinds as a map. Copy it now so that we can
         * use this copy to store the changes.
         */
        Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
        /*
         * Now look at all the voxels this agent covers.
         */
        Map<String,Double> concns = new HashMap<String,Double>();
        SolverGrid solute;
        MultigridSolute mGrid;
        double concn, productRate, volume, perVolume;

        double[] center = ((Body) agent.get(AspectRef.agentBody)).getCenter(shape);
        
        IntegerArray coord;
        
        if (agent.getBoolean(EPITHELIAL) != null
        		&& agent.getBoolean(EPITHELIAL))
        {
        	coord = new IntegerArray();
        }
        else
        {
        	coord = new IntegerArray(shape.getCoords(center, null, resolution));
        }


        if ( agent.getBoolean(EPITHELIAL) != null
        		&& agent.getBoolean(EPITHELIAL))
        {
        	volume = agent.getDouble(AspectRef.agentVolume);
        }
        else
        	volume = voxelVolume;
        
        perVolume = 1.0/volume;
        
        for ( Reaction r : volumeReactions )
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
                mGrid = FindGrid(concGrid, varName);
                if ( !Helper.isNullOrEmpty(coord.get()) &&
                		mGrid != null ) {
                    solute = mGrid._conc[resorder];
                    concn = solute.getValueAt(coord.get(), true);
                }
                else if ( biomass.containsKey(varName) )
                {
                    concn = biomass.get(varName) * perVolume;
                }
                else if ( agent.isAspect(varName) )
                {
                    /*
                     * Check if the agent has other mass-like aspects
                     * (e.g. EPS).
                     */
                    concn = agent.getDouble(varName) * perVolume;
                }
                else
                {
                    // TODO safety?
                    concn = 0.0;
                }
                concns.put(varName, concn);

            }
            /*
             * Now that we have the reaction rate, we can distribute the
             * effects of the reaction. Note again that the names in the
             * stoichiometry may not be the same as those in the reaction
             * variables (although there is likely to be a large overlap).
             */

            for ( String productName : r.getReactantNames() )
            {
                mGrid = FindGrid(concGrid, productName);
                if ( !Helper.isNullOrEmpty(coord.get())
                		&& mGrid != null )
                {
                    solute = mGrid._reac[resorder];
                    productRate = r.getProductionRate(concns, productName);
                    solute.addValueAt( productRate, coord.get() , true );
                }
            }
        }
    }
    
    private void solveTransferReactions(Agent agent, MultigridSolute[] concGrid,
    		int resorder, SolverGrid[] reacGrid, double[] resolution,
            double voxelVolume)
    {
    	/*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         */
        @SuppressWarnings("unchecked")
        List<Reaction> reactions =
                (List<Reaction>) agent.getValue(XmlRef.reactions);
        
        ArrayList<RegularReaction> transferReactions =
        		new ArrayList<RegularReaction>();
        
        for (Reaction r : reactions)
        {
        	if (r instanceof RegularReaction)
        	{
        		if ( ((RegularReaction) r).getType()
        				== ReactionType.TRANSFER)
        			transferReactions.add((RegularReaction) r);
        	}
        }
        
        if ( Helper.listIsNullOrEmpty(transferReactions))
        	return;
        /*
         * Get the distribution map
         */
        
        Shape shape = agent.getEpithelium().getCompartment().getShape();
        
        if (!agent.isAspect(SD_TAG))
        	this.setupAgentDistributionMaps(shape);
        Map<Shape, HashMap<IntegerArray,Double>> map =
        		(Map<Shape, HashMap<IntegerArray,Double>>) agent.get(SD_TAG);
        HashMap<IntegerArray,Double> coverageMap = map.get(
        		agent.getEpithelium().getCompartment().getShape());
        		
        double surfaceArea = agent.getEpithelium().
        		epithelialCellSurfaceArea(agent);
        
        double agentVolume = (double) agent.get(AspectRef.agentVolume);
        
        /*
         * Get the agent biomass kinds as a map. Copy it now so that we can
         * use this copy to store the changes.
         */
        Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
        /*
         * Now look at all the voxels this agent covers.
         */
        Map<String,Double> concns = new HashMap<String,Double>();
        SolverGrid solute;
        MultigridSolute mGrid;
        double concn, transferRate;
        
        for ( RegularReaction r : transferReactions )
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
	            	
	            concn = 0.0;
	           	
	           	SoluteAtSite constituent =
	            		new SoluteAtSite(varName);
	           	
	           	if (constituent.site.equals("compartment"))
	           	{
	           		constituent.setSite(this._compartmentName);
	           	}
	            
	            if (constituent.site instanceof Compartment)
	            {
	            	if (constituent.siteName.equals(this._compartmentName))
		           	{
	            		for (IntegerArray coord : coverageMap.keySet())
	                    {
	            			double[] inside;
	            			if (coord.get().length == 2)
	            			{
	            				inside = new double[] {0.5, 0.5};
	            			}
	            			
	            			else
	            			{
	            				inside = new double[] {0.5, 0.5, 0.5};
	            			}
	            			
	            			/*
	            			 * Global location of the global
	            			 * coordinate in which the cell sits
	            			 */
	            			double[] coordLocation = shape.
	            					getLocation(coord.get(), inside);
	            			
	            			int[] resolvedCoord = shape.
	            					getCoords(coordLocation, null, resolution);
	            			
		           			mGrid = FindGrid(concGrid, constituent.soluteName);
		    	            if ( mGrid != null ) 
		    	            {
		    	                solute = mGrid._conc[resorder];
		    	                concn += solute.getValueAt(resolvedCoord, true)
		    	               		*coverageMap.get(coord);
		    	            }
	                    }
		           	}
	            		
	            	else
		            {
		            	SpatialGrid sg = ((Compartment) constituent.site).
		            		getSolute(constituent.soluteName);
		            	concn = sg.getSingleValue(CONCN);
		            }
	            }
        	
	            else if (constituent.site instanceof String &&
	            		((String) constituent.site).equalsIgnoreCase("agent"))
	            {
	            	double soluteMass;
	            	
	            	if ( biomass.containsKey(constituent.soluteName) )
	            	{
	            		soluteMass =
		           				biomass.get(constituent.soluteName);
	            	}
	            	
	            	/*
	                 * Check if the agent has other mass-like aspects
	                 * (e.g. EPS).
	                 */
	            	else if ( agent.isAspect(constituent.soluteName) )
	            	{
	            		soluteMass =
	            				agent.getDouble(constituent.soluteName);
	            	}
	            	
	            	else
	            	{
	            		if (Log.shouldWrite(Tier.CRITICAL))
	    					Log.out(Tier.CRITICAL, "Solute " +
	    					constituent.soluteName + " not found. PDEWrapper "
	    					+ "using value of 0.");
	            		soluteMass = 0.0;
	            	}
	            		
	            	concn = soluteMass / agentVolume;
	            }
	            	
	            else
	            {
	            	if (Log.shouldWrite(Tier.CRITICAL))
    					Log.out(Tier.CRITICAL, "Site " +
    					constituent.site + " not found in transfer reaction.");
	            }
	            
	            	
	        concns.put(varName, concn);
		    }
	        
	        for (IntegerArray coord : coverageMap.keySet())
	        {
	        	
	        	double[] inside;
    			if (coord.get().length == 2)
    			{
    				inside = new double[] {0.5, 0.5};
    			}
    			
    			else
    			{
    				inside = new double[] {0.5, 0.5, 0.5};
    			}
    			
    			/*
    			 * Global location of the global
    			 * coordinate in which the cell sits
    			 */
    			double[] coordLocation = shape.
    					getLocation(coord.get(), inside);
    			
    			int[] resolvedCoord = shape.
    					getCoords(coordLocation, null, resolution);
	        	
	            /*
	             * Now that we have the reaction rate, we can distribute the
	             * effects of the reaction. Note again that the names in the
	             * stoichiometry may not be the same as those in the reaction
	             * variables (although there is likely to be a large overlap).
	             */
	
	            for ( String productName : r.getReactantNames() )
	            {	
	            	SoluteAtSite product = new SoluteAtSite(productName);
	            	
	            	if (product.site.equals("compartment"))
		           	{
		           		product.setSite(this._compartmentName);
		           	}
	            	
	            	if (product.site instanceof Compartment)
	            	{
	            		if (product.siteName.equals(this._compartmentName))
		            	{
	            			mGrid = FindGrid(concGrid, product.soluteName);
	    	                if ( mGrid != null ) 
	    	                {
	    	                	solute = mGrid._reac[resorder];
	    	                	
	    	                	/*
	    	                	 * The transfer rate returned by a transfer reaction
	    	                	 * is a rate of mass production per unit time, per
	    	                	 * unit surface area (in a 3D sim) or per unit length
	    	                	 * (in a 2D sim). However, when applying a production
	    	                	 * rate, the solver grid expects a rate of production
	    	                	 * per unit time per unit volume (3D) or per unit area
	    	                	 * (2D). In order to match this, the transfer rate
	    	                	 * should be divided by the grid resolution as the
	    	                	 * transfer only takes place at one face of the voxel,
	    	                	 * not throughout it as with other reactions.
	    	                	 * 
	    	                	 * Furthermore, the transfer rate is multiplied by the
	    	                	 * proportion of the agent's surface that is in contact
	    	                	 * with the focal voxel.
	    	                	 */
	    	                    transferRate = r.getProductionRate(concns, productName);
	    	                    double productionRate = (transferRate * coverageMap.get(coord))
	    	                    		/ resolution[0];
	    	                    solute.addValueAt( productionRate, resolvedCoord , true );
	    	                }
		            	}
	            	}
	            }
        	}
	    }
    }
    
    private void applyAgentGrowth(Agent agent)
    {
        /*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         */
        @SuppressWarnings("unchecked")
        List<RegularReaction> reactions =
                (List<RegularReaction>) agent.getValue(XmlRef.reactions);
        
        ArrayList<Reaction> volumeReactions =
        		new ArrayList<Reaction>();
        
        for (Reaction r : reactions)
        {
        	if (r instanceof RegularReaction)
        	{
        		if ( ((RegularReaction) r).getType()
        				== ReactionType.VOLUME)
        			volumeReactions.add((RegularReaction) r);
        	}
        	else
        		volumeReactions.add(r);
        }
        
        if ( Helper.listIsNullOrEmpty(volumeReactions) )
            return;

        Shape shape = this._agents.getShape();
        double[] center = ((Body) agent.get(AspectRef.agentBody)).getCenter(shape);
        
        IntegerArray coord;
        
        if (agent.getBoolean(EPITHELIAL) != null
        		&& agent.getBoolean(EPITHELIAL))
        {
        	coord = new IntegerArray();
        }
        else
        {
        	coord = new IntegerArray(shape.getCoords(center));
        }
        
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
        double concn, productRate, volume, perVolume;
        
        if (agent.getBoolean(EPITHELIAL) != null
        		&& agent.getBoolean(EPITHELIAL))
        {
        	volume = agent.getDouble(AspectRef.agentVolume);
        }
        else
        	volume = this._agents.getShape().getVoxelVolume( coord.get() );
        
        perVolume = 1.0 / volume;
        for ( Reaction r : volumeReactions )
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
                if ( !Helper.isNullOrEmpty(coord.get()) &&
                		this._environment.isSoluteName( varName ) )
                {
                    solute = this._environment.getSoluteGrid( varName );
                    concn = solute.getValueAt( CONCN, coord.get() );
                }
                else if ( biomass.containsKey( varName ) )
                {
                    concn = biomass.get( varName ) * perVolume;

                }
                else if ( agent.isAspect( varName ) )
                {
                    /*
                     * Check if the agent has other mass-like aspects
                     * (e.g. EPS).
                     */
                    concn = agent.getDouble( varName ) * perVolume;
                }
                else
                {
                    // TODO safety?
                    concn = 0.0;
                }
                concns.put(varName, concn);
            }
            /*
             * Now that we have the reaction rate, we can distribute the
             * effects of the reaction. Note again that the names in the
             * stoichiometry may not be the same as those in the reaction
             * variables (although there is likely to be a large overlap).
             */

            for ( String productName : r.getReactantNames() )
            {
                /* FIXME: it is probably faster if we get the reaction rate
                 * once and then calculate the rate per product from that
                 * for each individual product
                 */
                productRate = r.getProductionRate(concns,productName);
                double quantity;

                if ( !Helper.isNullOrEmpty(coord.get()) &&
                		this._environment.isSoluteName(productName) )
                {
                    solute = this._environment.getSoluteGrid(productName);
                    quantity =
                            productRate * volume * this.getTimeStepSize();
                    solute.addValueAt(PRODUCTIONRATE, coord.get(), quantity
                    );
                }
                else if ( newBiomass.containsKey(productName) )
                {
                    quantity =
                            productRate * this.getTimeStepSize() * volume;
                    newBiomass.put(productName, newBiomass.get(productName)
                            + quantity );
                }
                /* FIXME this can create conflicts if users try to mix mass-
                 * maps and simple mass aspects	 */
                else if ( agent.isAspect(productName) )
                {
                    /*
                     * Check if the agent has other mass-like aspects
                     * (e.g. EPS).
                     */
                    quantity =
                            productRate * this.getTimeStepSize() * volume;
                    newBiomass.put(productName, agent.getDouble(productName)
                            + quantity);
                }
                else
                {
                    quantity =
                            productRate * this.getTimeStepSize() * volume;
                    //TODO quick fix If not defined elsewhere add it to the map
                    newBiomass.put(productName, quantity);
                    System.out.println("agent reaction catched " +
                            productName);
                    // TODO safety?

                }
                if( Global.bookkeeping )
                    agent.getCompartment().registerBook(
                            KeeperEntry.EventType.REACTION,
                            productName,
                            String.valueOf( agent.identity() ),
                            String.valueOf( quantity ), null );
            }
        }
        ProcessMethods.updateAgentMass(agent, newBiomass);
    }

    private void applyTransferReactions(Agent agent)
    {
    	/*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         */
        @SuppressWarnings("unchecked")
        List<Reaction> reactions =
                (List<Reaction>) agent.getValue(XmlRef.reactions);
        
        ArrayList<RegularReaction> transferReactions =
        		new ArrayList<RegularReaction>();
        
        for (Reaction r : reactions)
        {
        	if (r instanceof RegularReaction)
        	{
        		if ( ((RegularReaction) r).getType()
        				== ReactionType.TRANSFER)
        			transferReactions.add((RegularReaction) r);
        	}
        }
        
        if ( Helper.listIsNullOrEmpty(transferReactions)  )
            return;
        
        double surfaceArea = agent.getEpithelium().
        		epithelialCellSurfaceArea(agent);
        
        /*
         * Get the distribution map
         */
        
        if (!agent.isAspect(SD_TAG))
        	this.setupAgentDistributionMaps(
        			agent.getEpithelium().getCompartment().getShape());
        Map<Shape, HashMap<IntegerArray,Double>> map =
        		(Map<Shape, HashMap<IntegerArray,Double>>) agent.get(SD_TAG);
        HashMap<IntegerArray,Double> coverageMap = map.get(
        		agent.getEpithelium().getCompartment().getShape());
        		
        /*
         * Get the agent biomass kinds as a map. Copy it now so that we can
         * use this copy to store the changes.
         */
        Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
        /*
         * Now look at all the voxels this agent covers.
         */
        Map<String,Double> concns = new HashMap<String,Double>();
        SpatialGrid solute;
        double concn, productRate;
        
        double agentVolume = (double) agent.get(AspectRef.agentVolume);
        
        for ( RegularReaction r : transferReactions )
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
	            	
	            concn = 0.0;
	           	
	           	SoluteAtSite constituent =
	            		new SoluteAtSite(varName);
	            
	           	if (constituent.site.equals("compartment"))
	           	{
	           		constituent.setSite(this._compartmentName);
	           	}
	           	
	            if (constituent.site instanceof Compartment)
	            {
	            	if (constituent.siteName.equals(this._compartmentName))
		           	{
	            		for (IntegerArray coord : coverageMap.keySet())
	                    {
		           			solute = this._environment.getSoluteGrid( 
	            					constituent.soluteName );
		    	            concn += solute.getValueAt( CONCN, coord.get() )
	    	                    		*coverageMap.get(coord);
	                    }
		           	}
	            		
	            	else
		            {
		            	SpatialGrid sg = ((Compartment) constituent.site).
		            		getSolute(constituent.soluteName);
		            	concn = sg.getSingleValue(CONCN);
		            }
	            }
        	
	            else if (constituent.site instanceof String &&
	            		((String) constituent.site).equalsIgnoreCase("agent"))
	            {
	            	double soluteMass;
	            	
	            	if ( biomass.containsKey(constituent.soluteName) )
	            	{
	            		soluteMass =
		           				biomass.get(constituent.soluteName);
	            	}
	            	
	            	/*
	                 * Check if the agent has other mass-like aspects
	                 * (e.g. EPS).
	                 */
	            	else if ( agent.isAspect(constituent.soluteName) )
	            	{
	            		soluteMass =
	            				agent.getDouble(constituent.soluteName);
	            	}
	            	
	            	else
	            	{
	            		if (Log.shouldWrite(Tier.CRITICAL))
	    					Log.out(Tier.CRITICAL, "Solute " +
	    					constituent.soluteName + " not found. PDEWrapper "
	    					+ "using value of 0.");
	            		soluteMass = 0.0;
	            	}
	            		
	            	concn = soluteMass / agentVolume;
	            }
	            	
	            else
	            {
	            	if (Log.shouldWrite(Tier.CRITICAL))
    					Log.out(Tier.CRITICAL, "Site " +
    					constituent.site + " not found in transfer reaction.");
	            }
	            
	            	
	            concns.put(varName, concn);
	        
        	}
	        
	        for ( String productName : r.getReactantNames() )
       	 	{
			 
				/*
	             * Now that we have the reaction rate, we can distribute the
	             * effects of the reaction. Note again that the names in the
	             * stoichiometry may not be the same as those in the reaction
	             * variables (although there is likely to be a large overlap).
	             */
	        	
	        	/*
	        	 * This is a rate in units mass per unit time, per unit
	        	 * surface area in 3D or units mass per unit time, per
	        	 * unit length in 2D
	        	 */
	        	productRate = r.getProductionRate(concns, productName);
				 
	            double productMass;
	           	
	           	SoluteAtSite product =
	            		new SoluteAtSite(productName);
	           	
	           	if (product.site.equals("compartment"))
	           	{
	           		product.setSite(this._compartmentName);
	           	}
	            
	            if (product.site instanceof Compartment)
	            {
	            	if (product.siteName.equals(this._compartmentName))
		           	{
	            		for (IntegerArray coord : coverageMap.keySet())
	                    {
		           			solute = this._environment.getSoluteGrid( 
	            					product.soluteName );
		    	            productMass =
	                                productRate * surfaceArea * this.getTimeStepSize()
	                                *coverageMap.get(coord);
	                        solute.addValueAt(PRODUCTIONRATE, coord.get(), productMass);
	                    }
		           	}
	            		
	            	else
		            {
		            	SpatialGrid sg = ((Compartment) product.site).
	            				getSolute(product.soluteName);
	            		double productMassFlowRate =
                               productRate * surfaceArea;
	            		if (agent.getEpithelium().getBoundary().
	            				getPartnerCompartmentName().
	            				equalsIgnoreCase(product.siteName))
	            		{
	            			agent.getEpithelium().getBoundary().
	            				increaseMassFlowRate(product.soluteName,
	            						-productMassFlowRate);
	            		}
	            		
	            		else
	            		{
	            			if (Log.shouldWrite(Tier.CRITICAL))
		    					Log.out(Tier.CRITICAL, "Transfer reaction site "
		    					+ product.siteName + " does not match "
		    					+ "epithelium's partner boundary, " +
		    					agent.getEpithelium().getBoundary().
		    					getPartnerCompartmentName() + ".");
	            		}
		            }
	            }
       	
	            else if (product.site instanceof String &&
	            		((String) product.site).equalsIgnoreCase("agent"))
	            {
	            	Map<String,Double> newBiomass = (HashMap<String,Double>)
	                            ObjectFactory.copy(biomass);
	            	
	            	if ( newBiomass.containsKey(product.soluteName) )
	            	{
	            		productMass =
	                            productRate * this.getTimeStepSize() 
	                            * surfaceArea;
	            		
						newBiomass.put(product.soluteName,
								newBiomass.get(product.soluteName)
	                            + productMass );
	            	}
	            	
	            	/*
	                 * Check if the agent has other mass-like aspects
	                 * (e.g. EPS).
	                 */
	            	else if ( agent.isAspect(product.soluteName) )
	            	{
	            		 productMass =
	                                productRate * this.getTimeStepSize() * surfaceArea;
	                        newBiomass.put(product.soluteName,
	                        		agent.getDouble(product.soluteName)
	                                + productMass);
	            	}
	            	
	            	ProcessMethods.updateAgentMass(agent, newBiomass);
	            }
	            	
	            else
	            {
	            	if (Log.shouldWrite(Tier.CRITICAL))
    					Log.out(Tier.CRITICAL, "Site " +
    					product.site + " not found in transfer reaction.");
	            }
		    }
	    }
    }
    
    

    private MultigridSolute FindGrid(MultigridSolute[] grids, String name)
    {
        for ( MultigridSolute grid : grids )
            if ( grid.soluteName.equals(name) ) {
//                Quick debug check to see which grid (coarse/fine) we are handling
//                System.out.println( grid.getVoxelVolume() );
                return grid;
            }
        return null;
    }
}
