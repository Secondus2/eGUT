package aspect.event;

import java.util.HashMap;
import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import expression.Expression;
import physicalObject.PhysicalObject;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import surface.Surface;
import surface.collision.Collision;

public class DifferentialInteractionForce extends Event {
	
	private String INTERACTIONS = AspectRef.interactionFunctionMap;
	private String BODY = AspectRef.agentBody;
	private String RADIUS = AspectRef.bodyRadius;
	private String SPECIES = XmlRef.species;
	public String CURRENT_PULL_FORCE = AspectRef.collisionCurrentPullForce;
	private Surface initiatorSurface;
	private Surface compliantSurface;
	
	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		Shape shape = (Shape) ( (Agent) initiator ).getCompartment().getShape();
		Map<String,String> interactions = 
				(Map<String,String>) initiator.getValue(INTERACTIONS);
		initiatorSurface = ((Body) initiator.getValue(BODY))
				.getSurfaces().get(0);
		if (compliant.isAspect("body"))
		{
		compliantSurface = ((Body) compliant.getValue(BODY))
				.getSurfaces().get(0);
		}
		else 
		{
			compliantSurface = ((PhysicalObject) compliant).getSurface();
		}
		Collision collision = new Collision(shape);
		double distance = collision.distance(
				initiatorSurface, compliantSurface);
		if (distance > 0.001)
		{
			Expression interactionFunction = 
					new Expression(interactions.get(compliant.getValue(SPECIES)));
			HashMap<String, Double> distanceMap = new HashMap<String, Double>();
			distanceMap.put("distance", distance);
			double force = interactionFunction.getValue((Map)distanceMap);
			initiator.set(CURRENT_PULL_FORCE, force);
		}
		
		
	}
	
	
}
