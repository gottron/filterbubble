package eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

public class LogResults {

	// # Network Size 10000
	public int networkSize = 0;
	public final static String networkSizePrefix = "# Network Size ";
	// # Network Initial Core 10
	public int networkInitialCore = 0;
	public final static String networkInitialCorePrefix = "# Network Initial Core ";
	// # Network Min Edges 5
	public int networkMinEdges = 0;
	public final static String networkMinEdgesPrefix = "# Network Min Edges ";
	// # Network Tile count 3
	public int networkTileCount = 0;
	public final static String networkTileCountPrefix = "# Network Tile count ";
	// # Topic count : 100
	public int topicCount = 0;
	public final static String topicCountPrefix = "# Topic count : ";
	// # Topic alpha : 0.01
	public double topicAlpha = 0;
	public final static String topicAlphaPrefix = "# Topic alpha : ";
	// # Vocabulary size : 10000
	public int vocabularySize = 0;
	public final static String vocabularySizePrefix = "# Vocabulary size : ";
	// # Term beta : 0.0010
	public double termBeta = 0;
	public final static String termBetaPrefix = "# Term beta : ";
	// # Personalization: term
	public String personalization = "";
	public final static String personalizationPrefix = "# Personalization: ";
	// # Message length: 10
	public int messageLength = 0;
	public final static String messageLengthPrefix = "# Message length: ";
	// # Message no. Chi Prior: 0.56
	public double messageNoChiPrior = 0; 
	public final static String messageNoChiPriorPrefix = "# Message no. Chi Prior: ";
	// # Core Lift factor: 2.0
	public double coreLiftFactor = 0;
	public final static String coreLiftFactorPrefix = "# Core Lift factor: ";
	// # Core Topic Probability: 0.8
	public double coreTopicProbability = 0;
	public final static String coreTopicProbabilityPrefix = "# Core Topic Probability: ";
	// # Core Mark Prob: 0.8
	public double coreMarkProbability = 0;
	public final static String coreMarkProbabilityPrefix = "# Core Mark Prob: ";
	// # Tail Mark Prob: 0.4
	public double tailMarkProbability = 0;
	public final static String tailMarkProbabilityPrefix = "# Tail Mark Prob: ";
	// # Tile 1 size 1684
	// # Tile 2 size 3032
	// # Tile 3 size 5284
	public TreeMap<Integer,Integer> tileSizes = new TreeMap<Integer, Integer>();
	public final static String tailPrefix = "# Tail ";
	public final static String tailInfix = " size ";
			
	public TreeMap<Integer, TreeMap<Integer, Measurement>> tileMetrics = new TreeMap<Integer, TreeMap<Integer,Measurement>>();

	public static LogResults fromFile(File fin) {
		LogResults result = new LogResults();
		try {
			BufferedReader in = new BufferedReader(new FileReader(fin));
			String line = null;
			boolean intro = true;
			while ( (line = in.readLine()) != null) {
				if (intro) {
					if (line.startsWith(LogResults.networkSizePrefix)) {
						result.networkSize = Integer.parseInt(line.substring(LogResults.networkSizePrefix.length()));
					} else if (line.startsWith(LogResults.networkInitialCorePrefix)) {
						result.networkInitialCore = Integer.parseInt(line.substring(LogResults.networkInitialCorePrefix.length()));
					} else if (line.startsWith(LogResults.networkMinEdgesPrefix)) {
						result.networkMinEdges = Integer.parseInt(line.substring(LogResults.networkMinEdgesPrefix.length()));
					} else if (line.startsWith(LogResults.topicCountPrefix)) {
						result.topicCount = Integer.parseInt(line.substring(LogResults.topicCountPrefix.length()));
					} else if (line.startsWith(LogResults.topicAlphaPrefix)) {
						result.topicAlpha = Double.parseDouble(line.substring(LogResults.topicAlphaPrefix.length()));
					} else if (line.startsWith(LogResults.vocabularySizePrefix)) {
						result.vocabularySize = Integer.parseInt(line.substring(LogResults.vocabularySizePrefix.length()));
					} else if (line.startsWith(LogResults.termBetaPrefix)) {
						result.termBeta = Double.parseDouble(line.substring(LogResults.termBetaPrefix.length()));
					} else if (line.startsWith(LogResults.personalizationPrefix)) {
						result.personalization = line.substring(LogResults.personalizationPrefix.length());
					} else if (line.startsWith(LogResults.messageLengthPrefix)) {
						result.messageLength = Integer.parseInt(line.substring(LogResults.messageLengthPrefix.length()));
					} else if (line.startsWith(LogResults.messageNoChiPriorPrefix)) {
						result.messageNoChiPrior = Double.parseDouble(line.substring(LogResults.messageNoChiPriorPrefix.length()));
					} else if (line.startsWith(LogResults.coreLiftFactorPrefix)) {
						result.coreLiftFactor = Double.parseDouble(line.substring(LogResults.coreLiftFactorPrefix.length()));
					} else if (line.startsWith(LogResults.coreTopicProbabilityPrefix)) {
						result.coreTopicProbability = Double.parseDouble(line.substring(LogResults.coreTopicProbabilityPrefix.length()));
					} else if (line.startsWith(LogResults.coreMarkProbabilityPrefix)) {
						result.coreMarkProbability = Double.parseDouble(line.substring(LogResults.coreMarkProbabilityPrefix.length()));
					} else if (line.startsWith(LogResults.tailMarkProbabilityPrefix)) {
						result.tailMarkProbability = Double.parseDouble(line.substring(LogResults.tailMarkProbabilityPrefix.length()));
					} else if (line.startsWith(LogResults.tailPrefix)) {
						String frags[] = line.substring(LogResults.coreLiftFactorPrefix.length()).split(LogResults.tailInfix);
						int tile = Integer.parseInt(frags[0]);
						int size = Integer.parseInt(frags[1]);
						result.tileSizes.put(tile, size);
					} else if (!line.startsWith("#")) {
						intro = false;
					}
				}
				if (!intro) {
					String[] frags = line.split("\\t");
					int iteration = Integer.parseInt(frags[0]);
					int tile = 0;
					if (frags[1].startsWith("Tile ")) {
						tile = Integer.parseInt(frags[1].substring(5));
					}
					if (! result.tileMetrics.containsKey(tile)) {
						result.tileMetrics.put(tile, new TreeMap<Integer, Measurement>());
					}
					Measurement m = new Measurement();
					for (int i = 2; i < frags.length; i++) {
						if (frags[i].startsWith(Measurement.coreRatioPrefix)) {
							m.coreRatio = Double.parseDouble(frags[i].substring(Measurement.coreRatioPrefix.length()));
						} else if (frags[i].startsWith(Measurement.edgeRatioPrefix)) {
							m.edgeRatio = Double.parseDouble(frags[i].substring(Measurement.edgeRatioPrefix.length()));
						} else if (frags[i].startsWith(Measurement.vocabularFractionPrefix)) {
							m.vocabularFraction = Double.parseDouble(frags[i].substring(Measurement.vocabularFractionPrefix.length()));
						} else if (frags[i].startsWith(Measurement.seenFractionPrefix)) {
							m.seenFraction = Double.parseDouble(frags[i].substring(Measurement.seenFractionPrefix.length()));
						} else if (frags[i].startsWith(Measurement.precisionPrefix)) {
							m.precision = Double.parseDouble(frags[i].substring(Measurement.precisionPrefix.length()));
						} else if (frags[i].startsWith(Measurement.recallPrefix)) {
							m.recall = Double.parseDouble(frags[i].substring(Measurement.recallPrefix.length()));
						}
					}
					result.tileMetrics.get(tile).put(iteration, m);
				}
			}
			in.close();
		} catch (IOException ioe) {
			
		} 
		return result;
	}
	
}
