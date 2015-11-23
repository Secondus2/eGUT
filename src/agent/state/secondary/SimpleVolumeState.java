package agent.state.secondary;

import agent.Agent;
import agent.state.State;

public class SimpleVolumeState implements State {
	
	private Agent agent;

	public void init(Agent agent, Object state)
	{
		this.agent = agent;
	}
	
	public Object get()
	{
		return  (double) agent.get("mass") / (double) agent.get("density");
	}
	
	public Agent getAgent()
	{
		return agent;
	}
	
	public void setAgent(Agent agent)
	{
		this.agent = agent;
	}
}
