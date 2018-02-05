package org.cytoscape.examine.internal;


import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.settings.NetworkSettings;
import org.cytoscape.examine.internal.visualization.InteractiveVisualization;
import org.cytoscape.examine.internal.visualization.SnapshotVisualization;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.util.ListMultipleSelection;

import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	/**
	 * Create a data set for consumption by eXamine.
	 */
	private static DataSet createDataSet(CyServices services, NetworkSettings networkSettings) {
		return new DataSet(
				services.getApplicationManager().getCurrentNetwork(),
				services.getGroupManager(),
				networkSettings.getSelectedLabelColumnName(),
				networkSettings.getSelectedURLColumnName(),
				networkSettings.getSelectedScoreColumnName(),
				networkSettings.getSelectedGroupColumnNames(),
				networkSettings.getSelectedGroupColumnSizes()
		);
	}

	/**
	 * Create a data model for keeping track of interaction state.
	 */
	private static Model createModel(DataSet dataSet, CyServices services, NetworkSettings networkSettings) {
		return new Model(
				dataSet,
				services.getApplicationManager(),
				services.getVisualMappingManager(),
				services.getGroupManager(),
				networkSettings.getShowScore(),
				networkSettings.getGroupSelection());
	}

	/**
	 * Open a new visualization window.
	 */
	public static void openVisualizationWindow(CyServices services, NetworkSettings networkSettings) {
		final DataSet dataSet = createDataSet(services, networkSettings);
		final Model model = createModel(dataSet, services, networkSettings);

		new InteractiveVisualization(dataSet, model);
	}

	/**
	 * Export the visualization as an image.
	 */
	public static void exportVisualization(CyServices services, NetworkSettings networkSettings) {
		final DataSet dataSet = createDataSet(services, networkSettings);
		final Model model = createModel(dataSet, services, networkSettings);
		final SnapshotVisualization visualization = new SnapshotVisualization(dataSet, model);

		// Target file via dialog.
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setName("Export SVG");
		fileChooser.setSelectedFile(new File("eXamine-export.svg"));
		int fileConfirm = fileChooser.showSaveDialog(null);

		if (fileConfirm == JFileChooser.APPROVE_OPTION) {
			try {
				visualization.exportSVG(fileChooser.getSelectedFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
