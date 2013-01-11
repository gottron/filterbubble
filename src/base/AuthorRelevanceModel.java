package base;

import java.util.ArrayList;

public class AuthorRelevanceModel extends RelevanceModel {

	/**
	 * Count how often an author wrote a relevant (marked) message
	 */
	private int[] relCount = null;
	/**
	 * Count how often an author wrote an irrelevant (seen but not marked)
	 * message
	 */
	private int[] irrelCount = null;
	/**
	 * Smoothing factor for computing the probabilities. Will be added to the
	 * relevant and irrelevant count for each author
	 */
	private double smoothingFactor = 1;

	/**
	 * Initialize relevance Model over a network of a given size.
	 * 
	 * @param vocabularySize
	 */
	public AuthorRelevanceModel(int networkSize) {
		this.relCount = new int[networkSize];
		this.irrelCount = new int[networkSize];
	}


	
	@Override
	public void learn(ArrayList<Message> msgs, ArrayList<Boolean> marks,
			int[] rankMap, int topK) {
		for (int i = 0; i < Math.min(topK, rankMap.length); i++) {
			Message msg = msgs.get(rankMap[i]);
			if (marks.get(rankMap[i])) {
				// relevant Message
				this.relCount[msg.authorId]++;
			} else {
				// irrelevant Message
				this.irrelCount[msg.authorId]++;
			}
		}
	}

	@Override
	public double weight(Message msg) {
		double result = 0;
		double authorRel = (this.relCount[msg.authorId] + smoothingFactor)
					/ (this.relCount[msg.authorId] + this.irrelCount[msg.authorId] + 2 * smoothingFactor);
		result += Math.log(authorRel);
		return result;
	}

}
