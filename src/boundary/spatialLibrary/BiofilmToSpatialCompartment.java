package boundary.spatialLibrary;

import org.w3c.dom.Element;

import boundary.library.ChemostatToBoundaryLayer;
import dataIO.XmlHandler;
import referenceLibrary.XmlRef;
import settable.Settable;

public class BiofilmToSpatialCompartment extends BiofilmBoundaryLayer{
	
	/**
	 * A parameter which describes the rate of agent exchange between two
	 * compartments with mixing, but without directional flow.
	 */
	public double _exchangeRate;
	
	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		super.instantiate(xmlElement, parent);
		this._exchangeRate = Double.valueOf(XmlHandler.obtainAttribute(
				xmlElement, XmlRef.exchangeRate, this.defaultXmlTag()));
	}
	
	@Override
	public double getExchangeRate()
	{
		return _exchangeRate;
	}
	
	@Override
	public Class<?> getPartnerClass()
	{
		return SpatialCompartmentToBoundaryLayer.class;
	}
	
	@Override
	public void additionalPartnerUpdate()
	{
		SpatialCompartmentToBoundaryLayer p = 
				(SpatialCompartmentToBoundaryLayer) this._partner;
		((SpatialCompartmentToBoundaryLayer) this._partner).setExchangeRate(
				this._exchangeRate);
		for ( String soluteName : this._environment.getSoluteNames() )
			this._concns.put(soluteName, p.getSoluteConcentration(soluteName));
	}

}
