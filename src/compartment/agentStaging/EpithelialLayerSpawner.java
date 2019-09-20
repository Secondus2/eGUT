package compartment.agentStaging;

import org.w3c.dom.Element;
import agent.Agent;
import agent.Body;
import agent.Body.Morphology;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import physicalObject.PhysicalObject;
import compartment.AgentContainer;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import surface.BoundingBox;
import surface.OrientedCuboid;
import surface.Plane;
import surface.Point;
import utility.ExtraMath;
import shape.Dimension;
import shape.Dimension.DimName;
import spatialRegistry.EpithelialGrid;
import shape.Shape;

/**
 * This class produces a layer of epithelial cells as defined in the XML file.
 * The possible size and shape of the epithelium is tightly restricted, as
 * can be seen from the checkDimensions() and orientEpithelialLayer() methods.
 * 
 * 	 * TODO - this will only produce cuboidal cells. Could create more general
	 * class for regularly spaced bacterial cells if needed.
	 * 
 * @author Tim Foster
 */
public class EpithelialLayerSpawner extends Spawner {

	private int _numberOfDimensions;
	
	private double[] _cellSideLengths;
	
	private double[] _layerSideLengths;
	
	private double[] _bottomCorner;
	
	private double[] _topCorner;
	
	private double[] _apicalCorner;
	
	protected double[] _normal;
	
	protected Plane _apicalSurface;
	
	private double[][] _layerCorners;
	
	private int[] _cellArray;
	
	protected EpithelialGrid thisEpithelium;

	public void init(
		
		Element xmlElem, AgentContainer agents, String compartmentName) {
		
		super.init(xmlElem, agents, compartmentName);
		
		this._layerCorners = this.calculateLayerCorners();
		
		this._numberOfDimensions = _layerCorners[0].length;
		
		this._layerSideLengths = this.getSideLengths(_layerCorners);
		
		this._bottomCorner = _layerCorners[0];
		
		this._topCorner = _layerCorners[1];
		
		if ( XmlHandler.hasAttribute(xmlElem, XmlRef.cellShape) )
		{
			this._cellSideLengths =	Vector.dblFromString(
					xmlElem.getAttribute(XmlRef.cellShape));
		}
		
		this.checkDimensions();
		
		this.orientEpithelialLayer();
		
		this.calculateCellNumbers();
		
		this.thisEpithelium = new EpithelialGrid();
		
		thisEpithelium.setBottomCorner(_bottomCorner);
		thisEpithelium.setTopCorner(_topCorner);
		thisEpithelium.setNormal(_normal);
		thisEpithelium.setCellNumber(_numberOfAgents);
		thisEpithelium.setCellGrid(_cellArray);
		thisEpithelium.setCellSize(_cellSideLengths);
		thisEpithelium.init();
		

	}

/** SIMPLER METHOD. THIS MAY BE PREFERABLE.
	public void spawn() {
		
		double[] bottomCorner = new double[this._numberOfDimensions];
		for (int i = 0; i < this._bottomCorner.length; i++) {
			bottomCorner[i] = this._bottomCorner[i];			
		}
		
		//counter counts number of cells produced
		int counter = 0;

		while (counter < this._numberOfAgents) {
			//shifter is the dimension in which the bottomCorner value will be
			//increased.
			int shifter = 0;
			createEpithelialCell(bottomCorner);
			counter++;
			bottomCorner[shifter] += this._cellSideLengths[shifter];
			while (bottomCorner[shifter] == this._topCorner[shifter]
					&& shifter < this._numberOfDimensions - 1) {
					bottomCorner[shifter] = this._bottomCorner[shifter];
					shifter ++;
					bottomCorner[shifter] += this._cellSideLengths[shifter];
			}
		}
	
	}
**/

	public void spawn() {
		if (this._numberOfDimensions == 2)
			spawn(false);
		else if (this._numberOfDimensions == 3)
			spawn(true);
	}
	
	public void spawn (boolean thirdDimension) {
		double[] bottomCorner = new double[this._numberOfDimensions];
		int[] cellCoordinates;
		int xWidth = this._cellArray[0];
		int yHeight = this._cellArray[1];
		for (int i = 0; i < this._numberOfAgents; i++) {
			if (thirdDimension) {
				cellCoordinates = ExtraMath.
					CoordinatesFromLinearIndex(i, xWidth, yHeight);}
			else {
				cellCoordinates = ExtraMath.
					CoordinatesFromLinearIndex(i, xWidth);
			}
			for (int j = 0; j < this._numberOfDimensions; j++) {
				bottomCorner[j] = 
						this._bottomCorner[j] + 
						((double) cellCoordinates[j] * this._cellSideLengths[j]);
			}
			Point[] position = positionNewCell(bottomCorner);
			spawnEpithelialAgent (position, i);
		}
		this._agents.setEpithelium (thisEpithelium);
	}
	
	/**
	 * Returns a 2D array of two rows by x columns, where x is the number of
	 * dimensions. The "top" row (corners[0][i]) contains the min values (lower
	 * corner), while the "bottom" row (corners[1][i]) contains the max values
	 * (higher corner).
	 */
	public double[][] calculateLayerCorners() {
		BoundingBox spawnDomain = this.getSpawnDomain();
		double[] lower = spawnDomain.lowerCorner();
		double[] higher = spawnDomain.higherCorner();
		double[][] corners = {lower, higher};
		return corners;
	}
	
	public double[] getSideLengths(double[][] corners) {
		
		double[] sideLengths = new double[this._numberOfDimensions];
		for (int i = 0; i < this._numberOfDimensions; i++) {
			sideLengths[i] = corners[1][i] - corners[0][i];			
		}
		return sideLengths;
	}
	
	
	public void checkDimensions() {
		
		if (this._numberOfDimensions != 
				this.getCompartment().getShape().getNumberOfDimensions()) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Compartment "
						+ this.getCompartment().getName() + " and epithelial "
						+ "layer have different numbers of dimensions");
			Idynomics.simulator.interupt("Interrupted due to dimension "
					+ "mismatch between compartment and epithelial layer.");
		}
		if (this._numberOfDimensions != this._cellSideLengths.length) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Epithelial layer and "
						+ "epithelial cells have different numbers of"
						+ " dimensions.");
			Idynomics.simulator.interupt("Interrupted due to dimension "
					+ "mismatch between epithelial cells and epithelial "
					+ "layer.");
		}
		int multiCellDimensions = 0;
		for (int i = 0; i < _layerCorners[0].length; i++) {
			if (this._layerSideLengths[i] % this._cellSideLengths[i] != 0.0) {
				if( Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, "Warning: Epithelial layer side"
							+ " length not divisible by cell side length in"
							+ " dimension " + i+1);
				Idynomics.simulator.interupt("Interrupted to prevent bad cell "
						+ "fitting");
			}
			if (this._layerSideLengths[i] / this._cellSideLengths[i] != 1.0) {
				multiCellDimensions ++;
			}
		}
		if (multiCellDimensions == _layerCorners[0].length) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Epithelial layer is more than"
						+ "one cell thick.");
		}
	}
	
	/**
	 * Calculates the orientation of the epithelial layer, interrupting the 
	 * simulation if it cannot be calculated, and adds a plane to the
	 * Compartment, corresponding to the apical surface of the epithelium
	 */
	public void orientEpithelialLayer()
	{
		//count tracks how many dimensions the epithelium does not fully span
		int count = 0;
		double[] normal = new double[this._numberOfDimensions];
		Shape compartmentShape = this.getCompartment().getShape();
		for (int i = 0; i < this._numberOfDimensions; i++) {
			if (this._layerSideLengths[i] == compartmentShape.
					getDimensionLengths()[i]) 
				normal[i] = 0.0;
			
			else {
				DimName dimName = compartmentShape.getDimensionName(i);
				Dimension dim = compartmentShape.getDimension(dimName);
				double dimBottom = dim.getExtreme(0);
				double dimTop = dim.getExtreme(1);
				if (this._bottomCorner[i] == dimBottom)
				{
					//The cells sit along the bottom of the dimension, so the 
					//epithelial layer faces "up" (+1.0)
					normal[i] = 1.0;
					this._apicalCorner = this._topCorner;
				}
				else if (this._topCorner[i] == dimTop)
				{
					//The cells sit along the top of the dimension, so the 
					//epithelial layer faces "down" (-1.0)
					normal[i] = -1.0;
					this._apicalCorner = this._bottomCorner;
				}
				else
				{
					if( Log.shouldWrite(Tier.CRITICAL))
						Log.out(Tier.CRITICAL, "Epithelial layer must lie at "
								+ "the extreme of a dimension.");
					Idynomics.simulator.interupt("Epithelial layer must lie "
							+ "at the extreme of a dimension");
				}
				count++;
			}
		}
		
		if (count != 1) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: There is more than one "
						+ "dimension in which the epithelial layer does not "
						+ "span the compartment. Therefore its orientation "
						+ "cannot be calculated.");
			Idynomics.simulator.interupt("Interrupted as epithelial layer does "
					+ "not span the compartment");
		}
		this._normal = normal;
		//this._apicalSurface = new Plane(normal, this._apicalCorner);
		//PhysicalObject epithelialSurface = new PhysicalObject();
		//epithelialSurface.setParent(this._parentNode);
		//epithelialSurface.setSurface(this._apicalSurface);
		//epithelialSurface.set("species",this.get("species"));
		//this._agents.addPhysicalObject(epithelialSurface);
	}
	
	/**
	 * Calculate the number of cells in the epithelial layer.
	 */
	
	public void calculateCellNumbers () {
		this._cellArray = new int[this._numberOfDimensions];
		int agentNumber = 1;
		for (int i = 0; i < this._numberOfDimensions; i++) {
			this._cellArray[i] = (int) 
					(this._layerSideLengths[i] / this._cellSideLengths[i]);
			agentNumber *= 
					(this._layerSideLengths[i] / this._cellSideLengths[i]);
		}
		this.setNumberOfAgents(agentNumber);
	}
	
	/**
	 * Position epithelial cell, given co-ordinates for the bottom corner
	 * and calculating the top corner from this._cellSideLengths.
	 * 
	 * @param bottomCorner
	 */
	public Point[] positionNewCell(double[] bottomCorner) {
		double[] topCorner = new double[bottomCorner.length];
		for (int j = 0; j < bottomCorner.length; j++) {
			topCorner[j] = bottomCorner[j] + this._cellSideLengths[j];
		}
		
		Point bCPoint = new Point(bottomCorner);
		Point tCPoint = new Point(topCorner);
		Point[] out = {bCPoint, tCPoint};
		return out;
	}
	
	public void spawnEpithelialAgent(Point[] position, int index) {
		Agent newEpithelialCell = new Agent(this.getTemplate());
		newEpithelialCell.set(AspectRef.cuboidOrientation,
					this._normal);
		newEpithelialCell.set(AspectRef.agentBody, new Body(
				position, this._normal));
		newEpithelialCell.setCompartment( this.getCompartment() );
		newEpithelialCell.registerBirth();
		thisEpithelium.listAgent(newEpithelialCell, index);
	}
	
	
}
