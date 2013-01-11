package base;

import cc.mallet.types.Dirichlet;
import cc.mallet.types.Multinomial;

public class TopicDistributions {

	public Multinomial[] topicDistrib = null;
	
	public TopicDistributions(Dirichlet betaPrior, int k) {
		this.topicDistrib = new Multinomial[k];
		for (int i = 0; i < k; i++) {
			this.topicDistrib[i] = new Multinomial(betaPrior.nextDistribution());
		}
	}
	
}
