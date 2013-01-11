package eval;

public class Measurement {

	/**
	 * The ratio of core interest messages in the seen messages
	 */
	public double coreRatio = 0;
	public final static String coreRatioPrefix = "Core ";
	/**
	 * The ratio of edges of the personal network from which messages were received
	 */
	public double edgeRatio = 0;
	public final static String edgeRatioPrefix = "Edge ";
	/**
	 * The ratio of the global vocabulary that has been seen in the received messages 
	 */
	public double vocabularFraction = 0;
	public final static String vocabularFractionPrefix = "VocFrac ";
	/**
	 * Faction of seen message over all received messages 
	 */
	public double seenFraction = 0;
	public final static String seenFractionPrefix = "Frac ";
	/**
	 * Precision (Faction of marked messages in the seen messages)
	 */
	public double precision = 0;
	public final static String precisionPrefix = "Prec ";
	/**
	 * Recall (Fraction of marked seen messages over all marked messages)
	 */
	public double recall = 0;
	public final static String recallPrefix = "Rec ";
	
	
	
}
