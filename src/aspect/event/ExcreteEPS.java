package aspect.event;

import java.util.Map;

import agent.Agent;
import agent.Body;
import agent.Body.Morphology;
import aspect.AspectInterface;
import aspect.Event;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import surface.CuboidSurface;
import surface.OrientedCuboidSurface;
import surface.Point;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class ExcreteEPS extends Event
{
	
	public String EPS = AspectRef.productEPS;
	public String MAX_INTERNAL_EPS = AspectRef.maxInternalEPS;
	public String EPS_SPECIES = AspectRef.epsSpecies;
	public String MASS = AspectRef.agentMass;
	public String UPDATE_BODY = AspectRef.agentUpdateBody;
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;

	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		Tier level = Tier.BULK;
		/*
		 * Find out how much EPS the agent has right now. If it has none, there
		 * is nothing  more to do.
		 */
		double currentEPS = this.getCurrentEPS(initiator);
		if ( currentEPS == 0.0 )
			return;
		/*
		 * Find out how much EPS the agent can hold before it must excrete.
		 */
		double maxEPS = initiator.getDouble(this.MAX_INTERNAL_EPS);
		/*
		 * Vary this number randomly by about 10%. If the agent has less EPS
		 * than this amount, then exit.
		 */
		// TODO this should probably be set when the agent has its max EPS		
		// value set, to avoid timestep size artifacts
		double epsBlob = ExtraMath.deviateFromCV(maxEPS, 0.1);
		if ( maxEPS > epsBlob )
			return;
		/*
		 * While the agent has more EPS than the "blob", excrete EPS particles.
		 */
		Body body = (Body) initiator.getValue(BODY);
		Morphology morphology = body.getMorphology();
		String epsSpecies = initiator.getString(EPS_SPECIES);
		Compartment comp = ((Agent) initiator).getCompartment();
		double[] epsPos;
		while ( currentEPS > epsBlob )
		{
			if (morphology == Morphology.CUBOID) {
				OrientedCuboidSurface cuboidSurface = 
						(OrientedCuboidSurface) body.getSurfaces().get(0);
				Point[] apicalFace = cuboidSurface.getApicalFace();
				double[] corner1 = apicalFace[0].getPosition();
				double[] corner2 = apicalFace[1].getPosition();
				double[] randomPointOnFace = new double[corner1.length];
				for (int i = 0; i < corner1.length; i++) {
					randomPointOnFace[i] = 
							ExtraMath.getUniRand(corner1[i], corner2[i]);
				}
				epsPos = randomPointOnFace;
				}
				//Fairly rough - allows EPS to be excreted at either "end" of a
				//rod cell, but does not prevent the EPS appearing inside the
				//cell.
			else if (morphology == Morphology.BACILLUS) {
				int rodEnd;
				if (ExtraMath.getRandBool())
					rodEnd = 0;
				else
					rodEnd = 1;
				double[] originalPos = body.getPosition(rodEnd);
				double[] shift = Vector.randomPlusMinus(originalPos.length, 
						0.6 * initiator.getDouble(RADIUS));
				epsPos = Vector.minus(originalPos, shift);
				// FIXME this is not correct, calculate with density
			}
			
			else {
				double[] originalPos = body.getPosition(0);
				double[] shift = Vector.randomPlusMinus(originalPos.length, 
						0.6 * initiator.getDouble(RADIUS));
				epsPos = Vector.minus(originalPos, shift);
				// FIXME this is not correct, calculate with density
			}
			
			compliant = new Agent(epsSpecies, 
					new Body(new Point(epsPos),0.0),
					comp); 
			compliant.set(MASS, epsBlob);
			compliant.reg().doEvent(compliant, null, 0.0, UPDATE_BODY);
			currentEPS -= epsBlob;
			((Agent) compliant).registerBirth();
			if ( Log.shouldWrite(level) )
				Log.out(level, "EPS particle created");
			epsBlob = ExtraMath.deviateFromCV(maxEPS, 0.1);
		}
		this.updateEPS(initiator, currentEPS);
	}
	
	/**
	 * \brief Ask the given agent how much EPS is has right now.
	 * 
	 * @param initiator Agent.
	 * @return Mass of EPS this agent owns.
	 */
	private double getCurrentEPS(AspectInterface initiator)
	{
		/*
		 * Check first if it is just an aspect.
		 */
		if ( initiator.isAspect(this.EPS) )
			return initiator.getDouble(this.EPS);
		/*
		 * Check if it is part of a map of masses.
		 */
		if ( initiator.isAspect(MASS) )
		{
			Object massObject = initiator.getValue(this.MASS);
			if ( massObject instanceof Map )
			{
				@SuppressWarnings("unchecked")
				Map<String,Double> massMap = (Map<String,Double>) massObject;
				if ( massMap.containsKey(this.EPS) )
					return massMap.get(this.EPS);
			}
		}
		/*
		 * Assume there is no EPS.
		 */
		return 0.0;
	}
	
	/**
	 * \brief Tell the given agent to update the mass of EPS it owns.
	 * 
	 * @param initiator Agent.
	 * @param newEPS New EPS mass for this agent.
	 */
	private void updateEPS(AspectInterface initiator, double newEPS)
	{
		/*
		 * Check first if it is just an aspect.
		 */
		if ( initiator.isAspect(this.EPS) )
			initiator.set(this.EPS, newEPS);
		/*
		 * Check if it is part of a map of masses.
		 */
		if ( initiator.isAspect(this.MASS) )
		{
			Object massObject = initiator.getValue(this.MASS);
			if ( massObject instanceof Map )
			{
				@SuppressWarnings("unchecked")
				Map<String,Double> massMap = (Map<String,Double>) massObject;
				if ( massMap.containsKey(this.EPS) )
				{
					massMap.put(this.EPS, newEPS);
					initiator.set(this.MASS, massMap);
				}
			}
		}
	}
}
