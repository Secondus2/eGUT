package guiTools;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import dataIO.Log;
import idynomics.GuiLaunch;
import idynomics.GuiLaunch.ViewType;
import idynomics.Idynomics;
import idynomics.Simulator;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import modelBuilder.SubmodelRequirement;

public class ConsoleSimBuilder
{
	public static void makeSimulation()
	{
		GuiLaunch.setView(ViewType.CONSOLE);
		Log.printToScreen("~~~~~~~~~~~~~~~~~~~~~", false);
		Log.printToScreen("Making new simulation", false);
		Log.printToScreen("~~~~~~~~~~~~~~~~~~~~~", false);
		Idynomics.simulator = new Simulator();
		buildSubmodel(Idynomics.simulator);
	}
	
	private static void buildSubmodel(IsSubmodel aSubmodel)
	{
		String subName = aSubmodel.getClass().getSimpleName();
		GuiConsole.writeOut("Making submodel \""+subName+"\"\n");
		Map<String, Class<?>> parameters = aSubmodel.getParameters();
		for ( String name : parameters.keySet() )
		{
			String className = parameters.get(name).getSimpleName();
			String value = GuiConsole.requestInput("Please enter \""+className+
						"\" value for "+subName+" parameter \""+name+"\": ");
			aSubmodel.setParameter(name, value);
			GuiConsole.writeOut("\t"+subName+" accepts \""+value+" as "+name+"\n");
		}
		
		/* Now go through the sub-sub-models. */
		List<SubmodelMaker> makers = aSubmodel.getSubmodelMakers();
		for ( SubmodelMaker aMaker : makers )
		{
			if ( aMaker.makeImmediately() )
			{
				aMaker.actionPerformed(null);
				buildSubmodel(aMaker.getLastMadeSubmodel());
				if (aMaker.getRequirement() == SubmodelRequirement.EXACTLY_ONE)
					continue;
			}
			String[] options = aMaker.getClassNameOptions();
			if ( options != null )
			{
				GuiConsole.writeOut("\tPossible options are:\n");
				for ( int i = 0; i < options.length; i++ )
					GuiConsole.writeOut("\t\t["+i+"] "+options[i]+"\n");
				String value = GuiConsole.requestInput(
						"Please choose an option by number: ");
				int i = Integer.valueOf(value);
				String optionToUse = options[i];
				// TODO this is where it breaks!
				aMaker.actionPerformed(new ActionEvent(null, 0, optionToUse));
			}
			
		}
	}
	
}
