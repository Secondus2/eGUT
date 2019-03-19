package compartment.agentStaging;

import org.w3c.dom.Element;
import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import compartment.AgentContainer;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import surface.BoundingBox;
import surface.Plane;
import surface.Point;



public class EpithelialLayerSpawner extends Spawner {

	private int _numberOfDimensions;
	
	private double[] _cellSideLengths;
	
	private double[] _layerSideLengths;
	
	private double[] _bottomCorner;
	
	private double[] _topCorner;
	
	private Plane _apicalSurface;
	
	private double[][] _layerCorners;

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
		
		this.createApicalSurface();

		this.setNumberOfAgents(this.calculateNumberOfAgents());
	}


	public void spawn() {
		
		double[] bottomCorner = new double[this._numberOfDimensions];
		for (int i = 0; i < this._bottomCorner.length; i++) {
			bottomCorner[i] = this._bottomCorner[i];			
		}
		
		//counter counts number of cells produced
		int counter = 0;

		while (counter < this.getNumberOfAgents()) {
			/*shifter is the dimension in which the bottomCorner value will be
			increased */
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
	public void createApicalSurface()
	{
		//count tracks how many dimensions the epithelium does not fully span
		int count = 0;
		double[] normal = new double[this._numberOfDimensions];
		for (int i = 0; i < this._numberOfDimensions; i++) {
			if (this._layerSideLengths[i] == 
					this.getCompartment().getShape().getDimensionLengths()[i]) {
				normal[i] = 0.0;
			}
			
			else {
				normal[i] = 1.0;
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
		
		this._apicalSurface = new Plane(normal, this._topCorner);
		this.getCompartment().addSurface(this._apicalSurface);
	}
	
	/**
	 * Calculate the number of cells in the epithelial layer.
	 */
	public int calculateNumberOfAgents () {
		int numberOfEpithelialCells = 1;
		for (int i = 0; i < this._numberOfDimensions; i++) {
			numberOfEpithelialCells *= 
					this._layerSideLengths[i] / this._cellSideLengths[i];
		}
		return numberOfEpithelialCells;
	}
	
	/**
	 * Create cuboidal epithelial cell, given co-ordinates for the bottom corner
	 * and calculating the top corner from this._cellSideLengths.
	 * 
	 * @param bottomCorner
	 */
	public void createEpithelialCell(double[] bottomCorner) {
		double[] topCorner = new double[bottomCorner.length];
		for (int j = 0; j < bottomCorner.length; j++) {
			topCorner[j] = bottomCorner[j] + this._cellSideLengths[j];
		}
		
		Point bCPoint = new Point(bottomCorner);
		Point tCPoint = new Point(topCorner);
		Point[] bothPoints = {bCPoint, tCPoint};
		Agent newEpithelialCell = new Agent(this.getTemplate());
		newEpithelialCell.set(AspectRef.agentBody, new Body(
				this.getMorphology(), bothPoints,0,0));
		newEpithelialCell.setCompartment( this.getCompartment() );
		newEpithelialCell.registerBirth();
	}
	
	
}
