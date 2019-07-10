/**
 * 
 */
package tsfinder.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.Modifier;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import tsfinder.Label;
import tsfinder.Utils;

/**
 * @author Andrew Habib
 *
 */
public class StatsTransformer extends SceneTransformer {

	List<String> classesToAnalyze;
	List<FeatureVector> analysisResults;

	public StatsTransformer(List<String> classesToAnalyze) {
		this.classesToAnalyze = classesToAnalyze;
		this.analysisResults = new ArrayList<FeatureVector>();
	}

	public void addLabelsToVectors(List<String> tsClasses, List<String> ntsClasses) {
		for (FeatureVector vec : analysisResults) {
			if (tsClasses.contains(vec.getName()))
				vec.label = Label.TS;
			else if (ntsClasses.contains(vec.getName()))
				vec.label = Label.nTS;
			else
				System.err.println("Class: " + vec.getName() + "does not have a known label.");
		}
	}

	public List<FeatureVector> getAnalysisResults() {
		return analysisResults;
	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		for (SootClass cls : Scene.v().getClasses()) {
			if (classesToAnalyze.contains(cls.getName())) {
				FeatureVector v = ComputeClassStats(cls);
				analysisResults.add(v);
			}
		}

	}

	private FeatureVector ComputeClassStats(SootClass cls) {
		FeatureVector v = new FeatureVector(cls.getName());

		// Fields stats
		double fieldsCount = 0;
		double pubFieldsCount = 0;
		double volFieldsCount = 0;
		double volPubFieldsCount = 0;
		double finFieldsCount = 0;
		double finPubFieldsCount = 0;
		double percentPubFields, percentVolFields, percentVolPubFields, percentFinFields, percentFinPubFields;

		for (SootField f : cls.getFields()) {

			fieldsCount++;

			if (Modifier.isPublic(f.getModifiers()))
				pubFieldsCount++;

			if (Modifier.isVolatile(f.getModifiers())) {
				volFieldsCount++;
				if (Modifier.isPublic(f.getModifiers()))
					volPubFieldsCount++;
			}

			if (Modifier.isFinal(f.getModifiers())) {
				finFieldsCount++;
				if (Modifier.isPublic(f.getModifiers()))
					finPubFieldsCount++;
			}
		}
		percentPubFields = fieldsCount == 0 ? 0 : pubFieldsCount / fieldsCount;
		percentVolFields = fieldsCount == 0 ? 0 : volFieldsCount / fieldsCount;
		percentVolPubFields = pubFieldsCount == 0 ? 0 : volPubFieldsCount / fieldsCount;
		percentFinFields = fieldsCount == 0 ? 0 : finFieldsCount / fieldsCount;
		percentFinPubFields = pubFieldsCount == 0 ? 0 : finPubFieldsCount / fieldsCount;

//		v.features.put(Feature.FIELDS, fieldsCount);

//		v.features.put(Feature.pubFIELDS, pubFieldsCount);
//		v.features.put(Feature.volFIELDS, volFieldsCount);
//		v.features.put(Feature.volPubFIELDS, volPubFieldsCount);
//		v.features.put(Feature.finFIELDS, finFieldsCount);
//		v.features.put(Feature.finPubFIELDS, finPubFieldsCount);

		v.features.put(Feature.percPubFIELDS, percentPubFields);
		v.features.put(Feature.percVolFIELDS, percentVolFields);
		v.features.put(Feature.percVolPubFIELDS, percentVolPubFields);
		v.features.put(Feature.percFinFIELDS, percentFinFields);
		v.features.put(Feature.percFinPubFIELDS, percentFinPubFields);

		// Methods stats
		double methodsCount = 0;
		double pubMethodsCount = 0;
		double methodsSyncedCount = 0;
		double methodsWithSyncBlocksCount = 0;
		double pubMethodsSyncedCount = 0;
		double pubMethodsWithSyncBlocksCount = 0;
		double percentPub, percentSyn, percentPubSyn, percentPubSynM, percentPubSynB;

		for (SootMethod m : cls.getMethods()) {
			methodsCount++;

			if (Modifier.isPublic(m.getModifiers()))
				pubMethodsCount++;

			if (Modifier.isSynchronized(m.getModifiers())) {
				methodsSyncedCount++;
				if (Modifier.isPublic(m.getModifiers()))
					pubMethodsSyncedCount++;
			}

			if (Utils.hasSyncBlock(m)) {
				methodsWithSyncBlocksCount++;
				if (Modifier.isPublic(m.getModifiers()))
					pubMethodsWithSyncBlocksCount++;
			}
		}
		percentPub = methodsCount == 0 ? 0 : pubMethodsCount / methodsCount;
		percentSyn = methodsCount == 0 ? 0 : (methodsSyncedCount + methodsWithSyncBlocksCount) / methodsCount;
		percentPubSyn = pubMethodsCount == 0 ? 0
				: (pubMethodsSyncedCount + pubMethodsWithSyncBlocksCount) / methodsCount;
		percentPubSynM = pubMethodsCount == 0 ? 0 : pubMethodsSyncedCount / methodsCount;
		percentPubSynB = pubMethodsCount == 0 ? 0 : pubMethodsWithSyncBlocksCount / methodsCount;

//		v.features.put(Feature.METHODS, methodsCount);
//		v.features.put(Feature.pubMETHODS, pubMethodsCount);
//		v.features.put(Feature.syncMETHODS, (methodsSyncedCount + methodsWithSyncBlocksCount));
//		v.features.put(Feature.pubSyncMETHODS, (pubMethodsSyncedCount + pubMethodsWithSyncBlocksCount));
//		v.features.put(Feature.pubSyncMETHODS_M, pubMethodsSyncedCount);
//		v.features.put(Feature.pubSyncMETHODS_B, pubMethodsWithSyncBlocksCount);

		v.features.put(Feature.percPubMETHODS, percentPub);
		v.features.put(Feature.percSyncMETHODS, percentSyn);
		v.features.put(Feature.percPubSyncMETHODS, percentPubSyn);
//		v.features.put(Feature.percPubSyncMETHODS_M, percentPubSynM);
//		v.features.put(Feature.percPubSyncMETHODS_B, percentPubSynB);

		return v;
	}

}
