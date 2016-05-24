package surface;

import dataIO.ObjectFactory;
import generalInterfaces.Copyable;
import generalInterfaces.HasBoundingBox;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Ball extends Surface implements HasBoundingBox, Copyable
{
	/**
	 * Location of the center of this sphere.
	 */
	public Point _point;
	/**
	 * Radius of this sphere.
	 */
	public double _radius;

	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/

	public Ball(Point point, double radius)
	{
		this._point = point;
		this._radius = radius;
	}


	/**
	 * \brief Copy constructor.
	 * 
	 * @param sphere
	 */
	public Ball(Ball sphere)
	{
		this._point = (Point) ObjectFactory.copy(sphere._point);
		this._radius = (double) ObjectFactory.copy(sphere._radius);
	}

	/**
	 * 
	 * @param center
	 * @param radius
	 */
	public Ball(double[] center, double radius)
	{
		this._point = new Point(center);
		this._radius = radius;
	}

	/**
	 * \brief Construct a ball with zero radius.
	 * 
	 * @param center
	 */
	public Ball(double[] center)
	{
		this(center, 0.0);
	}

	@Override
	public Object copy()
	{
		Point p = (Point) this._point.copy();
		double r = (double) ObjectFactory.copy(this._radius);
		return new Ball(p, r);
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS & SETTERS
	 ************************************************************************/

	public Type type()
	{
		return Surface.Type.SPHERE;
	}

	/**
	 * @return Location vector of the center of this sphere.
	 */
	public double[] getCenter()
	{
		return this._point.getPosition();
	}

	public double getRadius()
	{
		return this._radius;
	}

	public void set(double radius, double notUsed)
	{
		this._radius = radius;
	}

	/*************************************************************************
	 * BOUNDING BOX
	 ************************************************************************/
	
	public BoundingBox boundingBox(double margin)
	{
		return new BoundingBox(this.getCenter(), this._radius, margin);
	}

	public BoundingBox boundingBox()
	{
		return new BoundingBox(this.getCenter(), this._radius);
	}
}