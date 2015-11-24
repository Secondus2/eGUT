/**
 * 
 */
package processManager;

import java.util.Collection;

import boundary.Boundary;
import agent.Agent;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

/**
 * 
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) 
 */
public class PrepareSoluteGrids implements ProcessManager
{
	
protected String _name;
	
	protected int _priority;
	
	protected double _timeForNextStep = 0.0;
	
	protected double _timeStepSize;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public void init()
	{
		
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public String getName()
	{
		return this._name;
	}
	
	public void setPriority(int priority)
	{
		this._priority = priority;
	}
	
	public int getPriority()
	{
		return this._priority;
	}
	
	public void setTimeForNextStep(double newTime)
	{
		this._timeForNextStep = newTime;
	}
	
	public double getTimeForNextStep()
	{
		return this._timeForNextStep;
	}
	
	public void setTimeStepSize(double newStepSize)
	{
		this._timeStepSize = newStepSize;
	}
	
	public double getTimeStepSize()
	{
		return this._timeStepSize;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param boundaries
	 */
	public void showBoundaries(Collection<Boundary> boundaries)
	{
		
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step(EnvironmentContainer environment, AgentContainer agents)
	{
		//System.out.println("STEP");//bughunt
		//System.out.println("timeForNextStep = "+_timeForNextStep);//bughunt
		//System.out.println("timeStepSize = "+_timeStepSize);//bughunt
		/*
		 * This is where subclasses of Mechanism do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep(environment, agents);
		/*
		 * Increase the 
		 */
		this._timeForNextStep += this._timeStepSize;
		//System.out.println("timeForNextStep = "+_timeForNextStep);//bughunt
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public StringBuffer report()
	{
		StringBuffer out = new StringBuffer();
		
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 */
	public PrepareSoluteGrids()
	{
		
	}
	
	/**
	 * \brief TODO
	 * 
	 * 
	 * 
	 */
	@Override
	public void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		/*
		 * Reset each solute grid's relevant arrays.
		 */
		for ( String sName : environment.getSoluteNames() )
		{
			environment.getSoluteGrid(sName).newArray(ArrayType.PRODUCTIONRATE);
			//environment.getSoluteGrid(sName).newArray(ArrayType.DIFFPRODUCTIONRATE);
			environment.getSoluteGrid(sName).newArray(ArrayType.DOMAIN);
			environment.getSoluteGrid(sName).newArray(ArrayType.DIFFUSIVITY);
		}
		/*
		 * Iterate through the agents, asking them to apply the relevant
		 * information.
		 */
		for ( Agent agent : agents.getAllLocatedAgents() )
		{
			// TODO Give agent solute grids; agent updates reac rates, 
			// diffReac, domain... diffusivity?
		}
		
		// TODO update domain to include boundary layer
		
		// TODO reaction rates not catalysed by agents
	}

}
