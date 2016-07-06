package nodeFactory.primarySetters;

import org.w3c.dom.Element;

import dataIO.ObjectFactory;
import generalInterfaces.Instantiatable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;

public class PileEntry<T> implements NodeConstructor, Instantiatable {

	/**
	 * 
	 */
	public T mapObject;
	public Pile<T> pile;
	
	public PileEntry(Pile<T> pile, T object )
	{
		this.pile = pile;
		this.mapObject = object;
	}
	
	public PileEntry()
	{
		// NOTE for instatniatable interface
	}

	@SuppressWarnings("unchecked")
	public void init(Element xmlElem, NodeConstructor parent)
	{
		this.pile = (Pile<T>) parent;
		this.pile.add(this.mapObject);
	}
	
	public ModelNode getNode() 
	{
		ModelNode modelNode = new ModelNode(this.defaultXmlTag() , this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		if (mapObject == null)
			modelNode.add(new ModelAttribute( pile.valueLabel, 
					"", null, true));
		else
			modelNode.add(new ModelAttribute( pile.valueLabel, 
					String.valueOf(mapObject), null, true));
		
		return modelNode;
	}
	
	@SuppressWarnings("unchecked")
	public void setNode(ModelNode node)
	{
		this.pile.remove( mapObject );
		
		mapObject = (T) ObjectFactory.loadObject(
				node.getAttribute( pile.valueLabel ).getValue(), 
				pile.entryClass.getSimpleName() );

		this.pile.add( mapObject );
		NodeConstructor.super.setNode(node);
	}

	public void removeNode(String specifier)
	{
		this.pile.remove(this.mapObject);
	}

	@Override
	public String defaultXmlTag() 
	{
		return pile.nodeLabel;
	}
}