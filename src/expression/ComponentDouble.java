package expression;

import static dataIO.Log.Tier.CRITICAL;

import java.util.Collection;
import java.util.Map;

import dataIO.Log;

/**
 * \brief A component of a mathematical expression composed of strictly two
 * sub-components.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ComponentDouble extends ComponentNumerical
{
	/**
	 * {@code String} description of the expression 
	 */
	protected String _expr;
	
	/**
	 * One of the two sub-components.
	 */
	protected ComponentNumerical _a, _b;
	
	/**
	 * \brief Construct a component of a mathematical expression from two
	 * sub-components.
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-components.
	 */
	public ComponentDouble(ComponentNumerical a, ComponentNumerical b)
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
	public String reportEvaluation(Map<String, Double> variables)
	{
		String out = this._a.reportEvaluation(variables) + this._expr +
											this._b.reportEvaluation(variables);
		return ( isNegative() ) ? "-("+out+")" : out;
	}
	
	@Override
	public void appendVariablesNames(Collection<String> names)
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
	protected void infiniteValueWarning(Map<String, Double> variables)
	{
		Log.out(CRITICAL,"WARNING! Infinite value: " + this.getName() + 
									" = " + this.reportEvaluation(variables));
	}
}