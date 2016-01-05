package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.SecondaryState;
import agent.state.State;

public class JointsState extends SecondaryState implements State {
	
	// input: body

	public void set(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		return ((Body) agent.get(input[0])).getJoints();
	}
	
	public State copy()
	{
		return this;
	}
}
