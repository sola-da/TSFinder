package tsfinder.graphs.fieldfocused;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Graph;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import tsfinder.graphs.factory.GraphFactoryUnlabeledEdges;

/**
 * @author Andrew Habib
 *
 */
public class FieldFocusedGraphs {
	private SootClass cls;
	private CallGraph cg;
	private AccessorsLists lists;
	// private static boolean debug = false;
	private List<Graph> graphs;

	public FieldFocusedGraphs(SootClass cls, CallGraph cg) {
		this.cls = cls;
		this.cg = cg;
		this.graphs = new ArrayList<Graph>();

		FlatClass flatClass = new FlatClass(cls);
		if (flatClass.isClFlat()) {
			this.lists = new AccessorsLists(cls, flatClass.getMethodsList(), flatClass.getFieldsList());
			constructGraphs();
		}
	}

	public List<Graph> getGraphs() {
		return this.graphs;
	}

	protected void constructGraphs() {

		// construct graphs for fields accessed by at least one method
		lists.getFieldsToAccessors().forEach((f, list) -> {
			SingleFieldFocusedGraph graph = new SingleFieldFocusedGraph(cls, f, cg, lists);
			graphs.add(graph.getGraph());
		});

		// construct graphs for pairs of fields accessed by at least one method
		lists.getFieldsPairsToAccessors().forEach((set, list) -> {
			SootField f1 = (SootField) set.toArray()[0], f2 = (SootField) set.toArray()[1];
			PairFieldsFocusedGraph graph = new PairFieldsFocusedGraph(cls, f1, f2, cg, lists);
			graphs.add(graph.getGraph());
		});
	}

}

class SingleFieldFocusedGraph extends OneGraph {
	private SootField f;
	private AccessorsLists lists;

	SingleFieldFocusedGraph(SootClass cls, SootField f, CallGraph cg, AccessorsLists lists) {
		super(cls, cg);
		this.f = f;
		this.lists = lists;
		constructGraph();
	}

	@Override
	protected void constructGraph() {
		// GraphFactory graphFactory = new GraphFactory(g);
		GraphFactoryUnlabeledEdges graphFactory = new GraphFactoryUnlabeledEdges(g);
		graphFactory.addFieldNode(f);
		for (SootMethod m : lists.getFieldsToAccessors().get(f)) {
			graphFactory.addMethodNode(m);
		}
		graphFactory.addFieldDirectRWEdges(f, lists.getFieldsToReaders().get(f), lists.getFieldsToWriters().get(f));
		graphFactory.addCallGraphEdges(cg);
		g = graphFactory.getGraph();
	}
}

class PairFieldsFocusedGraph extends OneGraph {
	private SootField f1, f2;
	private AccessorsLists lists;

	PairFieldsFocusedGraph(SootClass cl, SootField f1, SootField f2, CallGraph cg, AccessorsLists lists) {
		super(cl, cg);
		this.f1 = f1;
		this.f2 = f2;
		this.lists = lists;
		constructGraph();
	}

	@Override
	protected void constructGraph() {
		// GraphFactory graphFactory = new GraphFactory(g);
		GraphFactoryUnlabeledEdges graphFactory = new GraphFactoryUnlabeledEdges(g);
		graphFactory.addFieldNode(f1);
		graphFactory.addFieldNode(f2);
		for (SootMethod m : lists.getFieldsToAccessors().get(f1)) {
			graphFactory.addMethodNode(m);
		}
		for (SootMethod m : lists.getFieldsToAccessors().get(f2)) {
			graphFactory.addMethodNode(m);
		}
		graphFactory.addFieldDirectRWEdges(f1, lists.getFieldsToReaders().get(f1), lists.getFieldsToWriters().get(f1));
		graphFactory.addFieldDirectRWEdges(f2, lists.getFieldsToReaders().get(f2), lists.getFieldsToWriters().get(f2));
		graphFactory.addCallGraphEdges(cg);
		g = graphFactory.getGraph();
	}
}

// class TripleFieldsFocusedGraph {
// private SootField f1, f2, f3;
// private AccessorsLists lists;
//
// public TripleFieldsFocusedGraph(SootClass cl, SootField f1, SootField f2,
// SootField f3, CallGraph cg, String path, AccessorsLists lists) {
// super(cl, cg, path);
// this.f1 = f1;
// this.f2 = f2;
// this.f3 = f3;
// this.lists = lists;
// constructGraph();
// }
//
// @Override
// protected void constructGraph() {
//// GraphFactory graphFactory = new GraphFactory(g);
// GraphFactoryUnlabeledEdges graphFactory = new GraphFactoryUnlabeledEdges(g);
// graphFactory.addFieldNode(f1);
// graphFactory.addFieldNode(f2);
// graphFactory.addFieldNode(f3);
// for (SootMethod m : lists.getFieldsToAccessors().get(f1)) {
// graphFactory.addMethodNode(m);
// }
// for (SootMethod m : lists.getFieldsToAccessors().get(f2)) {
// graphFactory.addMethodNode(m);
// }
// for (SootMethod m : lists.getFieldsToAccessors().get(f3)) {
// graphFactory.addMethodNode(m);
// }
// graphFactory.addFieldDirectRWEdges(f1, lists.getFieldsToReaders().get(f1),
// lists.getFieldsToWriters().get(f1));
// graphFactory.addFieldDirectRWEdges(f2, lists.getFieldsToReaders().get(f2),
// lists.getFieldsToWriters().get(f2));
// graphFactory.addFieldDirectRWEdges(f3, lists.getFieldsToReaders().get(f3),
// lists.getFieldsToWriters().get(f3));
// graphFactory.addCallGraphEdges(cg);
// g = graphFactory.getGraph();
// }
//
// }
