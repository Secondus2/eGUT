package gui;


import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import idynomics.Idynomics;
import idynomics.launchable.GuiLaunch;


/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiMain
{
	public static JTabbedPane tabbedPane = GuiComponent.newPane();
		
	final static int CONSOLEPANE = 0;
	
	public static JComponent getConstructor() 
	{
		return (JComponent) GuiComponent.setPane(tabbedPane);
	}
		
	public static void update()
	{
		if (GuiLaunch.classicGui())
		{
			GuiMain.getConstructor();
		}
		GuiEditor.addComponent(Idynomics.simulator.getModule(), 
				GuiMain.tabbedPane);
	}

}
