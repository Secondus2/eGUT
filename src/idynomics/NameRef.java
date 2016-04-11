package idynomics;

/**
 * \brief Aspect name references.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class NameRef
{
	/**
	 * Agent state references
	 */
	
	/**
	 * 
	 */
	public static String agentBody = "body";

	/**
	 * 
	 */
	public static String agentPulldistance = "pullDistance";
	
	/**
	 * 
	 */
	public static String agentPullStrength = "pullStrength";
	
	/**
	 * 
	 */
	public static String bodyRadius = "radius";

	/**
	 * 
	 */
	public static String bodyLength = "#bodyLength";
	
	/**
	 * 
	 */
	public static String isLocated = "#isLocated";
	
	/**
	 * 
	 */
	public static String bodyUpdate = "updateBody";
	
	/**
	 * NOTE: linker springs will be reworked later, subject to change.
	 */
	public static String filialLinker = "filialLinker";

	/**
	 * List with all surface objects associated with the object
	 */
	public static String surfaceList = "surfaces";

	/**
	 * the solute name for the default BiomassGrid (the grid in which all 
	 * biomass/biofilm is represented
	 */
	public static String defaultBiomassGrid = "biomass";
	
	/**
	 * list with reactions owned by the agent
	 */
	// FIXME what is the difference between this and XmlLabel.reactions?
	public static String agentReactions = "reactions";
}
