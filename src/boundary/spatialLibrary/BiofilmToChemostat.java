package boundary.spatialLibrary;

import org.w3c.dom.Element;

import boundary.library.ChemostatToBoundaryLayer;
import settable.Settable;

public class BiofilmToChemostat extends BiofilmBoundaryLayer{
	
	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		super.instantiate(xmlElement, parent);
		
	}
	
	@Override
	public Class<?> getPartnerClass()
	{
		return ChemostatToBoundaryLayer.class;
	}
	
	@Override
	public void additionalPartnerUpdate()
	{
		ChemostatToBoundaryLayer p = (ChemostatToBoundaryLayer) this._partner;
		for ( String soluteName : this._environment.getSoluteNames() )
			this._concns.put(soluteName, p.getSoluteConcentration(soluteName));
	}
}
