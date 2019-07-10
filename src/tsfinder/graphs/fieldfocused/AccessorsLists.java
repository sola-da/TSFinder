/**
 * 
 */
package tsfinder.graphs.fieldfocused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.InvokeExprBox;
import tsfinder.Utils;

/**
 * @author Andrew Habib
 * 
 *         A map from accessed fields to accessor methods.
 *
 */
public class AccessorsLists {

	SootClass cl;
	Map<SootField, List<SootMethod>> fieldsToAccessors;
	Map<SootField, List<SootMethod>> fieldsToWriters;
	Map<SootField, List<SootMethod>> fieldsToReaders;
	Map<SootMethod, List<SootField>> accessorsToFields;
	Map<Set<SootField>, List<SootMethod>> fieldsPairsToAccessors;
	Map<Set<SootField>, List<SootMethod>> fieldsTriplesToAccessors;

	List<SootMethod> methods;
	List<SootField> fields;

	private static final boolean debug = false;

	private AccessorsLists() {
		accessorsToFields = new ConcurrentHashMap<SootMethod, List<SootField>>();
		fieldsToAccessors = new ConcurrentHashMap<SootField, List<SootMethod>>();
		fieldsToWriters = new ConcurrentHashMap<SootField, List<SootMethod>>();
		fieldsToReaders = new ConcurrentHashMap<SootField, List<SootMethod>>();
		fieldsPairsToAccessors = new ConcurrentHashMap<Set<SootField>, List<SootMethod>>();
		fieldsTriplesToAccessors = new ConcurrentHashMap<Set<SootField>, List<SootMethod>>();

	}

	public AccessorsLists(SootClass clazz) {
		this();
		cl = clazz;
		methods = cl.getMethods();
		fields = new ArrayList<>();
		cl.getFields().forEach(f -> fields.add(f));
		constructMaps();
	}

	public AccessorsLists(SootClass clazz, List<SootMethod> mList, List<SootField> fList) {
		this();
		cl = clazz;
		methods = mList;
		fields = fList;
		constructMaps();
	}

	public Map<SootField, List<SootMethod>> getFieldsToAccessors() {
		return fieldsToAccessors;
	}

	public Map<SootField, List<SootMethod>> getFieldsToWriters() {
		return fieldsToWriters;
	}

	public Map<SootField, List<SootMethod>> getFieldsToReaders() {
		return fieldsToReaders;
	}

	public Map<SootMethod, List<SootField>> getAccessorsToFields() {
		return accessorsToFields;
	}

	public Map<Set<SootField>, List<SootMethod>> getFieldsPairsToAccessors() {
		return fieldsPairsToAccessors;
	}

	public Map<Set<SootField>, List<SootMethod>> getFieldsTriplesToAccessors() {
		return fieldsTriplesToAccessors;
	}

	private void constructMaps() {
		// TODO Do we have to handle static initiazers: clinit in a special way?
		// if (cl.declaresMethodByName("clinit"))
		if (debug) {
			// System.out.println("&&& " + "constructMaps" + " &&&");
			System.out.println("$$$$$$$$$$$$$");
			System.out.println("Class: " + cl);
			System.out.println("$$$$$$$$$$$$$");
		}
		// for (SootMethod mth : cl.getMethods()) {
		for (SootMethod mth : methods) {
			if (debug) {
				// System.out.println("-> Method: " + mth);
				// System.out.println(mth.isConstructor()? "--> Constructor" : "--> NOT
				// Constructor");
				// System.out.println(mth.isStaticInitializer()? "--> Static init" : "--> NOT
				// Static init");
				// System.out.println(mth.getName() == "clinit" ? "--> matches 'clinit'" : "-->
				// NOT match 'clinit'");
			}

			// retrieve method body can throw an exception if the body is not
			// there
//			try {
				if (mth.isConcrete()) {
					Body body = mth.retrieveActiveBody();
					if (debug) {
						// System.out.println(body);
					}
					// TODO Some methods does not use the @this local ref which throws exceptions
					// when doing body.getThisLocal()
					createFieldsToReadersMap(body);
					createFieldsToWritersMap(body);
					addArrayTypeWrites(body);
					addObjectMethodWrites(body);
					accessorsToTFields(body);
				}
//			} catch (Exception e) {
//
//			}
		}
		// remove methods that never access any fields
		accessorsToFields.forEach((m, fList) -> {
			if (fList.isEmpty())
				accessorsToFields.remove(m);
		});
		// remove fields that are never accessed by any method
		fieldsToAccessors.forEach((f, mList) -> {
			if (mList.isEmpty())
				fieldsToAccessors.remove(f);
		});
		// create pairs of fields list for any pair of fields accessed by the same
		// method
		createPairsOfFieldsToAccessorsMap();
		// create triples of fields list for any triplet of fields accessed by the same
		// method
		// createTriplesOfFieldsToAccessorsMap();
		if (debug)
			displayDebuggingInfo();
	}

	protected void displayDebuggingInfo() {
		// cl.getMethods().forEach(m -> {
		// System.out.println(m.getActiveBody());
		// });
		System.out.println("@@@ fieldsToAccessors @@@");
		getFieldsToAccessors().forEach((f, list) -> {
			System.out.println("=> " + f.getSignature() + " accessed by ");
			list.forEach(m -> {
				System.out.println(m.getSignature());
			});
		});
		System.out.println("\n@@@ fieldsToReaders @@@");
		getFieldsToReaders().forEach((f, list) -> {
			System.out.println("=> " + f.getSignature() + " read by ");
			list.forEach(m -> {
				System.out.println(m.getSignature());
			});
		});
		System.out.println("\n@@@ fieldsToWriters @@@");
		getFieldsToWriters().forEach((f, list) -> {
			System.out.println("=> " + f.getSignature() + " written by ");
			list.forEach(m -> {
				System.out.println(m.getSignature());
			});
		});
		System.out.println("\n@@@ accessorsToFields ");
		getAccessorsToFields().forEach((m, list) -> {
			System.out.println("=> " + m.getSignature() + " accesses ");
			list.forEach(f -> {
				System.out.println(f.getSignature());
			});
		});
		System.out.println("\n@@@ fieldsPairsToAccessors @@@");
		getFieldsPairsToAccessors().forEach((set, list) -> {
			System.out.println("=> " + set.toArray()[0] + "  ,  " + set.toArray()[1] + " accessed by ");
			list.forEach(e -> {
				System.out.println(e.getSignature());
			});
		});
		System.out.println("\n@@@ fieldsTriplesToAccessors @@@");
		getFieldsTriplesToAccessors().forEach((set, list) -> {
			System.out.println(
					"=> " + set.toArray()[0] + "  ,  " + set.toArray()[1] + set.toArray()[2] + " accessed by ");
			list.forEach(e -> {
				System.out.println(e.getSignature());
			});
		});
	}

	private void createFieldsToReadersMap(Body body) {
		// if (debug)
		// System.out.println("&&& " + "createFieldsToReadersMap" + " &&&");
		List<SootMethod> readersList;
		for (ValueBox box : body.getUseBoxes()) {
			try {
				if (box.getValue() instanceof FieldRef && (box.getValue() instanceof StaticFieldRef
						// && cl.declaresField(((StaticFieldRef)
						// box.getValue()).getField().getSubSignature())
						&& fields.contains(((StaticFieldRef) box.getValue()).getField())
						|| (box.getValue() instanceof InstanceFieldRef
								&& ((InstanceFieldRef) box.getValue()).getBase() == body.getThisLocal()))) {
					SootField f = ((FieldRef) box.getValue()).getField();
					if (!fieldsToReaders.containsKey(f))
						fieldsToReaders.put(f, new ArrayList<SootMethod>());
					readersList = fieldsToReaders.get(f);
					if (!readersList.contains(body.getMethod())) {
						readersList.add(body.getMethod());
						fieldsToReaders.put(f, new ArrayList<>(readersList));
					}
				}
			} catch (Exception e) {

			}
		}
	}

	private void createFieldsToWritersMap(Body body) {
		// if (debug)
		// System.out.println("&&& " + "createFieldsToWritersMap" + " &&&");
		List<SootMethod> writersList;
		for (ValueBox box : body.getDefBoxes()) {
			try {
				if (box.getValue() instanceof FieldRef && (box.getValue() instanceof StaticFieldRef
						// && cl.declaresField(((StaticFieldRef)
						// box.getValue()).getField().getSubSignature())
						&& fields.contains(((StaticFieldRef) box.getValue()).getField())
						|| (box.getValue() instanceof InstanceFieldRef
								&& ((InstanceFieldRef) box.getValue()).getBase() == body.getThisLocal()))) {
					SootField f = ((FieldRef) box.getValue()).getField();
					if (!fieldsToWriters.containsKey(f))
						fieldsToWriters.put(f, new ArrayList<SootMethod>());
					writersList = fieldsToWriters.get(f);
					if (!writersList.contains(body.getMethod())) {
						writersList.add(body.getMethod());
						fieldsToWriters.put(f, new ArrayList<>(writersList));
					}
				}
			} catch (Exception e) {

			}
		}
	}

	private void addArrayTypeWrites(Body body) {
		// if (debug)
		// System.out.println("&&& " + "addArrayTypeWrites" + " &&&");
		List<SootMethod> writersList;
		for (SootField f : getModifiedFieldsOfTypeArray(body)) {
			if (!fieldsToWriters.containsKey(f))
				fieldsToWriters.put(f, new ArrayList<SootMethod>());
			writersList = fieldsToWriters.get(f);
			if (!writersList.contains(body.getMethod())) {
				writersList.add(body.getMethod());
				fieldsToWriters.put(f, new ArrayList<>(writersList));
			}
		}
	}

	private void addObjectMethodWrites(Body body) {
		// if (debug)
		// System.out.println("&&& " + "addObjectMethodWrites" + " &&&");
		List<SootMethod> writersList;
		for (Unit u : body.getUnits()) {
			// Case: the Invoke expression is not assigned to local
			if (u instanceof InvokeStmt) {
				for (ValueBox box : u.getUseBoxes()) {
					if (box instanceof InvokeExprBox && box.getValue() instanceof VirtualInvokeExpr) {
						VirtualInvokeExpr virInvExpr = (VirtualInvokeExpr) box.getValue();
						SootField f = Utils.getFieldFromLocal((Local) virInvExpr.getBase(), body);
						if (f != null) {
							if (!fieldsToWriters.containsKey(f))
								fieldsToWriters.put(f, new ArrayList<SootMethod>());
							writersList = fieldsToWriters.get(f);
							if (!writersList.contains(body.getMethod())) {
								writersList.add(body.getMethod());
								fieldsToWriters.put(f, new ArrayList<>(writersList));
							}
						}
					}
				}
			}

			// Case: The invoke expression is assigned to local (e.g. to return its value)
			// if (u instanceof JAssignStmt) {
			// JAssignStmt st = (JAssignStmt) u;
			// if (st.containsInvokeExpr()) {
			// InvokeExpr inv = st.getInvokeExpr();
			// if (inv instanceof VirtualInvokeExpr) {
			// SootField f = SootHelper.getFieldFromLocal((Local)
			// ((VirtualInvokeExpr)inv).getBase(), body);
			// if (f != null) {
			// if (!fieldsToWriters.containsKey(f))
			// fieldsToWriters.put(f, new ArrayList<SootMethod>());
			// writersList = fieldsToWriters.get(f);
			// if (!writersList.contains(body.getMethod())) {
			// writersList.add(body.getMethod());
			// fieldsToWriters.put(f, new ArrayList<>(writersList));
			// }
			// }
			// }
			// }
			// }
		}
	}

	private void accessorsToTFields(Body body) {
		// if (debug)
		// System.out.println("&&& " + "accessorsToTFields" + " &&&");
		List<SootMethod> accessorsList = new ArrayList<>();
		List<SootField> accessedFieldsList = new ArrayList<>();
		// accessorsToFields && fieldsToAccessors
		if (!accessorsToFields.containsKey(body.getMethod())) {
			accessorsToFields.put(body.getMethod(), new ArrayList<SootField>());
		}
		accessedFieldsList = accessorsToFields.get(body.getMethod());
		for (ValueBox box : body.getUseAndDefBoxes()) {
			try {
				if (box.getValue() instanceof FieldRef
						// && (box.getValue() instanceof StaticFieldRef &&
						// cl.declaresField(((StaticFieldRef)
						// box.getValue()).getField().getSubSignature())
						&& (box.getValue() instanceof StaticFieldRef
								&& fields.contains(((StaticFieldRef) box.getValue()).getField())
								|| (box.getValue() instanceof InstanceFieldRef
										&& ((InstanceFieldRef) box.getValue()).getBase() == body.getThisLocal()))) {
					SootField f = ((FieldRef) box.getValue()).getField();

					if (!accessedFieldsList.contains(f)) {
						accessedFieldsList.add(f);
						accessorsToFields.put(body.getMethod(), new ArrayList<>(accessedFieldsList));
					}

					if (!fieldsToAccessors.containsKey(f))
						fieldsToAccessors.put(f, new ArrayList<SootMethod>());

					accessorsList = fieldsToAccessors.get(f);
					if (!accessorsList.contains(body.getMethod())) {
						accessorsList.add(body.getMethod());
						fieldsToAccessors.put(f, new ArrayList<>(accessorsList));
					}
				}
			} catch (Exception e) {

			}
		}
	}

	private void createPairsOfFieldsToAccessorsMap() {
		// if (debug)
		// System.out.println("&&& " + "createPairsOfFieldsToAccessorsMap" + " &&&");
		Set<SootField> fieldsPair;
		List<SootMethod> accessors1, accessors2, intersection;
		Set<SootField> fieldsAsKeys = fieldsToAccessors.keySet().isEmpty() ? null
				: Collections.unmodifiableSet(new HashSet<SootField>(fieldsToAccessors.keySet()));
		if (fieldsAsKeys != null) {
			for (SootField f1 : fieldsAsKeys) {
				for (SootField f2 : fieldsAsKeys) {
					if (f1 != f2) {
						accessors1 = new ArrayList<>(fieldsToAccessors.get(f1));
						accessors2 = new ArrayList<>(fieldsToAccessors.get(f2));
						intersection = new CopyOnWriteArrayList<>(accessors1);
						intersection.retainAll(accessors2);
						// TODO pairs of fields in constructors and clinit
						// getClass().remove constructors and clinit from intersection
						// clinit causes trouble with classes with very large number of static final
						// fields
						// QualitasCorpus-Compiled/itext-5.0.3/core/com/itextpdf/text/pdf/PdfName.java
						// also i think we should not worry about pairs of fields accessed in either
						// constructors or clinit
						if (!intersection.isEmpty())
							for (SootMethod m : intersection)
								if (m.isStaticInitializer() || m.isConstructor())
									intersection.remove(m);
						if (!intersection.isEmpty()) {
							fieldsPair = new HashSet<SootField>(2);
							fieldsPair.add(f1);
							fieldsPair.add(f2);
							fieldsPairsToAccessors.put(Collections.unmodifiableSet(new HashSet<>(fieldsPair)),
									Collections.unmodifiableList(new ArrayList<>(intersection)));
						}
					}
				}
			}
		}
	}

	private void createTriplesOfFieldsToAccessorsMap() {
		// if (debug)
		// System.out.println("&&& " + "createPairsOfFieldsToAccessorsMap" + " &&&");
		Set<SootField> fieldsTripple, fieldsIntersection;
		Set<SootMethod> accessors1, accessors2, methodsIntersection;

		for (Set<SootField> pair1 : fieldsPairsToAccessors.keySet()) {
			for (Set<SootField> pair2 : fieldsPairsToAccessors.keySet()) {
				// computing intersection of pairs of fields to find triples
				fieldsIntersection = new HashSet<>(pair1);
				fieldsIntersection.retainAll(pair2);
				if (!fieldsIntersection.isEmpty() && fieldsIntersection.size() == 1) {
					// computing intersection of the two sets of methods
					accessors1 = new HashSet<>(fieldsPairsToAccessors.get(pair1));
					accessors2 = new HashSet<>(fieldsPairsToAccessors.get(pair2));
					methodsIntersection = new HashSet<>(accessors1);
					methodsIntersection.retainAll(accessors2);
					// no need to remove init and clinit from lists since they are
					// removed in createPairsOfFieldsToAccessorsMap
					if (!methodsIntersection.isEmpty()) {
						fieldsTripple = new HashSet<SootField>(3);
						fieldsTripple.addAll(pair1);
						fieldsTripple.addAll(pair2);
						assert fieldsTripple.size() == 3 : "Fields triples are not actually triples.";
						fieldsTriplesToAccessors.put(Collections.unmodifiableSet(new HashSet<>(fieldsTripple)),
								Collections.unmodifiableList(new ArrayList<>(methodsIntersection)));
					}

				}
			}
		}

	}

	private Set<SootField> getModifiedFieldsOfTypeArray(Body body) {
		// if (debug)
		// System.out.println("&&& " + "getModifiedFieldsOfTypeArray" + " &&&");
		Set<SootField> modifiedFieldsofTypeArray = new HashSet<>();
		Map<Local, SootField> localsToArrays = new HashMap<>();

		for (Unit unit : body.getUnits()) {
			// case 1: unit contains an explicit ArrayRef
			if (((Stmt) unit).containsArrayRef()) {
				// case 1.1: assignment stmt
				if (unit instanceof AssignStmt) {
					AssignStmt defStmt = (AssignStmt) unit;
					Value lhsOp = defStmt.getLeftOp();
					Value rhsOp = defStmt.getRightOp();
					// case 1.1.1: ArrayRef on lhs
					if (lhsOp instanceof ArrayRef) {
						Local l = (Local) ((ArrayRef) lhsOp).getBase();
						SootField f = Utils.getFieldFromLocal(l, body);
						// if (f != null && cl.declaresField(f.getSubSignature()))
						if (f != null && fields.contains(f))
							modifiedFieldsofTypeArray.add(f);
					}
					// case 1.1.2: ArrayRef on rhs
					// TODO here, we handle write to the ArrayRef and elements
					// in the array the same way.
					// We may need to change this later.
					if (rhsOp instanceof ArrayRef) {
						// first, add the local used to refer to the array
						Local l = (Local) ((ArrayRef) rhsOp).getBase();
						SootField f = Utils.getFieldFromLocal(l, body);
						// if (f !=null && cl.declaresField(f.getSubSignature())) {
						if (f != null && fields.contains(f)) {
							localsToArrays.put(l, f);
							// second, add the local used to refer to the array
							// element
							l = (Local) lhsOp;
							localsToArrays.put(l, f);
						}
					}
				}
			}

			// case 2: unit uses a local that corresponds to ArrayRef expression
			Set<Value> unitUseVals = new HashSet<>();
			unit.getUseBoxes().forEach(b -> unitUseVals.add(b.getValue()));
			unitUseVals.retainAll(localsToArrays.keySet());
			if (!unitUseVals.isEmpty()) {
				for (Value l : unitUseVals)
					modifiedFieldsofTypeArray.add(localsToArrays.get(l));
			}
		}

		return modifiedFieldsofTypeArray;
	}

	private void addOnlyFieldsFromTheClass(Set<SootField> s, Body body, ValueBox _use) {
		if (_use.getValue() instanceof FieldRef) {
			if (_use.getValue() instanceof StaticFieldRef
					&& cl.declaresField(((StaticFieldRef) _use.getValue()).getField().getSubSignature())
					|| (_use.getValue() instanceof InstanceFieldRef
							&& ((InstanceFieldRef) _use.getValue()).getBase() == body.getThisLocal()))
				s.add(((FieldRef) _use.getValue()).getField());
		}
	}

}