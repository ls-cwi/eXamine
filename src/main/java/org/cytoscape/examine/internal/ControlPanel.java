package org.cytoscape.examine.internal;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.examine.internal.data.DataSet;
import org.cytoscape.examine.internal.model.Model;
import org.cytoscape.examine.internal.settings.NetworkSettings;
import org.cytoscape.examine.internal.settings.SessionSettings;
import org.cytoscape.examine.internal.tasks.GenerateGroups;
import org.cytoscape.examine.internal.tasks.RemoveGroups;
import org.cytoscape.examine.internal.visualization.InteractiveVisualization;
import org.cytoscape.examine.internal.visualization.SnapshotVisualization;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
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
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.TaskIterator;

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

//TODO: Move swing components with distinct function to different classes


@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements
		CytoPanelComponent,
		SetCurrentNetworkListener,
		RowsSetListener,
		ColumnCreatedListener,
		ColumnDeletedListener,
		ColumnNameChangedListener,
		NetworkDestroyedListener,
		SessionLoadedListener {

    // Enable/disable listeners
    public static AtomicBoolean LISTENERS_ENABLED = new AtomicBoolean(true);

	// User interface elements

	//TODO: Parameterize Generics
	private JPanel pnlNetwork;
	private JTable tblFeedBack;
	private JScrollPane pnlScroll;
	private JPanel pnlNodes;

	private JLabel lblNode;
	private JComboBox<CyColumn> cmbNodeLabel;
	private JLabel lblNodeURL;
	private JComboBox<CyColumn> cmbNodeURL;

	private JLabel lblGroupScore;
	private JComboBox<CyColumn> cmbGroupScore;
	private JLabel lblGroupSelection;
	private JComboBox<Constants.Selection> cmbGroupSelection;
	private JPanel pnlGroups;
	private JPanel pnlGroups1;
	private JPanel pnlGroups2;
	private HashMap<CyColumn,JCheckBox> checkBoxes;
	private HashMap<CyColumn,JSpinner> spinners;
	private JPanel pnlButtons;
	private JButton btnRemoveAll;
	private JButton btnGenerateSelection;
	private JButton btnExamine;
	private JButton btnExport;
	private JCheckBox showScoreCheckBox;

	private final CyServices services;

    // UI components listener as to ensure UI matches current network settings
    private ItemChangeListener itemChangeListener;

    // Number of selected nodes
    private int nSelectedNodes;

    // Session settings, maintained for storing and retrieval of settings upon network selection change.
    private final SessionSettings sessionSettings = new SessionSettings();

	public ControlPanel(CyServices services) {

	    this.services = services;
		this.itemChangeListener = new ItemChangeListener();
		this.nSelectedNodes = 0;

		initUserInterface();
		
		CyNetwork network = services.getApplicationManager().getCurrentNetwork();

		if (network != null) {
			updateUserInterface();
			updateFeedbackTableModel();
			updateButtons();
		} else {
			disableUserInterface();
		}

		setVisible(true);
	}

	/**
	 * Update user interface to match network settings for currently selected network.
	 */
	private void updateUserInterface() {
		GridBagConstraints gridBagConstraints;
		NetworkSettings ns = sessionSettings.getNetworkSettings(services.getApplicationManager().getCurrentNetwork());

		// Fill check boxes
		List<CyColumn> groupColumns = ns.getAllGroupColumns();
		List<CyColumn> selectedGroupColumns = ns.getSelectedGroupColumns();
		checkBoxes = new HashMap<CyColumn,JCheckBox>();
		spinners = new HashMap<CyColumn,JSpinner>();

		pnlGroups1.removeAll();

		int i = 0;
		for (CyColumn j : groupColumns) {
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridx = (i % 2) * 2;
			gridBagConstraints.gridy = i / 2;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.weighty = 0.5;

			JCheckBox checkBox = new JCheckBox(j.getName(),
					selectedGroupColumns.contains(j));
			checkBox.addItemListener(itemChangeListener);
			pnlGroups1.add(checkBox, gridBagConstraints);
			checkBoxes.put(j, checkBox);
			
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
			spinners.put(j, spinner);

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
		//Clear combo-boxes
		cmbNodeLabel.removeAllItems();
		cmbNodeURL.removeAllItems();

		for (CyColumn clmn : ns.getAllStringColumns()) {
			cmbNodeLabel.addItem(clmn);
			cmbNodeURL.addItem(clmn);
		}

		if (!ns.getAllStringColumns().isEmpty()) {
			cmbNodeLabel.setSelectedItem(ns.getSelectedLabelColumn());
			cmbNodeURL.setSelectedItem(ns.getSelectedURLColumn());
		}

		cmbGroupScore.removeAllItems();
		List<CyColumn> doubleColumns = ns.getAllDoubleColumns();
		for (CyColumn j : doubleColumns) {
			cmbGroupScore.addItem(j);
		}
		
		cmbGroupSelection.setSelectedIndex(ns.getGroupSelectionMode().ordinal());

		cmbGroupScore.setEnabled(doubleColumns.size() > 0);
		showScoreCheckBox.setEnabled(doubleColumns.size() > 0);
		if (doubleColumns.size() > 0) {
			cmbGroupScore.setSelectedItem(ns.getSelectedScoreColumn());
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
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		pnlNetwork.add(pnlScroll, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
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

		cmbNodeLabel = new JComboBox<CyColumn>();
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

		cmbNodeURL = new JComboBox<CyColumn>();
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
		
		cmbGroupScore = new JComboBox<CyColumn>();
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
		
		cmbGroupSelection = new JComboBox<Constants.Selection>();
		cmbGroupSelection.addItem(Constants.Selection.NONE);
		cmbGroupSelection.addItem(Constants.Selection.UNION);
		cmbGroupSelection.addItem(Constants.Selection.INTERSECTION);
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
		btnExamine.addActionListener(arg0 -> {
            NetworkSettings ns = getCurrentNetworkSettings();
            openVisualizationWindow(ns);
        });
		btnExport = new JButton("Export");
        btnExport.setFont(btnExport.getFont().deriveFont(11.f));
        btnExport.setEnabled(false);
        btnExport.addActionListener(arg0 -> {
            NetworkSettings ns = getCurrentNetworkSettings();
            exportVisualization(ns);
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
					CyNetwork network = services.getApplicationManager().getCurrentNetwork();
					services.getTaskManager().execute(new TaskIterator(new RemoveGroups(services, network)));
				}
			}
		});
		btnGenerateSelection.setEnabled(false);
		btnGenerateSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CyNetwork network = services.getApplicationManager().getCurrentNetwork();
				NetworkSettings ns = getCurrentNetworkSettings();
				List<String> groupColumnNames = ns.getSelectedGroupColumnNames();
				services.getTaskManager().execute(new TaskIterator(
						new GenerateGroups(services, network, groupColumnNames, false)));
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

    /**
     * Create a data set for consumption by eXamine.
     */
    private DataSet createDataSet(NetworkSettings networkSettings) {
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
    private Model createModel(DataSet dataSet, NetworkSettings networkSettings) {
        return new Model(
                dataSet,
                services.getApplicationManager(),
                services.getVisualMappingManager(),
                services.getGroupManager(),
                networkSettings.getShowScore(),
                networkSettings.getGroupSelectionMode());
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
		final CyNetwork net = services.getApplicationManager().getCurrentNetwork();

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
		NetworkSettings ns = getCurrentNetworkSettings();
		if (ns != null) {
			List<CyColumn> selectedGroups = getCurrentNetworkSettings().getSelectedGroupColumns();
			boolean enabled = nSelectedNodes > 0 && selectedGroups.size() > 0;
			btnGenerateSelection.setEnabled(enabled);
			btnExamine.setEnabled(enabled);
			btnExport.setEnabled(enabled);
		}
	}
	

	//TODO: Merge both following functions into one that takes a boolean argument to remove redundancy

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

	private CyNetwork getCurrentNetwork() {
		return services.getApplicationManager().getCurrentNetwork();
	}

	private NetworkSettings getCurrentNetworkSettings() {
		CyNetwork network = getCurrentNetwork();
		return network == null ? null : sessionSettings.getNetworkSettings(network);
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
		if (LISTENERS_ENABLED.get()) {
			System.out.println("Network selection changed event, source: " + e.getSource());
		
			SwingUtilities.invokeLater(() -> {
                CyNetwork network = e.getNetwork();
                if (network != null) {
                    enableUserInterface();

                    updateUserInterface();
                    updateFeedbackTableModel();
                    updateButtons();
                } else {
                    updateFeedbackTableModel();
                    disableUserInterface();
                }
            });
		}
	}

	/**
	 * Update UI to match change in node selection.
	 */
	@Override
	public void handleEvent(final RowsSetEvent e) {
		if (LISTENERS_ENABLED.get()) {
			if (services.getApplicationManager().getCurrentNetwork() == null) return;
			
			boolean isSelection = true;
			for (RowSetRecord change : e.getPayloadCollection()) {
				if (!change.getColumn().equals(CyNetwork.SELECTED)) {
					isSelection = false;
					break;
				}
			}
			
			if (isSelection) {
				System.out.println("Real: Row selection changed event" + ", source: " + e.getSource());
				SwingUtilities.invokeLater(() -> {
                    updateFeedbackTableModel();
                    updateButtons();
                });
			}
		}
	}

	/**
	 * Update UI and network settings to match change in column name.
	 */
	@Override
	public void handleEvent(final ColumnNameChangedEvent e) {
		if (LISTENERS_ENABLED.get()) {
			System.out.println("Column name changed event: " + e.getOldColumnName() + ", source: " + e.getSource());
		
			SwingUtilities.invokeLater(() -> {

                updateUserInterface();
            });
		}
	}

	/**
	 * Update UI and network settings to match column removal.
	 */
	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (LISTENERS_ENABLED.get()) {
			System.out.println("Column deleted event: " + e.getColumnName() + ", source: " + e.getSource());
		
			SwingUtilities.invokeLater(() -> {
                // get the root network first
                CyRootNetwork rootNetwork = services.getRootNetworkManager().getRootNetwork(getCurrentNetwork());

                // now for all subnetworks of this root network, update the
                // column name
                for (CyNetwork network : rootNetwork.getSubNetworkList()) {
                    sessionSettings.getNetworkSettings(network).deleteColumn(network.getDefaultNodeTable().getColumn(e.getColumnName()));
                }

                updateUserInterface();
            });
		}
	}

	/**
	 * Update UI and network settings to match column creation.
	 */
	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		if (LISTENERS_ENABLED.get()) {
			CyNetwork currentNetwork = getCurrentNetwork();
			if (currentNetwork == null) {
				System.out.println("Could not retrieve current network, ignoring ColumnCreatedEvent!");
				return;
			}

			CyRootNetwork rootNetwork = services.getRootNetworkManager().getRootNetwork(currentNetwork);

			if (e.getSource() == rootNetwork.getDefaultNodeTable()) {
				System.out.println("Column created event: " + e.getColumnName() + ", source: " + e.getSource());
				SwingUtilities.invokeLater(() -> {
                    // get the root network first
                    if (currentNetwork != null) {
                        CyRootNetwork rootNetwork1 = services.getRootNetworkManager().getRootNetwork(currentNetwork);

                        // now for all subnetworks of this root network, update the column name
                        for (CyNetwork network : rootNetwork1.getSubNetworkList()) {
                        	sessionSettings.getNetworkSettings(network).addColumn(e.getSource().getColumn(e.getColumnName()));
                        }

                        updateUserInterface();
                    }
                });
			}
		}
	}

	private void removeInactiveNetworkSettings() {
		for (long suid : sessionSettings.getCachedNetworkUUIDs()) {
			if (services.getNetworkManager().getNetwork(suid) == null) {
				sessionSettings.removeNetworkSettings(suid);
			}
		}
	}

	/**
	 * Remove removed network from network settings.
	 */
	@Override
	public void handleEvent(final NetworkDestroyedEvent e) {
		if (LISTENERS_ENABLED.get()) {
			System.out.println("Network destroyed event" + ", source: " + e.getSource());
			
			SwingUtilities.invokeLater(this::removeInactiveNetworkSettings);
		}
	}

	/**
	 * Remove all networks settings.
	 */
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		if (LISTENERS_ENABLED.get()) {
			System.out.println("Session loaded event" + ", source: " + e.getSource());
			
			SwingUtilities.invokeLater(this::removeInactiveNetworkSettings);
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
			CyColumn idxNodeLabel = (CyColumn) cmbNodeLabel.getSelectedItem();
			CyColumn idxNodeURL = (CyColumn) cmbNodeURL.getSelectedItem();
			CyColumn idxGroupScore = (CyColumn) cmbGroupScore.getSelectedItem();
			Constants.Selection idxGroupSelection = (Constants.Selection) cmbGroupSelection.getSelectedItem();

			NetworkSettings ns = getCurrentNetworkSettings();
			if (ns != null) {
				ns.setSelectedLabelColumn(idxNodeLabel);
				ns.setSelectedURLColumn(idxNodeURL);
				ns.setSelectedScoreColumn(idxGroupScore);
				ns.setGroupSelection(idxGroupSelection);
				ns.setShowScore(showScoreCheckBox.isSelected());

				ArrayList<CyColumn> selectedGroups = new ArrayList<CyColumn>();

				for (Map.Entry<CyColumn, JCheckBox> entry : checkBoxes.entrySet()) {
					if (entry.getValue().isSelected()) {
						selectedGroups.add(entry.getKey());
						spinners.get(entry.getKey()).setEnabled(true);
					} else {
						spinners.get(entry.getKey()).setEnabled(false);
					}

					Integer size = (Integer) spinners.get(entry.getKey()).getModel().getValue();
					ns.setGroupColumnSize(entry.getKey(),size);
				}
				ns.setSelectedGroupColumns(selectedGroups);
				updateButtons();
			}
		}
	}

}
