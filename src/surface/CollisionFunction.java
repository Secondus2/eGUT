package surface;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import idynomics.Global;
import dataIO.Log.Tier;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Settable;
import utility.Helper;

/**
 * \brief CollisionFunctions are used to shape and scale the physical
 * interactions between two objects
 */
public interface CollisionFunction extends Instantiable
{
	/**
	 * \brief return the currently set force scalar for this CollisionFunction
	 * 
	 * @return double force scalar
	 */
	public double forceScalar();
	
	/**
	 * \brief calculate a force between two objects based on the distance
	 * 
	 * @param distance
	 * @param var: functions as a scratch book to pass multiple in/output 
	 * variables between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var);

	/**
	 * default pull CollisionFunction
	 */
	public CollisionFunction DefaultPullFunction = new CollisionFunction()
	{
		private double _forceScalar = Global.collision_scalar;
		
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
			Log.out(Tier.BULK, "initiating DefaultPullFunction");
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
	};
	
	/**
	 * default push CollisionFunction
	 * FIXME currently not settable from xml yet
	 */
	public CollisionFunction DefaultPushFunction = new CollisionFunction()
	{
		private double _forceScalar = Global.pull_scalar;
		
		/**
		 * Implementation of the Instantiatable interface
		 */
		public void instantiate(Element xmlElement, Settable parent)
		{
			String forceScalar = XmlHandler.gatherAttribute( xmlElement, 
					XmlRef.forceScalar);
			if( !Helper.isNullOrEmpty( forceScalar ) )
					this._forceScalar = Double.valueOf( forceScalar );
			Log.out(Tier.BULK, "initiating DefaultPullFunction");
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
		public CollisionVariables interactionForce(CollisionVariables var)
		{
			/*
			 * If distance is negative, apply the repulsive force.
			 * Otherwise, return a zero vector. A small overlap is allowed to
			 * prevent objects bouncing in equilibrium 
			 */
			if ( var.distance < -0.001 ) 
			{
				/* Linear. */
//				double c = Math.abs( this._forceScalar * var.distance );
				/* two component, better force scaling for low vs high overlap */
				double c = this._forceScalar * (  Math.abs( var.distance ) + var.distance * var.distance * 1e2 );
				/* dP is overwritten here. */
				Vector.normaliseEuclidEquals( var.interactionVector, c );
				return var;
			}
			/* dP is not overwritten here. */
			Vector.setAll(var.interactionVector, 0.0);
			return var;
		}
	};
}

