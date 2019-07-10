/**
 * 
 */
package tsfinder.graphs.fieldfocused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * @author Andrew Habib
 *
 */
public class FlatClass {

	SootClass cl;

	List<SootClass> heirarchy;

	List<SootMethod> methods = new ArrayList<>();

	List<SootField> fields = new ArrayList<>();

	boolean isFlat = false;

	public FlatClass(SootClass clazz) {

		cl = clazz;

		// Add all methods and fields of the analyzed class
		fields.addAll(cl.getFields());
		methods.addAll(cl.getMethods());
		// for (SootMethod m : cl.getMethods()) {
		// if (!m.isConstructor() && !m.isStaticInitializer())
		// methods.add(m);
		// }

		// Get class heirarchy starting from the class parent
		heirarchy = getHeirarchy();

		// Initialize map from Java signatures to Soot signatures
		Map<String, String> mJavaSignToSootSign = new HashMap<>();
		for (SootMethod m : methods) {
			String mSubsign = m.getSubSignature();
			String mJSign = mSubsign.substring(mSubsign.indexOf(' ') + 1);
			mJavaSignToSootSign.put(mJSign, m.getSignature());
		}

		for (SootClass c : heirarchy) {
			// methods.addAll(c.getMethods());
			// fields.addAll(c.getFields());
			for (SootField f : c.getFields()) {
				// if (!Modifier.isPrivate(f.getModifiers()))
				fields.add(f);
			}
			for (SootMethod m : c.getMethods()) {
				// if (m.isStaticInitializer() || (m.isConstructor() &&
				// !Modifier.isPrivate(m.getModifiers()))) {
				if (m.isStaticInitializer() || m.isConstructor()) {
					methods.add(m);
				} else if (!m.isConstructor() && !m.isStaticInitializer() && !m.isPrivate()) {
					String mthSubsign = m.getSubSignature();
					String mthJavaSign = mthSubsign.substring(mthSubsign.indexOf(' ') + 1);
					if (!mJavaSignToSootSign.containsKey(mthJavaSign)) {
						mJavaSignToSootSign.put(mthJavaSign, m.getSignature());
						methods.add(m);
					}
				}

			}
		}
	}

	private List<SootClass> getHeirarchy() {

		List<SootClass> list = new ArrayList<>();
		SootClass c = cl.getSuperclass();

		// if (c.isJavaLibraryClass()) {
		// Scene.v().addBasicClass(c.getName(), SootClass.BODIES);
		// Scene.v().loadClassAndSupport(c.getName());
		// }
		// else {
		// Scene.v().loadClassAndSupport(c.getName());
		// }

		while (c.getName() != "java.lang.Object" && !c.isInterface() && !c.isPhantom() && c != null) {
			// while (c.getName() != "java.lang.Object" && !c.isInterface() && c != null) {

			list.add(c);
			c = c.getSuperclass();
			// if (c.isJavaLibraryClass()) {
			// Scene.v().addBasicClass(c.getName(), SootClass.BODIES);
			// Scene.v().loadClassAndSupport(c.getName());
			// }
			// else {
			// Scene.v().loadClassAndSupport(c.getName());
			// }
		}

		if (c.isPhantom() || c == null) {
			// if (c == null) {
			isFlat = false;
			return Collections.emptyList();
		} else {
			isFlat = true;
			return list;
		}
	}

	public List<SootMethod> getMethodsList() {
		return methods;
	}

	public List<SootField> getFieldsList() {
		return fields;
	}

	public boolean isClFlat() {
		return isFlat;
	}

}
