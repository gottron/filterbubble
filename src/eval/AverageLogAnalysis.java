package eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class AverageLogAnalysis {

	public static void main(String[] args) throws IOException {
//		avg-10000-author-135721251.log
		File fin = new File("logs/avg-10000-random-135735191.log");
//		File fin = new File("avg-10000-author-135721251.log");
		File outDir = new File("logs/plot/");
		AverageLogAnalysis.prep(fin, outDir);
	}
	
	public static void prep(File fin, File outDir) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(fin));
		String line = null;
		PrintStream[] out = null;
		int maxTiles = 0;
 		while ( (line = in.readLine()) != null) {
 			if (line.startsWith("#")) {
 				if (line.startsWith("# Tile ")) {
 					String[] parts = line.split("\\s",4);
 					int tileNumber = Integer.parseInt(parts[2]);
 					maxTiles = Math.max(maxTiles,tileNumber);
 				}
 			} else {
 				if (out == null) {
 					out = new PrintStream[maxTiles+1];
 					out[0] = new PrintStream(new File(outDir, "avg.dat"));
 					out[0].println("# Data average of the entire Simulation");
 					for (int i= 1; i < out.length; i++) {
 	 					out[i] = new PrintStream(new File(outDir, "tile-"+i+".dat"));
 	 					out[i].println("# Data of Tile "+i+" / "+maxTiles+" of the Simulation");
 					}
 					for (int i= 0; i < out.length; i++) {
 						out[i].println("# Step\tCore\tEdge\tVocFrac\tFrac\tPrecision\tRecall");
 					}
 				} 
 				String[] parts = line.split("\\t");
 				int logId = 0;
 				if (parts[1].startsWith("Tile")) {
 					logId = Integer.parseInt(parts[1].substring(5));
 				}
 				out[logId].print(parts[0]+"\t");
 				out[logId].print(parts[2].substring(5)+"\t");
 				out[logId].print(parts[3].substring(5)+"\t");
 				out[logId].print(parts[4].substring(8)+"\t");
 				out[logId].print(parts[5].substring(5)+"\t");
 				out[logId].print(parts[6].substring(5)+"\t");
 				out[logId].println(parts[7].substring(4));
 			}
 		}
 		PrintStream plotScript = new PrintStream(new File(outDir, "plot.gp"));
 		plotScript.println("#!/usr/bin/gnuplot");
 		plotScript.println("set   autoscale                        # scale axes automatically");
 		plotScript.println("unset log                              # remove any log-scaling");
 		plotScript.println("unset label                            # remove any previous labels");
 		plotScript.println("set xtic auto                          # set xtics automatically");
 		plotScript.println("set ytic auto                          # set ytics automatically");
 		plotScript.println("set title \"Filter Bubble Simulation (Average)\"");
// 		plotScript.println("set xlabel \"Deflection (meters)\"");
// 		plotScript.println("set ylabel \"Force (kN)\"");
//	 	plotScript.println("set key 0.01,100");
// 		plotScript.println("set label "Yield Point" at 0.003,260");
// 		plotScript.println("set arrow from 0.0028,250 to 0.003,280
// 		plotScript.println("set xr [0.0:0.022]");
// 		plotScript.println("set yr [0:325]");
 		plotScript.println("set terminal postscript landscape enhanced color dashed lw 1 \"Helvetica\" 14"); 
 		plotScript.println("set output \"plot-avg.ps\"");
 		plotScript.println("plot    \"avg.dat\" using 1:2 title 'Core Interest' with lines , \\");
 		plotScript.println("		\"avg.dat\" using 1:3 title 'Edge Cover' with lines, \\");
 		plotScript.println("		\"avg.dat\" using 1:4 title 'Seen Vocabulary' with lines, \\");
 		plotScript.println("		\"avg.dat\" using 1:5 title 'Seen Messages' with lines, \\");
 		plotScript.println("		\"avg.dat\" using 1:6 title 'Precision' with lines, \\");
 		plotScript.println("		\"avg.dat\" using 1:7 title 'Recall' with lines;");
		out[0].close();
		for (int i= 1; i < out.length; i++) {
	 		plotScript.println();
	 		plotScript.println("set title \"Filter Bubble Simulation (Tile "+i+")\"");
	 		plotScript.println("set output \"plot-tile-"+i+".ps\"");
	 		plotScript.println("plot    \"tile-"+i+".dat\" using 1:2 title 'Core Interest' with lines , \\");
	 		plotScript.println("		\"tile-"+i+".dat\" using 1:3 title 'Edge Cover' with lines, \\");
	 		plotScript.println("		\"tile-"+i+".dat\" using 1:4 title 'Seen Vocabulary' with lines, \\");
	 		plotScript.println("		\"tile-"+i+".dat\" using 1:5 title 'Seen Messages' with lines, \\");
	 		plotScript.println("		\"tile-"+i+".dat\" using 1:6 title 'Precision' with lines, \\");
	 		plotScript.println("		\"tile-"+i+".dat\" using 1:7 title 'Recall' with lines;");
			out[i].close();
 		}
 		plotScript.close();
		in.close();
	}
	
}
