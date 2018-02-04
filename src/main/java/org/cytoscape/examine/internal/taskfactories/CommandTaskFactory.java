package org.cytoscape.examine.internal.taskfactories;

import org.cytoscape.examine.internal.CyServices;
import org.cytoscape.examine.internal.tasks.ExamineCommand;
import org.cytoscape.examine.internal.tasks.GenerateGroups;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * This class describes a TaskFactory that generates ObservableTask instances for a given command, invoked via CyRest
 */
public class CommandTaskFactory implements TaskFactory {

	private final CyServices services;

	// The command for which the task factory generates tasks.
	private final ExamineCommand command;

	/**
	 * Default constructor, creates a TaskFactory that generates instances of the task associated with the given command
	 */
	public CommandTaskFactory(CyServices services, ExamineCommand command) {
		this.services = services;
		this.command = command;
	}

	public TaskIterator createTaskIterator() {
		//We simply switch between the possible commands
		if (command == ExamineCommand.GENERATE_GROUPS) {
			return new TaskIterator(
					new GenerateGroups(services)
					);
		}
		else return null; //TODO: Might be useful to generate an error/ throw an exception here as this should never be invoked
	}

	//TODO: Think about when that would actually make sense / prevent launching of multiple tasks that shouldn't be running parallel
	public boolean isReady () { return true; } 
}
