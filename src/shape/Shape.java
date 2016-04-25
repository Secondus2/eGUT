/**
 * 
 */
package shape;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import boundary.BoundaryConnected;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import generalInterfaces.XMLable;
import grid.SpatialGrid.GridGetter;
import grid.resolution.ResolutionCalculator.ResCalc;
import grid.subgrid.SubgridPoint;
import linearAlgebra.Vector;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelMaker.Requirement;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import shape.Dimension.DimensionMaker;
import shape.ShapeConventions.DimName;
import surface.Plane;
import surface.Surface;
import utility.Helper;
/**
 * \brief Abstract class for all shape objects.
 * 
 * <p>These are typically used by {@code Compartment}s; cell shapes are
 * currently described using {@code Body} and {@code Surface} objects.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Shape implements
					CanPrelaunchCheck, IsSubmodel, XMLable, NodeConstructor

{
	/**
	 * Ordered dictionary of dimensions for this shape.
	 */
	protected LinkedHashMap<DimName, Dimension> _dimensions = 
								 	   new LinkedHashMap<DimName, Dimension>();
	/**
	 * List of boundaries in a dimensionless compartment, or internal
	 * boundaries in a dimensional compartment.
	 */
	protected Collection<Boundary> _otherBoundaries = 
													new LinkedList<Boundary>();
	/**
	 * Surface Object for collision detection methods
	 */
	protected Collection<Surface> _surfaces = new LinkedList<Surface>();
	/**
	 * TODO
	 */
	protected ModelNode _modelNode;
	/**
	 * Current coordinate considered by the internal iterator.
	 */
	protected int[] _currentCoord;
	/**
	 * The number of voxels, in each dimension, for the current coordinate of
	 * the internal iterator.
	 */
	protected int[] _currentNVoxel;
	/**
	 * Current neighbour coordinate considered by the neighbor iterator.
	 */
	protected int[] _currentNeighbor;
	/**
	 * Whether the neighbor iterator is currently valid (true) or invalid
	 * (false).
	 */
	protected boolean _nbhValid;
	/**
	 * A helper vector for finding the location of the origin of a voxel.
	 */
	protected final static double[] VOXEL_ORIGIN_HELPER = Vector.vector(3,0.0);
	/**
	 * A helper vector for finding the location of the centre of a voxel.
	 */
	protected final static double[] VOXEL_CENTRE_HELPER = Vector.vector(3,0.5);
	/**
	 * A helper vector for finding the 'upper most' location of a voxel.
	 */
	protected final static double[] VOXEL_All_ONE_HELPER = Vector.vector(3,1.0);
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public ModelNode getNode()
	{
		if ( this._modelNode == null )
		{
			ModelNode myNode = new ModelNode(XmlLabel.compartmentShape, this);
			myNode.requirement = Requirements.EXACTLY_ONE;
			myNode.add(new ModelAttribute(XmlLabel.classAttribute, 
											this.getName(), null, false ));
			this._modelNode = myNode;
		}
		return this._modelNode;
	}

	@Override
	public void setNode(ModelNode node)
	{
		// TODO check if a node is being overwritten?
		this._modelNode = node;
	}

	@Override
	public NodeConstructor newBlank()
	{
		return (Shape) Shape.getNewInstance(
				Helper.obtainInput(getAllOptions(), "Shape class", false));
	}

	@Override
	public void addChildObject(NodeConstructor childObject)
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public String defaultXmlTag()
	{
		return XmlLabel.compartmentShape;
	}
	
	/**
	 * \brief Initialise from an XML element.
	 * 
	 * <p>Note that all subclasses of Shape use this for initialisation,
	 * except for Dimensionless.</p>
	 * 
	 * @param xmlNode
	 */
	// TODO remove once ModelNode, etc is working
	public void init(Element xmlElem)
	{
		NodeList childNodes;
		Element childElem;
		String str;
		/* Set up the dimensions. */
		DimName dimName;
		childNodes = XmlHandler.getAll(xmlElem, XmlLabel.shapeDimension);
		for ( int i = 0; i < childNodes.getLength(); i++ )
		{
			childElem = (Element) childNodes.item(i);
			try
			{
				str = XmlHandler.obtainAttribute(childElem,
												XmlLabel.nameAttribute);
				dimName = DimName.valueOf(str);
				this.getDimension(dimName).init(childElem);
			}
			catch (IllegalArgumentException e)
			{
				Log.out(Tier.CRITICAL, "Warning: input Dimension not "
						+ "recognised by shape " + this.getClass().getName()
						+ ", use: " + Helper.enumToString(DimName.class));
			}
		}
		/* Set up any other boundaries. */
		Boundary aBoundary;
		childNodes = XmlHandler.getAll(xmlElem, XmlLabel.dimensionBoundary);
		for ( int i = 0; i < childNodes.getLength(); i++ )
		{
			childElem = (Element) childNodes.item(i);
			str = childElem.getAttribute(XmlLabel.classAttribute);
			aBoundary = (Boundary) Boundary.getNewInstance(str);
			aBoundary.init(childElem);
			this.addOtherBoundary(aBoundary);
		}
	}
	
	@Override
	public String getXml()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public String getName()
	{
		return this.getClass().getSimpleName();
	}
	
	/**
	 * @return The grid-getter object for this shape.
	 */
	public abstract GridGetter gridGetter();
	
	protected abstract double[] getLocalPosition(double[] cartesian);
	
	protected abstract double[] getGlobalLocation(double[] local);
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimensionName {@code String} name of the dimension. Lower/upper
	 * case is irrelevant.
	 */
	public void makeCyclic(String dimensionName)
	{
		this.makeCyclic(DimName.valueOf(dimensionName.toUpperCase()));
	}
	
	/**
	 * @param dimension The name of the dimension requested.
	 * @return The {@code Dimension} object.
	 */
	public Dimension getDimension(DimName dimension)
	{
		return this._dimensions.get(dimension);
	}
	
	/**
	 * \brief Finds the index of the dimension name given.
	 * 
	 * <p>For example, in a cuboid, {@code Z} has index {@code 2}.</p>
	 * 
	 * @param dimension DimName of a dimension thought to be in this
	 * {@code Shape}.
	 * @return Index of the dimension, if present; {@code -1}, if not.
	 */
	public int getDimensionIndex(DimName dimension)
	{
		int out = 0;
		for ( DimName d : this._dimensions.keySet() )
		{
			if ( d == dimension )
				return out;
			out++;
		}
		return -1;
	}
	
	/**
	 * \brief Get the requested {@code Dimension} in a safe way.
	 * 
	 * @param dimension The name of the dimension requested.
	 * @return The {@code Dimension} object.
	 */
	protected Dimension getDimensionSafe(DimName dimension)
	{
		if ( this._dimensions.containsKey(dimension) )
		{
			Dimension dim = this._dimensions.get(dimension);
			if ( dim == null )
				this._dimensions.put(dimension, (dim = new Dimension()));
			return dim;
		}
		else
		{
			// TODO safety
			return this.getDimension(dimension);
		}
	}
	
	/**
	 * \brief Make the given dimension cyclic.
	 * 
	 * @param dimension {@code DimName} enumeration of the dimension.
	 */
	public void makeCyclic(DimName dimension)
	{
		this.getDimensionSafe(dimension).setCyclic();
	}
	
	public void setBoundary(DimName dimension, int index, Boundary bndry)
	{
		this.getDimensionSafe(dimension).setBoundary(bndry, index);
	}
	
	
	/**
	 * \brief Set the minimum and maximum boundaries for the given dimension.
	 * 
	 * @param dimensionName {@code String} name of the dimension.
	 * @param bndry
	 * @param setMin
	 */
	public void setBoundary(String dimension, Boundary bndry, int index)
	{
		this.setBoundary(DimName.valueOf(dimension), index, bndry);
	}
	
	
	/**
	 * \brief Gets the side lengths of only the significant dimensions.
	 * 
	 * @param lengths {@code double} array of significant side lengths.
	 * @see #getSideLengths()
	 */
	public double[] getDimensionLengths()
	{
		double[] out = new double[this._dimensions.size()];
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			out[i] = dim.getLength();
			i++;
		}
		return out;
	}
	
	/**
	 * \brief Set the dimension lengths of this shape.
	 * 
	 * <p>NOTE: If <b>lengths</b> has more than elements than this shape has
	 * dimensions, the extra elements will be ignored. If <b>lengths</b> has
	 * fewer elements than this shape has dimensions, the remaining dimensions
	 * will be given length zero.</p>
	 * 
	 * @param lengths {@code double} array of dimension lengths.
	 */
	public void setDimensionLengths(double[] lengths)
	{
		int i = 0;
		Dimension dim;
		for ( DimName d : this._dimensions.keySet() )
		{
			dim = this.getDimensionSafe(d);
			dim.setLength(( i < lengths.length ) ? lengths[i] : 0.0);
			i++;
		}
	}
	
	/**
	 * \brief Set the resolution calculator for the given dimension, taking
	 * care to resolve any dependencies on other dimensions.
	 * 
	 * @param dName The name of the dimension to set for.
	 * @param resC A resolution calculator.
	 */
	public abstract void setDimensionResolution(DimName dName, ResCalc resC);
	
	/**
	 * \brief Set up this {@code Shape}'s surfaces.
	 * 
	 * <p>Surfaces are used by {@code Agent}s in collision detection.</p>
	 */
	public abstract void setSurfaces();
	
	/**
	 * \brief Set a flat surface at each extreme of the given dimension.
	 * 
	 * @param aDimName The name of the dimension required.
	 */
	protected void setPlanarSurfaces(DimName aDimName)
	{
		Dimension dim = this.getDimension(aDimName);
		/* Safety. */
		if ( dim == null )
			throw new IllegalArgumentException("Dimension not recognised");
		/* Cyclic behaviour is handled elsewhere. */
		if ( dim.isCyclic() )
			return;
		/*
		 * Create planar surfaces at each extreme.
		 */
		int index = this.getDimensionIndex(aDimName);
		double[] normal = Vector.zerosDbl( this.getNumberOfDimensions() );
		Plane p;
		/* The minimum extreme. */
		normal[index] = 1.0;
		p = new Plane( Vector.copy(normal), dim.getExtreme(0) );
		this._surfaces.add( p );
		/* The maximum extreme. */
		normal[index] = -1.0;
		p = new Plane( Vector.copy(normal), - dim.getExtreme(1) );
		this._surfaces.add( p );
	}
	
	/**
	 * @return The set of {@code Surface}s for this {@code Shape}.
	 */
	public Collection<Surface> getSurfaces()
	{
		return this._surfaces;
	}
	
	/**
	 * \brief Return the number of "true" dimensions this shape has.
	 * 
	 * <p>Note that even 0-, 1- and 2-dimensional shapes may have a nonzero 
	 * thickness on their "missing" dimension(s).</p>
	 * 
	 * @return {@code int} number of dimensions for this shape.
	 */
	// TODO clarify javadoc when dimensions restructured.
	public int getNumberOfDimensions()
	{
		return this._dimensions.size();
	}
	
	/**
	 * @return The set of dimension names for this {@code Shape}.
	 */
	public Set<DimName> getDimensionNames()
	{
		return this._dimensions.keySet();
	}
	
	/**
	 * \brief Get the Resolution Calculator for the given dimension, at the
	 * given coordinate.
	 * 
	 * @param coord 
	 * @param dim 
	 * @return 
	 */
	protected abstract ResCalc getResolutionCalculator(int[] coord, int dim);
	
	/*************************************************************************
	 * BOUNDARIES
	 ************************************************************************/
	
	/**
	 * \brief Add the given {@code Boundary} to this {@code Shape}'s list of
	 * "other" boundaries, i.e. those not associated with a {@code Dimension}.
	 * 
	 * @param aBoundary {@code Boundary} object.
	 */
	public void addOtherBoundary(Boundary aBoundary)
	{
		this._otherBoundaries.add(aBoundary);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<BoundaryConnected> getConnectedBoundaries()
	{
		LinkedList<BoundaryConnected> cB = new LinkedList<BoundaryConnected>();
		for ( Dimension dim : this._dimensions.values() )
				for ( Boundary b : dim.getBoundaries() )
					if ( b instanceof BoundaryConnected )
						cB.add((BoundaryConnected) b);
		for ( Boundary b : this._otherBoundaries )
			if ( b instanceof BoundaryConnected )
				cB.add((BoundaryConnected) b);
		return cB;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<Boundary> getOtherBoundaries()
	{
		return this._otherBoundaries;
	}
	
	/**
	 * \brief Check if a given location is inside this shape.
	 * 
	 * @param location 
	 * @return
	 */
	public boolean isInside(double[] location)
	{
		double[] position = this.getLocalPosition(location);
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( ! dim.isInside(position[i]) )
				return false;
			i++;
		}
		return true;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param location
	 */
	public void applyBoundaries(double[] location)
	{
		double[] position = this.getLocalPosition(location);
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			position[i] = dim.getInside(position[i]);
			i++;
		}
		Vector.copyTo(location, this.getGlobalLocation(position));
	}
	
	/**
	 * \brief Find all neighbouring points in space that would  
	 * 
	 * <p>For use by the R-Tree.</p>
	 * 
	 * @param location
	 * @return
	 */
	public LinkedList<double[]> getCyclicPoints(double[] location)
	{
		// TODO safety with vector length
		/*
		 * Find all the cyclic points in 
		 */
		double[] position = this.getLocalPosition(location);
		LinkedList<double[]> localPoints = new LinkedList<double[]>();
		localPoints.add(position);
		LinkedList<double[]> temp = new LinkedList<double[]>();
		double[] newPoint;
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			if ( dim.isCyclic() )
			{
				// TODO We don't need these in an angular dimension with 2 * pi
				for ( double[] loc : localPoints )
				{
					/* Add the point below. */
					newPoint = Vector.copy(loc);
					newPoint[i] -= dim.getLength();
					temp.add(newPoint);
					/* Add the point above. */
					newPoint = Vector.copy(loc);
					newPoint[i] += dim.getLength();
					temp.add(newPoint);
				}
				/* Transfer all from temp to out. */
				localPoints.addAll(temp);
				temp.clear();
			}
			/* Increment the dimension iterator, even if this isn't cyclic. */
			i++;
		}
		/* Convert everything back into global coordinates and return. */
		LinkedList<double[]> out = new LinkedList<double[]>();
		for ( double[] p : localPoints )
			out.add(this.getGlobalLocation(p));
		return localPoints;
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p><b>a</b> - <b>b</b>, i.e. the vector from <b>b</b> to <b>a</b>.</p>
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double[] getMinDifference(double[] a, double[] b)
	{
		// TODO safety with vector length & number of dimensions
		// TOD check this is the right approach in polar geometries
		Vector.checkLengths(a, b);
		double[] aLocal = this.getLocalPosition(a);
		double[] bLocal = this.getLocalPosition(b);
		double[] diffLocal = new double[a.length];
		int i = 0;
		for ( Dimension dim : this._dimensions.values() )
		{
			diffLocal[i] = dim.getShortest(aLocal[i], bLocal[i]);
			i++;
		}
		return this.getGlobalLocation(diffLocal);
	}
	
	/*************************************************************************
	 * VOXELS
	 ************************************************************************/
	
	/**
	 * \brief Converts a coordinate in the grid's array to a location in simulated 
	 * space. 
	 * 
	 * 'Subcoordinates' can be transformed using the 'inside' array.
	 * For example type getLocation(coord, new double[]{0.5,0.5,0.5})
	 * to get the center point of the grid cell defined by 'coord'.
	 * 
	 * @param coord - a coordinate in the grid's array.
	 * @param inside - relative position inside the grid cell.
	 * @return - the location in simulation space.
	 */
	public double[] getLocation(int[] coord, double[] inside)
	{
		double[] loc = Vector.copy(inside);
		int nDim = this.getNumberOfDimensions();
		ResCalc rC;
		for ( int dim = 0; dim < nDim; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			loc[dim] *= rC.getResolution(coord[dim]);
			loc[dim] += rC.getCumulativeResolution(coord[dim] - 1);
		}
		return loc;
	}
	
	/**
	 * \brief Find the location of the lower corner of the voxel specified by
	 * the given coordinates.
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return Continuous location of the lower corner of this voxel.
	 */
	public double[] getVoxelOrigin(int[] coord)
	{
		return getLocation(coord, VOXEL_ORIGIN_HELPER);
	}
	
	/**
	 * \brief Find the location of the centre of the voxel specified by the
	 * given coordinates.
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return Continuous location of the centre of this voxel.
	 */
	public double[] getVoxelCentre(int[] coord)
	{
		return getLocation(coord, VOXEL_CENTRE_HELPER);
	}
	
	/**
	 * \brief Get the corner farthest from the origin of the voxel specified. 
	 * 
	 * @param coord
	 * @return
	 */
	protected double[] getVoxelUpperCorner(int[] coord)
	{
		return getLocation(coord, VOXEL_All_ONE_HELPER);
	}
	
	/**
	 * \brief Calculate the volume of the voxel specified by the given
	 * coordinates.
	 * 
	 * @param coord Discrete coordinates of a voxel on this grid.
	 * @return Volume of this voxel.
	 */
	public abstract double getVoxelVolume(int[] coord);
	
	/**
	 * \brief Get the side lengths of the voxel given by the <b>coord</b>.
	 * Write the result into <b>destination</b>.
	 * 
	 * @param destination
	 * @param coord
	 */
	public void getVoxelSideLengthsTo(double[] destination, int[] coord)
	{
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			destination[dim] = rC.getResolution(coord[dim]);
		}
	}
	
	/*************************************************************************
	 * SUBVOXEL POINTS
	 ************************************************************************/
	
	/**
	 * \brief List of sub-voxel points at the current coordinate.
	 * 
	 * <p>Useful for distributing agent-mediated reactions over the grid.</p>
	 * 
	 * @param targetRes
	 * @return
	 */
	public List<SubgridPoint> getCurrentSubvoxelPoints(double targetRes)
	{
		/* 
		 * Initialise the list and add a point at the origin.
		 */
		ArrayList<SubgridPoint> out = new ArrayList<SubgridPoint>();
		SubgridPoint current = new SubgridPoint();
		out.add(current);
		/*
		 * For each dimension, work out how many new points are needed and get
		 * these for each point already in the list.
		 */
		int nP, nCurrent;
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			// TODO Rob[17Feb2016]: This will need improving for polar grids...
			// I think maybe would should introduce a subclass of Dimension for
			// angular dimensions.
			rC = this.getResolutionCalculator(this._currentCoord, dim);
			nP = (int) (rC.getResolution(this._currentCoord[dim])/targetRes);
			nCurrent = out.size();
			for ( int j = 0; j < nCurrent; j++ )
			{
				current = out.get(j);
				/* Shift this point up by half a sub-resolution. */
				current.internalLocation[dim] += (0.5/nP);
				/* Now add extra points at sub-resolution distances. */
				for ( double i = 1.0; i < nP; i++ )
					out.add(current.getNeighbor(dim, i/nP));
			}
		}
		/* Now find the real locations and scale the volumes. */
		// TODO this probably needs to be slightly different in polar grids
		// to be completely accurate
		double volume = this.getVoxelVolume(this._currentCoord) / out.size();
		for ( SubgridPoint aSgP : out )
		{
			aSgP.realLocation = this.getLocation(this._currentCoord,
													aSgP.internalLocation);
			aSgP.volume = volume;
		}
		return out;
	}
	
	/*************************************************************************
	 * COORDINATE ITERATOR
	 ************************************************************************/
	
	/**
	 * \brief Return the coordinate iterator to its initial state.
	 * 
	 * @return The value of the coordinate iterator.
	 */
	public int[] resetIterator()
	{
		if ( this._currentCoord == null )
			this._currentCoord = Vector.zerosInt(this.getNumberOfDimensions());
		else
			Vector.reset(this._currentCoord);
		return this._currentCoord;
	}
	
	/**
	 * \brief Determine whether the current coordinate of the iterator is
	 * outside the grid in the dimension specified.
	 * 
	 * @param dim Index of the dimension to look at.
	 * @return Whether the coordinate iterator is inside (false) or outside
	 * (true) the grid along this dimension.
	 */
	protected boolean iteratorExceeds(int dim)
	{
		return this._currentCoord[dim] >= this._currentNVoxel[dim];
	}
	
	/**
	 * \brief Check if the current coordinate of the internal iterator is
	 * valid.
	 * 
	 * @return True if is valid, false if it is invalid.
	 */
	public boolean isIteratorValid()
	{
		int nDim = this.getNumberOfDimensions();
		for ( int dim = 0; dim < nDim; dim++ )
			if ( this.iteratorExceeds(dim) )
				return false;
		return true;
	}
	
	/**
	 * \brief Step the coordinate iterator forward once.
	 * 
	 * @return The new value of the coordinate iterator.
	 */
	public int[] iteratorNext()
	{
		/*
		 * We have to step through last dimension first, because we use jagged 
		 * arrays in the PolarGrids.
		 */
		_currentCoord[2]++;
		if ( this.iteratorExceeds(2) )
		{
			_currentCoord[2] = 0;
			_currentCoord[1]++;
			if ( this.iteratorExceeds(1) )
			{
				_currentCoord[1] = 0;
				_currentCoord[0]++;
			}
		}
		return _currentCoord;
	}
	
	/**
	 * \brief Get the number of voxels in each dimension for the current
	 * coordinates.
	 * 
	 * <p>For {@code CartesianGrid} the value of <b>coords</b> will be
	 * irrelevant, but it will make a difference in the polar grids.</p>
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return A 3-vector of the number of voxels in each dimension.
	 */
	public int[] updateCurrentNVoxel()
	{
		this.getNVoxel(this._currentCoord, this._currentNVoxel);
		return this._currentNVoxel;
	}
	
	/**
	 * \brief Get the number of voxels in each dimension for the given
	 * coordinates.
	 * 
	 * <p>For {@code CartesianGrid} the value of <b>coords</b> will be
	 * irrelevant, but it will make a difference in the polar grids.</p>
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return A 3-vector of the number of voxels in each dimension.
	 */
	// TODO update javadoc
	protected abstract void getNVoxel(int[] coords, int[] outNVoxel);
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		/* Check all dimensions are ready. */
		for ( Dimension dim : this._dimensions.values() )
			if ( ! dim.isReadyForLaunch() )
			{
				// TODO
				return false;
			}
		/* If there are any other boundaries, check these are ready. */
		for ( Boundary bound : this._otherBoundaries )
			if ( ! bound.isReadyForLaunch() )
				return false;
		/* All checks passed: ready to launch. */
		return true;
	}
	
	/*************************************************************************
	 * XML-ABLE
	 ************************************************************************/
	
	public static Shape getNewInstance(String className)
	{
		return (Shape) XMLable.getNewInstance(className, "shape.ShapeLibrary$");
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	// TODO remove all this once ModelNode, etc is working
	
	public List<InputSetter> getRequiredInputs()
	{
		List<InputSetter> out = new LinkedList<InputSetter>();
		for ( DimName d : this._dimensions.keySet() )
			out.add(new DimensionMaker(d, Requirement.EXACTLY_ONE, this));
		// TODO other boundaries
		return out;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static String[] getAllOptions()
	{
		return Helper.getClassNamesSimple(
									ShapeLibrary.class.getDeclaredClasses());
	}
	
	public void acceptInput(String name, Object input)
	{
		if ( input instanceof Dimension )
		{
			Dimension dim = (Dimension) input;
			DimName dN = DimName.valueOf(name);
			this._dimensions.put(dN, dim);
		}
	}
	
	public static class ShapeMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = 1486068039985317593L;

		public ShapeMaker(Requirement req, IsSubmodel target)
		{
			super(XmlLabel.compartmentShape, req, target);
		}
		
		@Override
		public void doAction(ActionEvent e)
		{
			// TODO do safety properly
			String shapeName;
			if ( e == null )
				shapeName = "";
			else
				shapeName = e.getActionCommand();
			this.addSubmodel(Shape.getNewInstance(shapeName));
		}
		
		@Override
		public Object getOptions()
		{
			return Shape.getAllOptions();
		}
	}
	
	/*************************************************************************
	 * POLAR SHAPE
	 ************************************************************************/
	
	/**
	 * \brief Subclass of {@code Shape} that has at least one angular
	 * dimension.
	 * 
	 * <p>Note that the size of voxels in angular dimensions will depend on
	 * other dimensions.</p>
	 */
	public static abstract class Polar extends Shape
	{
		/**
		 * Storage container for dimensions that this {@code Shape} is not yet
		 * ready to initialise.
		 */
		protected HashMap<DimName,ResCalc> _rcStorage =
											new HashMap<DimName,ResCalc>();
		
		public Polar()
		{
			super();
			/* There is no need for an r-min boundary. */
			Dimension dim = new Dimension();
			dim.setBoundaryOptional(0);
			this._dimensions.put(DimName.R, dim);
		}
		
		/**
		 * \brief Try to initialise a resolution calculator from storage, for a
		 * dimension that is dependent on another.
		 * 
		 * @param dName Name of the dimension to try.
		 */
		protected void trySetDimRes(DimName dName)
		{
			ResCalc rC = this._rcStorage.get(dName);
			if ( rC != null )
				this.setDimensionResolution(dName, rC);
		}
	}
}
