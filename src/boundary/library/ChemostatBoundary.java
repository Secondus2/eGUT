package boundary.library;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import agent.Body;
import boundary.Boundary;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

/**
 * \brief abstract class that captures identical agent transfer behaviour for
 * chemostat boundaries. 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public abstract class ChemostatBoundary extends Boundary {
	
	protected boolean _agentRemoval = true;
	
	protected double _exchangeRate;
	
	public ChemostatBoundary()
	{
		super();
	}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	/**
	 * The number of agents at time t can be found using a simple 
	 * differential equation:
	 * 
	 * dA/dt = rA
	 * A(t) = A(0) * e^(rt)
	 * 
	 * here r is the removal rate and A is the number of agents.
	 * Translating this to agent based we can say the chance of any
	 * agent being removed over time t equals e^(rt).
	 */
	@Override
	public Collection<Agent> agentsToGrab()
	{
		int nAllAgents = this._agents.getNumAllAgents();
		LinkedList<Agent> removals = new LinkedList<Agent>();
		double flowRemovalProbability = 0.0;
		double mixingRemovalProbability = 0.0;
		if ( (nAllAgents > 0) && (this._volumeFlowRate < 0.0) )
		{
			/* do not remove if agent removal is disabled */
			if ( _agentRemoval )
			{
				/* calculate  removal chance */
				flowRemovalProbability = Math.exp( ( this.getDilutionRate() 
						* Idynomics.simulator.timer.getTimeStepSize() ) );
			}
		}
		if ( (nAllAgents > 0) && (this._exchangeRate > 0.0) )
			{
				double occupiedVolumeProportion =
						this._agents.getOccupiedVolume()/
						this._agents.getShape().getTotalVolume();
				double agentsToTransfer = 
						this._exchangeRate*occupiedVolumeProportion;
				mixingRemovalProbability = 
						agentsToTransfer/this._agents.getNumAllAgents();
			}
		
		double e = flowRemovalProbability + mixingRemovalProbability - 
				(flowRemovalProbability * mixingRemovalProbability);
		for ( int i = 0; i < nAllAgents; i++ )
		{
			if( ExtraMath.getUniRandDbl() < e )
				removals.add( this._agents.chooseAgent(i) );
		}
		return removals;
	}

	public void setExchangeRate(double exchangeRate)
	{
		this._exchangeRate = exchangeRate;
	}
}
