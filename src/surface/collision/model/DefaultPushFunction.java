package surface.collision.model;

import org.w3c.dom.Element;

import aspect.AspectInterface;
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
 * default push CollisionFunction
 * FIXME currently not settable from xml yet
 */
public class DefaultPushFunction implements CollisionFunction
{
	private double _forceScalar = Global.collision_scalar;
	
	/**
	 * Implementation of the Instantiatable interface
	 */
	public void instantiate(Element xmlElement, Settable parent)
	{
		Double forceScalar = XmlHandler.gatherDouble( xmlElement, 
				XmlRef.forceScalar);
		if( forceScalar != null )
				this._forceScalar = forceScalar;
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
	 * variables
	 * between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var,
			AspectInterface first, AspectInterface second)
	{
		/*
		 * If distance is negative, apply the repulsive force.
		 * Otherwise, return a zero vector. A small overlap is allowed to
		 * prevent objects bouncing in equilibrium 
		 */
		if ( var.distance < -0.001 ) 
		{
			/* Linear. */
			double c = Math.abs( this._forceScalar * var.distance );
			/* two component, better force scaling for low vs high overlap */
//			double c = this._forceScalar * (  Math.abs( var.distance ) + var.distance * var.distance * 1e2 );
			/* dP is overwritten here. */
			Vector.normaliseEuclidEquals( var.interactionVector, c );
			return var;
		}
		/* dP is not overwritten here. */
		Vector.setAll(var.interactionVector, 0.0);
		return var;
	}
}