/**
 * 
 */
package tsfinder.graphs.fieldfocused;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

import soot.SootClass;
import soot.jimple.toolkits.callgraph.CallGraph;

/**
 * @author ah
 *
 */
public abstract class OneGraph {

	protected SootClass cls;
	protected CallGraph cg;
	protected String clPath;
	protected Graph g;

	public OneGraph(SootClass cl, CallGraph cg, String clPath) {
		this.cls = cl;
		this.cg = cg;
		this.clPath = clPath;

		g = new MultiGraph(this.cls.getName());

		g.setStrict(true);

		g.addAttribute("Class.Name", cl.getName());
		g.addAttribute("Class.Path", clPath);
	}
	
	public OneGraph(SootClass cl, CallGraph cg) {
		this.cls = cl;
		this.cg = cg;
		g = new MultiGraph(this.cls.getName());
		g.setStrict(true);
		g.addAttribute("Class.Name", cl.getName());
	}

	/** 
	 * Start point of constructing graphs. 
	 * Should be explicitly called by all child classes' constructors
	 */
	protected abstract void constructGraph();

	public Graph getGraph() {
		return g;
	}

}
