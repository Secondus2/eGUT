package boundary.library;

import boundary.spatialLibrary.EpithelialBoundary;

public class ChemostatToEpithelium extends ChemostatBoundary {

	@Override
	public Class<?> getPartnerClass() 
	{
		return EpithelialBoundary.class;
	}

	@Override
	public void additionalPartnerUpdate() {
		// TODO Auto-generated method stub
		
	}

}
