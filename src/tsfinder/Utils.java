/**
 * 
 */
package tsfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;
import soot.jimple.MonitorStmt;

/**
 * @author Andrew Habib
 *
 */
public class Utils {

	public static List<String> ReadListFromFile(String path) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (lines == null)
			System.out.println("Warning: File at " + path + "is empty.");
		return lines;
	}

	public static String[] GetSootArgs() {
		String args = "";
//		args += "-w ";
//		args += "-allow-phantom-refs ";
		args += "-p cg ";
		args += "library:any-subtype,all-reachable:true ";
		args += "include-all ";
		args += "-w ";
		args += "-full-resolver ";
		args += "-allow-phantom-refs ";
		args += "-f n ";
		args += "-cp " + Config.SOOT_CP + " ";
		args += "-process-dir ";
		args += Config.DIR_TO_ANALYZE;
		System.out.println(args);
		return args.split(" ");
	}

	public static boolean hasSyncBlock(SootMethod m) {
		// RuntimeException thrown here is meant to
		// prevent the analysis from crashing when
		// m.retrieveActiveBody() throws an exception
		// when it can't get a method body.
		if (m.isConcrete()) {
			JimpleBody jbody = (JimpleBody) m.retrieveActiveBody();
			Iterator<Unit> it = jbody.getUnits().iterator();
			while (it.hasNext()) {
				if (it.next() instanceof MonitorStmt) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Careful when using this as it may return null if the local does not
	 * correspond to any class field.
	 * 
	 * @param l
	 * @param body
	 * @return SootField or null
	 */
	public static SootField getFieldFromLocal(Local l, Body body) {
		SootClass cl = body.getMethod().getDeclaringClass();
		List<SootField> hierarchyFields = new ArrayList<>();
		while (cl.getName() != "java.lang.Object" && !cl.isPhantom() && cl != null) {
			hierarchyFields.addAll(cl.getFields());
			cl = cl.getSuperclass();
		}
		DefinitionStmt defStmt;
		for (Unit u : body.getUnits()) {
			if (u instanceof DefinitionStmt) {
				defStmt = (DefinitionStmt) u;
				Value lhsOp = defStmt.getLeftOp();
				if (lhsOp == l) {
					Value rhsOp = defStmt.getRightOp();
					if (rhsOp instanceof FieldRef) {
						SootField f = ((FieldRef) rhsOp).getField();
						if (hierarchyFields.contains(f))
							return f;
					} else if (rhsOp instanceof ArrayRef)
						return getFieldFromLocal((Local) ((ArrayRef) rhsOp).getBase(), body);
				}
			}
		}
		return null;
	}

}
