package idynomics.launchable;

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import utility.Helper;

public class ExitCommand implements Launchable {

	@Override
	public void initialize(String[] args) {
		Idynomics.global.exitCommand = Helper.stringAToString( Helper.subset( args, 1, args.length) , " ");
		
	}

}
