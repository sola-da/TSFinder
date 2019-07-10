/**
 * 
 */
package tsfinder.graphs.factory;

/**
 * @author Andrew Habib
 * 
 *         Types of graph edges where the first attribute is the label.
 *
 */
public enum EdgeType { 			// ordinal value
	READS("reads"),				// 0
	WRITES("writes"),			// 1
	CALLS("calls"),				// 2
	isSYN("isSYN"),				// 3
	
	isVOL("isVOL"),				// 4
	isFIN("isFIN"),				// 5
	
	locksOn("locksOn"),			// 6
	
	PROTECTS("protects"), 		// 7
	MODIFIES("modifies"),		// 8
	
	isVisible("isVisible"),		// 10
	
	isStatic("isStatic"),		// 11
	
	NOLABEL("")
	;

	private final String label;

	EdgeType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
