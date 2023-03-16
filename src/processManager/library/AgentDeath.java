package processManager.library;

import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import aspect.Aspect;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import processManager.ProcessDeparture;
import referenceLibrary.AspectRef;

public class AgentDeath extends ProcessDeparture {

	private Double _deathMass;
	
	private String DEATHMASS= AspectRef.deathMass;
	
	public void init( Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		this._deathMass = this.getDouble( DEATHMASS );
	}
	
	
	@Override
	protected LinkedList<Agent> agentsDepart() {
		
		LinkedList<Agent> deadAgents = new LinkedList<Agent>();
		
		for (Agent a : this._agents.getAllLocatedAgents())
		{
			Object mass = a.get(AspectRef.agentMass);
			
			if ( mass != null && mass instanceof Double && 
					a.getAspectType( AspectRef.agentMass ) 
					== Aspect.AspectClass.PRIMARY)
			{
				if ((Double) mass < this._deathMass)
					deadAgents.add(a);
			}
			
			
			if ( mass != null && mass instanceof Map )
			{
				@SuppressWarnings("unchecked")
				Map<String,Double> massMap = (Map<String,Double>) mass;
				Double totalMass = 0.0;
				Double runningTotal = 0.0;
				
				for (String s : massMap.keySet())
				{
					runningTotal = totalMass;
					totalMass = Double.sum(
							runningTotal, massMap.get(s));
				}
				
				if (totalMass < this._deathMass)
					deadAgents.add(a);
			}
			
		}
		return deadAgents;
	}

}
