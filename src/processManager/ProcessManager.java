package processManager;

import java.util.HashMap;

import grid.SpatialGrid;
import idynomics.AgentContainer;

public abstract class ProcessManager
{
	protected String _name;
	
	protected int _priority;
	
	protected double _timeForNextStep;
	
	protected double _timeStepSize;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public ProcessManager()
	{
		
	}
	
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
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step(HashMap<String, SpatialGrid> solutes,
														AgentContainer agents)
	{
		/*
		 * This is where subclasses of Mechanism do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep(solutes, agents);
		/*
		 * Increase the 
		 */
		this._timeForNextStep += this._timeStepSize;
		
	}
	
	protected abstract void internalStep(HashMap<String, SpatialGrid> solutes,
											AgentContainer agents);
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public StringBuffer report()
	{
		StringBuffer out = new StringBuffer();
		
		return out;
	}
	
}