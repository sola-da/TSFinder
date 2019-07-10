/**
 * 
 */
package tsfinder.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import tsfinder.Label;

/**
 * @author Andrew Habib
 *
 */
public class ARFFformatter {
	File path;
	BufferedWriter writer;

	public ARFFformatter(String path) {
		this.path = new File(path);
		this.path.getParentFile().mkdirs();
		try {
			this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.path, false), "utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveResultsToDisk(List<FeatureVector> results) {
		try {
			writer.write("@relation TSFinder:stats");
			writer.newLine();
			writer.newLine();

			writer.write("@attribute className string");
			writer.newLine();
			
			for (Feature f : Feature.values()) {
				writer.write("@attribute " + f + " numeric");
				writer.newLine();
			}
			writer.write("@attribute label {" + Label.TS + "," + Label.nTS + "}");
			writer.newLine();
			writer.newLine();

			writer.write("@data");
			writer.newLine();
			for (FeatureVector v : results) {
				writer.write(v.getVectorAsString());
				writer.newLine();
			}
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
