package linearAlgebra;

import grid.ResolutionCalculator.ResCalc;

/**
 * \brief Utility class for PolarGrids.
 * 
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
 */
public final class PolarArray
{
	/**
	 * \brief Used to create an array to store a CylindricalGrid.
	 * 
	 * @param resCalc TODO
	 * @return An array used to store a CylindricalGrid
	 */
	public static double[][][] createCylinder(ResCalc[][] resCalc)
	{
		return createCylinder(resCalc, 0.0);
	}
	
	/**
	 * \brief Used to create an array to store a CylindricalGrid.
	 * 
	 * @param resCalc TODO
	 * @param val Initial value for all voxels in this array.
	 * @return An array used to store a CylindricalGrid.
	 */
	public static double[][][] createCylinder(ResCalc[][] resCalc, double val)
	{
		int nr, nz;
		nr = resCalc[0][0].getNVoxel();
		nz = resCalc[2][0].getNVoxel();
		double[][][] a = new double[nr][][];
		for ( int i = 0; i < nr; i++ )
			a[i] = Matrix.matrix(resCalc[1][i].getNVoxel(), nz, val);
		return a;
	}
	
	/**
	 * \brief Used to create an array to store a SphericalGrid.
	 * 
	 * @param resCalc TODO
	 * @return An array used to store a SphericalGrid.
	 */
	public static double[][][] createSphere(ResCalc[][][] resCalc)
	{
		return createSphere(resCalc, 0.0);
	}
	
	/**
	 * \brief Used to create an array to store a SphericalGrid.
	 * 
	 * @param resCalc TODO
	 * @param val Initial value for all voxels in this array.
	 * @return An array used to store a SphericalGrid.
	 */
	public static double[][][] createSphere(ResCalc[][][] resCalc, double val)
	{
		int nR = resCalc[0][0][0].getNVoxel();
		int nPhi;
		double[][][] a = new double[nR][][];
		for ( int r = 0; r < nR; r++ )
		{
			nPhi = resCalc[1][r][0].getNVoxel();
			a[r] = new double[nPhi][];
			for ( int p = 0; p < nPhi; p++ )
				a[r][p] = Vector.vector(resCalc[2][r][p].getNVoxel(), val);
		}
		return a;
	}
	
	/**
	 * \brief Computes the inner resolution for phi or theta dimensions.
	 * 
	 * TODO Rob [2Feb2016]: Is this essentially the number of quarter-circles
	 * covered by the given angle?
	 * 
	 * @param n - total size in theta or phi direction (in radian)
	 * @return - the inner resolution
	 */
	public static double ires(double n)
	{
		 // min 1, +1 for each quadrant (=4 for full circle)
//		return Math.max(2*nt/(Math.PI*res),1);
//		System.out.println(2*nt/Math.PI);
		return 2 * n / Math.PI;
	}
	
	/**
	 * @param a - a polar array
	 * @param b - another polar array
	 * @throws IllegalArgumentException if dimensions are not the same
	 */
	public static void checkDimensionsSame(double[][][] a, double[][][] b) 
			throws IllegalArgumentException
	{
		IllegalArgumentException e = 
				new IllegalArgumentException("Array dimensions must agree.");
		if ( a.length != b.length )
			throw e;
		if ( a[0][0].length != b[0][0].length )
			throw e;
		for ( int i = 0; i < a.length; i++ )
			if ( a[i].length != b[i].length )
				throw e;
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO Not currently used
	 * 
	 * @param r
	 * @param theta0
	 * @param theta1
	 * @param phi0
	 * @param phi1
	 * @return
	 */
	public static double area(double r, double theta0, double theta1,
											double phi0, double phi1)
	{
		return r * r * (theta1 - theta0) * (Math.cos(phi0)-Math.cos(phi1));
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO Not currently used
	 * 
	 * @param r
	 * @param theta0
	 * @param theta1
	 * @return
	 */
	public static double arcLength(double r, double theta0, double theta1)
	{
		return r * (theta1 - theta0);
	}
}
