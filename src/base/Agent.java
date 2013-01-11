package base;

import java.util.ArrayList;
import java.util.HashMap;

public interface Agent {

	public static final String MARKS = "mark";
	public static final String CORES = "core";
	
	/** 
	 * returns the ID of the Agent
	 * @return
	 */
	public int getId();
	
	/**
	 * Checks if a given Message is a core topic of the user, i.e. his primary interest
	 *  
	 * @param msg the Message to be checked
	 * @return true if it is a primary interest message
	 */
	public boolean isCoreTopic(Message msg);

	/**
	 * create the list of messages generate by that agent in one time frame.
	 *  
	 * @return List of Messages
	 */
	public ArrayList<Message> createMessages(int length);

	/**
	 * Produces the relevance / like / star marks of an agent for his incoming messages
	 * 
	 * @param messages
	 * @return
	 */
	public HashMap<String, ArrayList<Boolean>> markMessages(ArrayList<Message> messages);

}