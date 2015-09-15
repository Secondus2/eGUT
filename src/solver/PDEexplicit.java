/**
 * 
 */
package solver;

import grid.SpatialGrid;

import java.util.HashMap;

import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public class PDEexplicit extends PDEsolver
{
	/**
	 * \brief TODO
	 * 
	 */
	public PDEexplicit()
	{
		
	}
	
	
	
	/**
	 * 
	 * <p>Requires the arrays "diffusivity" and "concentration" to
	 * be pre-filled in each SpatialGrid.</p>
	 * 
	 * <p><b>[Rob 13Aug2015]</b> Time step is at most 10% of dx<sup>2</sup>/D,
	 * as this works well in tests.</p>
	 * 
	 */
	@Override
	public void solve(HashMap<String, SpatialGrid> variables, double tFinal)
	{
		/*
		 * Find the largest time step that suits all variables.
		 */
		double dt = tFinal;
		SpatialGrid var;
		int nIter = 1;
		for ( String varName : this._variableNames )
		{
			var = variables.get(varName);
			dt = Math.min(dt, 0.1 * ExtraMath.sq(var.getResolution()) /
												var.getMin(SpatialGrid.diff));
		}
		if ( dt < tFinal )
		{
			nIter = (int) Math.ceil(tFinal/dt);
			dt = tFinal/nIter;
		}
		/*
		 * 
		 */
		for ( int iter = 0; iter < nIter; iter++ )
			for ( String varName : this._variableNames )
			{
				var = variables.get(varName);
				var.newArray("lop");
				addLOperator(var, "lop");
				var.timesAll("lop", dt, false);
				var.addArrayToArray(SpatialGrid.concn, "lop", false);
			}
	}
}