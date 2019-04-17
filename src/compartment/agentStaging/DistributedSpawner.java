package compartment.agentStaging;

import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;


/**
 * \Brief Spawner places agents on grid with equidistant spacing.
 * 
 * Dimensions with spacing interval of 0.0 will only receive agents at the 
 * orient layer.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * 
 *
 */
public class DistributedSpawner extends Spawner {
	
	private double[] _spacing = null;
	private double[] _orient = null;
	private double[] _max;
	
	public void init(Element xmlElem, AgentContainer agents, 
			String compartmentName)
	{
		super.init(xmlElem, agents, compartmentName);
		
		this._max = this.getCompartment().getShape().getDimensionLengths();
		
		this._spacing = (double[]) this.getValue("spacing");
		this._orient = (double[]) this.getValue("orient");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void spawn() 
	{
		LinkedList<double[]> locations = new LinkedList<double[]>();
		for( double d : positions(_orient[0],_spacing[0], _max[0]))
			locations.add( new double[] {d} );
		
		LinkedList<double[]> temp = new LinkedList<double[]>();
		for(int i = 1; i < _orient.length; i++)
		{
			for( double d : positions(_orient[i],_spacing[i], _max[i]))
			{
				for( double[] loc : locations )
				{
					double[] dloc = Vector.copy( loc );
					temp.add( Vector.append( dloc , d ) );
				}
			}
			locations = (LinkedList<double[]>) temp.clone();
		}
		for ( double[] loc : locations )
			this.spawnAgent(loc);
	}
	
	/**
	 * A list with all equally spaced positions within linear space starting
	 * start and stopping before max.
	 * @param start
	 * @param space
	 * @param max
	 * @return
	 */
	private LinkedList<Double> positions(double start, double space, double max)
	{
		LinkedList<Double> out = new LinkedList<Double>();
		if ( space == 0.0 || start+space > max)
		{
			out.add(start);
		} else if ( start > max ) {
			Log.out(Tier.CRITICAL, this.getClass().getSimpleName() + " orient "
					+ "outside domain intervals." );
		} else {
			for(double position = start; position < max; position += space)
				out.add(position);
		}
		return out;
	}
	
	/**
	 * Spawn agent at location.
	 * @param location
	 */
	private void spawnAgent(double[] location)
	{
		/* use copy constructor */
		Agent newAgent = new Agent(this.getTemplate());
		newAgent.set(AspectRef.agentBody, 
				new Body( this.getMorphology(), location, 0.0, 0.0 ) );
		newAgent.setCompartment( this.getCompartment() );
		newAgent.registerBirth();
	}
}
