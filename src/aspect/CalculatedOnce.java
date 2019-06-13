
package aspect;

import org.w3c.dom.Element;

import dataIO.XmlHandler;
import generalInterfaces.Copyable;
import generalInterfaces.Redirectable;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Settable;


/**
 * The CalculatedOnce aspects are defined with an input and a class for calculation
 * After this initial calculation, they behave exactly like primary aspects.
 * @author trf896
 *
 */

public abstract class CalculatedOnce extends Aspect
	implements Copyable, Instantiable, Redirectable {
	
	/**
	 * input string
	 */

	protected String _input;
	
	/**
	 * StateExpressions require an input string to set the expression
	 * @param input
	 */

	public void setInput(String input)
	{
		this._input = input;
	}
	
	/**
	 * returns the input String array of this state
	 * @return
	 */

	public String getInput()
	{
		return _input;
	}
	
	public void instantiate(Element xmlElem, Settable parent)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, XmlRef.inputAttribute);
		if (input != "")
			this.setInput(input);
		
		this.redirect(xmlElem);		
	}
	
	public abstract void calculateValue();
	
	private void init(String input) {
		this.setInput(input);
	}
	
	/**
	 * Set the PRIMARY state of the aspect
	 */

	public abstract void setValue();

	
}

