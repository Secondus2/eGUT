package boundary.spatialLibrary;

import java.util.Collection;
import java.util.LinkedList;

import agent.Agent;
import agent.Body;
import boundary.SpatialBoundary;
import grid.SpatialGrid;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Dimension;
import shape.Shape;
import utility.ExtraMath;

public class SpatialCompartmentToBoundaryLayer extends SpatialBoundary {

	protected double _exchangeRate;
	
	
	public Collection<Agent> agentsToGrab()
	{
		int nAllAgents = this._agents.getNumAllAgents();
		LinkedList<Agent> removals = new LinkedList<Agent>();
		if ( (nAllAgents > 0) && (this._exchangeRate > 0.0))
		{
			double diffusionCoefficient = 1.0;
			
			double timeStep = Idynomics.simulator.timer.getTimeStepSize();
			
			for ( int i = 0; i < nAllAgents; i++ )
			{
				Body agentBody = (Body) this._agents.chooseAgent(i).
						get(AspectRef.agentBody);
				
				int numberOfDimensions = agentBody.nDim();
				
				double[] displacement = ExtraMath.randomWalkDisplacement(
						diffusionCoefficient, timeStep, numberOfDimensions);
				
				double[] displacedPosition = Vector.add(
						displacement, agentBody.getCenter());
				
				Shape shape = this._agents.getShape();
				
				Dimension dim = shape.getDimension(this._dim);
				
				double extremeValue = dim.getExtreme(this._extreme);
				
				int dimIndex = shape.getDimensionIndex(dim);
				
				if (this._extreme == 0 && 
						displacedPosition[dimIndex] < extremeValue
					||
					this._extreme == 1 && 
						displacedPosition[dimIndex] > extremeValue)
				{
					removals.add( this._agents.chooseAgent(i) );
				}
			}
		}
		return removals;
	}
	
	public double getSoluteConcentration(String soluteName)
	{
		return this._environment.getAverageConcentration(soluteName);
	}
	
	public void setExchangeRate(double exchangeRate)
	{
		this._exchangeRate = exchangeRate;
	}
	
	@Override
	protected boolean needsLayerThickness() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected double calcDiffusiveFlow(SpatialGrid grid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateWellMixedArray() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getPartnerClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void additionalPartnerUpdate() {
		// TODO Auto-generated method stub
		
	}

}
