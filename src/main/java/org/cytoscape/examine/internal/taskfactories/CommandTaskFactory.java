package org.cytoscape.examine.internal.taskfactories;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.examine.internal.settings.SessionSettings;
import org.cytoscape.examine.internal.tasks.ExamineCommand;
import org.cytoscape.examine.internal.tasks.ExportImage;
import org.cytoscape.examine.internal.tasks.GenerateGroups;
import org.cytoscape.examine.internal.tasks.Interact;
import org.cytoscape.examine.internal.tasks.SelectGroups;
import org.cytoscape.examine.internal.tasks.UpdateSettings;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * This class describes a TaskFactory that generates ObservableTask instances for a given command, invoked via CyRest
 */
public class CommandTaskFactory implements TaskFactory {

	private final CyServices services;
	private final SessionSettings settings;

	// The command for which the task factory generates tasks.
	private final ExamineCommand command;

	/**
	 * Default constructor, creates a TaskFactory that generates instances of the task associated with the given command
	 */
	public CommandTaskFactory(CyServices services, SessionSettings sessionSettings, ExamineCommand command) {
		this.services = services;
		this.settings = sessionSettings;
		this.command = command;
	}

	public TaskIterator createTaskIterator() {
		// We simply switch between the possible commands
		switch(command) {
			case GENERATE_GROUPS:	return new TaskIterator(new GenerateGroups(services));
			case UPDATE_SETTINGS:	return new TaskIterator(new UpdateSettings(services, settings));
			case SELECT_GROUPS:		return new TaskIterator(new SelectGroups(services));
			case INTERACT:			return new TaskIterator(new Interact(services, settings));
			case EXPORT:			return new TaskIterator(new ExportImage(services, settings));
			default:				return null;
		}
		//TODO: Might be useful to generate an error/ throw an exception here as this should never be invoked
	}

	//TODO: Think about when that would actually make sense / prevent launching of multiple tasks that shouldn't be running parallel
	public boolean isReady () { return true; } 
}
