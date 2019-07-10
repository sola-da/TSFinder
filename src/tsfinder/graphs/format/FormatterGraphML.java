/**
 * 
 */
package tsfinder.graphs.format;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import tsfinder.graphs.factory.EdgeType;
import tsfinder.graphs.factory.NodeType;

/**
 * @author Andrew Habib
 *
 */
public class FormatterGraphML extends GraphFormatter {

	public FormatterGraphML() {
		super();
	}

	public FormatterGraphML(Graph g) {
		super(g);
	}

	public String getGraphString() {
		if (g.getNodeCount() == 0) {
			return null;
		}
		writeGraphHeader();
		writeVertices();
		writeEdges();
		writeGraphFooter();
		return gBuilder.toString();
	}

	private void writeGraphHeader() {
		// xml header for the graph file
		gBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		gBuilder.append('\n');
		gBuilder.append("<graphml ");
		gBuilder.append("xmlns=\"http://graphml.graphdrawing.org/xmlns\"");
		gBuilder.append('\n');
		gBuilder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		gBuilder.append('\n');
		gBuilder.append("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns");
		gBuilder.append('\n');
		gBuilder.append("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
		gBuilder.append('\n');
		gBuilder.append("<!-- Created by manually  written generator by Andrew Habib -->");
		gBuilder.append('\n');

		gBuilder.append("<key id=\"v_label\" for=\"node\" attr.name=\"label\" attr.type=\"int\"/>");
		// gBuilder.append("<key id=\"v_label\" for=\"node\" attr.name=\"label\"
		// attr.type=\"string\"/>");

		gBuilder.append('\n');

		// gBuilder.append("<key id=\"e_label\" for=\"edge\" attr.name=\"label\"
		// attr.type=\"int\"/>");
		// gBuilder.append('\n');

		gBuilder.append("<graph id=\"G\" edgedefault=\"directed\">");
		// gBuilder.append("<graph id=\"G\" edgedefault=\"undirected\">");

		gBuilder.append('\n');
	}

	private void writeVertices() {
		NodeType nodeType;
		for (Node n : g.getEachNode()) {
			nodeType = n.getAttribute("type");
			gBuilder.append("<node id=" + '"' + 'n' + verticesMap.get(n.getId()) + '"' + '>');
			gBuilder.append('\n');
			gBuilder.append("<data key=\"v_label\">" + nodeType.ordinal() + "</data>");
			// gBuilder.append("<data key=\"v_label\">" + nodeType.getLabel() + "</data>");
			gBuilder.append('\n');
			gBuilder.append("</node>");
			gBuilder.append('\n');
		}
	}

	private void writeEdges() {
		Node src;
		Node tgt;
		EdgeType edgeType;
		for (Edge e : g.getEachEdge()) {
			src = e.getSourceNode();
			tgt = e.getTargetNode();
			edgeType = e.getAttribute("type");

			gBuilder.append("<edge ");
			gBuilder.append("source=" + '"' + 'n' + verticesMap.get(src.getId()) + '"');
			gBuilder.append(' ');
			gBuilder.append("target=" + '"' + 'n' + verticesMap.get(tgt.getId()) + '"' + '>');
			gBuilder.append('\n');
			// gBuilder.append("<data key=\"e_label\">" + edgeType.ordinal() + "</data>");
			// gBuilder.append('\n');

			gBuilder.append("</edge>");
			gBuilder.append('\n');
		}
	}

	private void writeGraphFooter() {
		gBuilder.append("</graph>");
		gBuilder.append('\n');
		gBuilder.append("</graphml>");
	}

}
