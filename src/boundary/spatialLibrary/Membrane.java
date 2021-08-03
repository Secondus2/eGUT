package boundary.spatialLibrary;

import boundary.SpatialBoundary;
import boundary.library.ChemostatToMembrane;
import grid.SpatialGrid;

public class Membrane extends SpatialBoundary {
	
	/**
	 * Level of permeability for each solute that can diffuse through the membrane
	 */
	protected Double[] permeability;
	
	
	
	@Override
	protected boolean needsLayerThickness() {
		// TODO Auto-generated method stub
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
	public Class<?> getPartnerClass() {
		// TODO Auto-generated method stub
		return ChemostatToMembrane.class;
	}

	@Override
	public void additionalPartnerUpdate() {
		// TODO Auto-generated method stub
		
	}
	
	
}
