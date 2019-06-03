package aspect.event;

import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import surface.Point;
import utility.ExtraMath;


public class StochasticMovement extends Event
{
	public String BODY = AspectRef.agentBody;
	public String VOLUME = AspectRef.agentVolume;
	private double boltzmannK = 1.38065e-23;
	//Temperature - settable in compartment??
	private double temperature = 310;
	//Water viscosity - find a better value for this
	private double viscosityWater = 7e-4;
	private double stochasticTimeStep = 1.0;

	public void start(AspectInterface initiator, AspectInterface compliant, 
			Double timeStep)
	{
		Agent agent = (Agent) initiator;
		Body agentBody = (Body) agent.get(BODY);
		int numberOfDimensions = agentBody.nDim();
		List<Point> points = agentBody.getPoints();
		double volume = agent.getDouble(VOLUME);
		
		/** 
		 * Here radius is estimated from volume. This is only a guide radius
		 * and does not reflect the true radius of non-coccoid agents.
		 */
		double radius;
		if (numberOfDimensions == 2)
		{
			radius = ExtraMath.radiusOfACircle(volume);
		}
		else
		{
			radius = ExtraMath.radiusOfASphere(volume);
		}

		//The Stokes-Einstein equation
		double diffusionCoefficient = 
				(boltzmannK*temperature)/(6*Math.PI*viscosityWater*radius);
		
		double meanSquareDistance = 2*(numberOfDimensions)*
				diffusionCoefficient*stochasticTimeStep;
		
		double standardDeviation = Math.sqrt(meanSquareDistance);
		
		double distance = ExtraMath.deviateFromSD(0, standardDeviation);
		
		double [] randDir = Vector.randomPlusMinus(numberOfDimensions, 1.0);
		
		double[] move = Vector.times(randDir, distance);
		
		for (Point p : points)
		{
			p.setPosition( Vector.add(p.getPosition(), move) );
		}
	}
}