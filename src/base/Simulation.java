package base;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import cc.mallet.types.Dirichlet;

public class Simulation {

	/**
	 * The list of agents
	 */
	private ArrayList<Agent> agents = new ArrayList<Agent>();

	/**
	 * The list of personalized ranking models
	 */
	private ArrayList<RelevanceModel> personalization = new ArrayList<RelevanceModel>();

	/**
	 * The network structure
	 */
	private Network network = null;

	private PrintStream detailLog = null;
	private PrintStream avgLog = null;
	
	private int stepCnt = 0;
	
	private String relModel = "";
	
	private TreeMap<Integer,ArrayList<Integer>> tiles = new TreeMap<Integer, ArrayList<Integer>>();
	
	
	
	public Simulation(PrintStream detail, PrintStream aggregate, String relModel) {
		this.detailLog = detail;
		this.avgLog = aggregate;
		this.relModel = relModel;
		
		this.avgLog.println("# Network Size "+GlobalSettings.networkSize);
		this.avgLog.println("# Network Initial Core "+GlobalSettings.coreSize);
		this.avgLog.println("# Network Min Edges "+GlobalSettings.minEdges);
		this.avgLog.println("# Network Tile count "+GlobalSettings.tileCount);
		this.avgLog.println("# Topic count : "+GlobalSettings.topicCount);
		this.avgLog.println("# Topic alpha : "+GlobalSettings.topicAlphaParameter);
		this.avgLog.println("# Vocabulary size : "+GlobalSettings.vocabularySize);
		this.avgLog.println("# Term beta : "+GlobalSettings.termBetaParameter);
		this.avgLog.println("# Personalization: "+this.relModel);
		this.avgLog.println("# Message length: "+GlobalSettings.messageLength);
		this.avgLog.println("# Message no. Chi Prior: "+GlobalSettings.messageLengthChiPrior);
		this.avgLog.println("# Core Lift factor: "+GlobalSettings.coreLiftFactor);
		this.avgLog.println("# Core Topic Probability: "+GlobalSettings.coreTopicProb);
		this.avgLog.println("# Core Mark Prob: "+GlobalSettings.coreRelProb);
		this.avgLog.println("# Tail Mark Prob: "+GlobalSettings.tailRelProb);

		
		this.detailLog.println("# Setting up network");
		this.detailLog.println("# Size "+GlobalSettings.networkSize);
		this.detailLog.println("# Initial Core "+GlobalSettings.coreSize);
		this.detailLog.println("# Min Edges "+GlobalSettings.minEdges);
		long startNet = System.currentTimeMillis();
		this.network = Network.BarabasiAlbertNetwork(GlobalSettings.networkSize, GlobalSettings.coreSize, GlobalSettings.minEdges);
		long stopNet = System.currentTimeMillis();
		this.detailLog.println("t="+(stopNet-startNet));
		
		this.detailLog.println("# Computing network tiles");
		this.detailLog.println("# Tile count "+GlobalSettings.tileCount);
		long startTile = System.currentTimeMillis();
		this.tiles = this.segmentNetwork(GlobalSettings.tileCount);
		long stopTile = System.currentTimeMillis();
		for (Integer tile : this.tiles.keySet()) {
			int tilesize = this.tiles.get(tile).size();
			this.detailLog.println("# Tile "+tile+" size "+tilesize);
			this.avgLog.println("# Tile "+tile+" size "+tilesize);
		}
		this.detailLog.println("t="+(stopTile-startTile));
		
		this.detailLog.println("# Setting up priors");
		this.detailLog.println("# Topic count : "+GlobalSettings.topicCount);
		this.detailLog.println("# Topic alpha : "+GlobalSettings.topicAlphaParameter);
		this.detailLog.println("# Vocabulary size : "+GlobalSettings.vocabularySize);
		this.detailLog.println("# Term beta : "+GlobalSettings.termBetaParameter);
		long startPrior = System.currentTimeMillis();
		Dirichlet topicPrior = new Dirichlet(GlobalSettings.topicCount, GlobalSettings.topicAlphaParameter);
		Dirichlet termPrior = new Dirichlet(GlobalSettings.vocabularySize, GlobalSettings.termBetaParameter);
		TopicDistributions td = new TopicDistributions(termPrior, GlobalSettings.topicCount);
		long stopPrior = System.currentTimeMillis();
		this.detailLog.println("t="+(stopPrior-startPrior));

		this.detailLog.println("# Setting up agents");
		long startAgent = System.currentTimeMillis();
		for (int i = 0; i < this.network.nodeCount(); i++) {
			this.agents.add(new TopicAgent(i, topicPrior, td));
			//this.personalization.add(new RandomRelevanceModel());
		}
		long stopAgent = System.currentTimeMillis();
		this.detailLog.println("t="+(stopAgent-startAgent));
 
		this.detailLog.println("# Setting up personalization (Model = "+this.relModel+")");
		long startRel = System.currentTimeMillis();
		this.resetPersonalization(this.relModel);
		long stopRel = System.currentTimeMillis();
		this.detailLog.println("t="+(stopRel-startRel));

		this.detailLog.println("# Network structure");
		this.network.printNetwork(this.detailLog);

	}
	
	public TreeMap<Integer,ArrayList<Integer>> segmentNetwork(int segments) {
		TreeMap<Integer, ArrayList<Integer>> result = new TreeMap<Integer, ArrayList<Integer>>();
		ArrayList<IndexWeight> nodes = new ArrayList<IndexWeight>();
		double edgeCnt = 0;
		for (int id = 0; id < this.network.nodeCount(); id++) {
			IndexWeight iw = new IndexWeight();
			iw.index = id;
			iw.weight = this.network.getEdges(id).size();
			edgeCnt += iw.weight;
			nodes.add(iw);
		}
		Collections.sort(nodes);
		double tileSize = edgeCnt / segments;
		int tile = 1;
		double tileLimit = tileSize;
		double seenEdges = 0;
		ArrayList<Integer> tileIds = new ArrayList<Integer>();
		for (IndexWeight iw : nodes) {
			seenEdges += iw.weight;
			tileIds.add(iw.index);
			if (seenEdges > tileLimit) {
				result.put(tile, tileIds);
				tile++;
				tileIds = new ArrayList<Integer>();
				tileLimit += tileSize;
			}
		}
		result.put(tile, tileIds);
		return result;
	}
	
	public void resetPersonalization(String model) {
		this.personalization = new ArrayList<RelevanceModel>();
		for (int i = 0; i < this.network.nodeCount(); i++) {
			this.personalization.add(RelevanceModelFactory.initiate(model));
		}
	}
	
	/**
	 * One time step in the simulation
	 */
	public void step() {
		this.stepCnt++;
		this.detailLog.println("# New Step "+this.stepCnt);
		this.detailLog.println("# Message creation and distribution");
		long startCreation = System.currentTimeMillis();
		HashMap<Integer, ArrayList<Message>> incoming = new HashMap<Integer, ArrayList<Message>>();
		for (int nodeId = 0; nodeId < this.network.nodeCount(); nodeId++) {
			Agent a = this.agents.get(nodeId);
			ArrayList<Message> nodeMsgs = a.createMessages(GlobalSettings.messageLength);
			this.detailLog.println(nodeId+"\t"+nodeMsgs.size());
			for (int followerId : network.getEdges(nodeId)) {
				ArrayList<Message> received = new ArrayList<Message>();
				received.addAll(nodeMsgs);
				if (incoming.containsKey(followerId)) {
					incoming.get(followerId).addAll(received);
				} else {
					incoming.put(followerId, received);
				}
			}
		}
		long stopCreation = System.currentTimeMillis();
		this.detailLog.println("t="+(stopCreation-startCreation));
		// All messages created and distributed
		// next: mark relevant ones.
		this.detailLog.println("# Ranking, Marking and Learning");
		long startRanking = System.currentTimeMillis();
		double coreRate = 0;
		double edgeRate = 0;
		double avgPrecision = 0;
		double avgRecall = 0;
		double frac = 0;
		double vocabFrac = 0;
		TreeMap<Integer, Double> tileCoreRate = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> tileEdgeRate = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> tileAvgPrecision = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> tileAvgRecall = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> tileFrac = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> tileVocabFrac = new TreeMap<Integer, Double>();
		for (Integer tile : this.tiles.keySet()) {
			tileCoreRate.put(tile, 0d);
			tileEdgeRate.put(tile, 0d);
			tileAvgPrecision.put(tile, 0d);
			tileAvgRecall.put(tile, 0d);
			tileFrac.put(tile, 0d);
			tileVocabFrac.put(tile, 0d);
		}

		for (int nodeId=0; nodeId < this.network.nodeCount(); nodeId++) {
			Agent a = this.agents.get(nodeId);
			int tileId = this.idToTile(nodeId);
			ArrayList<Message> received = incoming.get(nodeId);
			Collections.shuffle(received);
			HashMap<String, ArrayList<Boolean>> marks = a.markMessages(received);
			// apply relevance ranking
			int[] rankMapping = this.personalization.get(nodeId).rank(received);
			// learn relevance models (update)
			this.personalization.get(nodeId).learn(received, marks.get(Agent.MARKS),
					rankMapping, GlobalSettings.topKMsgPerception);
			HashSet<Integer> seenNodeIds = new HashSet<Integer>();
			HashSet<Integer> seenTermIds = new HashSet<Integer>();
			int seenMsg = Math.min(GlobalSettings.topKMsgPerception, received.size());
			int totMsg = received.size();
			int seenCoreMsg = 0;
			int seenMarkedCnt = 0;
			int allCoreMsg = 0;
			int allMarkedCnt = 0;
			for (int i = 0; i < totMsg; i++) {
				Message msg = received.get(rankMapping[i]);
				if (i < seenMsg) {
					seenNodeIds.add(msg.authorId);
					seenTermIds.addAll(msg.terms);
				}
				if (marks.get(Agent.CORES).get(rankMapping[i])) {
					allCoreMsg++;
					if (i < seenMsg) {
						seenCoreMsg++;
					}
				}
				if (marks.get(Agent.MARKS).get(rankMapping[i])) {
					allMarkedCnt++;
					if (i < seenMsg) {
						seenMarkedCnt++;
					}
				}
			}
			int seenNodeCnt = seenNodeIds.size();
			int seenTermCnt = seenTermIds.size();
			int edges = this.network.getEdges(nodeId).size();
			this.detailLog.println(nodeId+"\t"+totMsg+"\t"+seenMsg+"\t"+seenCoreMsg+"\t"+seenMarkedCnt+"\t"+edges+"\t"+seenNodeCnt);
			
			frac = ((double) seenMsg) / totMsg;
			double tFrac = tileFrac.get(tileId);
			tFrac += frac;
			tileFrac.put(tileId, tFrac);

			vocabFrac = ((double) seenTermCnt) / GlobalSettings.vocabularySize;
			double tVocabFrac = tileVocabFrac.get(tileId);
			tVocabFrac += vocabFrac;
			tileVocabFrac.put(tileId, tVocabFrac);

			if (seenMsg > 0) {
				double rate = ((double) seenCoreMsg) / seenMsg;
				double prec = ((double) seenMarkedCnt) / seenMsg;
				coreRate += rate;
				avgPrecision += prec;
				double tileRate = tileCoreRate.get(tileId);
				tileRate += rate;
				tileCoreRate.put(tileId, tileRate);
				double tilePrec = tileAvgPrecision.get(tileId);
				tilePrec += prec;
				tileAvgPrecision.put(tileId, tilePrec);
			}
			if (edges > 0) {
				double rate = ((double) seenNodeCnt) / edges;
				edgeRate += rate;
				double tileRate = tileEdgeRate.get(tileId);
				tileRate += rate;
				tileEdgeRate.put(tileId, tileRate);
			}
			if (allMarkedCnt > 0) {
				double rec =  ((double) seenMarkedCnt) / allMarkedCnt;
				avgRecall += rec;
				double tileRec = tileAvgRecall.get(tileId);
				tileRec += rec;
				tileAvgRecall.put(tileId, tileRec);
			}
		}
		coreRate /= this.network.nodeCount();
		edgeRate /= this.network.nodeCount();
		avgPrecision /= this.network.nodeCount();
		avgRecall /= this.network.nodeCount();
		long stopRanking = System.currentTimeMillis();
		this.detailLog.println("Avg\t"+coreRate+"\t"+edgeRate+"\t"+avgPrecision+"\t"+avgRecall);
		this.detailLog.println("t="+(stopCreation-startCreation));
		this.avgLog.println(this.stepCnt+"\tAvg\tCore "+coreRate+"\tEdge "+edgeRate+"\tVocFrac "+vocabFrac+"\tFrac "+frac+"\tPrec "+avgPrecision+"\tRec "+avgRecall);
		System.out.println(this.stepCnt+" Avg\tCore "+coreRate+"\tEdge "+edgeRate+"\tVocFrac "+vocabFrac+"\tFrac "+frac+"\tPrec "+avgPrecision+"\tRec "+avgRecall);
		for (Integer tile : tileAvgRecall.keySet()) {
			int tilesize = this.tiles.get(tile).size();
			double tRec = tileAvgRecall.get(tile) / tilesize;
			double tCoreRate = tileAvgPrecision.get(tile) / tilesize;
			double tPrec = tileAvgPrecision.get(tile) / tilesize;
			double tEdgeRate = tileEdgeRate.get(tile) / tilesize;
			double tFrac = tileFrac.get(tile) / tilesize;
			double tVocabFrac = tileVocabFrac.get(tile) / tilesize;
			this.avgLog.println(this.stepCnt+"\tTile "+tile+"\tCore "+tCoreRate+"\tEdge "+tEdgeRate+"\tVocFrac "+tVocabFrac+"\tFrac "+tFrac+"\tPrec "+tPrec+"\tRec "+tRec);
		}

	}
	
	public int idToTile(int nodeId) {
		for (int tile : this.tiles.keySet()) {
			if (this.tiles.get(tile).contains(nodeId)) {
				return tile;
			}
		}
		return 0;
	}
}