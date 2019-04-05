package agent.predicate;

/**
 * Predicate class to determine whether a cell is epithelial. Agent templates
 * used in epithelial layer spawner must have this tag.
 */

import java.util.function.Predicate;
import agent.Agent;
import referenceLibrary.AspectRef;


public class IsEpithelial implements Predicate<Agent> {

	@Override
	public boolean test(Agent agent){
		return isEpithelial(agent);
	}
	
	public static boolean isEpithelial(Agent agent) {
		return ( agent.get(AspectRef.isEpithelial) != null ) && 
				( agent.getBoolean(AspectRef.isEpithelial) );
	}
}
