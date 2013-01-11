package testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import cc.mallet.types.Dirichlet;
import base.GlobalSettings;
import base.Message;
import base.Network;
import base.RelevanceModelFactory;
import base.Simulation;
import base.TopicAgent;
import base.TopicDistributions;

public class Test {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Test.testSimulation(100);

	}
	
	public static void testSimulation(int rounds) throws FileNotFoundException {
		int[] netSizes = {10000};
//		double[] coreProb = {0.2,0.4,0.6,0.8,1};
//		double[] tailProb = {0.2,0.4,0.6,0.8,1};
		double[] coreProb = {0.0,0.1,0.3,0.5,0.7,0.9};
		double[] tailProb = {0.0};
		for (int size : netSizes) {
			for (double cP: coreProb) {
				for (double tP: tailProb) {
					GlobalSettings.networkSize = size;
					GlobalSettings.coreRelProb = cP;
					GlobalSettings.tailRelProb = tP;
					GlobalSettings.networkSize = size;
					for (String model : RelevanceModelFactory.models) {
						if (!model.equals("random")) {
							System.out.println("Testing with "+model);
							long time = System.currentTimeMillis()/10000;
							PrintStream detailLog = new PrintStream(new File("logs/detail-"+size+"-"+model+"-c"+cP+"-t"+tP+"-"+time+".log"));
							PrintStream avgLog = new PrintStream(new File("logs/avg-"+size+"-"+model+"-c"+cP+"-t"+tP+"-"+time+".log"));
							Simulation sim = new Simulation(detailLog, avgLog, model);
							for (int i = 0; i < rounds; i++) {
								sim.step();
							}
							detailLog.close();
							avgLog.close();
						}
					}
				}
			}
		}
	}

	public static void testNetwork() {
		for (int i= 1; i <= 1000; i++) {
			System.out.println("Network "+i);
			Network nba = Network.BarabasiAlbertNetwork(10000, 2, 1);
		}
		for (int size = 100; size <= 100000; size *=10) {
			long start = System.currentTimeMillis();
			Network nba = Network.BarabasiAlbertNetwork(size, 15, 10);
			long stop = System.currentTimeMillis();
			System.out.println("Size: "+size+"  Time (ms): "+(stop-start));
//			nba.printDegreeDistribution(System.out);
		}
		//nba.printNetwork(System.out);
	}
	
	public static void testMessageTopics() {
		Dirichlet topicPrior = new Dirichlet(GlobalSettings.topicCount, GlobalSettings.topicAlphaParameter);
		Dirichlet termPrior = new Dirichlet(GlobalSettings.vocabularySize, GlobalSettings.termBetaParameter);
		TopicDistributions td = new TopicDistributions(termPrior, GlobalSettings.topicCount);
		TopicAgent tester = new TopicAgent(0, topicPrior, td);
		TopicAgent receiver = new TopicAgent(1, topicPrior, td);
		int[] selfMark = new int[2];
		int[] otherMark = new int[2];
		for (int i = 0; i < 1000; i++) {
			System.out.print(i+" ");
			ArrayList<Message> msgs = tester.createMessages(GlobalSettings.messageLength);
			for (Message msg: msgs) {
				if (tester.isCoreTopic(msg)) {
					selfMark[0]++;
				} else {
					selfMark[1]++;
				}
				if (receiver.isCoreTopic(msg)) {
					otherMark[0]++;
				} else {
					otherMark[1]++;
				}
				System.out.print(".");
			}
			System.out.println();
		}
		System.out.println("KL (self-self)"+tester.klDivergence(tester));
		System.out.println("KL (self-other)"+tester.klDivergence(receiver));
		System.out.println("Self  (Rel/Irrel): "+selfMark[0]+" / "+selfMark[1]);
		System.out.println("Other (Rel/Irrel): "+otherMark[0]+" / "+otherMark[1]);
		
	}
	
}
