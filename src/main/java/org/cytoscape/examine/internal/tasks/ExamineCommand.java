package org.cytoscape.examine.internal.tasks;

/**
 * Describes commands for the eXamine application that can be invoked via REST/CyREST
 */
public enum ExamineCommand {

    // TODO: Add info.
    GENERATE_GROUPS(
            "generate groups",
            "Generates eXamine groups from a given set of columns."
    ),
    REMOVE_GROUPS(
            "remove groups",
            "Removes all eXamine groups from the current session."
    ),
    UPDATE_SETTINGS(
            "update settings",
            "Updates settings for the visualization, such as label and score columns"
    ),
    INTERACT(
            "interact",
            "Shows the selected network and eXamine groups in an interactive visualization."
    );

    private final String name;
    private final String description;

    ExamineCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}


