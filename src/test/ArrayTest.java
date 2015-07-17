package test;

import array.*;

public class ArrayTest
{

	public static void main(String[] args)
	{
		/*int[][] id = Matrix.identityInt(4, 3);
		for ( int[] row : id )
		{
			for ( int elem : row )
				System.out.print(elem+" ");
			System.out.println("");
		}*/
		/*
		int[] v = Vector.zerosInt(3);
		v[0] = 1;
		v[1] = -3;
		v[2] = 2;
		for ( int elem : v )
			System.out.println(elem);
		Vector.times(v, 2);
		for ( int elem : v )
			System.out.println(elem);
		*/
		
		boolean periodic = true;
		
		int m = 5;
		double[][] a = Matrix.times(Matrix.identityDbl(m), 6.0);
		for ( int i = 0; i < m -1; i++ )
		{
			a[i][i+1] = 2.3;
			a[i+1][i] = 1.8;
		}
		if ( periodic )
		{
			a[0][m-1] = 0.5;
			a[m-1][0] = 0.5;
		}
		double[] vector = Vector.add(Vector.toDbl(Vector.range(m)), 5.0);
		/*
		 * Solve it the brute force way.
		 */
		double[] vsolveold = Matrix.solve(a, vector);
		double[] vcheckold = Matrix.times(a, vsolveold);
		for ( int i = 0; i < m; i++ )
		{
			for ( int j = 0; j < m; j++ )
				System.out.print(a[i][j]+" ");
			System.out.print("   "+vsolveold[i]);
			System.out.print("   "+vector[i]);
			System.out.print("   "+vcheckold[i]);
			System.out.println("");
		}
		System.out.println("");
		
		/*
		 * Solve it the better way.
		 */
		double[][] td;
		if ( periodic )
			td = TriDiagonal.getTriDiagPeriodic(a);
		else
			td = TriDiagonal.getTriDiag(a);
		for ( double[] row : td )
		{
			for ( double elem : row )
				System.out.print(elem+" ");
			System.out.println("");
		}
		System.out.println("");
		double[] vsolvenew = Vector.copy(vector);
		if ( periodic )
			TriDiagonal.solvePeriodic(td, vsolvenew);
		else
			TriDiagonal.solve(td, vsolvenew);
		double[] vchecknew = Matrix.times(a, vsolvenew);
		for ( int i = 0; i < m; i++ )
		{
			for ( int j = 0; j < m; j++ )
				System.out.print(a[i][j]+" ");
			System.out.print("\t"+vsolvenew[i]);
			System.out.print("\t"+vector[i]);
			System.out.print("\t"+vchecknew[i]);
			System.out.println("");
		}
	}

}
