package org.cytoscape.examine.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Contains references to Cytoscape services that need to be accessed throughout various parts of the application
 * This can be used to avoid passing those services as arguments throughout the application to keep method signatures shorter and comprehensible
 */
public class CyReferences {
	
	//SINGLETON TEMPLATE
	
	private static CyReferences instance;
	
	public static CyReferences getInstance() {
		if (instance == null) {instance = new CyReferences();}
		return instance;
	}

	private CyNetworkManager networkManager;
	private CyRootNetworkManager rootNetworkManager;
	private CyApplicationManager applicationManager;
	private CyGroupFactory groupFactory;
	private CyGroupManager groupManager;
	private DialogTaskManager taskManager;

	/**
	 * Hidden constructor
	 */
	private CyReferences() {} 
	
	/**
	 * Pseudo-Constructor function, stores all references for later access
	 * @param taskManager 
	 * @param groupFactory 
	 * @param groupManager 
	 * @param applicationManager 
	 * @param rootNetworkManager 
	 * @param networkManager 
	 */
	public void storeReferences(
			CyNetworkManager networkManager, 
			CyRootNetworkManager rootNetworkManager, 
			CyApplicationManager applicationManager, 
			CyGroupManager groupManager, 
			CyGroupFactory groupFactory, 
			DialogTaskManager taskManager
			) {
		this.networkManager = networkManager;
		this.rootNetworkManager = rootNetworkManager;
		this.applicationManager = applicationManager;
		this.groupManager = groupManager;
		this.groupFactory = groupFactory;
		this.taskManager = taskManager;
	}
	
	//SETTER AND GETTER METHODS//
	
	public CyNetworkManager getNetworkManager() {return networkManager;}
	public CyRootNetworkManager getRootNetworkManager() {return rootNetworkManager;}
	public CyApplicationManager getApplicationManager() {return applicationManager;}
	public CyGroupManager getGroupManager() {return groupManager;}
	public CyGroupFactory getGroupFactory() {return groupFactory;}
	public DialogTaskManager getTaskManager() {return taskManager;}

	
}
