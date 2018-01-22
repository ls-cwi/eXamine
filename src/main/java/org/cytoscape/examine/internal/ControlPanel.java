package org.cytoscape.examine.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.examine.internal.Constants.Selection;
import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.tasks.GenerateGroups;
import org.cytoscape.examine.internal.tasks.RemoveGroups;
import org.cytoscape.examine.internal.visualization.Visualization;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

//TODO: Move swing components with distinct function to different classes

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements CytoPanelComponent,
		SetCurrentNetworkListener, RowsSetListener, ColumnCreatedListener,
		ColumnDeletedListener, ColumnNameChangedListener,
		NetworkDestroyedListener, SessionLoadedListener {

	
	// User interface elements
	
	//TODO: Parameterize Generics
	private JPanel pnlNetwork;
	private JTable tblFeedBack;
	private JScrollPane pnlScroll;
	private JPanel pnlNodes;
	private JLabel lblNode;
	private JComboBox cmbNodeLabel;
	private JLabel lblNodeURL;
	private JComboBox cmbNodeURL;
	private JLabel lblGroupScore;
	private JComboBox cmbGroupScore;
	private JLabel lblGroupSelection;
	private JComboBox cmbGroupSelection;
	private JPanel pnlGroups;
	private JPanel pnlGroups1;
	private JPanel pnlGroups2;
	private JCheckBox[] checkBoxes;
	private JSpinner[] spinners;
	private JPanel pnlButtons;
	private JButton btnRemoveAll;
	private JButton btnGenerateAll;
	private JButton btnGenerateSelection;
	private JButton btnExamine;
	private JCheckBox showScoreCheckBox;
	
	//Links
	
	CyReferences references = CyReferences.getInstance();
	
	public ControlPanel() {


		this.networkSettings = new HashMap<Long, NetworkSettings>();
		this.itemChangeListener = new ItemChangeListener();
		this.nSelectedNodes = 0;
		this.currentNetworkSUID = null;

		initUserInterface();
		
		CyNetwork network = references.getApplicationManager().getCurrentNetwork();
		
		if (network != null) {
			this.currentNetworkSUID = network.getSUID();
			this.networkSettings.put(currentNetworkSUID, new NetworkSettings(network));
	
			updateUserInterface();
			updateFeedbackTableModel();
			updateButtons();
		} else {
			disableUserInterface();
		}

		this.setVisible(true);
	}
	
	// Enable/disable listeners
	public static AtomicBoolean listenersEnabled = new AtomicBoolean(true);	

	// UI components listener as to ensure UI matches current network settings
	private ItemChangeListener itemChangeListener;
	// Current selected network
	private Long currentNetworkSUID;
	// Number of selected nodes
	private int nSelectedNodes;
	// Network settings, maintained for several networks as to accommodate
	// storing and retrieval of settings upon network selection change
	private Map<Long, NetworkSettings> networkSettings;

	/**
	 * Stores network settings, including selected columns and column names.
	 * 
	 * @author melkebir
	 * Move to separate class?
	 */
	private class NetworkSettings {
		//private Long networkSUID;
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
		private boolean showScore = true;

		public boolean getShowScore() {
			return showScore;
		}

		public void setShowScore(boolean showScore) {
			this.showScore = showScore;
		}

		public NetworkSettings(CyNetwork network) {
			
			if (network == null) {
				System.err.println("Attempted to generate NetworkSettings for a non-existing network ...");
				return;
			}
			
			//networkSUID = network.getSUID();

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


		public List<Integer> getAllGroupColumns() {
			return allGroupColumns;
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
	}


	/**
	 * Update user interface to match network settings for currently selected network.
	 */
	private void updateUserInterface() {
		GridBagConstraints gridBagConstraints;
		NetworkSettings ns = networkSettings.get(currentNetworkSUID);

		// Fill check boxes
		List<Integer> groupColumns = ns.getAllGroupColumns();
		List<Integer> selectedGroupColumns = ns.getSelectedGroupColumns();
		checkBoxes = new JCheckBox[groupColumns.size()];
		spinners = new JSpinner[groupColumns.size()];

		pnlGroups1.removeAll();

		int i = 0;
		for (Integer j : groupColumns) {
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridx = (i % 2) * 2;
			gridBagConstraints.gridy = i / 2;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.weighty = 0.5;

			JCheckBox checkBox = new JCheckBox(ns.getColumnName(j),
					selectedGroupColumns.contains(i));
			checkBox.addItemListener(itemChangeListener);
			pnlGroups1.add(checkBox, gridBagConstraints);
			checkBoxes[i] = checkBox;
			
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(Constants.CATEGORY_MAX_SIZE, 
					0, 10 * Constants.CATEGORY_MAX_SIZE, 1));
			
			spinner.setEnabled(checkBox.isSelected());
			
			spinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					itemChangeListener.itemStateChanged(null);
				}
			});
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridx = (i % 2) * 2 + 1;
			gridBagConstraints.gridy = i / 2;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.weighty = 0.5;
			pnlGroups1.add(spinner, gridBagConstraints);
			spinners[i] = spinner;

			i++;
		}

		pnlGroups1.validate();
		pnlGroups1.repaint();
		
		// Fill combo boxes
		// Remove all itemChangeListeners
		cmbNodeLabel.removeItemListener(itemChangeListener);
		cmbNodeURL.removeItemListener(itemChangeListener);
		cmbGroupScore.removeItemListener(itemChangeListener);
		cmbGroupSelection.removeItemListener(itemChangeListener);
		
		DefaultComboBoxModel mdlCmbNodeLabel = (DefaultComboBoxModel) cmbNodeLabel.getModel();
		mdlCmbNodeLabel.removeAllElements();
		DefaultComboBoxModel mdlCmbNodeURL = (DefaultComboBoxModel) cmbNodeURL.getModel();
		mdlCmbNodeURL.removeAllElements();

		List<Integer> stringColumns = ns.getAllStringColumns();
		for (Integer j : stringColumns) {
			mdlCmbNodeLabel.addElement(ns.getColumnName(j));
			mdlCmbNodeURL.addElement(ns.getColumnName(j));
		}

		if (stringColumns.size() > 0) {
			cmbNodeLabel.setSelectedIndex(ns.getSelectedLabelColumn());
			cmbNodeURL.setSelectedIndex(ns.getSelectedURLColumn());
		}

		DefaultComboBoxModel mdlCmbGroupScore = (DefaultComboBoxModel) cmbGroupScore.getModel();
		mdlCmbGroupScore.removeAllElements();
		List<Integer> doubleColumns = ns.getAllDoubleColumns();
		for (Integer j : doubleColumns) {
			mdlCmbGroupScore.addElement(ns.getColumnName(j));
		}
		
		cmbGroupSelection.setSelectedIndex(ns.getGroupSelection().ordinal());

		cmbGroupScore.setEnabled(doubleColumns.size() > 0);
		showScoreCheckBox.setEnabled(doubleColumns.size() > 0);
		if (doubleColumns.size() > 0) {
			cmbGroupScore.setSelectedIndex(ns.getSelectedScoreColumn());
		}

		updateButtons();
		
		showScoreCheckBox.setSelected(ns.getShowScore());
		
		// Add all itemChangeListeners
		cmbNodeLabel.addItemListener(itemChangeListener);
		cmbNodeURL.addItemListener(itemChangeListener);
		cmbGroupScore.addItemListener(itemChangeListener);
		cmbGroupSelection.addItemListener(itemChangeListener);
	}

	/**
	 * Initialize user interface.
	 */
	private void initUserInterface() {
		GridBagConstraints gridBagConstraints;
		setLayout(new GridBagLayout());

		// Network panel
		pnlNetwork = new JPanel();
		pnlNetwork.setBorder(BorderFactory.createTitledBorder(""));
		pnlNetwork.setLayout(new GridBagLayout());
		pnlNetwork.setMinimumSize(new Dimension(pnlNetwork.getWidth(), 60));
		pnlScroll = new JScrollPane();
		tblFeedBack = new JTable();
		pnlScroll.setViewportView(tblFeedBack);
		pnlScroll.setBorder(BorderFactory.createEmptyBorder());
		tblFeedBack.setEnabled(false);
		tblFeedBack.setFocusable(false);
		tblFeedBack.setBorder(BorderFactory.createEmptyBorder());
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH; // .HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		// gridBagConstraints.insets = new java.awt.Insets(0, 0, 1, 1);
		pnlNetwork.add(pnlScroll, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		// gridBagConstraints.insets = new Insets(1, 1, 2, 1);
		add(pnlNetwork, gridBagConstraints);

		String[][] data = { { "", "" } };
		String[] col = { "Network", "Nodes" };
		DefaultTableModel model = new DefaultTableModel(data, col);
		tblFeedBack.setModel(model);

		// Nodes panel
		pnlNodes = new JPanel();
		pnlNodes.setBorder(BorderFactory.createTitledBorder("Nodes"));
		pnlNodes.setLayout(new GridBagLayout());

		lblNode = new JLabel("Label");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(10, 5, 0, 0);
		pnlNodes.add(lblNode, gridBagConstraints);

		cmbNodeLabel = new JComboBox();
		DefaultComboBoxModel mdlCmbNodeLabel = new DefaultComboBoxModel();
		cmbNodeLabel.setModel(mdlCmbNodeLabel);
		cmbNodeLabel.addItemListener(itemChangeListener);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 10, 0, 10);
		pnlNodes.add(cmbNodeLabel, gridBagConstraints);

		lblNodeURL = new JLabel("URL");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(10, 5, 0, 0);
		pnlNodes.add(lblNodeURL, gridBagConstraints);

		cmbNodeURL = new JComboBox();
		DefaultComboBoxModel mdlCmbNodeURL = new DefaultComboBoxModel();
		cmbNodeURL.setModel(mdlCmbNodeURL);
		cmbNodeURL.addItemListener(itemChangeListener);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 10, 0, 10);
		pnlNodes.add(cmbNodeURL, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.ipady = 4;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		add(pnlNodes, gridBagConstraints);

		// Groups panel
		pnlGroups = new JPanel();
		pnlGroups.setBorder(BorderFactory.createTitledBorder("Groups"));
		pnlGroups.setLayout(new BoxLayout(pnlGroups, BoxLayout.PAGE_AXIS));

		pnlGroups1 = new JPanel();
		pnlGroups1.setLayout(new GridBagLayout());
		pnlGroups2 = new JPanel();
		pnlGroups2.setLayout(new GridBagLayout());
		
		lblGroupScore = new JLabel("Score");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(10, 5, 0, 0);
		pnlGroups2.add(lblGroupScore, gridBagConstraints);
		
		cmbGroupScore = new JComboBox();
		DefaultComboBoxModel mdlCmbGroupScore = new DefaultComboBoxModel();
		cmbGroupScore.setModel(mdlCmbGroupScore);
		cmbGroupScore.addItemListener(itemChangeListener);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 10, 0, 10);
		pnlGroups2.add(cmbGroupScore, gridBagConstraints);

		lblGroupSelection = new JLabel("Selection");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(10, 5, 0, 0);
		pnlGroups2.add(lblGroupSelection, gridBagConstraints);
		
		cmbGroupSelection = new JComboBox();
		DefaultComboBoxModel mdlCmbGroupSelection = new DefaultComboBoxModel();
		mdlCmbGroupSelection.addElement("None");
		mdlCmbGroupSelection.addElement("Union");
		mdlCmbGroupSelection.addElement("Intersection");
		cmbGroupSelection.setModel(mdlCmbGroupSelection);
		cmbGroupSelection.addItemListener(itemChangeListener);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 10, 0, 10);
		pnlGroups2.add(cmbGroupSelection, gridBagConstraints);

		showScoreCheckBox = new JCheckBox("Show group score");
		showScoreCheckBox.addItemListener(itemChangeListener);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 10, 0, 10);
		pnlGroups2.add(showScoreCheckBox, gridBagConstraints);
		
		pnlGroups.add(pnlGroups1);
		pnlGroups.add(pnlGroups2);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(pnlGroups, gridBagConstraints);
		
		// Buttons panel
		pnlButtons = new JPanel();
		pnlButtons.setLayout(new FlowLayout());
		btnExamine = new JButton("eXamine");
		btnExamine.setFont(btnExamine.getFont().deriveFont(11.f));
		btnExamine.setEnabled(false);
		btnExamine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				NetworkSettings ns = networkSettings.get(currentNetworkSUID);
				openVisualizationWindow(ns);
			}
		});
		btnGenerateAll = new JButton("Generate");
		btnGenerateAll.setFont(btnGenerateAll.getFont().deriveFont(11.f));
		btnGenerateSelection = new JButton("Generate");
		btnGenerateSelection.setFont(btnGenerateSelection.getFont().deriveFont(11.f));
		btnRemoveAll = new JButton("Remove");
		btnRemoveAll.setFont(btnRemoveAll.getFont().deriveFont(11.f));
		btnRemoveAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedOption = JOptionPane.showConfirmDialog(null, 
						"Do you want to remove all groups?", 
						"Group removal", JOptionPane.YES_NO_OPTION);

				if (selectedOption == JOptionPane.YES_OPTION) {
					CyNetwork network = references.getApplicationManager().getCurrentNetwork();
					references.getTaskManager().execute(new TaskIterator(new RemoveGroups(references.getGroupManager(), network)));
				}
			}
		});
		btnGenerateAll.setEnabled(false);
		btnGenerateAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CyNetwork network = references.getApplicationManager().getCurrentNetwork();
				CyTable nodeTable = network.getDefaultNodeTable();
				NetworkSettings ns = networkSettings.get(currentNetworkSUID);
				List<String> groupColumnNames = ns.getSelectedGroupColumnNames();
				references.getTaskManager().execute(new TaskIterator(
						new GenerateGroups( network, nodeTable, groupColumnNames, true)));
			}
		});
		btnGenerateSelection.setEnabled(false);
		btnGenerateSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CyNetwork network = references.getApplicationManager().getCurrentNetwork();
				CyTable nodeTable = network.getDefaultNodeTable();
				NetworkSettings ns = networkSettings.get(currentNetworkSUID);
				List<String> groupColumnNames = ns.getSelectedGroupColumnNames();
				references.getTaskManager().execute(new TaskIterator(
						new GenerateGroups( network, nodeTable, groupColumnNames, false)));
			}
		});
		pnlButtons.add(btnGenerateSelection);
		//pnlButtons.add(btnGenerateAll);
		pnlButtons.add(btnExamine);
		pnlButtons.add(btnRemoveAll);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 0, 10, 0);
		add(pnlButtons, gridBagConstraints);
	}

    /**
     * Open a new visualization window.
     */
    private void openVisualizationWindow(NetworkSettings networkSettings) {
        final DataSet dataSet = new DataSet(
                references.getApplicationManager().getCurrentNetwork(),
				references.getGroupManager(),
                networkSettings.getSelectedLabelColumnName(),
                networkSettings.getSelectedURLColumnName(),
                networkSettings.getSelectedScoreColumnName(),
                networkSettings.getSelectedGroupColumnNames(),
                networkSettings.getSelectedGroupColumnSizes()
        );

        final Model model = new Model(dataSet, references.getApplicationManager(), references.getVisualMappingManager());
        model.setSelectionMode(networkSettings.getGroupSelection());
        model.selection.change.signal();

        new Visualization(dataSet, model, networkSettings.getShowScore());
    }

	/**
	 * Update the network/nodes table.
	 */
	private void updateFeedbackTableModel() {
		String netName = "";
		String nodeCount = "";
		final CyNetwork net = references.getApplicationManager().getCurrentNetwork();

		if (net != null) {
			this.nSelectedNodes = net.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, true);
			netName = net.getRow(net).get(CyNetwork.NAME, String.class);
			nodeCount = net.getNodeCount() + "(" + this.nSelectedNodes + ")";
		}

		tblFeedBack.getModel().setValueAt(netName, 0, 0);
		tblFeedBack.getModel().setValueAt(nodeCount, 0, 1);
	}
	
	/**
	 * Enable/disable Generate and eXamine buttons.
	 */
	private void updateButtons() {
		NetworkSettings ns = networkSettings.get(currentNetworkSUID);
		if (ns != null) {
			List<Integer> selectedGroups = ns.getSelectedGroupColumns();
			btnGenerateAll.setEnabled(selectedGroups.size() > 0);
			btnGenerateSelection.setEnabled(nSelectedNodes > 0 && selectedGroups.size() > 0);
			btnExamine.setEnabled(nSelectedNodes > 0 && selectedGroups.size() > 0);
		}
	}
	

	//TODO: Merge both following functions into one that takes a boolean argument to remove redundancy
	
	/**
	 * Disable user interface.
	 */
	private void disableUserInterface() {
		pnlButtons.setEnabled(false);
		btnExamine.setEnabled(false);
		btnRemoveAll.setEnabled(false);
		btnGenerateAll.setEnabled(false);
		btnGenerateSelection.setEnabled(false);
		cmbNodeLabel.setEnabled(false);
		cmbNodeURL.setEnabled(false);
		cmbGroupScore.setEnabled(false);
		cmbGroupSelection.setEnabled(false);
		pnlGroups.setEnabled(false);
		pnlNodes.setEnabled(false);
		pnlGroups1.removeAll();
		pnlGroups1.validate();
	}
	
	/**
	 * Enable user interface.
	 */
	private void enableUserInterface() {
		pnlButtons.setEnabled(true);
		pnlGroups.setEnabled(true);
		pnlNodes.setEnabled(true);
		pnlButtons.setEnabled(true);
		btnExamine.setEnabled(true);
		btnRemoveAll.setEnabled(true);
		btnGenerateAll.setEnabled(true);
		btnGenerateSelection.setEnabled(true);
		cmbNodeLabel.setEnabled(true);
		cmbNodeURL.setEnabled(true);
		cmbGroupScore.setEnabled(true);
		cmbGroupSelection.setEnabled(true);
		pnlGroups.setEnabled(true);
		pnlNodes.setEnabled(true);
	}

	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	public String getTitle() {
		return "eXamine";
	}

	public Icon getIcon() {
		return null;
	}

	/**
	 * Update UI to match change in network selection.
	 */
	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		if (listenersEnabled.get()) {
			System.out.println("Network selection changed event, source: " + e.getSource());
		
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					CyNetwork network = e.getNetwork();
					if (network != null) {
						currentNetworkSUID = network.getSUID();
						if (!networkSettings.containsKey(currentNetworkSUID)) {
							networkSettings.put(currentNetworkSUID,	new NetworkSettings(network));
						}
		
						enableUserInterface();
						
						updateUserInterface();
						updateFeedbackTableModel();
						updateButtons();
					} else {
						updateFeedbackTableModel();
						disableUserInterface();
					}
				}
			});
		}
	}

	/**
	 * Update UI to match change in node selection.
	 */
	@Override
	public void handleEvent(final RowsSetEvent e) {
		if (listenersEnabled.get()) {	
			if (references.getApplicationManager().getCurrentNetwork() == null) return;
			
			boolean isSelection = true;
			for (RowSetRecord change : e.getPayloadCollection()) {
				if (!change.getColumn().equals(CyNetwork.SELECTED)) {
					isSelection = false;
					break;
				}
			}
			
			if (isSelection) {
				System.out.println("Real: Row selection changed event" + ", source: " + e.getSource());
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateFeedbackTableModel();
						updateButtons();
					}
				});
			}
		}
	}

	/**
	 * Update UI and network settings to match change in column name.
	 */
	@Override
	public void handleEvent(final ColumnNameChangedEvent e) {
		if (listenersEnabled.get()) {
			System.out.println("Column name changed event: " + e.getOldColumnName() + ", source: " + e.getSource());
		
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// get the root network first
					CyNetwork currentNet = references.getNetworkManager().getNetwork(currentNetworkSUID);
					CyRootNetwork rootNetwork = references.getRootNetworkManager().getRootNetwork(currentNet);
	
					// now for all subnetworks of this root network, update the
					// column name
					for (CyNetwork network : rootNetwork.getSubNetworkList()) {
						if (networkSettings.containsKey(network.getSUID())) {
							networkSettings.get(network.getSUID()).changeColumnName(e.getOldColumnName(), e.getNewColumnName());
						}
					}
	
					updateUserInterface();
				}
			});
		}
	}

	/**
	 * Update UI and network settings to match column removal.
	 */
	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (listenersEnabled.get()) {
			System.out.println("Column deleted event: " + e.getColumnName() + ", source: " + e.getSource());
		
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// get the root network first
					// get the root network first
					CyNetwork currentNet = references.getNetworkManager().getNetwork(currentNetworkSUID);
					CyRootNetwork rootNetwork = references.getRootNetworkManager().getRootNetwork(currentNet);
		
					// now for all subnetworks of this root network, update the
					// column name
					for (CyNetwork network : rootNetwork.getSubNetworkList()) {
						if (networkSettings.containsKey(network.getSUID())) {
							networkSettings.get(network.getSUID()).deleteColumnName(e.getColumnName());
						}
					}
	
					updateUserInterface();
				}
			});
		}
	}

	/**
	 * Update UI and network settings to match column creation.
	 */
	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		if (listenersEnabled.get()) {
			//We ignore calls when no current network is initialized
			//Such calls can occur during the loading of sessions as some events are parallel and thus a ColumnCreatedEvent can be fired before the corresponding network has been created and fully registered
			if (currentNetworkSUID == null) {
				System.out.println("Current Network SUID has not been set! Ignoring ColumnCreatedEvent ..."); //TODO: Dedicated log/ error stream?
				return;
			}
			CyNetwork currentNetwork = references.getNetworkManager().getNetwork(currentNetworkSUID);
			if (currentNetwork == null) {
				System.out.println("Could not retrieve current network, ignoring ColumnCreatedEvent!");
			}
			
			CyRootNetwork rootNetwork = references.getRootNetworkManager().getRootNetwork(currentNetwork);

			if (e.getSource() == rootNetwork.getDefaultNodeTable()) {
				System.out.println("Column created event: " + e.getColumnName() + ", source: " + e.getSource());
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// get the root network first
						if (currentNetworkSUID != null) {
							CyRootNetwork rootNetwork = references.getRootNetworkManager().getRootNetwork(references.getNetworkManager().getNetwork(currentNetworkSUID));
			
							// now for all subnetworks of this root network, update the
							// column name
							for (CyNetwork network : rootNetwork.getSubNetworkList()) {
								if (networkSettings.containsKey(network.getSUID())) {
									networkSettings.get(network.getSUID()).addColumnName(network, e.getColumnName());
								}
							}
			
							updateUserInterface();
						}
					}
				});
			}
		}
	}

	private void removeInactiveNetworkSettings() {
		for (long suid : networkSettings.keySet()) {
			if (references.getNetworkManager().getNetwork(suid) == null) {
				networkSettings.remove(suid);
			}
		}
	}

	/**
	 * Remove removed network from network settings.
	 */
	@Override
	public void handleEvent(final NetworkDestroyedEvent e) {
		if (listenersEnabled.get()) {
			System.out.println("Network destroyed event" + ", source: " + e.getSource());
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					removeInactiveNetworkSettings();
				}
			});
		}
	}

	/**
	 * Remove all networks settings.
	 */
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		if (listenersEnabled.get()) {
			System.out.println("Session loaded event" + ", source: " + e.getSource());
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					removeInactiveNetworkSettings();
				}
			});
		}
	}

	/**
	 * Ensures current network settings match UI, called upon UI changes.
	 *
	 * @author melkebir
	 *
	 */
	private class ItemChangeListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent event) {
			// update network settings to match ui
			int idxNodeLabel = cmbNodeLabel.getSelectedIndex();
			int idxNodeURL = cmbNodeURL.getSelectedIndex();
			int idxGroupScore = cmbGroupScore.getSelectedIndex();
			int idxGroupSelection = cmbGroupSelection.getSelectedIndex();

			NetworkSettings ns = networkSettings.get(currentNetworkSUID);
			if (ns != null) {
				ns.setSelectedLabelColumn(idxNodeLabel);
				ns.setSelectedURLColumn(idxNodeURL);
				ns.setSelectedScoreColumn(idxGroupScore);
				ns.setGroupSelection(Selection.values()[idxGroupSelection]);
				ns.setShowScore(showScoreCheckBox.isSelected());

				ArrayList<Integer> selectedGroups = new ArrayList<Integer>();
				ArrayList<Integer> groupSizes = new ArrayList<Integer>();
				for (int idxGroup = 0; idxGroup < checkBoxes.length; idxGroup++) {
					if (checkBoxes[idxGroup].isSelected()) {
						selectedGroups.add(idxGroup);
						spinners[idxGroup].setEnabled(true);
					} else {
						spinners[idxGroup].setEnabled(false);
					}

					Integer size = (Integer) spinners[idxGroup].getModel().getValue();
					groupSizes.add(size);
				}
				ns.setSelectedGroupColumns(selectedGroups);
				ns.setAllGroupColumnSizes(groupSizes);

				updateButtons();
			}
		}
	}

}
