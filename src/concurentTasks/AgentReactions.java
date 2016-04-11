package concurentTasks;

import static grid.SpatialGrid.ArrayType.CONCN;
import static grid.SpatialGrid.ArrayType.PRODUCTIONRATE;

import java.util.HashMap;
import java.util.List;

import agent.Agent;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import reaction.Reaction;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class AgentReactions implements ConcurrentTask
{

	private List<Agent> agentList;
	private EnvironmentContainer environment;
	private double dt;

	public AgentReactions(AgentContainer agents, EnvironmentContainer 
			environment, double dt)
	{
		this.environment = environment;
		this.agentList = agents.getAllLocatedAgents();
		this.dt = dt;
	}
	
	public AgentReactions(List<Agent> agents, EnvironmentContainer environment, 
			double dt)
	{
		this.environment = environment;
		this.agentList = agents;
		this.dt = dt;
	}
	
	public ConcurrentTask part(int start, int end) 
	{		
		return new AgentReactions(agentList.subList(start, end), 
				environment, dt);
	}

	@SuppressWarnings("unchecked")
	public void task() {
		// Calculate forces
		/*
		 * Loop over all agents, applying their reactions to the
		 * relevant solute grids, in the voxels calculated before the 
		 * updater method was set.
		 */
		HashMap<String,Double> concns = new HashMap<String,Double>();
		SpatialGrid aSG;
		List<Reaction> reactions;
		HashMap<int[],Double> distributionMap;
		for ( Agent a : agentList)
		{
			if ( ! a.isAspect("reactions") )
				continue;
			reactions = (List<Reaction>) a.getValue("reactions");
			distributionMap = (HashMap<int[],Double>)
									a.getValue("volumeDistribution");
			
			a.set("growthRate",0.0);
			if (a.isAspect("internalProduction"))
			{
				HashMap<String,Double> internalProduction = 
						(HashMap<String,Double>) 
						a.getValue("internalProduction");
				for (String key : internalProduction.keySet())
					internalProduction.put(key, 0.0);
			}
			/*
			 * Calculate the total volume covered by this agent,
			 * according to the distribution map. This is likely to be
			 * slightly different to the agent volume calculated 
			 * directly.
			 */
			double totalVoxVol = 0.0;
			for ( double voxVol : distributionMap.values() )
				totalVoxVol += voxVol;
			/*
			 * Now look at all the voxels this agent covers.
			 */
			double concn;
			for ( int[] coord : distributionMap.keySet() )
			{
				for ( Reaction r : reactions )
				{
					/* 
					 * Build the dictionary of variable values. Note
					 * that these will likely overlap with the names in
					 * the reaction stoichiometry (handled after the
					 * reaction rate), but will not always be the same.
					 * Here we are interested in those that affect the
					 * reaction, and not those that are affected by it.
					 */
					concns.clear();
					for ( String varName : r.variableNames )
					{
						if ( environment.isSoluteName(varName) )
						{
							aSG = environment.getSoluteGrid(varName);
							concn = aSG.getValueAt(CONCN, coord);
						}
						else if ( a.isAspect(varName) )
						{
							// TODO divide by the voxel volume here?
							concn = a.getDouble(varName); 
							concn *= distributionMap.get(coord);
							concn /= totalVoxVol;
						}
						else
						{
							// TODO safety?
							concn = 0.0;
						}
						concns.put(varName, concn);
					}
					/*
					 * Calculate the reaction rate based on the 
					 * variables just retrieved.
					 */
					double rate = r.getRate(concns);
					/* 
					 * Now that we have the reaction rate, we can 
					 * distribute the effects of the reaction. Note
					 * again that the names in the stoichiometry may
					 * not be the same as those in the reaction
					 * variables (although there is likely to be a
					 * large overlap).
					 */
					// TODO move this part to a "poststep" updater method?
					double productionRate;
					for ( String productName : 
										r.getStoichiometry().keySet())
					{
						productionRate = rate * 
									r.getStoichiometry(productName);
						if ( environment.isSoluteName(productName) )
						{
							aSG = environment.getSoluteGrid(productName);
							aSG.addValueAt(PRODUCTIONRATE, 
												coord, productionRate);
						}
						else if ( a.isAspect(productName) )
						{
							System.out.println("agent reaction catched " + 
									productName);
							/* 
							 * NOTE Bas [17Feb2016]: Put this here as 
							 * example, though it may be nicer to
							 * launch a separate agent growth process
							 * manager here.
							 */
							/* 
							 * NOTE Bas [17Feb2016]: The average growth
							 * rate for the entire agent, not just for
							 * the part that is in one grid cell later
							 * this may be specific separate
							 * expressions that control the growth of
							 * separate parts of the agent (eg lipids/
							 * other storage compounds)
							 */
						}
						else if ( a.getString("species").equals(productName))
						{
							double curRate = a.getDouble("growthRate");
							a.set("growthRate", curRate + productionRate * 
									distributionMap.get(coord) / totalVoxVol);

							
						}
						else if ( a.isAspect("internalProduction"))
						{
							HashMap<String,Double> internalProduction = 
									(HashMap<String,Double>) 
									a.getValue("internalProduction");
							for( String p : internalProduction.keySet())
							{
								if(p.equals(productName))
								{
									internalProduction.put(productName, 
											internalProduction.get(productName) 
											+ productionRate * distributionMap.get(coord) / totalVoxVol);
								}
							}

						} 
						else
						{
							System.out.println("agent reaction catched " + 
									productName);
							// TODO safety?
						}
					}
				}
			}
			a.event("growth", dt);
			a.event("produce", dt);
		}
	}

	public int size() 
	{
		return agentList.size();
	}

}