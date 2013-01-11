package base;

public class IndexWeight implements Comparable<IndexWeight> {

	public double weight = 0;
	public int index = 0;

	@Override
	public int compareTo(IndexWeight o) {
		return (int) Math.signum(o.weight-this.weight);
	}
	
}
