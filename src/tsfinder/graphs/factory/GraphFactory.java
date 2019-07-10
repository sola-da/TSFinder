/**
 * 
 */
package tsfinder.graphs.factory;

import java.util.HashMap;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

import soot.Modifier;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import tsfinder.Utils;

/**
 * @author Andrew Habib
 *
 */
public class GraphFactory {

	private Graph g;
	private String clName;
	private static boolean debug = false;

	public GraphFactory(Graph g) {
		this.g = g;
		this.clName = g.getAttribute("Class.Name");
	}

	public Graph getGraph() {
		return g;
	}

	@SuppressWarnings("serial")
	private void addNode(NodeType nType, String nID, String nLabel, String nShape, String nColor) {
		if (g.getNode(nID) == null) {
			g.addNode(nID).addAttributes(new HashMap<String, Object>() {
				{
					put("type", nType);
					if (nLabel == null)
						put("ui.label", nType.getLabel());
					else
						put("ui.label", nLabel);
					if (nShape != null && nColor != null)
						put("ui.style", nShape+nColor);
					else if (nShape != null)
						put("ui.style", nShape);
					else if (nColor != null)
						put("ui.style", nColor);
				}
			});
		}
	}

	@SuppressWarnings("serial")
	private void addEdge(EdgeType eType, String eID, String src, String tgt) {
		// check for duplicate edges and also that
		// src and edge nodes exist
		Edge edge = g.getEdge(eID);
		if (edge == null && g.getNode(src) != null && g.getNode(tgt) != null) {
			g.addEdge(eID, src, tgt, true);
			edge = g.getEdge(eID);
			// check if the new edge is successfully created
			if (edge != null) {
				edge.addAttributes(new HashMap<String, Object>() {
					{
						put("type", eType);
						put("ui.label", eType.getLabel());
					}
				});
			}
		}
	}

	public void addFieldNode(SootField f) {
		addNode(NodeType.FIELD, f.getSignature(), f.getSignature(), "shape: cross;", "fill-color: red;");		

		String volNodeID = "VOL:" + f.getSignature();
		String volEdgeID = "isVOL# " + f.getSignature();
		if (Modifier.isVolatile(f.getModifiers())) {
			addNode(NodeType.VOL, volNodeID, null, null, null);
			addEdge(EdgeType.isVOL, volEdgeID, f.getSignature(), volNodeID);
		}
		
		String finNodeID = "FIN:" + f.getSignature();
		String finEdgeID = "isFIN# " + f.getSignature();
		if (Modifier.isFinal(f.getModifiers())) {
			addNode(NodeType.FIN, finNodeID, null, null, null);
			addEdge(EdgeType.isFIN, finEdgeID, f.getSignature(), finNodeID);
		}
		
		String edgeID = "visible# " + f.getSignature();
		if (Modifier.isPublic(f.getModifiers())) {
			String nodeID = "PUB:" + f.getSignature();
			addNode(NodeType.Public, nodeID, null, null, null);
			addEdge(EdgeType.isVisible, edgeID, f.getSignature(), nodeID);
		}
//		else if (Modifier.isPrivate(f.getModifiers())){
//			String nodeID = "PRIV:" + f.getSignature();
//			addNode(NodeType.Private, nodeID, null, null, null);
//			addEdge(EdgeType.isVisible, edgeID, f.getSignature(), nodeID);
//		}
//		else if (Modifier.isProtected(f.getModifiers())){
//			String nodeID = "PRPTECT:" + f.getSignature();
//			addNode(NodeType.Protected, nodeID, null, null, null);
//			addEdge(EdgeType.isVisible, edgeID, f.getSignature(), nodeID);
//		}
		
		// add <code>static</code> info
		String statNodeID = "Stat:" + f.getSignature();
		String statEdgeID = "isStat# " + f.getSignature();
		if (Modifier.isStatic(f.getModifiers())) {
			addNode(NodeType.STATIC, statNodeID, null, null, null);
			addEdge(EdgeType.isVisible, statEdgeID, f.getSignature(), statNodeID);
		}
//		else
//			addNode(NodeType.notSTATIC, statNodeID, null, null, null);
//		addEdge(EdgeType.isVisible, statEdgeID, f.getSignature(), statNodeID);
	}

	public void addMethodNode(SootMethod m) {
		// case 1: constructor
		if (m.isConstructor())
			addNode(NodeType.INIT, m.getSignature(), m.getSignature(), "shape: box;", null);
		else
			// case 2: static constructor
			if (m.isStaticInitializer()) {
				addNode(NodeType.CLINIT, m.getSignature(), m.getSignature(), "shape: box;", null);
			}
			// case 3: methods
			else {
				addNode(NodeType.METHOD, m.getSignature(), m.getSignature(), "shape: box;", null);				
				// add synchronization info
				String synNodeID = "SYN:" + m.getSignature();
				String synEdgeID = "IsSYN:" + m.getSignature();
				if (m.isSynchronized()) {
					addNode(NodeType.SYN, synNodeID, "SYN:this", null, null);
					addEdge(EdgeType.isSYN, synEdgeID, m.getSignature(), synNodeID);
				}
				else if (Utils.hasSyncBlock(m)) {
					addNode(NodeType.SYN, synNodeID, "SYN:TBD", null, null);
					addEdge(EdgeType.isSYN, synEdgeID, m.getSignature(), synNodeID);
				}
//				else
//					addNode(NodeType.notSYN, synNodeID, "notSYN", null, null);
//				addEdge(EdgeType.isSYN, synEdgeID, m.getSignature(), synNodeID);
				
				// add static info
				String statNodeID = "Stat:" + m.getSignature();
				String statEdgeID = "IsStat:" + m.getSignature();
				if (m.isStatic()) {
					addNode(NodeType.STATIC, statNodeID, "Stat", null, null);
					addEdge(EdgeType.isStatic, statEdgeID, m.getSignature(), statNodeID);
				}
//				else
//					addNode(NodeType.notSTATIC, statNodeID, "notStat", null, null);
//				addEdge(EdgeType.isStatic, statEdgeID, m.getSignature(), statNodeID);
			}
		
		// add <code>public</code> info
//		String pubNodeID = "PUB:" + m.getSignature();
//		String pubEdgeID = "isPUB# " + m.getSignature();
//		if (Modifier.isPublic(m.getModifiers())) {
//			addNode(NodeType.Public, pubNodeID, null, null, null);
//			addEdge(EdgeType.isVisible, pubEdgeID, m.getSignature(), pubNodeID);
//		}
		String edgeID = "visible# " + m.getSignature();
		if (Modifier.isPublic(m.getModifiers())) {
			String nodeID = "PUB:" + m.getSignature();
			addNode(NodeType.Public, nodeID, null, null, null);
			addEdge(EdgeType.isVisible, edgeID, m.getSignature(), nodeID);
		}
		else if (Modifier.isPrivate(m.getModifiers())){
			String nodeID = "PRIV:" + m.getSignature();
			addNode(NodeType.Private, nodeID, null, null, null);
			addEdge(EdgeType.isVisible, edgeID, m.getSignature(), nodeID);
		}
		else if (Modifier.isProtected(m.getModifiers())){
			String nodeID = "PRPTECT:" + m.getSignature();
			addNode(NodeType.Protected, nodeID, null, null, null);
			addEdge(EdgeType.isVisible, edgeID, m.getSignature(), nodeID);
		}
//		else
//			addNode(NodeType.notPublic, pubNodeID, null, null, null);
//		addEdge(EdgeType.isVisible, pubEdgeID, m.getSignature(), pubNodeID);
	}

	public void addFieldDirectRWEdges(SootField f, List<SootMethod> readers, List<SootMethod> writers) {
		if (readers !=null && !readers.isEmpty()) {
			for (SootMethod m : readers) {
				String edgeID = "reads# " + m.getSignature() + " # " + f.getSignature();
				addEdge(EdgeType.READS, edgeID, m.getSignature(), f.getSignature());
			}
		}
		if (writers != null && !writers.isEmpty()) {
			for (SootMethod m : writers) {
				String edgeID = "writes# " + m.getSignature() + " # " + f.getSignature();
				addEdge(EdgeType.WRITES, edgeID, m.getSignature(), f.getSignature());
			}
		}

	}

	public void addCallGraphEdges(CallGraph cg) {
		// one way
		g.getNodeSet().forEach(node -> {
			if (node.getAttribute("type") == NodeType.INIT || node.getAttribute("type") == NodeType.METHOD) {
				SootMethod m = Scene.v().getMethod(node.getId());	
				cg.edgesInto(m).forEachRemaining(edge -> {
					SootMethod src = edge.src();
					// TODO discuss with Michael
					// add calls from methods only in the same class
					if (src != null && src.getDeclaringClass().getName() == clName) {
						if (g.getNode(src.getSignature()) == null) {
							addMethodNode(src);
						}
						String edgeID = "calls# " + src.getSignature() + " # " + m.getSignature();
						addEdge(EdgeType.CALLS, edgeID, src.getSignature(), m.getSignature());
					}
				});
			}
		});	
		// another way
		// due to soot tutorial
		// required adding few extra checks to work
//		g.getNodeSet().forEach(node -> {
//			if (node.getAttribute("type") == NodeType.INIT || node.getAttribute("type") == NodeType.METHOD) {
//				if (Scene.v().containsMethod(node.getId()) && Scene.v().getMethod(node.getId()).getClass().getName() == clName) {
//					SootMethod tgt = Scene.v().getMethod(node.getId());
//					Iterator<MethodOrMethodContext> sources = new Sources(cg.edgesInto(tgt));
//					while (sources.hasNext()) {
//						SootMethod src = (SootMethod) sources.next();
//						if (src != null) {
//							if (g.getNode(src.getSignature()) == null) {
//								addMethodNode(src);
//							}
//							String edgeID = "calls# " + src.getSignature() + " # " + tgt.getSignature();
//							addEdge(EdgeType.CALLS, edgeID, src.getSignature(), tgt.getSignature());
//						}
//					}
//				}				
//			}
//		});		
	}

}
