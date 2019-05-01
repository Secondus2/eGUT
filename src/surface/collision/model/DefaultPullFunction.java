package surface.collision.model;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.Global;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Settable;
import surface.collision.CollisionFunction;
import surface.collision.CollisionVariables;
import utility.Helper;

/**
 * default pull CollisionFunction
 */
public class  DefaultPullFunction implements CollisionFunction
{
	private double _forceScalar = Global.pull_scalar;
	
	/**
	 * Implementation of the Instantiatable interface
	 * FIXME currently not settable from xml yet
	 */
	public void instantiate(Element xmlElement, Settable parent)
	{
		String forceScalar = XmlHandler.gatherAttribute(xmlElement, 
				XmlRef.forceScalar);
		if( !Helper.isNullOrEmpty( forceScalar ) )
				this._forceScalar = Double.valueOf( forceScalar );
		if(Log.shouldWrite(Tier.BULK))
			Log.out(Tier.BULK, "initiating " + 
					this.getClass().getSimpleName());
	}
	
	/**
	 * \brief return the currently set force scalar for this 
	 * CollisionFunction
	 * 
	 * @return double force scalar
	 */
	public double forceScalar()
	{
		return this._forceScalar;
	}
	
	/**
	 * \brief calculate a force between two objects based on the distance
	 * 
	 * @param distance
	 * @param var: functions as a scratch book to pass multiple in/output 
	 * variables between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var)
	{
		/*
		 * If distance is in the range, apply the pull force.
		 * Otherwise, return a zero vector. A small distance is allowed to
		 * prevent objects bouncing in equilibrium 
		 */
		if ( var.distance > 0.001 && var.distance < var.pullRange ) 
		{
			/* Linear. */
			double c = Math.abs(this._forceScalar * var.distance);
			/* dP is overwritten here. */
			Vector.normaliseEuclidEquals(var.interactionVector, c);
			return var;
		} 
		Vector.setAll(var.interactionVector, 0.0);
		return var;
	}
}