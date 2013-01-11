package base;

import java.util.ArrayList;

public class Message {

	/**
	 * The ID of the author
	 */
	public int authorId;

	/**
	 * A list of (virtual) terms that constitute the message
	 */
	public ArrayList<Integer> terms = new ArrayList<Integer>();

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Integer termId : this.terms) {
			buffer.append(termId);
			buffer.append(' ');
		}
		return buffer.toString();
	}
	
}