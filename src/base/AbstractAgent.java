package base;

import java.util.ArrayList;

public abstract class AbstractAgent implements Agent {

	private int id = 0;

	public AbstractAgent(int id) {
		this.id = id;
	}
	
	@Override
	public int getId() {
		return this.id;
	}



}
