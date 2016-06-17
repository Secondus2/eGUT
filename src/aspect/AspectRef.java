package aspect;

/**
 * \brief Aspect name references.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AspectRef
{
	/**
	 * Agent state references
	 */

	/**
	 * The time when an agent was born.
	 */
	public static String birthday = "birthday";
	
	/**
	 * The time at which an agent died.
	 */
	public static String deathday = "deathday";
	
	/**
	 * TODO
	 */
	public static String agentBody = "body";

	/**
	 * TODO
	 */
	public static String agentPulldistance = "pullDistance";
	
	/**
	 * TODO
	 */
	public static String agentPullStrength = "pullStrength";
	
	/**
	 * TODO
	 */
	public static String bodyRadius = "radius";

	/*
	 * TODO
	 */
	public static String bodyLength = "#bodyLength";
	
	/**
	 * TODO
	 */
	public static String isLocated = "#isLocated";
	
	/**
	 * TODO
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

	/**
	 * TODO
	 */
	public static String agentMass = "mass";
	
	/**
	 * Agent mass that should trigger division.
	 */
	public static String divisionMass = "divisionMass";
	/**
	 * 
	 */
	public static String mumMassFrac = "mumMassFrac";
	/**
	 * 
	 */
	public static String mumMassFracCV = "mumMassFracCV";
	
	/**
	 * TODO
	 */
	public static String agentLinks = "linkedAgents";

	/**
	 * TODO
	 */
	public static String linkerDistance = "linkerDist";

	/**
	 * TODO
	 */
	public static String agentUpdateBody = "updateBody";

	/**
	 * TODO
	 */
	public static String agentDivide = "divide";

	/**
	 * TODO
	 */
	public static String agentVolumeDistributionMap = "volumeDistribution";

	/**
	 * TODO
	 */
	public static String agentDensity = "density";

	/**
	 * TODO
	 */
	public static String agentVolume = "volume";

	/**
	 * TODO
	 */
	public static String internalProducts = "internalProducts";

	/**
	 * TODO
	 */
	public static String internalProduction = "produce";
	
	/**
	 * TODO
	 */
	public static String productEPS = "eps";

	/**
	 * TODO
	 */
	public static String maxInternalEPS = "maxInternalEPS";

	/**
	 * TODO
	 */
	public static String epsSpecies = "epsSpecies";

	/**
	 * TODO
	 */
	public static String internalProductionRate = "internalProduction";

	/**
	 * Reference tag for the growth event.
	 */
	// NOTE This may be merged with internalProduction.
	public static String growth = "growth";
	
	/**
	 * TODO
	 */
	public static String growthRate = "specGrowthRate";

	/**
	 * TODO
	 */
	public static String agentPreferencedistance = "prefDist";

	/**
	 * TODO
	 */
	public static String agentPreferenceIdentifier = "prefIdentifier";

	/**
	 * TODO
	 */
	public static String agentAttachmentPreference = "preference";

	/**
	 * TODO
	 */
	public static String agentCurrentPulldistance = "#curPullDist";

	/**
	 * TODO
	 */
	public static String agentStochasticStep = "stochasticStep";

	/**
	 * TODO
	 */
	public static String agentStochasticDirection = "stochasticDirection";

	/**
	 * TODO
	 */
	public static String agentStochasticPause = "stochasticPause";

	/**
	 * TODO
	 */
	public static String agentStochasticDistance = "stochasticDistance";

	/**
	 * TODO
	 */
	public static String agentDivision = "divide";

	/**
	 * 
	 */
	public static String collisionSearchDistance = "searchDist";

	/**
	 * TODO
	 */
	public static String collisionPullEvaluation = "evaluatePull";

	/**
	 * TODO
	 */
	public static String collisionCurrentPullDistance = "#curPullDist";

	/**
	 * TODO
	 */
	public static String collisionBaseDT = "dtBase";

	/**
	 * TODO
	 */
	public static String collisionMaxMOvement = "maxMovement";

	/**
	 * TODO
	 */
	public static String agentStochasticMove = "stochasticMove";

	/**
	 * TODO
	 */
	public static String collisionRelaxationMethod = "relaxationMethod";

	/**
	 * TODO
	 */
	public static String agentExcreteEps = "epsExcretion";

	/**
	 * Used by RefreshMassGrids, calls event
	 */
	public static String massToGrid = "massToGrid";

	/**
	 * TODO
	 */
	public static String biomass = "biomass";

	/**
	 * TODO
	 */
	public static String soluteNames = "soluteNames";

	/**
	 * TODO
	 */
	public static String solver = "solver";

	/**
	 * TODO
	 */
	public static String solverhMax = "hMax";

	/**
	 * TODO
	 */
	public static String solverTolerance = "tolerance";
	
	/**
	 * TODO
	 */
	public static String agentPigment = "pigment";

	/**
	 * TODO
	 */
	public static String gridArrayType = "arrayType";

	/**
	 * TODO
	 */
	public static String visualOutMaxValue = "maxConcentration";

	/**
	 * TODO
	 */
	public static String soluteName= "solute";

	/**
	 * TODO
	 */
	public static String filePrefix = "prefix";

	/**
	 * TODO
	 */
	public static String graphicalOutputWriter = "outputWriter";
}
