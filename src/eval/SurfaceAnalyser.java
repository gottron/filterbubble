package eval;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class SurfaceAnalyser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void surfacePlots(File inDir, File outDir) throws IOException {
		File[] logFiles = inDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().startsWith("avg");
			}
		});
		TreeMap<Integer, TreeMap<Double, TreeMap<Double, Measurement>>> surfaces =  new TreeMap<Integer, TreeMap<Double,TreeMap<Double, Measurement>>>(); 
		for (File f : logFiles) {
			LogResults logContent = LogResults.fromFile(f);
			for (int tile : logContent.tileMetrics.keySet()) {
				if (! surfaces.containsKey(tile)) {
					surfaces.put(tile, new TreeMap<Double, TreeMap<Double, Measurement>>());
				}
				if (! surfaces.get(tile).containsKey(logContent.coreMarkProbability)) {
					surfaces.get(tile).put(logContent.coreMarkProbability, new TreeMap<Double, Measurement>());
				}
				TreeMap<Integer, Measurement> tileData= logContent.tileMetrics.get(tile);
				int lastIteration = tileData.lastKey();
				surfaces.get(tile).get(logContent.coreMarkProbability).put(logContent.tailMarkProbability, tileData.get(lastIteration));
			}
		}
		String[] metrics = {Measurement.coreRatioPrefix, Measurement.edgeRatioPrefix, Measurement.vocabularFractionPrefix, Measurement.seenFractionPrefix, Measurement.precisionPrefix, Measurement.recallPrefix};
		for (String m : metrics) {
			for (int tile : surfaces.keySet()) {
				PrintStream out = new PrintStream(new File(outDir, m.trim()+"-Tile-"+tile+".dat"));
				out.println("# Data of Metric "+m+" for Tile "+tile+" over various settings for probabilities");
				for (double coreP : surfaces.get(tile).keySet()) {
					out.print(coreP);
					for (double tailP : surfaces.get(coreP).keySet()) {
						Measurement msnt = surfaces.get(tile).get(coreP).get(tailP);
						if (m.equals(Measurement.coreRatioPrefix)) {
							
						} else if (m.equals(Measurement.edgeRatioPrefix)) {
							
						}
					}				
				}
			}
		}
		
		
	}

}
