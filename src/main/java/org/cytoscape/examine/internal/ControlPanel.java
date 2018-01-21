package org.cytoscape.examine.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.examine.internal.Constants.Selection;
import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.visualization.InteractiveVisualization;
import org.cytoscape.examine.internal.visualization.SnapshotVisualization;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
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
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements CytoPanelComponent,
		SetCurrentNetworkListener, RowsSetListener, ColumnCreatedListener,
		ColumnDeletedListener, ColumnNameChangedListener,
		NetworkDestroyedListener, SessionLoadedListener {

	private final CyNetworkManager networkManager;
	private final VisualMappingManager visualMappingManager;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyApplicationManager applicationManager;
	private final CyGroupManager groupManager;
	private final CyGroupFactory groupFactory;
	private final TaskManager taskManager;
	
	// User interface elements
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
	private JButton btnGenerateSelection;
	private JButton btnExamine;
	private JButton btnExport;
	private JCheckBox showScoreCheckBox;
	
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

	public ControlPanel(
			CyNetworkManager networkManager,
			VisualMappingManager visualMappingManager,
			CyRootNetworkManager rootNetworkManager,
			CyApplicationManager applicationManager,
			CyGroupManager groupManager,
			CyGroupFactory groupFactory,
			TaskManager taskManager) {

		this.networkManager = networkManager;
		this.visualMappingManager = visualMappingManager;
		this.rootNetworkManager = rootNetworkManager;
		this.applicationManager = applicationManager;
		this.groupManager = groupManager;
		this.groupFactory = groupFactory;
		this.taskManager = taskManager;
		this.networkSettings = new HashMap<Long, NetworkSettings>();
		this.itemChangeListener = new ItemChangeListener();
		this.nSelectedNodes = 0;
		this.currentNetworkSUID = null;

		initUserInterface();
		
		CyNetwork network = applicationManager.getCurrentNetwork();
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
		btnExport = new JButton("Export");
        btnExport.setFont(btnExport.getFont().deriveFont(11.f));
        btnExport.setEnabled(false);
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                NetworkSettings ns = networkSettings.get(currentNetworkSUID);
                exportVisualization(ns);
            }
        });
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
					CyNetwork network = applicationManager.getCurrentNetwork();
					taskManager.execute(new TaskIterator(new RemoveGroups(groupManager, network)));
				}
			}
		});
		btnGenerateSelection.setEnabled(false);
		btnGenerateSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CyNetwork network = applicationManager.getCurrentNetwork();
				CyTable nodeTable = network.getDefaultNodeTable();
				NetworkSettings ns = networkSettings.get(currentNetworkSUID);
				List<String> groupColumnNames = ns.getSelectedGroupColumnNames();
				taskManager.execute(new TaskIterator(
						new GenerateGroups(applicationManager, groupManager, groupFactory, network, nodeTable, groupColumnNames, false)));
			}
		});
		pnlButtons.add(btnGenerateSelection);
		pnlButtons.add(btnExamine);
		pnlButtons.add(btnRemoveAll);
		pnlButtons.add(btnExport);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 0, 10, 0);
		add(pnlButtons, gridBagConstraints);
	}

	private DataSet createDataSet(NetworkSettings networkSettings) {
	    return new DataSet(
                applicationManager.getCurrentNetwork(),
                groupManager,
                networkSettings.getSelectedLabelColumnName(),
                networkSettings.getSelectedURLColumnName(),
                networkSettings.getSelectedScoreColumnName(),
                networkSettings.getSelectedGroupColumnNames(),
                networkSettings.getSelectedGroupColumnSizes()
        );
    }

    private Model createModel(DataSet dataSet, NetworkSettings networkSettings) {
        return new Model(
                dataSet,
                applicationManager,
                visualMappingManager,
                groupManager,
                networkSettings.getShowScore(),
                networkSettings.getGroupSelection());
    }

    /**
     * Open a new visualization window.
     */
    private void openVisualizationWindow(NetworkSettings networkSettings) {
        final DataSet dataSet = createDataSet(networkSettings);
        final Model model = createModel(dataSet, networkSettings);

        new InteractiveVisualization(dataSet, model);
    }

    /**
     * Export the visualization as an image.
     */
    private void exportVisualization(NetworkSettings networkSettings) {
        final DataSet dataSet = createDataSet(networkSettings);
        final Model model = createModel(dataSet, networkSettings);
        final SnapshotVisualization visualization = new SnapshotVisualization(dataSet, model);

        // Target file via dialog.
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName("Export SVG");
        fileChooser.setSelectedFile(new File("eXamine-export.svg"));
        int fileConfirm = fileChooser.showSaveDialog(this);

        if (fileConfirm == JFileChooser.APPROVE_OPTION) {
            try {
                visualization.exportSVG(fileChooser.getSelectedFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * Update the network/nodes table.
	 */
	private void updateFeedbackTableModel() {
		String netName = "";
		String nodeCount = "";
		final CyNetwork net = applicationManager.getCurrentNetwork();

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
			boolean enabled = nSelectedNodes > 0 && selectedGroups.size() > 0;
			btnGenerateSelection.setEnabled(enabled);
			btnExamine.setEnabled(enabled);
			btnExport.setEnabled(enabled);
		}
	}
	
	/**
	 * Disable user interface.
	 */
	private void disableUserInterface() {
		pnlButtons.setEnabled(false);
		btnExamine.setEnabled(false);
		btnExport.setEnabled(false);
		btnRemoveAll.setEnabled(false);
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
		btnExport.setEnabled(true);
		btnRemoveAll.setEnabled(true);
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
			if (applicationManager.getCurrentNetwork() == null) return;
			
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
					CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(networkManager.getNetwork(currentNetworkSUID));
	
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
					CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(networkManager.getNetwork(currentNetworkSUID));
	
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
			CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(networkManager.getNetwork(currentNetworkSUID));

			if (e.getSource() == rootNetwork.getDefaultNodeTable()) {
				System.out.println("Column created event: " + e.getColumnName() + ", source: " + e.getSource());
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// get the root network first
						if (currentNetworkSUID != null) {
							CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(networkManager.getNetwork(currentNetworkSUID));
			
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
			if (networkManager.getNetwork(suid) == null) {
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
