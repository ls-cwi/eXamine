package org.cytoscape.examine.internal.tasks;

/**
 * Describes commands for the eXamine application that can be invoked via REST/CyREST
 *
 */
public enum ExamineCommand {
	
	GENERATE_GROUPS, /**Generates groups from TODO: Add info*/
	REMOVE_GROUPS;

	@Override
	public String toString() {
		if (this==GENERATE_GROUPS) {
			return "generate groups";
		}
		return "INVALID_COMMAND";
	}
}


