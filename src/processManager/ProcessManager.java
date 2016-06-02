package processManager;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.Log.Tier;
import dataIO.Log;
import dataIO.XmlLabel;
import generalInterfaces.XMLable;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import utility.Helper;
import nodeFactory.ModelNode.Requirements;

/**
 * \brief Abstract class for managing a process within a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ProcessManager implements XMLable, AspectInterface,
		NodeConstructor
{
	/**
	 * The name of this {@code ProcessManager}, for reporting.
	 */
	protected String _name;
	/**
	 * In the event of a time-clash between {@code ProcessManager}s, the one
	 * with greater priority goes first.
	 */
	protected int _priority;
	/**
	 * The time at which this should next perform its step.
	 */
	protected double _timeForNextStep = 0.0;
	/**
	 * How often this should perform its step.
	 */
	protected double _timeStepSize;
	/**
     * The aspect registry... TODO
     */
    private AspectReg _aspectRegistry = new AspectReg();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem)
	{
		//FIXME quick fix: cut/paste from
		//"public static ProcessManager getNewInstance(Node xmlNode)"
		
		this.loadAspects(xmlElem);
		/*
		 * Read in the process attributes. 
		 */
		Element p = (Element) xmlElem;
		this.setName( p.getAttribute( XmlLabel.nameAttribute ));
		/* Process priority - default is zero. */
		int priority = 0;
		if ( p.hasAttribute(XmlLabel.processPriority) )
			priority = Integer.valueOf(p.getAttribute(XmlLabel.processPriority));
		this.setPriority(priority);
		/* Initial time to step. */
		double time = Idynomics.simulator.timer.getCurrentTime();
		if ( p.hasAttribute(XmlLabel.processFirstStep) )
			time = Double.valueOf( p.getAttribute(XmlLabel.processFirstStep) );
		this.setTimeForNextStep(time);
		/* Time step size. */
		time = Idynomics.simulator.timer.getTimeStepSize();
		if ( p.hasAttribute(XmlLabel.timerStepSize) )
			time = Double.valueOf( p.getAttribute(XmlLabel.timerStepSize) );
		this.setTimeStepSize(time);
	}
	
	/**
	 * Implements XMLable interface, return new instance from xml Node.
	 * 
	 * @param xmlNode
	 * @return
	 */
	public static ProcessManager getNewInstance(Node xmlNode)
	{
		ProcessManager proc = (ProcessManager) XMLable.getNewInstance(xmlNode);
		proc.init((Element) xmlNode);
		return proc;
	}
	
	public static ProcessManager getNewInstance(String className)
	{
		//return (ProcessManager) XMLable.getNewInstance(className);
		
		return (ProcessManager) XMLable.getNewInstance(className, 
				Idynomics.xmlPackageLibrary.get(className));
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Return the aspect registry (implementation of aspect interface).
	 */
	public AspectReg reg()
	{
		return _aspectRegistry;
	}
	
	/**
	 * \brief Get this {@code ProcessManager}'s name.
	 * 
	 * @return {@code String} name.
	 */
	public String getName()
	{
		if (this._name == null)
			return "";
		else
		return this._name;
	}
	
	/**
	 * \brief Set this {@code ProcessManager}'s name.
	 * 
	 * @param {@code String} name.
	 */
	public void setName(String name)
	{
		this._name = name;
	}
	
	/**
	 * \brief Set the priority of this {@code ProcessManager}.
	 * 
	 * <p>A higher priority {@code ProcessManager} goes first if two have a
	 * time clash in the {@code Compartment} they belong to.</p>
	 * 
	 * @param priority Any integer value.
	 */
	public void setPriority(int priority)
	{
		this._priority = priority;
	}
	
	/**
	 * @return The priority of this {@code ProcessManager}.
	 */
	public int getPriority()
	{
		return this._priority;
	}
	
	/**]
	 * \brief Set the time point at which this will next step: useful for
	 * testing.
	 * 
	 * @param newTime The time point at which this should next step.
	 */
	public void setTimeForNextStep(double newTime)
	{
		this._timeForNextStep = newTime;
	}
	
	/**
	 * @return The time point at which this will next step.
	 */
	public double getTimeForNextStep()
	{
		return this._timeForNextStep;
	}
	
	/**
	 * \brief Set the time step directly: useful for testing.
	 * 
	 * @param newStepSize Time step to use.
	 */
	public void setTimeStepSize(double newStepSize)
	{
		this._timeStepSize = newStepSize;
	}
	
	/**
	 * @return The local time step.
	 */
	public double getTimeStepSize()
	{
		return this._timeStepSize;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * \brief Perform the step of this process manager, also updating its local
	 * time manager.
	 * 
	 * @param environment The {@code EnvironmentContainer} of the
	 * {@code Compartment} this process belongs to.
	 * @param agents The {@code AgentContainer} of the
	 * {@code Compartment} this process belongs to.
	 */
	public void step(EnvironmentContainer environment, AgentContainer agents)
	{
		/*
		 * This is where subclasses of ProcessManager do their step. Note that
		 * this._timeStepSize may change if an adaptive timestep is used.
		 */
		this.internalStep(environment, agents);
		/*
		 * Move the time for next step forward by the step size.
		 */
		this._timeForNextStep += this._timeStepSize;
		Log.out(Tier.DEBUG,
				this._name+": timeForNextStep = "+this._timeForNextStep);
	}
	
	/**
	 * \brief Perform the internal step for this process manager: this will be
	 * implemented by each sub-class of {@code ProcessManager}.
	 * 
	 * @param environment The {@code EnvironmentContainer} of the
	 * {@code Compartment} this process belongs to.
	 * @param agents The {@code AgentContainer} of the
	 * {@code Compartment} this process belongs to.
	 */
	protected abstract void internalStep(
					EnvironmentContainer environment, AgentContainer agents);
	
	public static List<String> getAllOptions()
	{
		return Idynomics.xmlPackageLibrary.getAll("processManager.library.");
	}
	
	
	/**
	 * 
	 */
	public ModelNode getNode()
	{
		ModelNode modelNode = new ModelNode(defaultXmlTag(), this);
		modelNode.requirement = Requirements.ZERO_TO_MANY;
		modelNode.title = String.valueOf(this._name);
		
		modelNode.add(new ModelAttribute(XmlLabel.nameAttribute, 
						this._name, null, true ));
		
		modelNode.add(new ModelAttribute(XmlLabel.classAttribute, 
				this.getClass().getSimpleName(), null, true ));
		
		modelNode.add(new ModelAttribute(XmlLabel.processPriority, 
				String.valueOf(this._priority), null, true ));
		
		modelNode.add(new ModelAttribute(XmlLabel.processFirstStep, 
				String.valueOf(this._timeForNextStep), null, true ));
		
		/* TODO: add aspects */
		
		for ( String key : this.reg().getLocalAspectNames() )
			modelNode.add(reg().getAspectNode(key));
		
		modelNode.childConstructors.put(reg().new Aspect(reg()), 
				ModelNode.Requirements.ZERO_TO_MANY);
		
		return modelNode;
	}
	
	
	/**
	 * 
	 */
	public NodeConstructor newBlank() 
	{
		String input = Helper.obtainInput(ProcessManager.getAllOptions(), 
				"process manager", false);
		return  ProcessManager.getNewInstance(input);
	}
	
	/**
	 * 
	 */
	public void setNode(ModelNode node) 
	{
		
	}

	/**
	 * 
	 */
	public void addChildObject(NodeConstructor childObject) 
		{

		}
		
	/**
	 * 
	 */
	public String defaultXmlTag() 
		{
		return XmlLabel.process;
		}
		
	/**
	 * 
	 */
	public String getXml() 
		{
		return getNode().getXML();
	}
}