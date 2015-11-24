package agent.state.secondary;

import agent.Agent;
import agent.body.Body;
import agent.state.State;

public class JointsState implements State {

	public void init(Object state)
	{

	}
	
	public Object get(Agent agent)
	{
		Body myBody = (Body) agent.get("body");
		return myBody.getJoints();
	}

}
