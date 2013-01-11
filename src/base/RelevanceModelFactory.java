package base;

public class RelevanceModelFactory {

	public static String[] models = {"author", "term", "random"};
	
	public static RelevanceModel initiate(String name) {
		if (name.equals(models[0])) {
			return new AuthorRelevanceModel(GlobalSettings.networkSize);
		} else if (name.equals(models[1])) {
			return new TermRelevanceModel(GlobalSettings.vocabularySize);
		} else {
			return new RandomRelevanceModel();
		}
	}
	
}
