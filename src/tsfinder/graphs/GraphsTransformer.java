/**
 * 
 */
package tsfinder.graphs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Graph;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.jimple.toolkits.callgraph.CallGraph;
import tsfinder.Config;
import tsfinder.graphs.fieldfocused.FieldFocusedGraphs;
import tsfinder.graphs.format.FormatterGraphML;
import tsfinder.graphs.format.GraphFormatter;

/**
 * @author Andrew Habib
 *
 */
public class GraphsTransformer extends SceneTransformer {

	String PATH_TO_GRAPHS = Config.OUTPUT_DIR + "/graphs_raw/";
	List<String> classesToAnalyze;

	public GraphsTransformer(List<String> classesToAnalyze) {
		this.classesToAnalyze = classesToAnalyze;
	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		CallGraph cg = Scene.v().getCallGraph();
		for (SootClass cls : Scene.v().getClasses()) {
			if (classesToAnalyze.contains(cls.getName())) {
				buildGraphsFromClass(cls, cg);

			}
		}

	}

	private void buildGraphsFromClass(SootClass cls, CallGraph cg) {
		FieldFocusedGraphs fieldFocusedGraphs = new FieldFocusedGraphs(cls, cg);
		saveGraphsToDisk(cls.getName(), fieldFocusedGraphs.getGraphs());

	}

	private void saveGraphsToDisk(String clsName, List<Graph> graphs) {
		File root = new File(this.PATH_TO_GRAPHS + clsName);
		root.mkdirs();
		int i = 1;
		for (Graph g : graphs) {
			saveGraphMLtoDisk(root, i, g);
			i++;
		}
	}

	private void saveGraphMLtoDisk(File root, int i, Graph g) {
		File path = new File(root.getPath() + "/" + i + ".graphml");
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false), "utf-8"));
			GraphFormatter formatter = new FormatterGraphML(g);
			writer.write(formatter.getGraphString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
