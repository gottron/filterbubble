package base;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Network {

	/**
	 * Adjacency list of the network structure. Edges lead to the ids of the followers, i.e. the agent receiving the messages of a given node. If the network is undirected, then the lists must be maintained manually to be symmetric
	 */
	private ArrayList<Set<Integer>> adjacencyList = new ArrayList<Set<Integer>>();

	/**
	 * Returns the number of nodes in the network
	 * @return
	 */
	public int nodeCount() {
		return this.adjacencyList.size();
	}

	/**
	 * Returns the set of edges connected to a given nodeId (i.e. the ids of the follower nodes)
	 * @param nodeId
	 * @return
	 */
	public Set<Integer> getEdges(int nodeId) {
		return this.adjacencyList.get(nodeId);
	}

	/**
	 * Print the network structure to a Printstream;
	 * 
	 * @param out
	 */
	public void printNetwork(PrintStream out) {
		for (int id = 0; id < this.nodeCount(); id++) {
			out.print(id + "\t{");
			TreeSet<Integer> sortedNodes = new TreeSet<Integer>();
			sortedNodes.addAll(this.adjacencyList.get(id));
			for (Integer targetId : sortedNodes) {
				out.print(targetId+" ");
			}
			out.println("}");
		}
		out.flush();
	}

	
	/**
	 * Print the degree distribution to a Printstream;
	 * 
	 * @param out
	 */
	public void printDegreeDistribution(PrintStream out) {
		int maxDegree = 0;
		for (int id = 0; id < this.nodeCount(); id++) {
			maxDegree = Math.max(maxDegree, this.getEdges(id).size());
		}
		int degree[] = new int[maxDegree+1];
		for (int id = 0; id < this.nodeCount(); id++) {
			int nodeDegree = this.getEdges(id).size();
			degree[nodeDegree]++;
		}
		for (int i = 0; i < degree.length; i++) {
			out.println(i + "\t"+ degree[i]);
		}
		out.flush();
	}

	
	/**
	 * Create an undirected network according to the Barabasi Albert Model for preferential attachement
	 * 
	 * @param nodeCount
	 * @param core
	 * @param m
	 * @return
	 */
	public static Network BarabasiAlbertNetwork(int nodeCount, int core, int m) {
		assert(nodeCount > core);
		assert(core >= m);
		Network result = new Network();
		// keep track of degree for faster computation
		int[] degree = new int[nodeCount];
		int edgeCount = 0;
		// initialize a fully connected initial network of m nodes
		for (int initial= 0; initial < core; initial++) {
			HashSet<Integer> followers = new HashSet<Integer>();
			for (int followerId= 0; followerId < core; followerId++) {
				if (followerId != initial) {
					followers.add(followerId);
				}
			}			
			result.adjacencyList.add(initial, followers);
			degree[initial] = followers.size();
			edgeCount += degree[initial]; 
		}
		// complete the graph with preferential attachement
		for (int id = core; id < nodeCount; id++) {
			HashSet<Integer> followers = new HashSet<Integer>();
			while (followers.size()<m) {
				double[] rnd = new double[m-followers.size()];
				for (int i = 0; i < rnd.length; i++) {
					rnd[i] = Math.random()*edgeCount;
				}
				Arrays.sort(rnd);
				int cnt = 0;
				int pos = 0;
				int targetId = 0;
				while (targetId< id) {
					if (rnd[pos] < cnt ) {
						followers.add(targetId);
						pos++;
					} else {
						cnt += degree[targetId];
						targetId++;
					}
					if (pos >= rnd.length) { 
						break;
					}
				}
			}
			// create the edges
			for (Integer nodeId : followers) {
				result.adjacencyList.get(nodeId).add(id);
			}
			result.adjacencyList.add(id, followers);
			degree[id] = followers.size();
			edgeCount += degree[id]; 
		}
		return result;
	}
	
}