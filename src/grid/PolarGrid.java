package grid;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.DoubleFunction;

import dataIO.LogFile;
import grid.GridBoundary.GridMethod;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.BoundarySide;

/**
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
 * 
 * abstract super class of all polar grids (Cylindrical and Spherical)
 *
 */
public abstract class PolarGrid extends SpatialGrid {
	// current index of iterator and neighborhood iterator
	protected int _nbhIdx, _subNbhIdx;   
	// array, pre-defining neighbors of a grid cell relative to the cell
	protected int[][] _nbhs;
	// _nVoxel[1] in radian -> length in theta, currently only multiples of Pi/2 
	protected double _nt_rad;
	// inner resolutions (number of quadrants) for polar dimensions 
	protected double[] _ires;
	// Set to store (maybe multiple) neighbors for the current neighbor direction
	protected ArrayList<int[]> _subNbhSet;	
	
	/**
	 * Shared constructor commands. Initializes all members and resets iterators.
	 * 
	 * @param nVoxel - length in each dimension
	 * @param resolution - Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	private void init(int[] nVoxel, double[][] resolution){
		// theta periodic in 1..360
		nVoxel[1] = nVoxel[1]%361; 
		// [r theta z], r=0 || theta=0 -> no grid, z=0 -> polar grid
		_nVoxel = Vector.copy(nVoxel);  
		// scales r but not ires 
		_res = resolution;				
		 // length in t in radian
		_nt_rad = nVoxel[1]*Math.PI/180;
		_ires=new double[3];
		// determine inner resolution in theta automatically for all polarGrids
		_ires[1] = PolarArray.ires(nVoxel[0], _nt_rad, _res[1][0]);  
		 // neighbours (r,t,p)
		_nbhs=new int[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}};
		_subNbhSet = new ArrayList<int[]>();
	}

	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution - Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	PolarGrid(int[] nVoxel, double[][] resolution){
		init(nVoxel,resolution);
	}
	
	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	PolarGrid(int[] nVoxel, double[] resolution){
		// convert resolution to double[][]
		double [][] res = new double[3][0];
		for (int i=0; i<res.length; ++i){
			// TODO: other Polar Grids (if there are any in the future) need 
			// implement own conversions here, which is not so handy maybe.
			
			// convert dependent resolutions
			if (this instanceof CylindricalGrid)
				// only one dependent variable t for cyl. grid (const. res)
				res[i] = i==1 ? new double[1] : new double[nVoxel[i]];
			else if (this instanceof SphericalGrid)
				// two dependent variables t and p for spher. grid (const. res)
				res[i] = i==1 || i==2 ? new double[1] : new double[nVoxel[i]];
			else
				try {
					throw new IllegalClassFormatException(
							"Only spherical and cylindrical Grid is allowed here");
				} catch (IllegalClassFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			// write to new array
			for (int j=0; j<res[i].length; ++j){
				res[i][j]=resolution[i];
			}
		}
		init(nVoxel,res);
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getNumVoxels()
	 */
	@Override
	public int[] getNumVoxels() {
		return Vector.copy(this._nVoxel);
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getSignificantAxes()
	 */
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		for ( int axis = 0; axis < 3; axis++ )
			// since periodicity is handled in constructor 
			// this should work for all PolarGrids
			out[axis] = ( this._nVoxel[axis] > 1 );  
		return out;
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#numSignificantAxes()
	 */
	public int numSignificantAxes()
	{
		int out = 0;
		for ( int axis = 0; axis < 3; axis++ )
			out += ( this._nVoxel[axis] > 1 ) ? 1 : 0;
		return out;
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getValueAt(grid.SpatialGrid.ArrayType, int[])
	 */
	public double getValueAt(ArrayType type, int[] coord)
	{
		if ( this._array.containsKey(type) )
			return this._array.get(type)[coord[0]][coord[1]][coord[2]];
		else
			return Double.NaN;
	}

	/**
	 * \brief Change the value of one coordinate on the given array type.
	 * 
	 * @param type Type of array to be set.
	 * @param coord Coordinate on this array to set.
	 * @param newValue New value with which to overwrite the array.
	 */
	public void setValueAtNew(ArrayType type, int[] coord, double newValue)
	{
		if ( ! this._array.containsKey(type) )
		{
			LogFile.writeLog("Warning: tried to set coordinate in "+
							type.toString()+" array before initialisation.");
			this.newArray(type);
		}
		this._array.get(type)[coord[0]][coord[1]][coord[2]] = newValue;
	}

	/**
	 * \brief Applies the given function to the array element at the given
	 * <b>voxel</b> coordinates (assumed adjusted for padding). 
	 * 
	 * @param name String name of the array.
	 * @param aC Internal array coordinates of the voxel required. 
	 * @param f DoubleFunction to apply to the array element at <b>voxel</b>.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double applyToVoxel(ArrayType type, int[] aC,
			DoubleFunction<Double> f)
	{
		try
		{
			double[][][] array = this._array.get(type);
			return array[aC[0]][aC[1]][aC[2]] = 
					f.apply(array[aC[0]][aC[1]][aC[2]]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ArrayIndexOutOfBoundsException(
					"Voxel coordinates must be inside array: "
							+aC[0]+", "+aC[1]+", "+aC[2]);
		}
	}

	/**
	 * \brief TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param f
	 */
	protected double applyToCoord(ArrayType type, int[] gridCoords,
			DoubleFunction<Double> f)
	{
		return this.applyToVoxel(type, gridCoords, f);
	}

	/**
	 * TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param value
	 */
	public void setValueAt(ArrayType type, int[] gridCoords, double value)
	{
		this.applyToVoxel(type, gridCoords, (double v)->{return value;});
	}

	/**
	 * TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param value
	 */
	public void addValueAt(ArrayType type, int[] gridCoords, double value)
	{
		this.applyToVoxel(type, gridCoords, (double v)->{return v + value;});
	}

	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param gridCoords
	 * @param value
	 */
	public void timesValueAt(ArrayType type, int[] gridCoords, double value)
	{
		this.applyToVoxel(type, gridCoords, (double v)->{return v * value;});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#setAllTo(grid.SpatialGrid.ArrayType, double)
	 */
	@Override
	public void setAllTo(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), ()->{return value;});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#addToAll(grid.SpatialGrid.ArrayType, double)
	 */
	@Override
	public void addToAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v+value;});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#timesAll(grid.SpatialGrid.ArrayType, double)
	 */
	@Override
	public void timesAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v*value;});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getMax(grid.SpatialGrid.ArrayType)
	 */
	@Override
	public double getMax(ArrayType type) {
		final double[] max=new double[]{Double.NEGATIVE_INFINITY};
		PolarArray.applyToAll(
				_array.get(type),(double v)->{max[0]=v>max[0] ? v : max[0];}
		);
		return max[0];
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getMin(grid.SpatialGrid.ArrayType)
	 */
	@Override
	public double getMin(ArrayType type) {
		final double[] min=new double[]{Double.POSITIVE_INFINITY};
		PolarArray.applyToAll(
				_array.get(type),(double v)->{min[0]=v<min[0] ? v : min[0];}
		);
		return min[0];
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#addArrayToArray(grid.SpatialGrid.ArrayType, grid.SpatialGrid.ArrayType)
	 */
	@Override
	public void addArrayToArray(ArrayType destination, ArrayType source) {
		PolarArray.applyToAll(
				_array.get(destination), 
				_array.get(source), 
				(double vd, double vs)->{return vd+vs;}
		);
	}

	/**
	 * \brief Discard the iterative coordinate.
	 */
	public void closeIterator()
	{
		this._currentCoord = null;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getValueAtCurrent(grid.SpatialGrid.ArrayType)
	 */
	public double getValueAtCurrent(ArrayType type)
	{
		return this.getValueAt(type, this._currentCoord);
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#setValueAtCurrent(grid.SpatialGrid.ArrayType, double)
	 */
	public void setValueAtCurrent(ArrayType type, double value)
	{
		this.setValueAt(type, this._currentCoord, value);
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#nbhIteratorIsOutside()
	 */
	public GridMethod nbhIteratorIsOutside()
	{
		BoundarySide bSide = this.isOutside(this._currentNeighbor);
		if ( bSide == null )
			return null;
		GridMethod m = this._boundaries.get(bSide);
		//TODO: throw IllegalAccessError ?
		if (m==null){ 
			System.err.println(
				"trying to access non-existent boundary side "+bSide.toString());
		}
		return m;
	}
	
	/**
	 * updates the current neighbor coordinate
	 * called when the neighborhood iterator was manipulated.
	 */
	public void currentNbhIdxChanged(){
		_subNbhIdx=0;
		_subNbhSet.clear();
		fillNbhSet();
//		if (_subNbhSet.isEmpty())
//			nbhIteratorNext();
		_currentNeighbor = _subNbhSet.get(0);
	}
	
	public abstract void fillNbhSet();
	
	/**
	 * converts (r,t,p) or (r,t,z) coordinates into an index 
	 * (for SphericalGrid and CylindricalGrid, respectively). 
	 * 
	 * @param coord - a (r,t,p) or (r,t,z) coordinate
	 * @return the corresponding index
	 */
	public abstract int coord2idx(int[] coord);
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isIteratorValid()
	 */
	@Override
	public boolean isIteratorValid() {return _currentCoord[0] < _nVoxel[0];}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#resetIterator()
	 */
	public int[] resetIterator()
	{
		if ( this._currentCoord == null )
			this._currentCoord = Vector.zerosInt(3);
		else
			for ( int i = 0; i < 3; i++ )
				this._currentCoord[i] = 0;
		return this._currentCoord;
	}
	
	protected abstract boolean iteratorExceeds(int axis);
	
	/**
	 * TODO
	 * 
	 * @return int[3] coordinates of next position.
	 * @exception IllegalStateException Iterator exceeds boundaries.
	 */
	public int[] iteratorNext()
	{
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
//		System.out.println(Arrays.toString(_currentCoord));
		return _currentCoord;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#resetNbhIterator()
	 */
	public int[] resetNbhIterator(){
		_nbhIdx=0;
		currentNbhIdxChanged();
		_currentNeighbor = transInternal(_currentNeighbor);
		return _currentNeighbor;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isNbhIteratorValid()
	 */
	public boolean isNbhIteratorValid(){
		if (_subNbhIdx >= _subNbhSet.size()){
			return _nbhIdx < _nbhs.length - 1;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#nbhIteratorNext()
	 */
	public int[] nbhIteratorNext(){
		_subNbhIdx++;
		if (_subNbhIdx < _subNbhSet.size()){ // subiterator has next
			_currentNeighbor = _subNbhSet.get(_subNbhIdx);
		}else{
			_nbhIdx++;
			if (_nbhIdx < _nbhs.length) currentNbhIdxChanged();
		}
		_currentNeighbor = transInternal(_currentNeighbor);
		return _currentNeighbor;
	}
	
	protected abstract BoundarySide isOutside(int[] coord, int dim);
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isOutside(int[])
	 */
	@Override
	protected BoundarySide isOutside(int[] coord) {
		for (int dim=0; dim<3; ++dim){
			BoundarySide bs = isOutside(coord,dim);
			if (bs!=null) return bs;
		}
		return null;
	}
	
	/**
	 * Computes isOutside for all 3 dimensions. Does return minimum OR maximum, 
	 * not both. Decision for min or max if the coord is both min and max 
	 * depends on the actual implementation of isOutside in the sub class.
	 * 
	 * @param coord - an array coordinate.
	 * @param out - Array of length 3 to write BoundarySides into, can be null.
	 * @return - BoundarySides at all 3 dimensions 
	 *           (array of 3 nulls if no boundary was hit at all)
	 */
	protected BoundarySide[] getBoundarySides(int[]coord, BoundarySide[] out){
		if (out==null) out = new BoundarySide[3];
		for (int dim=0; dim<3; ++dim)
			out[dim]=isOutside(coord, dim);
		return out;
	}
	
	/**
	 * Performs cyclic transform for inside boundaries.
	 */
	protected int[] transInternal(int[] coord){
		BoundarySide[] bsa = new BoundarySide[3];
		int nc=0, ic=0; // null counter and internal with r>=0 counter
		bsa = getBoundarySides(coord, bsa);
		for (BoundarySide bs : bsa){
			if (bs==null) nc++;
			if (bs==BoundarySide.INTERNAL && coord[0]>=0) ic++;
		}
		if (nc+ic==3) {			
			// only null and internal with r>=0 -> transform
			coord=cyclicTransform(coord);
		}
		return coord;
	}
	
	/**
	 * @return the length of the grid (maximum index)
	 */
	public abstract int length();
	
	/**
	 * Converts a coordinate in the grid's array to a location in simulated 
	 * space. 
	 * 'Subcoordinates' can be transformed using the 'inside' array.
	 * For example type getLocation(coord, new double[]{0.5,0.5,0.5})
	 * to get the center point of the grid cell defined by 'coord'.
	 * 
	 * @param coord - a coordinate in the grid's array.
	 * @param inside - relative position inside the grid cell.
	 * @return - the location in simulation space.
	 */
	public abstract double[] getLocation(int[] coord, double[] inside);
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getCoords(double[])
	 */
	@Override
	public int[] getCoords(double[] loc) {
		return getCoords(loc,null);
	}
	
	/**
	 * Transforms a given location into array-coordinates and 
	 * computes sub-coordinates inside the grid element if inside != null. 
	 * 
	 * @param loc - a location in simpulated space.
	 * @param inside - array to write sub-coordinates into, can be null.
	 * @return - the array coordinates corresponding to location loc.
	 */
	public abstract int[] getCoords(double[] loc, double[] inside);
	
	/**************************************************************************/
	/************************* UTILITY METHODS ********************************/
	/**************************************************************************/
	
	/**
	 * @param x - any double
	 * @return - rounded value with 1e-10 precision
	 */
	protected double round10(double x){return Math.round(x*1e10)*1e-10;}
	
	/**
	 * @param x - any double
	 * @return - rounded value with 1e-100 precision
	 */
	protected double round100(double x){return Math.round(x*1e16)*1e-16;}
	
	/**
	 * Computes a factor that scales the number of elements for increasing 
	 * radius to keep element volume fairly constant.
	 * 
	 * @param r - radius.
	 * @return - a scaling factor for a given radius.
	 */
	protected int s(int r){return 2*r+1;}
	
	/**
	 * computes the number of elements in one triangle for radius r
	 * 
	 * @param r - radius
	 * @return - the number of elements in one triangle for radius r.
	 */
	protected  int sn(int r){
		return (int)(_ires[1]*s(r)*(r+1));
	}
	
	/**
	 * @param r - radius.
	 * @return - the number of rows for given radius.
	 */
	public int np(int r) {
		return (int)_ires[2]*s(r);
	}
	
	/**
	 * Computes the number of elements in row (r,p)
	 * 
	 * @param p - phi coordinate
	 * @param np - number of rows
	 * @return - the number of elements in row p
	 */
	public int nt(int r, int p){
		// number of rows
		int np=np(r);
		// index of row where p>90�
		double ir = np/_ires[2]*_res[2][0];
		// p>=np and p<0 need to be considered for neighbors
		return (int)((((p<ir || p>=np) && p>=0) ? p+1 : np-p)*_ires[1]);
	}
	
	/**
	 * computes the number of elements in a triangle until row p 
	 * 
	 * @param p - phi coordinate (row index)
	 * @return - number of cells in a triangle until row p
	 */
	public int n(int r, int p){
		// number of rows
		int np=np(r);
		// index of row where p>90�
		double ir = np/_ires[2]*_res[2][0];
		return (int)(((p<ir || p>=np) && p>=0) ? 
				1.0/2*_ires[1]*p*(p+1)
				: -1.0/8*_ires[1]*(np-2*p)*(3*np-2*p+2));
//		return (int)(p==ir ? 0 
//			: p<ir ? 1.0/2*_ires[1]*p*(p+1)
//			: -1.0/8*_ires[1]*(np-2*p)*(3*np-2*p+2));
	}
	
	/**
	 * computes the number of elements in the whole matrix until and including 
	 * matrix-slice r
	 * 
	 * @param r - radius
	 * @return - the number of grid cells until and including matrix r
	 */
	public int N(int r){
		return (int)((_ires[1]*_ires[2]*(r+1)*(r+2)*(4*r+3))/6);
	}

	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void rowToBuffer(double[] row, StringBuffer buffer)
	{
		for ( int i = 0; i < row.length - 1; i++ )
			buffer.append(row[i]+", ");
		buffer.append(row[row.length-1]);
	}
	
	public void matrixToBuffer(double[][] matrix, StringBuffer buffer)
	{
		for ( int i = 0; i < matrix.length - 1; i++ )
		{
			if ( matrix[i].length == 1 )
				buffer.append(matrix[i][0]+", ");
			else
			{
				rowToBuffer(matrix[i], buffer);
				buffer.append(";\n");
			}
		}
		rowToBuffer(matrix[matrix.length - 1], buffer);
	}
	
	public StringBuffer arrayAsBuffer(ArrayType type)
	{
		StringBuffer out = new StringBuffer();
		double[][][] array = this._array.get(type);
		for ( int i = 0; i < array.length - 1; i++ )
		{
			matrixToBuffer(array[i], out);
			if ( array[i].length == 1 )
				out.append(", ");
			else
				out.append("\n");
		}
		matrixToBuffer(array[array.length - 1], out);
		return out;
	}
	
	public String arrayAsText(ArrayType type)
	{
		return this.arrayAsBuffer(type).toString();
	}

}
