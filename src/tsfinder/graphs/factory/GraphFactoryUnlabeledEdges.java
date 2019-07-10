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
public class GraphFactoryUnlabeledEdges {

	private Graph g;
	private String clName;
	private static boolean debug = false;

	public GraphFactoryUnlabeledEdges(Graph g) {
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
					// label
					if (nLabel == null)
						put("ui.label", nType.getLabel());
					else
						put("ui.label", nLabel);
					// ui.style
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

		if (Modifier.isVolatile(f.getModifiers())) {
			String volNodeID = "VOL:" + f.getSignature();
			String volEdgeID = "isVOL# " + f.getSignature();
			addNode(NodeType.VOL, volNodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, volEdgeID, f.getSignature(), volNodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, f.getSignature(), volNodeID);
		}
		
		if (Modifier.isFinal(f.getModifiers())) {
			String finNodeID = "FIN:" + f.getSignature();
			String finEdgeID = "isFIN# " + f.getSignature();
			addNode(NodeType.FIN, finNodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, finEdgeID, f.getSignature(), finNodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, f.getSignature(), finNodeID);
		}
		
		if (Modifier.isStatic(f.getModifiers())) {
			String statNodeID = "Stat:" + f.getSignature();
			String statEdgeID = "isStat# " + f.getSignature();
			addNode(NodeType.STATIC, statNodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, statEdgeID, f.getSignature(), statNodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, f.getSignature(), statNodeID);
		}
		
		String edgeID = "visible# " + f.getSignature();
		if (Modifier.isPublic(f.getModifiers())) {
			String nodeID = "PUB:" + f.getSignature();
			addNode(NodeType.Public, nodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, edgeID, f.getSignature(), nodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, f.getSignature(), nodeID);
		}
		else if (Modifier.isPrivate(f.getModifiers())){
			String nodeID = "PRIV:" + f.getSignature();
			addNode(NodeType.Private, nodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, edgeID, f.getSignature(), nodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, f.getSignature(), nodeID);
		}
		else if (Modifier.isProtected(f.getModifiers())){
			String nodeID = "PROTECT:" + f.getSignature();
			addNode(NodeType.Protected, nodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, edgeID, f.getSignature(), nodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, f.getSignature(), nodeID);
		}
		
		String signature = f.getSubSignature();
		if (signature.contains("Concurrent") || signature.contains("Synchronized")) {
			String TSNodeID = "TS# " + f.getSignature();
			String TSEdgeID = "isTS# " + f.getSignature();
			addNode(NodeType.TS_FIELD, TSNodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, TSEdgeID, f.getSignature(), TSNodeID);
//			addNodeInsteadOfEdge(NodeType.isTS, f.getSignature(), TSNodeID);

		}

	}

	public void addMethodNode(SootMethod m) {
		if (m.isConstructor())
			addNode(NodeType.INIT, m.getSignature(), m.getSignature(), "shape: box;", null);
		else
			if (m.isStaticInitializer()) {
				addNode(NodeType.CLINIT, m.getSignature(), m.getSignature(), "shape: box;", null);
			}
			else {
				addNode(NodeType.METHOD, m.getSignature(), m.getSignature(), "shape: box;", null);				
				String synNodeID = "SYN:" + m.getSignature();
				String synEdgeID = "IsSYN:" + m.getSignature();
				if (m.isSynchronized()) {
					addNode(NodeType.SYN, synNodeID, "SYN:this", null, null);
					addEdge(EdgeType.NOLABEL, synEdgeID, m.getSignature(), synNodeID);
//					addNodeInsteadOfEdge(NodeType.isSYN, m.getSignature(), synNodeID);
				}

				else if (Utils.hasSyncBlock(m)) {
					addNode(NodeType.SYN, synNodeID, "SYN:TBD", null, null);
					addEdge(EdgeType.NOLABEL, synEdgeID, m.getSignature(), synNodeID);
//					addNodeInsteadOfEdge(NodeType.isSYN, m.getSignature(), synNodeID);
				}

				String statNodeID = "Stat:" + m.getSignature();
				String statEdgeID = "IsStat:" + m.getSignature();
				if (m.isStatic()) {
					addNode(NodeType.STATIC, statNodeID, "Stat", null, null);
					addEdge(EdgeType.NOLABEL, statEdgeID, m.getSignature(), statNodeID);
//					addNodeInsteadOfEdge(NodeType.MOD, m.getSignature(), statNodeID);
				}
			}
		
		String edgeID = "visible# " + m.getSignature();
		if (Modifier.isPublic(m.getModifiers())) {
			String nodeID = "PUB:" + m.getSignature();
			addNode(NodeType.Public, nodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, edgeID, m.getSignature(), nodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, m.getSignature(), nodeID);
		}
		else if (Modifier.isPrivate(m.getModifiers())){
			String nodeID = "PRIV:" + m.getSignature();
			addNode(NodeType.Private, nodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, edgeID, m.getSignature(), nodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, m.getSignature(), nodeID);
		}
		else if (Modifier.isProtected(m.getModifiers())){
			String nodeID = "PRPTECT:" + m.getSignature();
			addNode(NodeType.Protected, nodeID, null, null, null);
			addEdge(EdgeType.NOLABEL, edgeID, m.getSignature(), nodeID);
//			addNodeInsteadOfEdge(NodeType.MOD, m.getSignature(), nodeID);
		}
	}
	
	private void addNodeInsteadOfEdge(NodeType nType, String src, String tgt) {
		String nID = src + "_" + nType.getLabel() + "_" + tgt;
		addNode(nType, nID, null, null, null);
		
		String eSrcID = "src:"+nID;
		addEdge(EdgeType.NOLABEL, eSrcID, src, nID);
		
		String eTgtID = "tgt:"+nID;
		addEdge(EdgeType.NOLABEL, eTgtID, nID, tgt);
	}

	public void addFieldDirectRWEdges(SootField f, List<SootMethod> readers, List<SootMethod> writers) {
		if (readers !=null && !readers.isEmpty()) {
			for (SootMethod m : readers) {
				addNodeInsteadOfEdge(NodeType.READS, m.getSignature(), f.getSignature());
//				addNodeInsteadOfEdge(NodeType.READ_BY, f.getSignature(), m.getSignature());
			}
		}
		if (writers != null && !writers.isEmpty()) {
			for (SootMethod m : writers) {
				addNodeInsteadOfEdge(NodeType.WRITES, m.getSignature(), f.getSignature());
//				addNodeInsteadOfEdge(NodeType.WRITTEN_BY, f.getSignature(), m.getSignature());

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
						addNodeInsteadOfEdge(NodeType.CALLS, src.getSignature(), m.getSignature());
//						addNodeInsteadOfEdge(NodeType.CALLED_BY, m.getSignature(), src.getSignature());
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
