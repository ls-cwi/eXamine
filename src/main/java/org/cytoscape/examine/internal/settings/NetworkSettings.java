package org.cytoscape.examine.internal.settings;

import org.cytoscape.examine.internal.Constants;
import org.cytoscape.examine.internal.Constants.Selection;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Stores network settings, including selected columns and column names.
 *
 * @author melkebir
 */
public class NetworkSettings {

	/**
	 * Contains a list of columns that are tracked by the Settings
	 */
    private final List<CyColumn> trackedColumns = new ArrayList<>();

    private List<CyColumn> allGroupColumns = new ArrayList<>();
    private HashMap<CyColumn, Integer> allGroupColumnSizes = new HashMap<>();

    private List<CyColumn> allStringColumns = new ArrayList<>();
    private List<CyColumn> allDoubleColumns = new ArrayList<>();
    private List<CyColumn> selectedGroupColumns = new ArrayList<>();

    private CyColumn selectedLabelColumn = null;
    private CyColumn selectedURLColumn = null;

    private CyColumn selectedScoreColumn = null;

    private Selection groupSelection = Selection.NONE;
    private boolean showScore;

    public NetworkSettings(CyNetwork network) {

        for (CyColumn c : network.getDefaultNodeTable().getColumns()) {
            addColumn(c);
        }

        showScore = allDoubleColumns.size() > 0;
    }

    /**
     * Helper function that retrievs a String list of all names for a given set of CyColumns
     * @param columns
     * @return
     */
    private List<String> getColumnNames(List<CyColumn> columns) {
        ArrayList<String> res = new ArrayList<>();
        for (CyColumn i : columns) {
            res.add(i.getName());
        }
        return res;
    }

    public boolean existsColumn(CyColumn c) {
        return trackedColumns.contains(c);
    }

    /**
     * Adds a column to the current network settings.
     */
    public void addColumn(CyColumn c) {
    	
        if (c == null) {
        	//Why would this ever be the case?
        	System.out.println("Attempting to add a column that is null to the network settings!");
            return;
        }
        
        if (existsColumn(c))
            return;

        trackedColumns.add(c);

        if (c.getListElementType() == String.class) {
            allGroupColumns.add(c);
            selectedGroupColumns.add(c);
            allGroupColumnSizes.put(c, Constants.CATEGORY_MAX_SIZE);
        } else if (c.getType() == String.class) {
            allStringColumns.add(c);
        } else if (c.getType() == Double.class) {
            allDoubleColumns.add(c);
        }

        // Column presets.
        if (c.getType() == String.class) {
            if (c.getName().equals("URL") && selectedURLColumn == null) {
                selectedURLColumn = c;
            } else if (c.getName().equals("Symbol") && selectedLabelColumn == null) {
                selectedLabelColumn = c;
            }
        } else if (c.getType() == Double.class) {
            if (c.getName().equals("Score") && selectedScoreColumn == null) {
                selectedScoreColumn = c;
            }
        }
    }


    /**
     * Deletes column with name deletedColumnName.
     */
    public void deleteColumn(CyColumn deletedColumn) {

        if (trackedColumns.contains(deletedColumn)) {
            trackedColumns.remove(deletedColumn);
        }
        else {
        	System.out.println("Trying to delete column: "+deletedColumn.getName()+", which wasn't registered ...");
        	return;
        }

        trackedColumns.remove(deletedColumn);

        if (selectedLabelColumn == deletedColumn) selectedLabelColumn = null;
        else if (selectedURLColumn == deletedColumn) selectedURLColumn = null;

        allStringColumns.remove(deletedColumn);

        if(selectedScoreColumn == deletedColumn) selectedScoreColumn = null;

        allDoubleColumns.remove(deletedColumn);

        for (CyColumn grpClmn : allGroupColumns) {
            if (grpClmn == deletedColumn) {
                if (selectedGroupColumns.contains(deletedColumn)) {
                    selectedGroupColumns.remove(deletedColumn);
                }
                break;
            }
        }
        allGroupColumns.remove(deletedColumn);
    }

    public CyColumn getSelectedLabelColumn() {
        return selectedLabelColumn;
    }

    public void setSelectedLabelColumn(CyColumn c) {
        this.selectedLabelColumn = c;
    }

    public String getSelectedLabelColumnName() {
        return selectedLabelColumn.getName();
    }

    public CyColumn getSelectedURLColumn() {
        return selectedURLColumn;
    }

    public void setSelectedURLColumn(CyColumn c) {
        this.selectedURLColumn = c;
    }

    public String getSelectedURLColumnName() {
        return selectedURLColumn.getName();
    }

    public CyColumn getSelectedScoreColumn() {
        return selectedScoreColumn;
    }

    public void setSelectedScoreColumn(CyColumn c) {
        this.selectedScoreColumn = c;
    }

    public String getSelectedScoreColumnName() {
        if (allDoubleColumns.size() == 0) {
            return null;
        } else {
            return selectedScoreColumn.getName();
        }
    }

    public List<CyColumn> getSelectedGroupColumns() {
        return selectedGroupColumns;
    }

    public List<String> getSelectedGroupColumnNames() {
        return getColumnNames(selectedGroupColumns);
    }

    public List<Integer> getSelectedGroupColumnSizes() {
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (CyColumn i : selectedGroupColumns) {
            res.add(allGroupColumnSizes.get(i));
        }
        return res;
    }

    public void setSelectedGroupColumns(List<CyColumn> selectedGroupColumns) {
        this.selectedGroupColumns = selectedGroupColumns;
    }

    public List<String> getColumnNames() {
    	ArrayList<String> names = new ArrayList<String>();
    	for (CyColumn c : trackedColumns) {
    		names.add(c.getName());
    	}
        return names;
    }

    public List<CyColumn> getAllGroupColumns() {
        return allGroupColumns;
    }

    public List<CyColumn> getAllStringColumns() {
        return allStringColumns;
    }

    public List<CyColumn> getAllDoubleColumns() {
        return allDoubleColumns;
    }

    public Constants.Selection getGroupSelectionMode() {
        return groupSelection;
    }

    public void setGroupSelection(Constants.Selection idxGroupSelection) {
        this.groupSelection = idxGroupSelection;
    }

    public boolean getShowScore() {
        return showScore;
    }

    public void setShowScore(boolean showScore) {
        this.showScore = showScore;
    }

	public void setGroupColumnSize(CyColumn key, Integer size) {
		this.allGroupColumnSizes.put(key, size);
	}

	public int getColumnSize(CyColumn col) {
		if (this.allGroupColumnSizes.containsKey(col)) {
			return this.allGroupColumnSizes.get(col);
		}
		System.out.println("Trying to receive column size for a column that was not registered with the NetworkSettings object (Column: "+col.getName()+")");
		return 0;
	}

}
