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
	
	/**
	 * These constants need to be set in the protocol file, Global class or config.
	 */
	public String BODY = AspectRef.agentBody;
	public String VOLUME = AspectRef.agentVolume;
	public String STOCHASTIC_STEP = AspectRef.agentStochasticStep;
	public String TEMP = AspectRef.temp;
	private double boltzmannK = 4.970e7;
	//Temperature - settable in compartment??
	private double temperature;
	//Water viscosity - find a better value for this
	private double viscosityWater = 4.148e7;

	public void start(AspectInterface initiator, AspectInterface compliant, 
			Double timeStep)
	{
		Agent agent = (Agent) initiator;
		Body agentBody = (Body) agent.get(BODY);
		int numberOfDimensions = agentBody.nDim();
		List<Point> points = agentBody.getPoints();
		double volume = agent.getDouble(VOLUME);
		temperature = agent.getDouble(TEMP);
		
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
				diffusionCoefficient*timeStep;
		
		double standardDeviation = Math.sqrt(meanSquareDistance);
		
		double distance = ExtraMath.deviateFromSD(0, standardDeviation);
		
		double [] randDir = Vector.randomPlusMinus(numberOfDimensions, 1.0);
		
		double[] move = Vector.times(randDir, distance);
		
		agent.set(STOCHASTIC_STEP, move);
		
		for (Point p : points)
		{
			p.setPosition( Vector.add(p.getPosition(), move) );
		}
	}
}