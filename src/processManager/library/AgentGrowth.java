package processManager.library;

//FIXME this class is for testing purposes only!!!
import agent.Agent;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
/**
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentGrowth extends ProcessManager
{
	protected void internalStep(
					EnvironmentContainer environment, AgentContainer agents)
	{
		for ( Agent agent : agents.getAllAgents() )
		{
			agent.event("growth", this._timeStepSize);
			agent.event("divide", this._timeStepSize);
		}
	}
}