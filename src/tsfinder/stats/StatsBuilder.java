/**
 * 
 */
package tsfinder.stats;

import java.util.ArrayList;
import java.util.List;

import soot.PackManager;
import soot.Transform;
import tsfinder.Config;
import tsfinder.Utils;

/**
 * @author Andrew Habib
 *
 */
public class StatsBuilder {

	public StatsBuilder() {
		List<String> tsClasses = Utils.ReadListFromFile(Config.THREAD_SAFE_CLASSES);
		List<String> ntsClasses = Utils.ReadListFromFile(Config.THREAD_UNSAFE_CLASSES);
		List<String> classesToAnalyze = new ArrayList<>();
		classesToAnalyze.addAll(tsClasses);
		classesToAnalyze.addAll(ntsClasses);

		StatsTransformer statsTransformer = new StatsTransformer(classesToAnalyze);
		PackManager.v().getPack("wjtp")
				.add(new Transform("wjtp.stats" + ".statsTransformer", statsTransformer));
		soot.Main.main(Utils.GetSootArgs());
		statsTransformer.addLabelsToVectors(tsClasses, ntsClasses);

		String out_path = Config.OUTPUT_DIR + "Stats.arff";
		ARFFformatter arffFormatter = new ARFFformatter(out_path);
		arffFormatter.saveResultsToDisk(statsTransformer.getAnalysisResults());
	}

	public static void main(String[] args) {
		new StatsBuilder();
	}

}
