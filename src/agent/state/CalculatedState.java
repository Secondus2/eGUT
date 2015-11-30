package agent.state;

import agent.Agent;

public class CalculatedState implements State {
	
	private stateExpression expression;
	
	public interface stateExpression 
	{
		Object calculate(Agent agent);
	}

	public void set(Object stateExpression)
	{
		this.expression = (stateExpression) stateExpression;
	}
	
	public Object get(Agent agent)
	{
		return expression.calculate(agent);
	}
	
	public State copy()
	{
		return this;
	}
}