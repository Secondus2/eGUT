/**
 * 
 */
package boundary.agent;

import java.awt.event.ActionEvent;

import generalInterfaces.XMLable;
import modelBuilder.IsSubmodel;
import modelBuilder.SubmodelMaker;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class AgentMethod implements IsSubmodel, XMLable
{
	
	
	
	
	public static String[] getAllOptions()
	{
		return Helper.getClassNamesSimple(
				AgentMethodLibrary.class.getDeclaredClasses());
	}
	
	
	public static AgentMethod getNewInstance(String className)
	{
		return (AgentMethod) XMLable.getNewInstance(className, 
										"boundary.agent.AgentMethodLibrary$");
	}
	
	
	
	
	public static class AgentMethodMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = -4571936613706733683L;
		
		/**\brief TODO
		 * 
		 * @param name
		 * @param req
		 * @param target
		 */
		public AgentMethodMaker(String name, Requirement req, IsSubmodel target)
		{
			super(name, req, target);
		}
		
		@Override
		protected void doAction(ActionEvent e)
		{
			String name;
			if ( e == null )
				name = "";
			else
				name = e.getActionCommand();
			this.addSubmodel(AgentMethod.getNewInstance(name));
		}
		
		@Override
		public Object getOptions()
		{
			return AgentMethod.getAllOptions();
		}
	}
}