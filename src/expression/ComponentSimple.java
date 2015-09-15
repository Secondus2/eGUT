/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public abstract class ComponentSimple extends Component
{
	protected String _name;
	
	/**\brief TODO
	 * 
	 */
	public ComponentSimple(String name)
	{
		this._name = name;
	}
	
	public String getName()
	{
		String out = this._name;
		return ( isNegative() ) ? "-"+out : out;
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		double out = this.getValue(variables);
		if ( isNegative() )
			if ( out < 0 )
				return Double.toString(-out);
			else
				return "-"+Double.toString(-out);
		return Double.toString(out);
	}
}