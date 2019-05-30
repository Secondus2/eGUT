package aspect.event;

import aspect.AspectInterface;
import aspect.Event;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

public class RandomDifferentiation extends Event {

	public String DIFF_VARIABLE = AspectRef.randomDifferentiationVariable;
	
	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		double randomProportion = ExtraMath.getUniRandDbl();
		initiator.set(DIFF_VARIABLE, randomProportion);
	}
}
