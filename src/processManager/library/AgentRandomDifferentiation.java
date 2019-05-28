package processManager.library;

import java.util.HashMap;
import java.util.Map;

import agent.Agent;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import utility.ExtraMath;

public class AgentRandomDifferentiation extends ProcessManager {
	
	
	public static String DIFFERENTIATE = AspectRef.agentDifferentiation;
	public static String DIFF_LIKELIHOOD = AspectRef.differentiateLikelihood;
	
	protected void internalStep()
	{
		
		
		for ( Agent agent : this._agents.getAllLocatedAgents() )
		{

				if (agent.isAspect(DIFF_LIKELIHOOD)) {
					double probability = agent.getDouble(DIFF_LIKELIHOOD);
					if (ExtraMath.getUniRandDbl() < probability) 
					{
						agent.set("randomDifferentiation", true);
						agent.event(DIFFERENTIATE);
					}

			}
		}
	}
}
