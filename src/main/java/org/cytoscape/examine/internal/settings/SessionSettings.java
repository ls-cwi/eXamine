package org.cytoscape.examine.internal.settings;

import org.cytoscape.model.CyNetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores settings for a session, including settings per network.
 */
public class SessionSettings {

    // Network settings, maintained for several networks as to accommodate
    // storing and retrieval of settings upon network selection change
    private final Map<Long, NetworkSettings> networkSettings = new HashMap<Long,NetworkSettings>();

    public SessionSettings() {

    }

    public NetworkSettings getNetworkSettings(CyNetwork network) {
    	if (network != null) {
    		//Return the network settings if already existing, else generate a new object
            return networkSettings.computeIfAbsent(network.getSUID(), id -> new NetworkSettings(network));
    	}
    	else {
    		System.out.println("Attempted to get network settings for a network that is null");
    		return null;
    	}
    }

    public Set<Long> getCachedNetworkUUIDs() {
        return networkSettings.keySet();
    }

    public void removeNetworkSettings(long networkUUID) {
        networkSettings.remove(networkUUID);
    }

}
