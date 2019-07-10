/**
 * 
 */
package tsfinder.graphs.factory;

/**
 * @author Andrew Habib
 * 
 *         Types of graph nodes where the first attribute is the label and the
 *         second attribute(action: giv/inf) is meant for Nice2 framework.
 *
 */
public enum NodeType {
	INIT("init", "giv"),
	METHOD("m", "giv"),
	SYN("SYN", "inf"),
	notSYN("notSYN", "inf"),
	Public("pub", "giv"),
	Private("pub", "giv"),
	Protected("protected", "giv"),
	
	FIELD("f", "giv"),
	VOL("VOL", "inf"),
	notVOL("notVOL", "inf"),
	FIN("FIN", "inf"),
	notFIN("notFIN", "inf"),
	
	VOL_FIELD("VolField", "inf"),		
	FIN_FIELD("FinField", "inf"),
	TS_FIELD("TSField", "giv"),
	
	LOCK("lock", "inf"),
	
	notPublic("notPUB", "giv"),
	CLINIT("clinit", "giv"),
	STATIC("static", "giv"),
	notSTATIC("notStatic", "giv"),
	
	// nodes for relations instead of edges 
	READS("reads", "giv"),
	WRITES("writes", "giv"),
	CALLS("calls", "giv"),
	MOD("modifier", "giv"),
	isSYN("synced", "giv"),
	isTS("TSfield", "giv"),
	
	// backward edges
	READ_BY("read-by", "giv"),
	WRITTEN_BY("written-by", "giv"),
	CALLED_BY("called-by", "giv")
	;

	private final String label;
	private final String nice2Tag;

	NodeType(String label, String action) {
		this.label = label;
		this.nice2Tag = action;
	}

	public String getLabel() {
		return label;
	}

	public String getNice2Tag() {
		return nice2Tag;
	}
}
