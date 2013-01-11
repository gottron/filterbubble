package base;

import java.util.ArrayList;
import java.util.Collections;

public abstract class RelevanceModel {

	/**
	 * Update the relevance model learned from messages and feedback marks of an agent.
	 * The ranking is included as a mapping
	 * The number of actually considered (seen) messages is passed a s parameter.
	 * 
	 * @param msgs Actual messages
	 * @param marks Actual relevance marks of the users
	 * @param rankMap the mapping from the messages to a ranking
	 * @param topK the number of messages actually to be considered for learning.
	 */
	public abstract void learn(ArrayList<Message> msgs, ArrayList<Boolean> marks, int[] rankMap, int topK);

	/**
	 * Create a ranking mapping for incoming messages. The resulting array contains the ArayList entry number of the highest ranked Message as first entry. 
	 * @param msgs
	 * @return
	 */
	public int[] rank(ArrayList<Message> msgs) {
		int[] rankMap = new int[msgs.size()];
		ArrayList<IndexWeight> rankWeights = new ArrayList<IndexWeight>();
		for (int index = 0; index < msgs.size(); index++) {
			Message msg = msgs.get(index);
			double w = this.weight(msg);
			IndexWeight mw = new IndexWeight();
			mw.index = index;
			mw.weight = w;
			rankWeights.add(mw);
		}
		Collections.sort(rankWeights);
		for (int i = 0; i < rankMap.length; i++) {
			rankMap[i] = rankWeights.get(i).index;
		}
		return rankMap;
	}
	
	public abstract double weight(Message msg);

}