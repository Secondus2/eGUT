/**
 * 
 */
package boundary.library;
import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import settable.Settable;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief Boundary connecting a dimensionless compartment to a compartment
 * this spatial structure.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToBoundaryLayer extends ChemostatBoundary
{
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 */

	
	public ChemostatToBoundaryLayer()
	{
		super();
	}

	/* ************************************************************************
	 * PARTNER BOUNDARY
	 * ***********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return BiofilmBoundaryLayer.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override 
	public void additionalPartnerUpdate()
	{
		this._partner.additionalPartnerUpdate();
	}
	
	public double getSoluteConcentration(String soluteName)
	{
		return this._environment.getAverageConcentration(soluteName);
	}
	
	
	@Override
	public Collection<Agent> agentsToGrab()
	{
		int nAllAgents = this._agents.getNumAllAgents();
		LinkedList<Agent> removals = new LinkedList<Agent>();
		if ( (nAllAgents > 0) && ((this._volumeFlowRate < 0.0) || 
			(((BiofilmBoundaryLayer) this._partner).getExchangeRate() != 0.0)))
		{
			/* do not remove if agent removal is disabled */
			if ( _agentRemoval )
			{
				/* calculate (1 - removal chance) */
				//double e = Math.exp( ( this.getDilutionRate() * 
				//		Idynomics.simulator.timer.getTimeStepSize() ) ); 
				
				double e = ((BiofilmBoundaryLayer) this._partner).
						getExchangeRate() * Idynomics.simulator.timer.
						getTimeStepSize();
				
				
				for ( int i = 0; i < nAllAgents; i++ )
				{
					if( ExtraMath.getUniRandDbl() > e )
						removals.add( this._agents.chooseAgent(i) );
				}
			}
		}
		return removals;			
	}

}
