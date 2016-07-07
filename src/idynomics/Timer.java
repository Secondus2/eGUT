package idynomics;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import generalInterfaces.Instantiatable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;
import nodeFactory.NodeConstructor;
import dataIO.Log.Tier;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Timer implements Instantiatable, NodeConstructor
{
	/**
	 * TODO
	 */
	private int _iteration;
	
	/**
	 * TODO
	 */
	private double _now;
	
	/**
	 * TODO
	 */
	protected double _timerStepSize;
	
	/**
	 * TODO
	 */
	protected double _endOfSimulation;

	private NodeConstructor _parentNode;
		
	public Timer()
	{
		this._iteration = 0;
		this._now = 0.0;
	}
		
	public void init(Element xmlNode)
	{
		Log.out(Tier.NORMAL, "Timer loading...");

		/* Get starting time step */
		seteCurrentTime( Double.valueOf( Helper.setIfNone( 
				XmlHandler.gatherAttribute(
				xmlNode, XmlRef.currentTime ), "0.0" ) ) );
		
		/* Get the time step. */
		setTimeStepSize( Double.valueOf( XmlHandler.obtainAttribute(
				xmlNode, XmlRef.timerStepSize, this.defaultXmlTag() ) ) );

		/* Get the total time span. */
		setEndOfSimulation( Double.valueOf( XmlHandler.obtainAttribute(
				xmlNode, XmlRef.endOfSimulation, this.defaultXmlTag() ) ) );

		
		report(Tier.NORMAL);
		Log.out(Tier.NORMAL, "Timer loaded!\n");
	}
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/

	
	public void reset()
	{
		this._now = 0.0;
		this._iteration = 0;
	}
	
	public void setTimeStepSize(double stepSize)
	{
		this._timerStepSize = stepSize;
	}
	
	public void seteCurrentTime(double time)
	{
		this._now = time;
	}
	
	public double getCurrentTime()
	{
		return this._now;
	}
	
	public int getCurrentIteration()
	{
		return this._iteration;
	}
	
	public double getTimeStepSize()
	{
		return this._timerStepSize;
	}
	
	public double getEndOfCurrentIteration()
	{
		return this._now + getTimeStepSize();
	}
	
	public void step()
	{
		this._now += getTimeStepSize();
		this._iteration++;
		if ( Helper.gui )
			GuiLaunch.updateProgressBar();
	}
	
	public double getEndOfSimulation()
	{
		return this._endOfSimulation;
	}
	
	public void setEndOfSimulation(double timeToStopAt)
	{
		this._endOfSimulation = timeToStopAt;
	}
	
	public int estimateLastIteration()
	{
		return (int) (getEndOfSimulation() - this.getCurrentTime() / getTimeStepSize());
	}
	
	public boolean isRunning()
	{
		Log.out(Tier.DEBUG, "Timer.isRunning()? now = "+this._now+
				", end = "+this.getEndOfSimulation()+
				", so "+(this._now<getEndOfSimulation())); 
		return this._now < this.getEndOfSimulation();
	}
	
	public void report(Tier outputLevel)
	{
		Log.out(outputLevel, "Timer: time is   = "+_now);
		Log.out(outputLevel, "       iteration = "+_iteration);
		Log.out(outputLevel, "       step size = "+getTimeStepSize());
		Log.out(outputLevel, "       end time  = "+getEndOfSimulation());
	}
	
	/*************************************************************************
	 * model node
	 ************************************************************************/

	/**
	 * Get the ModelNode object for this Timer object
	 * @return ModelNode
	 */
	public ModelNode getNode()
	{
		/* the timer node */
		ModelNode modelNode = new ModelNode(XmlRef.timer, this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		
		/* now */
		modelNode.add(new ModelAttribute(XmlRef.currentTime, 
				String.valueOf(this._now), null, true ));
		
		/* time step size */
		modelNode.add(new ModelAttribute(XmlRef.timerStepSize, 
				String.valueOf(this._timerStepSize), null, true ));
		
		/* end of simulation */
		modelNode.add(new ModelAttribute(XmlRef.endOfSimulation, 
				String.valueOf(this._endOfSimulation), null, true ));
		
		return modelNode;
	}

	/**
	 * Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object
	 * @param node
	 */
	public void setNode(ModelNode node)
	{
		this.setTimeStepSize( Double.valueOf( 
				node.getAttribute( XmlRef.currentTime ).getValue() ));
		
		/* time step size */
		this.setTimeStepSize( Double.valueOf( 
				node.getAttribute( XmlRef.timerStepSize ).getValue() ));
		
		/* end of simulation */
		this.setEndOfSimulation( Double.valueOf( 
				node.getAttribute( XmlRef.endOfSimulation ).getValue() ));
	}
	
	/**
	 * Create a new minimal object of this class and return it
	 * @return NodeConstructor
	 */
	public NodeConstructor newBlank()
	{
		return new Timer();
	}

	/**
	 * return the default XMLtag for the XML node of this object
	 * @return String xmlTag
	 */
	@Override
	public String defaultXmlTag() {
		return XmlRef.timer;
	}

	@Override
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}
}
