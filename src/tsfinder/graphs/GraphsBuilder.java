/**
 * 
 */
package tsfinder.graphs;

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
public class GraphsBuilder {

	public GraphsBuilder() {
		List<String> tsClasses = Utils.ReadListFromFile(Config.THREAD_SAFE_CLASSES);
		List<String> ntsClasses = Utils.ReadListFromFile(Config.THREAD_UNSAFE_CLASSES);
		List<String> classesToAnalyze = new ArrayList<>();
		classesToAnalyze.addAll(tsClasses);
		classesToAnalyze.addAll(ntsClasses);
		
		GraphsTransformer statsTransformer = new GraphsTransformer(classesToAnalyze);
		PackManager.v().getPack("wjtp")
				.add(new Transform("wjtp.graphs" + ".statsTransformer", statsTransformer));
		soot.Main.main(Utils.GetSootArgs());
	}

	public static void main(String[] args) {
		new GraphsBuilder();
	}

}
