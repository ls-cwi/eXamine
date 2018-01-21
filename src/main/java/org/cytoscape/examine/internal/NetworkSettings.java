package org.cytoscape.examine.internal;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores network settings, including selected columns and column names.
 *
 * @author melkebir
 */
class NetworkSettings {

    private Long networkSUID;
    private List<String> columnNames;
    private Set<String> columnNamesSet = new HashSet<String>();
    private List<Integer> allGroupColumns = new ArrayList<Integer>();
    private List<Integer> allGroupColumnSizes = new ArrayList<Integer>();
    private List<Integer> allStringColumns = new ArrayList<Integer>();
    private List<Integer> allDoubleColumns = new ArrayList<Integer>();
    private List<Integer> selectedGroupColumns = new ArrayList<Integer>();
    private int selectedLabelColumn = 0;
    private int selectedURLColumn = 0;
    private int selectedScoreColumn = 0;
    private Constants.Selection groupSelection = Constants.Selection.NONE;
    private boolean showScore;

    public NetworkSettings(CyNetwork network) {
        this.networkSUID = network.getSUID();

        // columnNames
        List<CyColumn> columns = new ArrayList<CyColumn>();
        columns.addAll(network.getDefaultNodeTable().getColumns());

        columnNames = new ArrayList<String>(columns.size());

        int i = 0;
        for (CyColumn c : columns) {
            columnNames.add(c.getName());
            columnNamesSet.add(c.getName());

            if (c.getListElementType() == String.class) {
                if (c.getName().equals("Pathway")) {
                    selectedGroupColumns.add(allGroupColumns.size());
                }
                allGroupColumns.add(i);
                allGroupColumnSizes.add(Constants.CATEGORY_MAX_SIZE);
            } else if (c.getType() == String.class) {
                if (c.getName().equals("URL")) {
                    selectedURLColumn = allStringColumns.size();
                } else if (c.getName().equals("Symbol")) {
                    selectedLabelColumn = allStringColumns.size();
                }
                allStringColumns.add(i);
            } else if (c.getType() == Double.class) {
                if (c.getName().equals("Score")) {
                    selectedScoreColumn = allDoubleColumns.size();
                }
                allDoubleColumns.add(i);
            }

            i++;
        }

        showScore = allDoubleColumns.size() > 0;
    }

    private List<String> getColumnNames(List<Integer> indices) {
        ArrayList<String> res = new ArrayList<String>();
        for (Integer i : indices) {
            res.add(columnNames.get(i));
        }
        return res;
    }

    public boolean existsColumn(String columnName) {
        return columnNamesSet.contains(columnName);
    }

    /**
     * Adds a column to the current network settings.
     *
     * @param network
     * @param addedColumnName
     */
    public void addColumnName(CyNetwork network, String addedColumnName) {
        CyColumn c = network.getDefaultNodeTable().getColumn(addedColumnName);
        if (c == null)
            return;

        if (existsColumn(addedColumnName) && c != null)
            return;

        columnNamesSet.add(addedColumnName);
        columnNames.add(addedColumnName);
        int i = columnNames.size() - 1;

        if (c.getListElementType() == String.class) {
            allGroupColumns.add(i);
            allGroupColumnSizes.add(Constants.CATEGORY_MAX_SIZE);
        } else if (c.getType() == String.class) {
            allStringColumns.add(i);
        } else if (c.getType() == Double.class) {
            allDoubleColumns.add(i);
        }
    }

    /**
     * Changes name of column with name oldName to newName.
     *
     * @param oldName
     * @param newName
     */
    public void changeColumnName(String oldName, String newName) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equals(oldName)) {
                columnNames.set(i, newName);
            }
        }

        columnNamesSet.remove(oldName);
        columnNamesSet.add(newName);
    }

    /**
     * Deletes column with name deletedColumnName.
     *
     * @param deletedColumnName
     */
    public void deleteColumnName(String deletedColumnName) {
        boolean somethingDeleted = false;

        int deletedIdx;
        for (deletedIdx = 0; deletedIdx < columnNames.size(); deletedIdx++) {
            if (columnNames.get(deletedIdx).equals(deletedColumnName)) {
                columnNames.remove(deletedIdx);
                somethingDeleted = true;
                break;
            }
        }

        if (!somethingDeleted) {
            return;
        }

        columnNamesSet.remove(deletedColumnName);

        int idx = 0;
        for (Integer stringIdx : allStringColumns) {
            if (stringIdx == deletedIdx) {
                if (selectedLabelColumn == idx) {
                    selectedLabelColumn = 0;
                } else if (selectedURLColumn == idx) {
                    selectedURLColumn = 0;
                }
                break;
            }
            idx++;
        }
        allStringColumns.remove(deletedIdx);

        idx = 0;
        for (Integer doubleIdx : allDoubleColumns) {
            if (doubleIdx == deletedIdx) {
                if (selectedScoreColumn == idx) {
                    selectedScoreColumn = 0;
                }
                break;
            }
            idx++;
        }
        allDoubleColumns.remove(deletedIdx);

        idx = 0;
        for (Integer grpIdx : allGroupColumns) {
            if (grpIdx == deletedIdx) {
                if (selectedGroupColumns.contains(idx)) {
                    selectedGroupColumns.remove(idx);
                }
                break;
            }
            idx++;
        }
        allGroupColumns.remove(deletedIdx);
        allGroupColumnSizes.remove(deletedIdx);
    }

    public int getSelectedLabelColumn() {
        return selectedLabelColumn;
    }

    public void setSelectedLabelColumn(int selectedLabelColumn) {
        this.selectedLabelColumn = selectedLabelColumn;
    }

    public String getSelectedLabelColumnName() {
        return columnNames.get(allStringColumns.get(selectedLabelColumn));
    }

    public int getSelectedURLColumn() {
        return selectedURLColumn;
    }

    public void setSelectedURLColumn(int selectedURLColumn) {
        this.selectedURLColumn = selectedURLColumn;
    }

    public String getSelectedURLColumnName() {
        return columnNames.get(allStringColumns.get(selectedURLColumn));
    }

    public int getSelectedScoreColumn() {
        return selectedScoreColumn;
    }

    public void setSelectedScoreColumn(int selectedScoreColumn) {
        this.selectedScoreColumn = selectedScoreColumn;
    }

    public String getSelectedScoreColumnName() {
        if (allDoubleColumns.size() == 0) {
            return null;
        } else {
            return columnNames.get(allDoubleColumns.get(selectedScoreColumn));
        }
    }

    public List<Integer> getSelectedGroupColumns() {
        return selectedGroupColumns;
    }

    public List<String> getSelectedGroupColumnNames() {
        ArrayList<Integer> mappedIndices = new ArrayList<Integer>();
        for (Integer i : selectedGroupColumns) {
            mappedIndices.add(allGroupColumns.get(i));
        }
        return getColumnNames(mappedIndices);
    }

    public List<Integer> getSelectedGroupColumnSizes() {
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (Integer i : selectedGroupColumns) {
            res.add(allGroupColumnSizes.get(i));
        }
        return res;
    }

    public void setSelectedGroupColumns(ArrayList<Integer> selectedGroupColumns) {
        this.selectedGroupColumns = selectedGroupColumns;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Integer> getAllGroupColumns() {
        return allGroupColumns;
    }

    public List<Integer> getAllGroupColumnSizes() {
        return allGroupColumnSizes;
    }

    public void setAllGroupColumnSizes(List<Integer> allGroupColumnSizes) {
        this.allGroupColumnSizes = allGroupColumnSizes;
    }


    public List<Integer> getAllStringColumns() {
        return allStringColumns;
    }

    public List<Integer> getAllDoubleColumns() {
        return allDoubleColumns;
    }

    public String getColumnName(int i) {
        return columnNames.get(i);
    }

    public Constants.Selection getGroupSelection() {
        return groupSelection;
    }

    public void setGroupSelection(Constants.Selection groupSelection) {
        this.groupSelection = groupSelection;
    }

    public boolean getShowScore() {
        return showScore;
    }

    public void setShowScore(boolean showScore) {
        this.showScore = showScore;
    }

}
