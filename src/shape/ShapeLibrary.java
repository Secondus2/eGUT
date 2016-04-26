/**
 * 
 */
package shape;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.ObjectRef;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import grid.DummyGrid;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.resolution.ResolutionCalculator.ResCalc;
import shape.ShapeConventions.DimName;

/**
 * \brief Collection of instanciable {@code Shape} classes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public final class ShapeLibrary
{
	
	/*************************************************************************
	 * DUMMY SHAPE (for chemostats, etc)
	 ************************************************************************/
	
	/**
	 * \brief A zero-dimensional shape, which only has a volume.
	 * 
	 * <p>Used by {@code Compartment}s without spatial structure, e.g. a
	 * chemostat.</p>
	 */
	public static class Dimensionless extends Shape
	{
		protected double _volume = 0.0;
		
		public Dimensionless()
		{
			super();
		}
		
		@Override
		public void init(Element xmlElem)
		{
			// TODO read in as a Double
			String str = XmlHandler.attributeFromUniqueNode(
										xmlElem, "volume", ObjectRef.STR);
			this._volume = Double.parseDouble(str);
		}
		
		/**
		 * \brief Set this dimensionless shape's volume.
		 * 
		 * @param volume New volume to use.
		 */
		public void setVolume(double volume)
		{
			this._volume = volume;
		}
		
		@Override
		public GridGetter gridGetter()
		{
			Log.out(Tier.DEBUG, "Dimensionless shape volume is "+this._volume);
			return DummyGrid.dimensionlessGetter(this._volume);
		}
		
		@Override
		public double[] getLocalPosition(double[] location)
		{
			return location;
		}
		
		@Override
		public double[] getGlobalLocation(double[] local)
		{
			return local;
		}
		
		@Override
		protected ResCalc getResolutionCalculator(int[] coord, int axis)
		{
			return null;
		}
		
		@Override
		public void setDimensionResolution(DimName dName, ResCalc resC)
		{
			/* Do nothing! */
		}
		
		public void setSurfaces()
		{
			/* Do nothing! */
		}
		
		@Override
		public double getVoxelVolume(int[] coord)
		{
			return this._volume;
		}
		
		protected void nVoxelTo(int[] destination, int[] coords)
		{
			/* Dimensionless shapes have no voxels. */
			Vector.reset(destination);
		}
		
		@Override
		protected void resetNbhIter()
		{
			/* Do nothing! */
		}
		
		@Override
		public int[] nbhIteratorNext()
		{
			return null;
		}
		
		public boolean isReadyForLaunch()
		{
			if ( ! super.isReadyForLaunch() )
				return false;
			if ( this._volume <= 0.0 )
			{
				Log.out(Tier.CRITICAL,
							"Dimensionless shape must have positive volume!");
				return false;
			}
			return true;
		}
	}
	
	/*************************************************************************
	 * SHAPES WITH STRAIGHT EDGES
	 ************************************************************************/
	
	/**
	 * \brief One-dimensional, straight {@code Shape} class.
	 */
	public static class Line extends CartesianShape
	{
		public Line()
		{
			super();
			this.setSignificant(DimName.X);
		}
	}
	
	/**
	 * \brief Two-dimensional, straight {@code Shape} class.
	 */
	public static class Rectangle extends CartesianShape
	{
		public Rectangle()
		{
			super();
			this.setSignificant(DimName.X);
			this.setSignificant(DimName.Y);
		}
	}
	
	/**
	 * \brief Three-dimensional, straight {@code Shape} class.
	 */
	public static class Cuboid extends CartesianShape
	{
		public Cuboid()
		{
			super();
			this.setSignificant(DimName.X);
			this.setSignificant(DimName.Y);
			this.setSignificant(DimName.Z);
		}
	}
	
	/*************************************************************************
	 * CYLINDRICAL SHAPES
	 ************************************************************************/
	
	/**
	 * \brief Two-dimensional, round {@code Shape} class with an assumed linear
	 * thickness.
	 */
	public static class Circle extends CylindricalShape
	{
		public Circle()
		{
			super();
			this.setSignificant(DimName.THETA);
		}
	}
	
	/**
	 * \brief Three-dimensional, round {@code Shape} class with a linear third
	 * dimension.
	 */
	public static class Cylinder extends Circle
	{
		public Cylinder()
		{
			super();
			this.setSignificant(DimName.THETA);
			this.setSignificant(DimName.Z);
		}
		
		
		public void setSurfaces()
		{
			/* Do the R and THETA dimensions. */
			super.setSurfaces();
			/* Now the Z dimension. */
			this.setPlanarSurfaces(DimName.Z);
		}
	}
	
	/*************************************************************************
	 * SPHERICAL SHAPES
	 ************************************************************************/
	
	// TODO SphereRadius, SphereSlice?
	
	/**
	 * \brief Three-dimensional, round {@code Shape} class with both second and
	 * third dimensions angular.
	 */
	public static class Sphere extends SphericalShape
	{
		public Sphere()
		{
			super();
			
		}
	}
}
