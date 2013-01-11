package base;

import cc.mallet.util.Randoms;

public class GlobalSettings {

	public static int messageLength = 10;
	public static int vocabularySize = 10000;
	public static double termBetaParameter = 0.001;
	public static int topicCount = 100;
	public static double topicAlphaParameter = 0.01;
	public static int networkSize = 20000;
	public static int coreSize = 10;
	public static int minEdges = 5;
	public static Randoms random = new Randoms();
	public static int topKMsgPerception = 10;
	// Stats Twitter: Average number of tweets per day per user (in 2011): .56
	// http://www.quora.com/Twitter-1/What-is-the-tweet-total-of-a-typical-Twitter-user
	public static double messageLengthChiPrior = .56; 
	public static double coreLiftFactor = 2;
	public static double coreTopicProb = 0.8;
	public static double coreRelProb = 0.99;
	public static double tailRelProb = 0.01;
	/**
	 * number of tiles for network stratification
	 */
	public static int tileCount = 3;


}
