package idynomics;

import java.util.HashMap;
import java.util.Set;

import boundary.Boundary;
import grid.GridBoundary.GridMethod;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.SpatialGrid.GridGetter;
import shape.ShapeConventions.BoundarySide;
import shape.Shape;
import linearAlgebra.Vector;
import reaction.Reaction;

public class EnvironmentContainer
{
	protected Shape _shape;
	
	protected GridGetter _gridGetter;
	
	protected double[] _defaultTotalLength = Vector.vector(3, 1.0);
	
	protected double _defaultResolution = 1.0;
	
	/**
	 * 
	 */
	protected HashMap<String, SpatialGrid> _solutes = 
										new HashMap<String, SpatialGrid>();
	
	/**
	 * 
	 */
	protected HashMap<String, Reaction> _reactions = 
											new HashMap<String, Reaction>();
	
	protected HashMap<BoundarySide,HashMap<String,GridMethod>> _boundaries =
					   new HashMap<BoundarySide,HashMap<String,GridMethod>>();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public EnvironmentContainer()
	{
		
	}
	
	public void setShape(Shape aShape)
	{
		this._shape = aShape;
		this._gridGetter = this._shape.gridGetter();
	}
	
	public EnvironmentContainer(Shape aShape)
	{
		this._shape = aShape;
		this._gridGetter = this._shape.gridGetter();
	}
	
	public void init()
	{
		SpatialGrid aSG;
		Boundary bndry;
		for ( String soluteName : this._solutes.keySet() )
		{
			aSG = this._solutes.get(soluteName);
			for ( BoundarySide aBS : aSG.getBoundarySides() )
			{
				bndry = this._shape.getBoundary(aBS);
				aSG.addBoundary(aBS, bndry.getGridMethod(soluteName));
			}
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO Rob [8Oct2015]: this probably needs more work, just wanted
	 * something to get me rolling elsewhere. 
	 * 
	 * @param compartmentSize
	 * @param defaultRes
	 */
	public void setSize(double[] compartmentSize, double defaultRes)
	{
		this._defaultResolution = defaultRes;
		this._defaultTotalLength = compartmentSize;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName)
	{
		this.addSolute(soluteName, 0.0);
	}
	
	public void addSolute(String soluteName, double initialConcn)
	{
		/*
		 * TODO safety: check if solute already in hashmap
		 */
		SpatialGrid sg = this._gridGetter.newGrid(this._defaultTotalLength,
													this._defaultResolution);
		sg.newArray(ArrayType.CONCN, initialConcn);
		this._boundaries.forEach( (side, map) ->
							{ sg.addBoundary(side, map.get(soluteName)); });
		this._solutes.put(soluteName, sg);
	}
	
	public void addBoundary(BoundarySide aSide, String soluteName,
														GridMethod aMethod)
	{
		if ( ! this._boundaries.containsKey(aSide) )
			this._boundaries.put(aSide, new HashMap<String,GridMethod>());
		this._boundaries.get(aSide).put(soluteName, aMethod);
		this._solutes.get(soluteName).addBoundary(aSide, aMethod);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public Set<String> getSoluteNames()
	{
		return this._solutes.keySet();
	}
	
	public SpatialGrid getSoluteGrid(String soluteName)
	{
		return this._solutes.get(soluteName);
	}
	
	public HashMap<String, SpatialGrid> getSolutes()
	{
		return this._solutes;
	}
	
	/**
	 * FIXME: this is really a property of the compartment but we otherwise
	 * cannot access this information from the process manager, consider refact.
	 * @return
	 */
	public double[] getEdgeLengths()
	{
		return _shape.getDimensionLengths();
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printSolute(String soluteName)
	{
		System.out.println(soluteName+":");
		System.out.println(this._solutes.get(soluteName).arrayAsText(ArrayType.CONCN));
	}
	
	public void printAllSolutes()
	{
		this._solutes.forEach((s,g) -> {this.printSolute(s);;});
	}
}
