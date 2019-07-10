/**
 * 
 */
package tsfinder.graphs.format;

import java.util.HashMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import tsfinder.graphs.factory.NodeType;

/**
 * @author Andrew Habib
 *
 */
public abstract class GraphFormatter implements IGraphFormatter {

	protected Graph g;
	protected StringBuilder gBuilder;
	protected StringBuilder gMetaData;
	protected HashMap<String, Integer> verticesMap;
	protected String fileName;
	protected static final boolean debug = true;

	public GraphFormatter() {
		// g = graph;
		gBuilder = new StringBuilder();
		gMetaData = new StringBuilder();
		// verticesMap = createVerticesMap();
	}

	public GraphFormatter(Graph graph) {
		g = graph;
		gBuilder = new StringBuilder();
		gMetaData = new StringBuilder();
		verticesMap = createVerticesMap();
	}

	public void FormatGraph(Graph graph) {
		g = graph;
		gBuilder = new StringBuilder();
		gMetaData = new StringBuilder();
		verticesMap = createVerticesMap();
	}

	public void setFileName(String n) {
		fileName = n;
	}

	public String getFileName() {
		return fileName;
	}

	public String getClassName() {
		return g.getAttribute("Class.Name");
	}

	public String getClassPath() {
		return g.getAttribute("Class.Path");
	}

	public String getMethodName() {
		if (g.hasAttribute("Mehtod.Name"))
			return g.getAttribute("Method.Name");
		else
			return null;
	}

	public abstract String getGraphString();

	public String getGraphMetaData() {
		// gMetaData.append(getClassName());
		// gMetaData.append('\n');
		// gMetaData.append(getClassPath());
		// gMetaData.append('\n');
		// gMetaData.append(getMethodName() == null ? "" : getMethodName() + '\n');

		for (Node n : g.getEachNode()) {
			gMetaData.append(String.format("% 4d", n.getIndex()));
			gMetaData.append("  ");
			switch ((NodeType) (n.getAttribute("type"))) {
			case FIELD:
				gMetaData.append("F    ");
				gMetaData.append(n.getAttribute("ui.label").toString());
				break;
			case INIT:
				gMetaData.append("INIT ");
				gMetaData.append(n.getAttribute("ui.label").toString());
				break;
			case METHOD:
				gMetaData.append("M    ");
				gMetaData.append(n.getAttribute("ui.label").toString());
				break;
			default:
				gMetaData.append((NodeType) n.getAttribute("type"));
			}
			// gMetaData.append(n.getAttribute("ui.label").toString());
			gMetaData.append('\n');
		}

		for (Edge e : g.getEachEdge()) {
			gMetaData.append(String.format("%4d", e.getSourceNode().getIndex()));
			gMetaData.append("  ");
			gMetaData.append(String.format("%6s", e.getAttribute("ui.label").toString()));
			gMetaData.append(String.format("%4d", e.getTargetNode().getIndex()));
			gMetaData.append('\n');
		}
		return gMetaData.toString();
	}

	public HashMap<String, Integer> createVerticesMap() {
		HashMap<String, Integer> vMap = new HashMap<String, Integer>();
		int vCount = 0;
		for (Node n : g.getNodeSet()) {
			vMap.put(n.getId(), ++vCount);
		}
		return vMap;
	}

}
