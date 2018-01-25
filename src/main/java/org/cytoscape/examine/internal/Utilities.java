package org.cytoscape.examine.internal;


import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.util.ListMultipleSelection;

public class Utilities {

	/**
	 * Helper function that fetches all column names for the node table associated with a network during runtime
	 * @param network The network for which the column list is to be populated
	 * @return A ListMultipleSelection<String> with all the columns in the node list
	 */
	public static ListMultipleSelection<String> populateColumnList(CyNetwork network) {
		ListMultipleSelection<String> ret = new ListMultipleSelection<String>();
		Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns();
		ArrayList<String> columnNames = new ArrayList<String>();
		
		//TODO: Only show group columns as valid options?

		for (CyColumn col : columns) {
			columnNames.add(col.getName());
		}
		
		ret.setPossibleValues(columnNames);
		
		return ret;
	}

}
