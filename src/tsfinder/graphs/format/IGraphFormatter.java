/**
 * 
 */
package tsfinder.graphs.format;

import java.util.HashMap;

import org.graphstream.graph.Graph;

/**
 * @author Andrew Habib
 *
 */
public interface IGraphFormatter {

	String getClassName();

	String getClassPath();

	String getGraphString();

	String getGraphMetaData();

	HashMap<String, Integer> createVerticesMap();

	void FormatGraph(Graph g);

}
