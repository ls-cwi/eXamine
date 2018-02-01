package org.cytoscape.examine.internal.tasks;

/**
 * Describes commands for the eXamine application that can be invoked via REST/CyREST
 *
 */
public enum ExamineCommand {
	
	GENERATE_GROUPS, /**Generates groups from TODO: Add info*/
	REMOVE_GROUPS; /**Removes all groups for a specific network*/

	@Override
	public String toString() {
		if (this==GENERATE_GROUPS) {
			return "generate groups";
		}
		else if (this == REMOVE_GROUPS) {
			return "remove groups";
		}
		return "INVALID_COMMAND";
	}

	public String getDescription() {
		if (this==GENERATE_GROUPS) {
			return "Generates eXamine groups from a given set of columns";
		}
		else if (this == REMOVE_GROUPS) {
			return "Removes all eXamine groups from the current session";
		}
		return "INVALID_COMMAND";
	}
}


