package aspect.calculated;

import aspect.AspectInterface;
import java.util.HashMap;

import aspect.Aspect;
import aspect.Calculated;
import expression.Expression;
import expression.Unit;

public class NumWithUnit extends Calculated {

	
	private double value;
	private boolean calculated = false;
	private Expression expression;
	private Unit unit;
	private HashMap<String, Double> variables = new HashMap<String, Double>();
	
	
	@Override
	public void setInput(String input)
	{
		this._input = input;
		this.expression = new Expression( input.replaceAll("\\s+","") );
		this.unit = new Unit(input);
	}
	
	@Override
	public Object get(AspectInterface aspectOwner) {
		if (calculated)
		{
			return value;
		}
		else
		{
			value = expression.getValue(variables);
			calculated = true;
			return value;
		}
	}

}
