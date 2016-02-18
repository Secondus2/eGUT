package solver;

import java.util.HashMap;

import grid.GridBoundary.GridMethod;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public abstract class PDEsolver extends Solver
{
	/**
	 * TODO
	 */
	protected PDEupdater _updater;
	
	/**
	 * \brief TODO
	 * 
	 * @param updater
	 */
	public void setUpdater(PDEupdater updater)
	{
		this._updater = updater;
	}
	
	/*************************************************************************
	 * SOLVER METHODS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @param tFinal
	 */
	public abstract void solve(HashMap<String, SpatialGrid> solutes,
															double tFinal);
	
	/**
	 * \brief Add the Laplacian Operator to the LOPERATOR array of the given
	 * grid.
	 * 
	 * <p>The Laplacian Operator is the divergence of the gradient of the
	 * concentration, and is commonly denoted by ∆ (capital delta) or
	 * nabla<sup>2</sup>.</p>
	 * 
	 * <p>Requires the arrays "domain", "diffusivity" and "concentration" to
	 * be pre-filled in <b>solute</b>.</p>
	 * 
	 * @param varName
	 * @param grid
	 * @param destType
	 */
	protected void addLOperator(String varName, SpatialGrid grid)
	{
		/*
		 * Coordinates of the current position. 
		 */
		int[] current;
		/*
		 * Temporary storage for the Laplace operator.
		 */
		double lop;
		/*
		 * Iterate over all core voxels calculating the Laplace operator. 
		 */
		for ( current = grid.resetIterator(); grid.isIteratorValid();
											  current = grid.iteratorNext())
		{
			if ( grid.getValueAt(ArrayType.DOMAIN, current) == 0.0 )
				continue;
			lop = 0.0;
			for ( grid.resetNbhIterator(); 
						grid.isNbhIteratorValid(); grid.nbhIteratorNext() )
			{
				lop += grid.getFluxWithNeighbor(varName);
			}
			/*
			 * Add on any reactions.
			 */
			lop += grid.getValueAt(ArrayType.PRODUCTIONRATE, current);
			/*
			 * Finally, apply this to the relevant array.
			 */
			//System.out.println(Arrays.toString(current)+": val = "+grid.getValueAtCurrent(ArrayType.CONCN)+", lop = "+lop); //bughunt
			grid.addValueAt(ArrayType.LOPERATOR, current, lop);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>Requires the arrays "domain", "diffusivity" and "diffReac" to be
	 * pre-filled in <b>solute</b>.</p>
	 * 
	 * @param solute 
	 * @param arrayName
	 */
	protected void divideByDiffLOperator(String sName, SpatialGrid solute, ArrayType arrayType)
	{
		/*
		 * Coordinates of the current position and of the current neighbor. 
		 */
		int[] current, nbh;
		/*
		 * The GridMethod to use if the current neighbor crosses a boundary.
		 */
		GridMethod gMethod;
		/*
		 * Solute diffusivity at the current grid coordinates and at the
		 * current neighbor coordinates.
		 */
		double currDiff, nbhDiff;
		/*
		 * Temporary storage for the derivative of the L-Operator.
		 */
		double dLop;
		/*
		 * Iterate over all core voxels calculating the derivative of the 
		 * L-Operator. 
		 */
		for ( current = solute.resetIterator(); solute.isIteratorValid();
											  current = solute.iteratorNext())
		{
			if ( solute.getValueAt(ArrayType.DOMAIN, current) == 0.0 )
				continue;
			dLop = 0.0;
			currDiff = solute.getValueAt(ArrayType.DIFFUSIVITY, current);
			for ( nbh = solute.resetNbhIterator(); 
					solute.isNbhIteratorValid(); nbh = solute.nbhIteratorNext() )
			{
				gMethod = solute.nbhIteratorIsOutside();
				if ( gMethod == null )
				{
					nbhDiff = solute.getValueAt(ArrayType.DIFFUSIVITY, nbh);
					dLop += 0.5*(nbhDiff + currDiff);
					// TODO
				}
				else
					dLop += gMethod.getBoundaryFlux(solute);
			}
			dLop += solute.getValueAt(ArrayType.DIFFPRODUCTIONRATE, current);
			solute.timesValueAt(arrayType, current, 1.0/dLop);
		}
	}	
}