package base;

import java.util.ArrayList;

public class TermRelevanceModel extends RelevanceModel {

	/**
	 * Count how often a term appeared in a relevant (marked) message
	 */
	private int[] relCount = null;
	/**
	 * Count how often a term appeared in a irrelevant (seen but not marked)
	 * message
	 */
	private int[] irrelCount = null;
	/**
	 * Smoothing factor for computing the probabilities. Will be added to the
	 * relevant and irrelevant count for each term
	 */
	private double smoothingFactor = 1;

	/**
	 * Initialize relevance Model over a vocabulary of given size.
	 * 
	 * @param vocabularySize
	 */
	public TermRelevanceModel(int vocabularySize) {
		this.relCount = new int[vocabularySize];
		this.irrelCount = new int[vocabularySize];
	}

	/**
	 * Weighing for a message. Inspired by the BIM. The weights are defined by
	 * the terms. Each term is associated to a probability of appearing in a
	 * relevant message (number of appearances in relevant messags divided by
	 * total number of messages). The result is the log probability (sum of logs).
	 */
	public double weight(Message msg) {
		double result = 0;
		for (int termId : msg.terms) {
			double termRel = (this.relCount[termId] + smoothingFactor)
					/ (this.relCount[termId] + this.irrelCount[termId] + 2 * smoothingFactor);
			result += Math.log(termRel);
		}
		return result;
	}

	@Override
	public void learn(ArrayList<Message> msgs, ArrayList<Boolean> marks,
			int[] rankMap, int topK) {
		for (int i = 0; i < Math.min(topK, rankMap.length); i++) {
			Message msg = msgs.get(rankMap[i]);
			if (marks.get(rankMap[i])) {
				// relevant Message
				for (int termId : msg.terms) {
					this.relCount[termId]++;
				}
			} else {
				// irrelevant Message
				for (int termId : msg.terms) {
					this.irrelCount[termId]++;
				}
			}
		}

	}

}
