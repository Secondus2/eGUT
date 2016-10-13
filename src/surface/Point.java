package surface;

import generalInterfaces.Copyable;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;
import nodeFactory.NodeConstructor;

/**
 * \brief TODO needs spring cleaning.. keep Point as a minimal object
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Point implements Copyable, NodeConstructor
{
	/**
	 * Unique identifier for each point.
	 */
	private static int UNIQUE_ID = 0;
	protected int _uid = ++UNIQUE_ID;

	/**
	 * Location vector.
	 */
	private double[] _p;

	/**
	 * Force vector.
	 */
	private double[] _f;

	/**
	 * Used by higher-order ODE solvers.
	 */
	private double[][] _c;
	private NodeConstructor _parentNode;

	/**
	 * Viscosity of the surrounding medium (in units of Pa s).
	 * Note that 298.15 K = 25°C
	 */
	// TODO make this settable from protocol or, even better, get this value
	// from a temperature or viscosity array on a grid.
	private final static double VISCOSITY = Drag.viscosityWater(298.15);
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
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
		this.setPosition(Vector.copy(q._p));
		this.setForce(Vector.zeros(q._p));
	}
	
	public Object copy() 
	{
		return new Point(this._p);
	}

	/* ***********************************************************************
	 * BASIC GETTERS & SETTERS
	 * **********************************************************************/

	public int identifier() 
	{
		return this._uid;
	}

	public int nDim()
	{
		return this._p.length;
	}
	
	public double[] getPosition()
	{
		return this._p;
	}
	
	public double[] getPolarPosition()
	{
		return Vector.spherify(this._p);
	}

	public void setPosition(double[] position)
	{
		if ( Double.isNaN(position[0]))
			System.out.println(_p);
		this._p = position;
	}
	
	public void setPolarPosition(double[] position)
	{
		if ( Double.isNaN(position[0]))
			System.out.println(_p);
		this._p = Vector.spherify(position);
	}
	
	public double[] getForce()
	{
		return this._f;
	}

	public void setForce(double[] force)
	{
		this._f = force;
	}

	public void resetForce()
	{
		Vector.reset(this._f);
	}

	/**
	 * \brief TODO
	 * 
	 * @param forceToAdd
	 */
	public void addToForce(double[] forceToAdd)
	{
		Vector.addEquals(this._f, forceToAdd);
	}
	
	/* ***********************************************************************
	 * ODE METHODS
	 * **********************************************************************/

	
	public void initialiseC(int size)
	{
		// TODO consider using lineraAlgebra.Matrix.zerosDbl(size, length) here
		this._c = new double[size][this._p.length];
	}

	
	/**
	 * \brief performs one Euler step for the mechanical relaxation.
	 * The velocity is expressed as v = (sum forces) / (3 Pi diameter viscosity)
	 * Currently the viscosity of water is assumed.
	 * 
	 * @param dt Current timestep of the mechanical relaxation.
	 * @param radius Radius of a sphere (in units of micrometer).
	 */
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
		Vector.addEquals(this._p, diff);
		this.resetForce();
	}

	/**
	 * \brief First stage of Heun's method.
	 * 
	 * @param dt Time step to use (in units of second).
	 * @param radius Radius of a sphere (in units of micrometer).
	 */
	public void heun1(double dt, double radius)
	{
		double[] diff = this.dxdt(radius);
		/* Store the old position and velocity. */
		// TODO consider using copyTo for setting c[0] and c[1]
		this._c[0] = Vector.copy(this._p);
		this._c[1] = Vector.copy(diff);
		/* Move the location and reset the force. */
		Vector.timesEquals(diff, dt);
		Vector.addEquals(this._p, diff);
		this.resetForce();
	}

	/**
	 * \brief Second stage of Heun's method.
	 * 
	 * @param dt Time step to use (in units of second).
	 * @param radius Radius of a sphere (in units of micrometer).
	 */
	public void heun2(double dt, double radius)
	{
		/*
		 * p = c0 + ((dxdt + c1) * dt / 2)
		 * -> c0 is the old position
		 * -> c1 is the old velocity
		 */
		Vector.addTo(this._p, this.dxdt(radius), this._c[1]);
		Vector.timesEquals(this._p, dt * 0.5);
		Vector.addEquals(this._p, this._c[0]);
		this.resetForce();
	}

	/**
	 * \brief Find the velocity of this point.
	 * 
	 * FIXME for non spherical objects the representative sphere radius should
	 * be used rather than the actual radius of the object
	 * 
	 * @param radius The radius of the sphere-swept volume this point belongs
	 * to will affect the drag on it by the surrounding fluid. Assumed in units
	 * of micrometer.
	 * @return Vector describing the velocity of this point in units of
	 * micrometer per second.
	 */
	// TODO consider making a _dxdt variable that is updated by this method,
	// rather than creating a new vector every time.
	public double[] dxdt(double radius)
	{
		return Vector.times(this.getForce(), 
				1.0/Drag.dragOnSphere(radius, VISCOSITY));
	}

	/**
	 * \brief TODO
	 * 
	 * <p>Legacy support: not identical but shoves like there is no
	 * tomorrow.</p>
	 * 
	 * @param dt Time step to use (in units of second).
	 * @param radius Radius of a sphere (in units of micrometer).
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
			scalar *= 2.0;
		}
		else
		{
			/* Anti catapult */
			scalar *= 0.2;
		}
		Vector.timesEquals(this._f, scalar);
		/*
		 * Apply the force and reset it.
		 */
		Vector.addEquals(this._p, this._f);
		this.resetForce();
	}
	
	/* ***********************************************************************
	 * NODE CONSTRUCTION
	 * **********************************************************************/

	@Override
	public ModelNode getNode()
	{
		/* point node */
		ModelNode modelNode = new ModelNode(XmlRef.point, this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);

		/* position attribute */
		modelNode.add(new ModelAttribute(XmlRef.position, 
				Vector.toString(this._p), null, true ));

		return modelNode;
	}

	@Override
	public void setNode(ModelNode node)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void addChildObject(NodeConstructor childObject)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String defaultXmlTag()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public NodeConstructor getParent() 
	{
		return this._parentNode;
	}
}
