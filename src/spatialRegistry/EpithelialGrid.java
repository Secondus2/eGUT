package spatialRegistry;

import java.util.ArrayList;
import java.util.List;

import agent.Agent;
import surface.BoundingBox;
import utility.ExtraMath;

/**
 * The EpithelialGrid  represents the epithelium and can be used to quickly
 *  search for epithelial agents by position. This class only works on the
 *  assumption that epithelial cells are in a regular, even, unchanging grid.
 *  It is created by the epithelial layer spawner during set-up
 *  TO DO - add mechanism for creating cell grid from existing output
 * @author Tim Foster - trf896
 *
 */

public class EpithelialGrid {
	
	private double[] _bottomCorner;
	
	private double[] _topCorner;
	
	private double[] _normal;
	
	private int _numberOfDimensions;
	
	/**
	 * This is the dimensions which defines what is "above" or "below" an
	 * epithelial cell. It is defined by the OrientedCuboid's apical normal.
	 */
	private int _normalDimension;
	
	private double[] _cellSize;
	
	private int[] _cellGrid;
	
	private int _cellNumber;
	
	private Agent[] _epithelialAgents;
	
	
	public EpithelialGrid() {
		
	}

	public void init ()
	{
		this._epithelialAgents = new Agent[this._cellNumber];
		this._numberOfDimensions = this._normal.length;
		for (int i = 0; i < this._normal.length; i++)
			if (this._normal[i] != 0.0)
				this._normalDimension = i;
	}

	/**
	 * Adds an agent to the list held here (index is important because it is used
	 * to infer the cells position).
	 * @param agent
	 * @param index
	 */
	public void listAgent (Agent agent, int index)
	{
		this._epithelialAgents[index] = agent;
	}
	
	public ArrayList <Agent> search (List <BoundingBox> boundingBoxes)
	{
		ArrayList <Agent> entryList = new ArrayList <Agent>();
		for(BoundingBox b : boundingBoxes)
			entryList.addAll(search(b));
		return entryList;
	}
	
	public ArrayList <Agent> search (BoundingBox boundingBox)
	{
		ArrayList <Agent> agentList = new ArrayList <Agent>();
		double[] lower = boundingBox.getLow();
		double[] upper = boundingBox.getHigh();
		
		if (lower[_normalDimension] > _topCorner[_normalDimension] ||
				upper[_normalDimension] < _bottomCorner[_normalDimension])
			//The bounding box is not directly above the epithelial agent.
			return agentList;
		
		else
		{
			
			int[] lowerCellCoords = cellCoordsFromPosition(lower);
			int[] upperCellCoords = cellCoordsFromPosition(upper);
			
			for (int i = 0; i < lowerCellCoords.length; i++)
			{
				if (upperCellCoords[i] < lowerCellCoords[i])
				{
					int higher = lowerCellCoords[i];
					lowerCellCoords[i] = upperCellCoords[i];
					upperCellCoords[i] = higher;
				}
			}
			
			/**
			 * The coordinates of all the cells (real or not) with which the
			 * bounding box would overlap
			 */
			ArrayList <int[]> allCellCoords = new ArrayList <int[]> ();
			
			/**
			 * A grid whose coordinates describe the size of allCellCoords
			 * in each dimension.
			 */
			int[] cellGridSection = new int[_numberOfDimensions];
			for (int i = 0; i < _numberOfDimensions; i++)
			{
				cellGridSection[i] = 
						upperCellCoords[i] - lowerCellCoords[i] + 1;
			}
			
			
			int[] variableCoords = new int[_numberOfDimensions];
			
			variableCoords = lowerCellCoords.clone();
			
			int[] countArray = new int[_numberOfDimensions];
			
			for (int i = 0; i < _numberOfDimensions; i++)
				countArray[i] = 0;
			
			/**
			 * This checks whether all coordinate sets have been added
			 */
			while (countArray[countArray.length - 1] != 
					cellGridSection[cellGridSection.length - 1])
			{
				int shifter = 0;
				
				allCellCoords.add(variableCoords.clone());
				
				countArray[shifter]++;
				variableCoords[shifter]++;
				
				/**
				 * This checks whether the end of a row/column in the
				 * cellGridSection has been reached
				 */
				while (countArray[shifter] == cellGridSection[shifter] &&
						shifter < _numberOfDimensions - 1)
				{
					variableCoords[shifter] = lowerCellCoords[shifter];
					
					countArray[shifter] = 0;
					
					shifter++;
					
					variableCoords[shifter]++;
					
					countArray[shifter]++;
				}
				
			}
			
			
			for (int i = 0; i < allCellCoords.size() ; i++)
			{
				int[] currentCell = allCellCoords.get(i);
				
				int index;
				
				if (this._numberOfDimensions == 3)
				{
					index = ExtraMath.linearIndexFromCoordinates(
						currentCell[0], currentCell[1], currentCell[2],
						_cellGrid[0], _cellGrid[1]);
				}
				else //Assume 2 dimensions
				{
					index = ExtraMath.linearIndexFromCoordinates(
						currentCell[0], currentCell[1], _cellGrid[0]);
				}
				
				if (-1 < index && index < this._cellNumber)
				{
					agentList.add(_epithelialAgents[index]);
				}
			}
		}
			
		return agentList;
	}
	
	
	/**
	 * This method returns zero-based coordinates, such that the lowermost
	 * coordinate in the system would be (0,0,0)
	 * @param position
	 * @return
	 */
	private int[] cellCoordsFromPosition (double[] position)
	{
		int[] intCoords = new int[position.length];
		for (int i = 0; i < _cellSize.length; i++)
		{
			intCoords[i] = (int) Math.floor(
					(position[i] - _bottomCorner[i])/_cellSize[i]);
		}
		return intCoords;
	}
	
	
	/**
	 * Setters
	 */
	
	public void setBottomCorner(double[] bottomCorner) {
		this._bottomCorner = bottomCorner;
	}

	public void setTopCorner(double[] topCorner) {
		this._topCorner = topCorner;
	}

	public void setNormal(double[] normal) {
		this._normal = normal;
	}

	public void setCellSize(double[] cellSize) {
		this._cellSize = cellSize;
	}

	public void setCellGrid(int[] cellGrid) {
		this._cellGrid = cellGrid;
	}

	public void setCellNumber(int cellNumber) {
		this._cellNumber = cellNumber;
	}
	
	
	
}