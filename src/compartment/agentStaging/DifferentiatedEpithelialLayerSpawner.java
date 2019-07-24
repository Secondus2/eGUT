package compartment.agentStaging;

import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import agent.Body.Morphology;
import compartment.AgentContainer;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import surface.Point;
import utility.ExtraMath;


public class DifferentiatedEpithelialLayerSpawner extends EpithelialLayerSpawner {
	
	private Agent[] _templates;
	
	private int[] _templateFrequency;
	
	private int _agentCount;
	
	public void init(
			
		Element xmlElem, AgentContainer agents, String compartmentName) {
		
		super.init(xmlElem, agents, compartmentName);
		
		_agentCount = 0;
		
		Collection <Element> templateCollection = XmlHandler.
				getDirectChildElements(xmlElem, XmlRef.templateAgent);
		
		LinkedList <Element> templates = new LinkedList <Element>();
		
		for (Element e: templateCollection)
			templates.add(e);
		
		_templates = new Agent[templates.size()];
		
		_templateFrequency = new int[templates.size()];
		
		for (int i = 0; i < templates.size(); i++) {
			Agent tempAgent = new Agent (templates.get(i), true);
			_templates[i] = tempAgent;
			if ( XmlHandler.hasAttribute(templates.get(i),
					XmlRef.numberOfAgents) ) {
				_templateFrequency[i] = Integer.valueOf(
						templates.get(i).getAttribute(XmlRef.numberOfAgents));
			}
		}
		
		int templateCount = 0;
		for (int i = 0; i < _templateFrequency.length; i++) {
			templateCount += _templateFrequency[i];
		}
		
		if (templateCount != this._numberOfAgents) {
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "Warning: Number of agents specified "
						+ "in the differentiated epithelial layer spawner is "
						+ "not equal to the number of agents calculated using "
						+ "the dimensions provided.");
			Idynomics.simulator.interupt("Interrupted due agent number "
					+ "mismatch.");
		}
	}
	
	@Override
	public void spawnEpithelialAgent (Point[] position, int index) {
		int cellType = ExtraMath.getUniRandInt(
				(this._numberOfAgents - _agentCount));
		int i = 0;
		while (cellType >= _templateFrequency[i]) {
				cellType -=_templateFrequency[i];
				i++;
		}
				
		this._template = _templates[i];
		Agent newEpithelialCell = new Agent(this.getTemplate());
		newEpithelialCell.set(AspectRef.agentBody, new Body(
				position, this._normal));
		newEpithelialCell.set(AspectRef.cuboidOrientation,
				this._normal);
		newEpithelialCell.setCompartment( this.getCompartment() );
		newEpithelialCell.registerBirth();
		_templateFrequency[i] --;
		_agentCount++;
		thisEpithelium.listAgent(newEpithelialCell, index);
	}
	
	
}