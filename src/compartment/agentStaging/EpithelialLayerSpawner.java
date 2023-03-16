package compartment.agentStaging;

import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import compartment.Compartment;
import compartment.Epithelium;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Settable;
import surface.BoundingBox;
import surface.Plane;
import surface.Point;
import utility.ExtraMath;
import shape.CartesianShape;
import shape.Dimension;
import shape.Dimension.DimName;
import spatialRegistry.EpithelialGrid;
import shape.Shape;
import shape.ShapeLibrary.Cuboid;
import shape.ShapeLibrary.Rectangle;

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
	
	private Dimension _normalDimension;
	
	private LinkedList <Dimension> _nonNormalDimensions
		= new LinkedList <Dimension>();
	
	private double[] _bottomCorner;
	
	private double[] _topCorner;
	
	private double[] _apicalCorner;
	
	protected double[] _normal;
	
	protected double[] _cellSideLengths;
	
	protected Shape _layerShape;
	
	protected Shape _cellShape;
	
	protected Plane _apicalSurface;
	
	private int[] _cellArray;
	
	private Epithelium _epithelium;
	
	@Override
	public void instantiate(Element xmlElem, Settable parent)
	{
		Compartment comp = ( (Epithelium) parent).getCompartment();
		
		this._epithelium = (Epithelium) parent;
		
		this.init(xmlElem, comp.agents, comp.getName());
	}

	public void init(
		
		Element xmlElem, AgentContainer agents, String compartmentName) {
		
		super.init(xmlElem, agents, compartmentName);
		
		if (this._compartment.getShape().getNumberOfDimensions() == 2)
		{
			this._layerShape = new Rectangle();
			this._cellShape = new Rectangle();
		}
		
		else
		{
			this._layerShape = new Cuboid();
			this._cellShape = new Cuboid();
		}
		
		double[][] layerCorners = this.calculateLayerCorners();
		
		double[] layerSideLengths = this.getSideLengths(layerCorners);
		
		this._layerShape.setDimensionLengths(layerSideLengths);
		
		this._bottomCorner = layerCorners[0];
		
		this._topCorner = layerCorners[1];
		
		if ( XmlHandler.hasAttribute(xmlElem, XmlRef.cellShape) )
		{
			this._cellSideLengths = Vector.dblFromString(
					xmlElem.getAttribute(XmlRef.cellShape));
		}
		
		this._cellShape.setDimensionLengths(this._cellSideLengths);
		
		this.checkDimensions();
		
		this.calculateCellNumbers();
		
		EpithelialGrid grid = this._epithelium.getGrid();
		
		grid.setBottomCorner(_bottomCorner);
		grid.setTopCorner(_topCorner);
		grid.setNormal(_normal);
		grid.setCellNumber(_numberOfAgents);
		grid.setCellGrid(_cellArray);
		grid.setCellShape(this._cellShape);
		grid.init();
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
		if (this._layerShape.getNumberOfDimensions() == 2)
			spawn(false);
		else if (this._layerShape.getNumberOfDimensions() == 3)
			spawn(true);
	}
	
	public void spawn (boolean thirdDimension) {
		double[] bottomCorner = 
			new double[this._layerShape.getNumberOfDimensions()];
		int[] cellCoordinates;
		int xWidth = this._cellArray[0];
		int yHeight = this._cellArray[1];
		for (int i = 0; i < this._numberOfAgents; i++) {
			if (thirdDimension) 
			{
				cellCoordinates = ExtraMath.
					CoordinatesFromLinearIndex(i, xWidth, yHeight);
			}
			else 
			{
				cellCoordinates = ExtraMath.
					CoordinatesFromLinearIndex(i, xWidth);
			}
			for (int j = 0; j < 
					this._cellShape.getNumberOfDimensions(); j++) 
			{
						bottomCorner[j] = 
						this._bottomCorner[j] + 
						((double) cellCoordinates[j] * this._cellSideLengths[j]);
			}
			Point[] position = positionNewCell(bottomCorner);
			spawnEpithelialAgent (position, i);
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
		double[] lower = spawnDomain.getLow();
		double[] higher = spawnDomain.getHigh();
		double[][] corners = {lower, higher};
		return corners;
	}
	
	public double[] getSideLengths(double[][] corners) {
		int numberDimensions = 
				this._compartment.getShape().getNumberOfDimensions();
		double[] sideLengths = new double[numberDimensions];
		for (int i = 0; i < numberDimensions; i++) {
			sideLengths[i] = corners[1][i] - corners[0][i];
		}
		return sideLengths;
	}
	
	
	public void checkDimensions() {
		
		//Find dimension the epithelium sits in (normal dimension)
		
		//Ensure epithelium is at the extreme of this dimension
		
		//Ensure the other dimension lengths are divisible by the cell size
		
		LinkedList<Integer> nonSpannedDimensions =
				new LinkedList<Integer>();
		
		LinkedList<Integer> spannedDimensions =
				new LinkedList<Integer>();
		
		LinkedList<Integer> zeroDimensions =
				new LinkedList<Integer>();
		
		int numberOfDimensions = this._layerShape.getNumberOfDimensions();
		
		this._normal = new double[numberOfDimensions];
		
		double[] layerLengths = this._layerShape.getDimensionLengths();
		
		double[] compartmentLengths= this._compartment.
				getShape().getDimensionLengths();
		
		for (int i = 0; i < numberOfDimensions; i++)
		{
			Dimension dim = this._compartment.getShape().getDimension(
					this._compartment.getShape().getDimensionName(i));
			if (layerLengths[i] == compartmentLengths[i])
			{
				this._normal[i] = 0;
				spannedDimensions.add(i);
				this._nonNormalDimensions.add(
						this._layerShape.getDimension(
						this._layerShape.getDimensionName(i)));
			}
			
			else
			{
				nonSpannedDimensions.add(i);
				
				this._normalDimension =
						this._layerShape.getDimension(
						this._layerShape.getDimensionName(i));
				
				double dimBottom = dim.getExtreme(0);
				double dimTop = dim.getExtreme(1);
				
				if (this._bottomCorner[i] == dimBottom)
					this._normal[i] = 1;
				else if (this._topCorner[i] == dimTop)
					this._normal[i] = -1;
				else
				{
					if( Log.shouldWrite(Tier.CRITICAL))
						Log.out(Tier.CRITICAL, "Epithelial layer must lie at "
								+ "the extreme of a dimension.");
					Idynomics.simulator.interupt("Epithelial layer must lie "
						+ "at the extreme of a dimension");
				}
			}
		}
		
		if (nonSpannedDimensions.size() > 1)
		{
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: There is more than one "
						+ "dimension in which the epithelial layer does not "
						+ "span the compartment. Therefore its orientation "
						+ "cannot be calculated.");
			Idynomics.simulator.interupt("Interrupted as epithelial layer does "
					+ "not span the compartment");
		}
		
		else if (nonSpannedDimensions.size() == 1)
		{
			int dimensionIndex = nonSpannedDimensions.get(0);
			if (layerLengths[dimensionIndex] == 0)
			{
				zeroDimensions.add(dimensionIndex);
			}
			else
			{
				if( Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, "Warning: epithelial layer in "
							+ this.getCompartment().getName() + " has thickness"
							+ " in its normal dimension. It should be a flat "
							+ "surface.");
				Idynomics.simulator.interupt("Interrupted due to epithelial"
						+ " layer thickness in normal dimension");
			}
				
		}
		
		/*
		 * All dimensions are spanned and therefore the epithelium
		 * is as big as the compartment
		 */
		else 
		{
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: epithelial layer in "
						+ this.getCompartment().getName() + " has the "
						+ "same dimensions as the compartment. It "
						+ "should be a flat surface");
			Idynomics.simulator.interupt("Interrupted due to epithelial"
					+ " layer having same dimensions as compartment.");
		}
		
		if (numberOfDimensions != 
				this.getCompartment().getShape().getNumberOfDimensions()) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Compartment "
						+ this.getCompartment().getName() + " and epithelial "
						+ "layer have different numbers of dimensions");
			Idynomics.simulator.interupt("Interrupted due to dimension "
					+ "mismatch between compartment and epithelial layer.");
		}
		
		
		if (numberOfDimensions != 
				this._cellShape.getNumberOfDimensions()) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Epithelial layer and "
						+ "epithelial cells have different numbers of"
						+ " dimensions.");
			Idynomics.simulator.interupt("Interrupted due to dimension "
					+ "mismatch between epithelial cells and epithelial "
					+ "layer.");
		}
		
		for (Dimension dim : this._nonNormalDimensions) {
			if (this._layerShape.getDimensionLength(dim) %
					this._cellShape.getDimensionLength(dim) != 0.0) {
				if( Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, "Warning: Epithelial layer side"
							+ " length not divisible by cell side length in"
							+ " dimension " + dim.getName());
				Idynomics.simulator.interupt("Interrupted to prevent bad cell "
						+ "fitting");
			}
		}
		
		//Check normal
		for (int i = 0; i < numberOfDimensions ;i++)
		{
			
		}
		
	}
	
	
	/**
	 * Calculate the number of cells in the epithelial layer.
	 */
	public void calculateCellNumbers () {
		this._cellArray = new int[this._layerShape.getNumberOfDimensions()];
		int agentNumber = 1;
		int dimIndex;
		int cellNumber;
		for (Dimension d : this._nonNormalDimensions) {
			dimIndex = this._layerShape.getDimensionIndex(d);
			cellNumber = (int) (this._layerShape.getDimensionLength(d) / 
							this._cellShape.getDimensionLength(d));
			this._cellArray[dimIndex] = cellNumber;
			agentNumber *= cellNumber;
		}
		dimIndex = this._layerShape.
				getDimensionIndex(this._normalDimension);
		this._cellArray[dimIndex] = 1;
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
		newEpithelialCell.registerBirth(this._epithelium, index);
	}
	
	
}
