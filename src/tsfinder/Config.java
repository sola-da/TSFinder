/**
 * 
 */
package tsfinder;

/**
 * @author Andrew Habib
 *
 */
public class Config {

	// Benchmark paths
	public static String THREAD_SAFE_CLASSES = "benchmark/thread-safe-classes.txt";
	public static String THREAD_UNSAFE_CLASSES = "benchmark/thread-unsafe-classes.txt";

	public static String DIR_TO_ANALYZE = "benchmark/jdk-8u152-linux-x64_rt.jar";
//	public static String DIR_TO_ANALYZE = "paper.example.bin";

	// Output path
	public static String OUTPUT_DIR = "output/";
	
	// Soot cp
	public static String SOOT_CP = "benchmark/jdk-8u152-linux-x64_rt.jar";
	
}
