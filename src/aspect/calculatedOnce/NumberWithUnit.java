
package aspect.calculatedOnce;

import java.util.HashMap;

import org.w3c.dom.Element;

import aspect.Aspect;
import aspect.AspectInterface;
import aspect.CalculatedOnce;
import dataIO.XmlHandler;
import expression.Expression;
import referenceLibrary.XmlRef;
import settable.Settable;
import utility.Helper;

public class NumberWithUnit extends CalculatedOnce {

	private Expression expression;
	private HashMap<String, Double> variables = new HashMap<String, Double>();
	private double value;

	
	@Override
	public void instantiate(Element xmlElem, Settable parent)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, XmlRef.inputAttribute);
		if (input != "")
			this.setInput(input);
		else
			this.setInput(Helper.obtainInput( "", "expression" ));
		
		this.calculateValue();
		this.redirect(xmlElem);		
	}
	
	@Override
	public void setInput(String input)
	{
		this._input = input;
		this.expression = new Expression( input.replaceAll("\\s+","") );
	}
	
	@Override
	public Object copy() {
		// TODO Auto-generated method stub
		return null;
	}

	public void calculateValue() {
		this.set((expression.getValue(variables)), this.key );
	}

	@Override
	public void setValue() {
		this.value = expression.getValue(variables);
		
	}

}

