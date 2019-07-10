/**
 * 
 */
package tsfinder.stats;

import java.util.HashMap;
import java.util.Map;

import tsfinder.Label;

/**
 * @author Andrew Habib
 *
 */
public class FeatureVector {

	String name;
	Map<Feature, Double> features;
	Label label;

	public FeatureVector(String name) {
		this.name = name;
		this.features = new HashMap<Feature, Double>();
	}

	String getName() {
		return this.name;
	}

	String getFeaturesAsString() {
		String ret = "";
		for (Feature f : Feature.values())
			ret += this.features.get(f) + ",";
		return ret;
	}

	String getVectorAsString() {
		return "'" + getName() + "'" + "," + getFeaturesAsString() + label;
	}

}
