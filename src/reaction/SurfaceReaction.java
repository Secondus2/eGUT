package reaction;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;

import compartment.Compartment;
import compartment.EnvironmentContainer;
import dataIO.XmlHandler;
import expression.Expression;
import instantiable.object.InstantiableList;
import referenceLibrary.XmlRef;
import settable.Settable;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import utility.Helper;

public class SurfaceReaction extends RegularReaction {
	
	
	/**
	 * Compartment to/from which transport occurs
	 */
	private Dimension _fluxDimension = null;
	
	private String _fluxDimensionName;
	
	
	@Override
	public void instantiate(Element xmlElem, Settable parent) {
		
		this._parentNode = parent;
		
		if ( parent instanceof EnvironmentContainer )
			((EnvironmentContainer) parent).addReaction(this);
		
		if (parent instanceof InstantiableList)
			((InstantiableList<SurfaceReaction>) parent).add(this);

		if ( !Helper.isNullOrEmpty(xmlElem) && XmlHandler.hasChild(xmlElem, XmlRef.reaction))
		{
			xmlElem = XmlHandler.findUniqueChild(xmlElem, XmlRef.reaction);
		}
		
		this._name = XmlHandler.obtainAttribute(
				xmlElem, XmlRef.nameAttribute, this.defaultXmlTag());
		
		String _fluxDimensionName = XmlHandler.obtainAttribute(xmlElem,
				XmlRef.fluxDimension, this.defaultXmlTag());
		
		/*
		 * Build the stoichiometric map.
		 */
		this._stoichiometry.instantiate( xmlElem, this, 
				String.class.getSimpleName(), Double.class.getSimpleName(), 
				XmlRef.stoichiometric );

		/*
		 * Build the reaction rate expression.
		 */
		if ( Helper.isNullOrEmpty(xmlElem) || !XmlHandler.hasChild(xmlElem, XmlRef.expression))
			this._kinetic = new Expression("");
		else
			this._kinetic = new 
				Expression(XmlHandler.findUniqueChild(xmlElem, XmlRef.expression));
	}
	
	/**
	 * Returns the names of constituents that are not labelled with a
	 * compartment
	 * @return
	 */
	public Collection<String> getUnlabelledConstituentNames()
	{
		if ( this._constituents == null )
		{
			this._constituents = new ArrayList<String>();
			
			for (String variableName : this.getVariableNames())
			{
				if (!variableName.contains("@"))
						this._constituents.add(variableName);
			}
			
			for(String stoichiometricComponent : 
				this.getStoichiometryAtStdConcentration().keySet() )
			{
				if (!stoichiometricComponent.contains("@"))
					this._constituents.add(stoichiometricComponent);
			}
		}
		return this._constituents;
	}
	
	/**
	 * Returns the names of all constituents tagged as belonging to the 
	 * given compartment with an "@" sign.
	 * @param c
	 * @return
	 */
	public Collection<String> getConstituentNamesByCompartment(Compartment c)
	{
		if ( this._constituents == null )
		{
			this._constituents = new ArrayList<String>();
			
			for (String variableName : this.getVariableNames())
			{
				String[] splitString = variableName.split("@", 1);
				if (splitString.length > 1)
				{
					String constituent = splitString[0];
					String compartment = splitString[1];
					if (compartment == c.getName())
					{
						this._constituents.add(constituent);
					}
				}
			}
			
			for(String stoichiometricComponent : 
				this.getStoichiometryAtStdConcentration().keySet() )
			{
				String[] splitString = stoichiometricComponent.split("@", 1);
				if (splitString.length > 1)
				{
					String constituent = splitString[0];
					String compartment = splitString[1];
					if (compartment == c.getName())
					{
						this._constituents.add(constituent);
					}
				}
			}
		}
		return this._constituents;
	}
	
	public void setDimension(Shape shape)
	{
		for (DimName dimName : shape.getDimensionNames())
		{
			if (dimName.toString() == this._fluxDimensionName)
			{
				this._fluxDimension = shape.getDimension(dimName);
			}
		}
	}

}
