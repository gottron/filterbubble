package base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cc.mallet.types.Dirichlet;
import cc.mallet.types.Multinomial;
import cc.mallet.util.Maths;

public class TopicAgent extends AbstractAgent {

	private Multinomial topicDistribution = null;
	
	private TopicDistributions topics = null;
	
	private double msgCountPrior = 0;
	
	private int[] coreTopics = null;
	
	public TopicAgent(int id, Dirichlet alphaPrior, TopicDistributions td) {
		super(id);
		this.topicDistribution = new Multinomial(alphaPrior.nextDistribution());
		this.topics = td;
		this.msgCountPrior = GlobalSettings.random.nextChiSq(GlobalSettings.messageLength);
		ArrayList<IndexWeight> topicWeights = new ArrayList<IndexWeight>();
		double[] p = this.topicDistribution.getValues();
		for (int i = 0; i < p.length; i++) {
			IndexWeight tw = new IndexWeight();
			tw.weight = p[i];
			tw.index = i;
			topicWeights.add(tw);
		}
		Collections.sort(topicWeights);
		double sum = 0;
		int cnt = 0;
		while(sum < GlobalSettings.coreTopicProb) {
			sum += topicWeights.get(cnt).weight;
			cnt++;
		}
		this.coreTopics = new int[cnt];
		for (int i = 0; i< this.coreTopics.length; i++) {
			this.coreTopics[i] = topicWeights.get(i).index;
		}
//		System.out.println("Main topic cnt:" + cnt);
	}
	
	@Override
	public boolean isCoreTopic(Message msg) {
		double prob = this.msgProbability(msg);
		double coreProb = this.msgCoreProbability(msg);
		double tailProb = prob - coreProb;
		coreProb = coreProb / GlobalSettings.coreTopicProb;
		tailProb = tailProb / (1- GlobalSettings.coreTopicProb);
		return coreProb > tailProb*GlobalSettings.coreLiftFactor;
	}

	@Override
	public ArrayList<Message> createMessages(int length) {
		ArrayList<Message> result = new ArrayList<Message>();
		int msgCount = GlobalSettings.random.nextPoisson(this.msgCountPrior);
		for (int i = 0; i < msgCount; i++) {
			Message msg = new Message();
			msg.authorId = this.getId();
			for (int term = 0; term < length; term++) {
				int topicId = this.topicDistribution.randomIndex(GlobalSettings.random);
				int termId = this.topics.topicDistrib[topicId].randomIndex(GlobalSettings.random);
				msg.terms.add(termId);
			}
			result.add(msg);
		}
		return result;
	}

	@Override
	public HashMap<String, ArrayList<Boolean>> markMessages(ArrayList<Message> messages) {
		HashMap<String, ArrayList<Boolean>> result = new HashMap<String, ArrayList<Boolean>>();
		ArrayList<Boolean> mark = new ArrayList<Boolean>();
		ArrayList<Boolean> core = new ArrayList<Boolean>();
		for (int index = 0; index < messages.size(); index++) {
			boolean marked = false;
			boolean iscore = this.isCoreTopic(messages.get(index)); 
			if (iscore) {
				marked = Math.random() < GlobalSettings.coreRelProb;
			} else {
				marked = Math.random() < GlobalSettings.tailRelProb;
			}
			mark.add(index, marked);
			core.add(index, iscore);
		}
		result.put(Agent.MARKS, mark);
		result.put(Agent.CORES, core);
		return result;
	}
	
	public double msgProbability(Message msg) {
		double prob = 0;
		for (int topicId = 0; topicId < this.topicDistribution.size(); topicId++) {
			double prior = this.topicDistribution.probability(topicId);
			for (Integer termId : msg.terms) {
				prob += prior * this.topics.topicDistrib[topicId].probability((int) termId);
			}
		}
		prob /= msg.terms.size();
		return prob;
	}
	
	public double msgCoreProbability(Message msg) {
		double prob = 0;
		for (int coreTopicIndex = 0; coreTopicIndex < this.coreTopics.length; coreTopicIndex++) {
			int topicId = this.coreTopics[coreTopicIndex];
			double prior = this.topicDistribution.probability(topicId);
			for (Integer termId : msg.terms) {
				prob += prior * this.topics.topicDistrib[topicId].probability((int) termId);
			}
		}
		prob /= msg.terms.size();
		return prob;
	}
	
	public double randomProb(Message msg) {
		double prob = 0;
		for (int topicId = 0; topicId < this.topicDistribution.size(); topicId++) {
			double prior =1f/GlobalSettings.topicCount;
			for (Integer termId : msg.terms) {
				prob += prior * this.topics.topicDistrib[topicId].probability((int) termId);
			}
		}
		prob /= msg.terms.size();
		return prob;
	}

	public double klDivergence(TopicAgent other) {
		return Maths.klDivergence(this.topicDistribution.getValues(), other.topicDistribution.getValues());
	}
	
}
