package boundary.spatialLibrary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.SpatialBoundary;
import boundary.library.ChemostatBoundary;
import boundary.library.ChemostatToBoundaryLayer;
import boundary.library.ChemostatToEpithelium;
import boundary.library.ChemostatToMembrane;
import compartment.Epithelium;
import compartment.AgentContainer;
import dataIO.XmlHandler;
import grid.SpatialGrid;
import processManager.ProcessMethods;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Settable;
import shape.Dimension.DimName;
import shape.Shape;
import shape.subvoxel.IntegerArray;
import solver.mgFas.MultigridSolute;
import solver.mgFas.SoluteGrid;
import solver.mgFas.SolverGrid;

public class EpithelialBoundary extends SpatialBoundary {
	
	private Epithelium _epithelium;
	
	public void instantiate(Element xmlElement, Settable parent) 
	{
		super.instantiate(xmlElement, parent);
	}
	
	public EpithelialBoundary()
	{
		this._dominant = true;
	}
	
	public Class<?> getPartnerClass()
	{
		return ChemostatToEpithelium.class;
	}
	
	public void setEpithelium(Epithelium epithelium)
	{
		this._epithelium = epithelium;
	}
	
	@Override
	protected boolean needsLayerThickness() {
		return false;
	}

	@Override
	protected double calcDiffusiveFlow(SpatialGrid grid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateWellMixedArray() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public void additionalPartnerUpdate() {
		// TODO Auto-generated method stub
		
	}
	
}
