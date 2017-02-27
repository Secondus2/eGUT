/**
 * 
 */
package shape.resolution;

import dataIO.Log;
import dataIO.Log.Tier;
import generalInterfaces.Copyable;
import shape.Dimension;

/**
 * \brief Abstract class for calculating grid resolutions.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ResolutionCalculator implements Copyable
{
	/**
	 * 
	 */
	protected Dimension _dimension;
	/**
	 * Total number of voxels along this dimension.
	 */
	protected int _nVoxel;
	/**
	 * The resolution for every voxel.
	 */
	protected double _resolution;
	/**
	 * Target resolution for every voxel.
	 */
	protected double _targetRes;

	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/

	/**
	 * Basic constructor for a ResolutionCalculator. The parent dimension will
	 * be set during {@link #setDimension(Dimension)}.
	 */
	public ResolutionCalculator()
	{ }
	
	/**
	 * Constructor where the parent Dimension is set directly. Used only in
	 * tests.
	 * 
	 * @param dimension Parent Dimension.
	 */
	public ResolutionCalculator(Dimension dimension)
	{
		this.setDimension(dimension);
	}
	
	public void setDimension(Dimension dimension)
	{
		this._dimension = dimension;
	}
	
	protected abstract void init(double resolution, double min, double max);
	
	/* ***********************************************************************
	 * BASIC GETTERS & SETTERS
	 * **********************************************************************/
	
	public int getNVoxel()
	{
		return this._nVoxel;
	}
	
	public double getTotalLength()
	{
		return this._dimension.getLength();
	}
	
	public void setResolution(double targetResolution)
	{
		this.init(targetResolution,
				this._dimension.getExtreme(0), this._dimension.getExtreme(1));
	}
	
	public double getResolution()
	{
		return this._resolution;
	}
	
	/**
	 * \brief Calculates the sum of all resolutions until 
	 * and including the resolution at voxelIndex.
	 * 
	 * @param voxelIndex Integer index of a voxel.
	 * @return Position of the minimum extreme of this voxel.
	 * @throws IllegalArgumentException if voxel is >= nVoxel.
	 */
	public double getCumulativeResolution(int voxelIndex)
	{
		if ( voxelIndex >= this._nVoxel )
			throw new IllegalArgumentException("Voxel index out of range");
		double min = this._dimension.getExtreme(0);
		if ( voxelIndex < 0 )
			return min;
		return min + this._resolution * (voxelIndex + 1);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param voxelIndex
	 * @param inside
	 * @return
	 */
	public double getPosition(int voxelIndex, double inside)
	{
		// TODO safety
		double out = this.getCumulativeResolution(voxelIndex - 1);
		out += this._resolution * inside;
		return out;
	}
	
	/**
	 * \brief Calculates which voxel the given location lies inside.
	 * 
	 * @param location Continuous location along this axis.
	 * @return Index of the voxel this location is inside.
	 * @throws IllegalArgumentException if location is outside [0, length)
	 */
	public int getVoxelIndex(double location)
	{
		if ( location < this._dimension.getExtreme(0) ||
				location >= this._dimension.getExtreme(1) )
		{
			throw new IllegalArgumentException("Location out of range");
		}
		return (int) ((location - this._dimension.getExtreme(0))
						/ this._resolution);
	}
	
	public Object copy()
	{
		ResolutionCalculator out = null;
		try
		{
			out = this.getClass().newInstance();
			out._nVoxel = this._nVoxel;
			out._dimension = this._dimension;
			out._resolution = this._resolution;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return out;
	}
	
	/*************************************************************************
	 * USEFUL STATIC METHODS
	 ************************************************************************/
	
	protected static double resDiff(double trialRes, double targetRes)
	{
		return Math.abs(trialRes - targetRes)/targetRes;
	}

	protected static boolean isAltResBetter(double res, double altRes,
			double targetRes)
	{
		return resDiff(altRes, targetRes) < resDiff(res, targetRes);
	}
}
