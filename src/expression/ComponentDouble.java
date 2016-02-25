package expression;

import java.util.HashMap;
import java.util.List;

import dataIO.Log;
import static dataIO.Log.tier.CRITICAL;

/**
 * \brief A component of a mathematical expression composed of strictly two
 * sub-components.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ComponentDouble extends Component
{
	/**
	 * {@code String} description of the expression 
	 */
	protected String _expr;
	
	/**
	 * One of the two sub-components.
	 */
	protected Component _a, _b;
	
	/**
	 * \brief Construct a component of a mathematical expression from two
	 * sub-components.
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-components.
	 */
	public ComponentDouble(Component a, Component b)
	{
		this._a = a;
		this._b = b;
	}
	
	@Override
	public String getName()
	{
		return ( isNegative() ? "-(" : "(") + this._a.getName() + this._expr + 
				this._b.getName() + ")";
	}
	
	@Override
	public String reportEvaluation(HashMap<String, Double> variables)
	{
		String out = this._a.reportEvaluation(variables) + this._expr +
											this._b.reportEvaluation(variables);
		return ( isNegative() ) ? "-("+out+")" : out;
	}
	
	@Override
	public void appendVariablesNames(List<String> names)
	{
		this._a.appendVariablesNames(names);
		this._b.appendVariablesNames(names);
	}
	
	/**
	 * \brief Helper method for sub-classes that may encounter infinite values.
	 * 
	 * @param variables Dictionary of variable names with associated values
	 * that triggered the infinite value.
	 */
	protected void infiniteValueWarning(HashMap<String, Double> variables)
	{
		Log.out(CRITICAL,"WARNING! Infinite value: " + this.getName() + 
									" = " + this.reportEvaluation(variables));
	}
}