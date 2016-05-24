package surface;

import generalInterfaces.Copyable;
import linearAlgebra.Vector;


/**
 * \brief TODO needs spring cleaning.. keep Point as a minimal object
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Point implements Copyable
{
	/**
	 * Unique identifier for each point.
	 */
	private static int UNIQUE_ID = 0;
	protected int _uid = ++UNIQUE_ID;

	/**
	 * Location vector.
	 */
	private double[] p;

	/**
	 * Force vector.
	 */
	private double[] f;

	/**
	 * Used by higher-order ODE solvers.
	 */
	private double[][] c;

	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Point(double[] p) 
	{
		/* Copying may be slower to initiate, but is safer. */
		this.setPosition(Vector.copy(p));
		this.setForce(Vector.zeros(p));
	}

	public Point(int nDim)
	{
		this(Vector.zerosDbl(nDim));
	}

	//FIXME: change this to set position random location lowerbound[] 
	// upperbound[], currently domain represents a simple spawn box with sizes
	// "domain", this needs to be a bit more specific
	public Point(int nDim, double domain) 
	{
		this(Vector.randomPlusMinus(nDim, domain));
	}

	public Point(String vectorString)
	{
		this(Vector.dblFromString(vectorString));
	}

	public Point(Point q)
	{
		this.setPosition(Vector.copy(q.p));
		this.setForce(Vector.zeros(p));
	}
	
	public Object copy() 
	{
		return new Point(this.p);
	}

	/*************************************************************************
	 * BASIC GETTERS & SETTERS
	 ************************************************************************/

	public int identifier() 
	{
		return this._uid;
	}

	public int nDim()
	{
		return p.length;
	}
	
	public double[] getPosition()
	{
		return this.p;
	}

	public void setPosition(double[] position)
	{
		this.p = position;
	}
	
	public double[] getForce()
	{
		return this.f;
	}

	public void setForce(double[] force)
	{
		this.f = force;
	}

	private void resetForce()
	{
		Vector.reset(this.f);
	}

	public void addToForce(double[] forceToAdd)
	{
		Vector.addEquals(this.f, forceToAdd);
	}
	
	/*************************************************************************
	 * ODE METHODS
	 ************************************************************************/

	
	public void initialiseC(int size)
	{
		this.c = new double[size][p.length];
	}

	
	/**
	 * \brief performs one Euler step for the mechanical relaxation.
	 * The velocity is expressed as v = (sum forces) / (3 Pi diameter viscosity)
	 * Currently the viscosity of water is assumed.
	 * @param vSquare Highest squared velocity in the system
	 * @param dt Current timestep of the mechanical relaxation
	 * @param radius Radius of the Point
	 * @return vSquare, if the squared velocity of this point is higher vSquare
	 * is updated.
	 */
	// TODO Rob [17May2016]: isn't a Point with a radius a Ball?
	public void euStep(double dt, double radius) 
	{
		// TODO for (longer) rod segments we cannot simply use the radius or
		// diameter but need to use the equivalent spherical diameter
		// definition by wiki: the equivalent diameter of a non-spherical 
		// particle is equal to a diameter of a spherical particle that exhibits 
		// identical properties (in this case hydrodynamic).
		// see pdf forces in microbial systems.
		double[] diff = this.dxdt(radius);
		Vector.timesEquals(diff, dt);
		Vector.addEquals(this.p, diff);
		this.resetForce();
	}

	/**
	 * \brief First stage of Heun's method.
	 * 
	 * @param dt
	 * @param radius
	 */
	public void heun1(double dt, double radius)
	{
		double[] diff = this.dxdt(radius);
		/* Store the old position and velocity. */
		this.c[0] = Vector.copy(this.p);
		this.c[1] = Vector.copy(diff);
		/* Move the location and reset the force. */
		Vector.timesEquals(diff, dt);
		Vector.addEquals(p, diff);
		this.resetForce();
	}

	/**
	 * \brief Second stage of Heun's method.
	 * 
	 * @param dt
	 * @param radius
	 */
	public void heun2(double dt, double radius)
	{
		/*
		 * p = c0 + ((dxdt + c1) * dt / 2)
		 * -> c0 is the old position
		 * -> c1 is the old velocity
		 */
		Vector.addTo(this.p, dxdt(radius), this.c[1]);
		Vector.timesEquals(this.p, dt/2.0);
		Vector.addEquals(this.p, this.c[0]);
		this.resetForce();
	}

	/**
	 * \brief TODO
	 * 
	 * @param radius
	 * @return
	 */
	public double[] dxdt(double radius)
	{
		/*
		 * 53.05 = 1/0.01885
		 * 0.01885 = 3 * pi * (viscosity of water)
		 */
		// TODO calculate from user divined viscosity
		// FIXME Is this from Stoke's Law? Being mysterious is not a virtue
		// when it comes to programming.
		return Vector.times(this.getForce(), 53.05/radius);
	}

	/**
	 * \brief TODO
	 * 
	 * <p>Legacy support: not identical but shoves like there is no
	 * tomorrow.</p>
	 * 
	 * @param dt
	 * @param radius
	 */
	public void shove(double dt, double radius) 
	{
		/*
		 * No point shoving if there's no force.
		 */
		if ( Vector.isZero(this.getForce()) )
			return;
		/*
		 * Scale the force.
		 */
		// TODO note that force is currently scaled may need to revise later
		//TODO explain why!
		double scalar = radius;
		if ( Vector.normEuclid(this.getForce()) < 0.2 )
		{
			/* Anti deadlock. */
			scalar *= 3.0;
		}
		else
		{
			/* Anti catapult */
			scalar *= 0.5;
		}
		Vector.times(this.f, scalar);
		/*
		 * Apply the force and reset it.
		 */
		Vector.addEquals(this.p, this.f);
		this.resetForce();
	}

	/*************************************************************************
	 * REDUNDANT METHODS...?
	 ************************************************************************/

	public double[] coord(double radius) 
	{
		double[] coord = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			coord[i] = p[i] - radius;
		return coord;
	}

	public double[] dimensions(double radius) 
	{
		double[] dimensions = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			dimensions[i] = radius * 2.0;
		return dimensions;
	}

	public double[] upper(double radius) 
	{
		double[] coord = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			coord[i] = p[i] + radius;
		return coord;
	}
	
	public void subtractFromForce(double[] forceToSubtract)
	{
		Vector.minusEquals(this.f, forceToSubtract);
	}
}
