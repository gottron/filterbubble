package base;

import java.util.ArrayList;

public class RandomRelevanceModel extends  RelevanceModel {

	@Override
	public void learn(ArrayList<Message> msgs, ArrayList<Boolean> marks,
			int[] rankMap, int topK) {
		// no need to learn anything ...
	}

	@Override
	public double weight(Message msg) {
		return Math.random();
	}

}
