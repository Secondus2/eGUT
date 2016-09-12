/**
 * 
 */
package processManager.library;

import org.w3c.dom.Element;

import agent.Agent;
import referenceLibrary.AspectRef;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;

/**
 * \brief Simple process that asks all agents to perform events by name.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class AgentEvents extends ProcessManager
{
	/**
	 * XML tag for the list of names this is responsible for.
	 */
	public static String EVENT_NAMES = AspectRef.agentEventNames;
	
	/**
	 * The names of all agent events this is responsible for.
	 */
	protected String[] _eventNames = new String[0];

	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this.init(environment, agents, compartmentName);
	}
	
	public void init(EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(environment, agents, compartmentName);
		this._eventNames = this.getStringA(EVENT_NAMES);
	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		for ( Agent agent : this._agents.getAllAgents() )
			for ( String eventName : this._eventNames )
				agent.event(eventName);
	}
}
