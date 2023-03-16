package compartment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import agent.predicate.IsEpithelial;
import boundary.Boundary;
import boundary.SpatialBoundary;
import boundary.spatialLibrary.EpithelialBoundary;
import compartment.agentStaging.Spawner;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import instantiable.Instance;
import instantiable.Instantiable;
import referenceLibrary.AspectRef;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import shape.Dimension;
import shape.Shape;
import shape.Dimension.DimName;
import shape.subvoxel.IntegerArray;
import solver.mgFas.SoluteGrid;
import spatialRegistry.EpithelialGrid;
import surface.BoundingBox;
import surface.Point;
import surface.Surface;
import utility.ExtraMath;
import utility.Helper;

public class Epithelium implements Settable, Instantiable {

	protected LinkedList<Agent> _agentList =
			new LinkedList<Agent>();
	
	private EpithelialGrid _epithelialGrid;
	
	private EpithelialBoundary _associatedBoundary;
	
	private static final String VD_TAG = 
			AspectRef.agentVolumeDistributionMap;
	
	private Compartment _compartment;
	
	//The dimension that the epithelium is at one extreme of
	private Dimension _dimension;
	
	//The extreme that the epithelium is at in the _dimension
	private int _extreme;
	
	private AgentContainer _parentNode;
	
	public void addAgent(Agent agent, int index)
	{
		if (IsEpithelial.isEpithelial(agent))
		{
			this._agentList.add(agent);
		}
		this._epithelialGrid.listAgent(agent, index);
	}
	
	public void instantiate(Element xmlElement, Settable parent) 
	{
		this.setParent(parent);
		
		String string = XmlHandler.obtainAttribute(
				xmlElement, XmlRef.shapeDimension, XmlRef.epithelium);
		
		for( Dimension.DimName d : this._parentNode._shape.getDimensionNames() )
		{
			Dimension dim = this._parentNode._shape.getDimension( d );
			
			if (dim.getName().name().equals(string))
				this._dimension = dim;
		}
		
		string = XmlHandler.obtainAttribute(
				xmlElement, XmlRef.extreme, XmlRef.epithelium);
		
		this._extreme = Integer.valueOf(string);
		
		this._compartment = this._parentNode._compartment;
		
		this.findAssociatedBoundary();
		
		this._epithelialGrid = new EpithelialGrid();
		
		Spawner spawner;
		TreeMap<Integer,Spawner> spawners = new TreeMap<Integer,Spawner>();
		for (Element e : XmlHandler.getDirectChildElements(
				xmlElement, XmlRef.spawnNode) )
		{
			spawner = (Spawner) Instance.getNew(e, this);
			
			/* check for duplicate priority */
			int priority = Helper.nextAvailableKey(
					spawner.getPriority(),spawners.keySet() );
			if (spawners.containsKey( spawner.getPriority() ))
			{
				if( Log.shouldWrite(Tier.EXPRESSIVE))
					Log.out(Tier.EXPRESSIVE, "WARNING: Spawner with "
							+ "duplicate priority next priority is picked "
							+ "by simulator.");
			}
			spawners.put(priority, spawner);
			
		}
		
		for( Spawner s : spawners.values() )
			s.spawn();
		
		
	}
	
	public void findAssociatedBoundary()
	{
		Collection<Boundary> _allBoundaries =
				this._compartment.getShape().getAllBoundaries();
		for (Boundary b : _allBoundaries)
		{
			if (b instanceof EpithelialBoundary)
			{
				if (((EpithelialBoundary) b).getDimName()
						== this._dimension.getName() &&
						((EpithelialBoundary) b).getExtreme()
						== this._extreme)
				{
					this._associatedBoundary = (EpithelialBoundary) b;
				}
			}
		}
		
		this._associatedBoundary.setEpithelium(this);
		
	}
	
	public double epithelialCellSurfaceArea (Agent agent)
	{
		double surfaceArea;
		
		if (this._agentList.contains(agent))
		{
			surfaceArea = 1.0;
			
			List<Point> agentPoints = 
					((Body) agent.get(AspectRef.agentBody))
					.getPoints();
			
			double[] position0 = 
					agentPoints.get(0).getPosition();
			
			int numDims = position0.length;
			
			double[][] allCoords = new double[2][numDims];
			
			for (int point = 0; point < 2; point++)
			{
				double[] position = 
						agentPoints.get(point).getPosition();
				//X coordinates
				allCoords[point][0] = position[0];
				//Y coordinates
				allCoords[point][1] = position[1];
				//Z coordinate
				if (numDims > 2)
					allCoords[point][2] = position[2];
			}
			
			for (int i = 0; i < numDims; i++)
			{
				if (this._dimension.getName().dimNum() != i)
				{
					double length = Math.abs(
							allCoords[1][i] - allCoords[0][i]);
					
					if (length != 0.0)
						surfaceArea *= length;
				}
			}
		}
		
		else
			surfaceArea = 0.0;
		
		return surfaceArea;
	}
	
	public EpithelialGrid getGrid()
	{
		return this._epithelialGrid;
	}
	
	public HashMap<IntegerArray, HashMap<Agent, Double>> 
		setUpEpithelialDistributionMaps(SoluteGrid solute)
	{
		/*
		 * This is a map of integer arrays representing location of voxels
		 * (from Shape.getVerifiedLocation) paired with the proportion of
		 * that voxel covered by each agent that borders it. Analogous to
		 * a distribution map as found in microbial agents, but there
		 * is just one map for the whole epithelium
		 */
		HashMap<IntegerArray, HashMap<Agent, Double>> epithelialMap = 
				new HashMap<IntegerArray, HashMap<Agent, Double>>();
		
		double resolution = solute.getResolution();
		
		double[] epitheliumSideLengths = 
				this._epithelialGrid.getLayerSideLengths();
		
		int[] numberVoxels = new int[epitheliumSideLengths.length];
		
		for (int i = 0; i < numberVoxels.length; i++)
		{
			numberVoxels[i] = (int) (epitheliumSideLengths[i]/resolution);
		}
		
		double[] boundingBoxBottomCorner =
				new double[numberVoxels.length];
		
		double[] boundingBoxTopCorner =
				new double[numberVoxels.length];
		
		ArrayList <Agent> agentList;
		
		BoundingBox boundingBox = new BoundingBox();
		
		
		
		for (int i = 0; i < numberVoxels[0]; i++)
		{
			boundingBoxBottomCorner[0] = resolution * i;
			boundingBoxTopCorner[0] = resolution * (i+1);
			for (int j = 0; j < numberVoxels[1]; j++)
			{
				boundingBoxBottomCorner[1] = resolution * j;
				boundingBoxTopCorner[1] = resolution * (j+1);
				
				if (numberVoxels.length == 3)
				{
					for (int k = 0; k < numberVoxels[2]; k++)
					{
						boundingBoxBottomCorner[2] = resolution * k;
						boundingBoxTopCorner[2] = resolution * (k+1);
						
						boundingBox.get(boundingBoxBottomCorner, 
								boundingBoxTopCorner);
						
						agentList = this._epithelialGrid.search(boundingBox);
						
						HashMap <Agent, Double> agentVoxelCoverage =
								new HashMap <Agent, Double>();
						
						int[] voxel = {i, j, k};
						
						IntegerArray wrappedVoxel = new IntegerArray(voxel);
						
						for (Agent a : agentList)
						{
							List<Point> agentPoints = 
									((Body) a.get(AspectRef.agentBody))
									.getPoints();
							
							double[][] allCoords = new double[2][3];
							
							for (int point = 0; point < 2; point++)
							{
								double[] position = 
										agentPoints.get(point).getPosition();
								//X coordinates
								allCoords[0][point] = position[0];
								//Y coordinates
								allCoords[1][point] = position[1];
								//Z coordinate
								allCoords[2][point] = position[2];
							}
							
							/*
							 * This double[] contains the side lengths
							 * of the cuboid formed by the intersection
							 * of the voxel and the agent. As the
							 * agent should be a flat surface, one of
							 * the doubles should be 0.0.
							 */
							double[] intersection = new double[3];
							for (int dim = 0; dim < 3; dim++)
							{
								intersection[dim] = 
									Math.min(ExtraMath.maximum(allCoords[dim]),
												boundingBoxTopCorner[dim])
									- Math.max(ExtraMath.minimum(allCoords[dim]),
											boundingBoxBottomCorner[dim]);
							}
							
							double intersectionArea = 1.0;
							double agentArea = 1.0;
							
							for (int dim = 0; dim < 3; dim++)
							{
								if (epitheliumSideLengths[dim] != 0.0)
								{
									intersectionArea *= intersection[dim];
									
									agentArea *= Math.abs(allCoords[dim][0] - 
											allCoords[dim][1]);
								}
							}
							
							/*
							 * We consider the voxel's surface area at the 
							 * edge of the domain, rather than its volume
							 */
							double voxelArea = Math.pow(resolution, 2);
							
							double voxelProportion = 
									intersectionArea/voxelArea;
							
							agentVoxelCoverage.put(a, voxelProportion);
							
							HashMap<IntegerArray,Double> map;
							
							if (a.isAspect(VD_TAG))
							{
								map = (HashMap<IntegerArray, Double>) a.get(VD_TAG);
							}
							else
							{
								map = new HashMap<IntegerArray,Double>();
								a.set(VD_TAG, map);
							}
							
							map.put(wrappedVoxel, intersectionArea/agentArea);
						}
						
						epithelialMap.put(wrappedVoxel, agentVoxelCoverage);
						
						
					}
				}
				else
				{
					boundingBox.get(boundingBoxBottomCorner, 
							boundingBoxTopCorner);
					agentList = this._epithelialGrid.search(boundingBox);
					
					agentList = this._epithelialGrid.search(boundingBox);
					
					HashMap <Agent, Double> agentVoxelCoverage =
							new HashMap <Agent, Double>();
					
					int[] voxel = {i, j};
					
					IntegerArray wrappedVoxel = new IntegerArray(voxel);
					
					for (Agent a : agentList)
					{
						List<Point> agentPoints = 
								((Body) a.get(AspectRef.agentBody))
								.getPoints();
						
						double[][] allCoords = new double[2][2];
						
						for (int point = 0; point < 2; point++)
						{
							double[] position = 
									agentPoints.get(point).getPosition();
							//X coordinates
							allCoords[0][point] = position[0];
							//Y coordinates
							allCoords[1][point] = position[1];
						}
						
						/*
						 * This double[] contains the side lengths
						 * of the cuboid formed by the intersection
						 * of the voxel and the agent. As the
						 * agent should be a flat surface, one of
						 * the doubles should be 0.0.
						 */
						double[] intersection = new double[2];
						for (int dim = 0; dim < 2; dim++)
						{
							intersection[dim] = 
								Math.min(ExtraMath.maximum(allCoords[dim]),
											boundingBoxTopCorner[dim])
								- Math.max(ExtraMath.minimum(allCoords[dim]),
										boundingBoxBottomCorner[dim]);
						}
						
						double intersectionArea = 1.0;
						double agentArea = 1.0;
						
						for (int dim = 0; dim < 2; dim++)
						{
							if (epitheliumSideLengths[dim] != 0.0)
							{
								intersectionArea *= intersection[dim];
								
								agentArea *= Math.abs(allCoords[dim][0] - 
										allCoords[dim][1]);
							}
						}
						
						/*
						 * We consider the voxel's length at the 
						 * edge of the domain, rather than its area
						 */
						double voxelLength = resolution;
						
						double voxelProportion = 
								intersectionArea/voxelLength;
						
						agentVoxelCoverage.put(a, voxelProportion);
						
						HashMap<IntegerArray,Double> map;
						
						if (a.isAspect(VD_TAG))
						{
							map = (HashMap<IntegerArray, Double>) a.get(VD_TAG);
						}
						else
						{
							map = new HashMap<IntegerArray,Double>();
							a.set(VD_TAG, map);
						}
						
						map.put(wrappedVoxel, intersectionArea/agentArea);
					}
					
					epithelialMap.put(wrappedVoxel, agentVoxelCoverage);
				}
			}
		}
		
		return epithelialMap;
	}
	
	public Module getModule() 
	{
		/* The epithelium node. */
		Module modelNode = new Module( XmlRef.epithelium, this);
		modelNode.setRequirements(Requirements.ZERO_OR_ONE);
		/* Add the agent childConstrutor for adding of additional agents. */
		modelNode.addChildSpec( ClassRef.agent,
				Module.Requirements.ZERO_TO_MANY);
		
		/* If there are agents, add them as child nodes. */
		for ( Agent a : this._agentList)
			modelNode.add( a.getModule() );
		
		return modelNode;
	
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = (AgentContainer) parent;
	}

	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
	
	public Compartment getCompartment()
	{
		return this._compartment;
	}
	
	public Dimension getDimension()
	{
		return this._dimension;
	}
	
	public int getExtreme()
	{
		return this._extreme;
	}
	
	public EpithelialBoundary getBoundary()
	{
		if (Helper.isNullOrEmpty(this._associatedBoundary))
			this.findAssociatedBoundary();
		
		return this._associatedBoundary;
	}

}
