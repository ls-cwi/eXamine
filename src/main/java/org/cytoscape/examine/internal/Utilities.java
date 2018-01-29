package org.cytoscape.examine.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
		

		//Is this the correct way for identifying groups columns?
		for (CyColumn col : columns) {
			//First we check if the column contains a list
			if (col.getType() == List.class) {
				//System.out.println(col.getName());
				//For an eXamine group column we want a list of Strings
				if (col.getListElementType() == String.class) {
					columnNames.add(col.getName());
				}
			}
		}
		
		ret.setPossibleValues(columnNames);
		
		return ret;
	}

}
