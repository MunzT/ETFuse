package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.uni_stuttgart.visus.etfuse.eyetracker.gazefilter.IVTFilter;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class PreferencesFrame extends JDialog {
	
	private JTextField txtVisibilityDuration;
	private JTextField txtGazeFilterVelocityThreshold;
	private JTextField txtGazeFilterDistanceThreshold;
	private JTextField txtMinDistPlotDist;
	private JTextField txtHeatMapGeneratorSkipPercentage;
	private JTextField txtHeatMapGeneratorHeatRadius;
	private JCheckBox chckbxHeatMapGeneratorSkipPercentage;
	private JComboBox comboBoxOverlayHeatMapSource;
	private JCheckBox chckbxEnableHeatMapPlot;
	private JCheckBox chckbxEnableRawDataPlot;
	private JCheckBox chckbxEnableFixationPlot;
	private JComboBox comboBoxMinDistPlotSource1;
	private JComboBox comboBoxMinDistPlotSource2;
	private JCheckBox chckbxHeatMapGeneratorGenFromFrequencyInstead;
	private JTextField txtHistogramCorrelationThreshold;
	private JTextField txtHistogramDeviatingCellsThreshold;
	private JSpinner spinnerHistogramGridSize;
	
	private VideoFrame vidFrame = null;
	
	public PreferencesFrame(VideoFrame vidFrame) {
		
		super(vidFrame, "Einstellungen", true);
		
		this.vidFrame = vidFrame;
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panelOverlayPrefs = new JPanel();
		tabbedPane.addTab("Overlay", null, panelOverlayPrefs, null);
		panelOverlayPrefs.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("fill:default"),
				RowSpec.decode("fill:default"),
				RowSpec.decode("fill:default"),}));
		
		JPanel fixationPrefsPanel = new JPanel();
		fixationPrefsPanel.setBorder(new TitledBorder(null, "Fixationen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelOverlayPrefs.add(fixationPrefsPanel, "1, 1, fill, center");
		fixationPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		chckbxEnableFixationPlot = new JCheckBox("Plot aktivieren");
		chckbxEnableFixationPlot.setBorder(null);
		chckbxEnableFixationPlot.setAlignmentY(Component.TOP_ALIGNMENT);
		fixationPrefsPanel.add(chckbxEnableFixationPlot, "2, 2, left, top");
		
		JLabel labelVisibilityDuration = new JLabel("Sichtbarkeit vollendeter Fixationen (in ms, default: 500):");
		fixationPrefsPanel.add(labelVisibilityDuration, "2, 4, fill, fill");
		
		txtVisibilityDuration = new JTextField();
		txtVisibilityDuration.setHorizontalAlignment(SwingConstants.LEFT);
		fixationPrefsPanel.add(txtVisibilityDuration, "4, 4, fill, center");
		txtVisibilityDuration.setColumns(10);
		
		JPanel rawDataPrefsPanel = new JPanel();
		rawDataPrefsPanel.setBorder(new TitledBorder(null, "Rohdaten", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelOverlayPrefs.add(rawDataPrefsPanel, "1, 2, fill, center");
		rawDataPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default"),
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		chckbxEnableRawDataPlot = new JCheckBox("Plot aktivieren");
		chckbxEnableRawDataPlot.setBorder(null);
		rawDataPrefsPanel.add(chckbxEnableRawDataPlot, "2, 2, fill, top");
		
		JPanel heatMapPrefsPanel = new JPanel();
		heatMapPrefsPanel.setBorder(new TitledBorder(null, "Attention Map", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelOverlayPrefs.add(heatMapPrefsPanel, "1, 3, fill, center");
		heatMapPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		chckbxEnableHeatMapPlot = new JCheckBox("Plot aktivieren");
		chckbxEnableHeatMapPlot.setBorder(null);
		heatMapPrefsPanel.add(chckbxEnableHeatMapPlot, "2, 2, fill, top");
		
		JLabel lblQuelldatensatz = new JLabel("Quelldatensatz:");
		heatMapPrefsPanel.add(lblQuelldatensatz, "2, 4, right, default");
		
		ArrayList<String> playerList = new ArrayList<String>();
		for (int i = 1; i <= vidFrame.getPanel().getProjectors().size(); i++) {
			playerList.add("Spieler " + i);
		}
		
		if (playerList.size() < 1)
			playerList.add("Keine Daten geladen");
		
		comboBoxOverlayHeatMapSource = new JComboBox();
		comboBoxOverlayHeatMapSource.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
		heatMapPrefsPanel.add(comboBoxOverlayHeatMapSource, "4, 4, fill, default");
		
		JPanel panelMinDistPrefs = new JPanel();
		tabbedPane.addTab("Mindestdistanz-Plot", null, panelMinDistPrefs, null);
		panelMinDistPrefs.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		JPanel minDistPrefsPanel = new JPanel();
		minDistPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)), "Mindestdistanz-Plot-Parameter", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelMinDistPrefs.add(minDistPrefsPanel, "2, 2, fill, fill");
		minDistPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		JLabel label = new JLabel("Mindestdistanz (in px; default: 150):");
		minDistPrefsPanel.add(label, "2, 2, right, default");
		
		txtMinDistPlotDist = new JTextField();
		txtMinDistPlotDist.setHorizontalAlignment(SwingConstants.LEFT);
		txtMinDistPlotDist.setColumns(10);
		minDistPrefsPanel.add(txtMinDistPlotDist, "4, 2, fill, default");
		
		JLabel lblQuelldatensatz_1 = new JLabel("Quelldatensatz 1:");
		minDistPrefsPanel.add(lblQuelldatensatz_1, "2, 4, right, default");
		
		comboBoxMinDistPlotSource1 = new JComboBox();
		comboBoxMinDistPlotSource1.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
		minDistPrefsPanel.add(comboBoxMinDistPlotSource1, "4, 4, fill, default");
		
		JLabel lblQuelldatensatz_2 = new JLabel("Quelldatensatz 2:");
		minDistPrefsPanel.add(lblQuelldatensatz_2, "2, 6, right, default");
		
		comboBoxMinDistPlotSource2 = new JComboBox();
		comboBoxMinDistPlotSource2.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
		minDistPrefsPanel.add(comboBoxMinDistPlotSource2, "4, 6, fill, default");
		
		JPanel panelGazeFilterPrefs = new JPanel();
		tabbedPane.addTab("Gaze Filter", null, panelGazeFilterPrefs, null);
		panelGazeFilterPrefs.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("4dlu:grow"),}));
		
		JPanel filterParameterPrefsPanel = new JPanel();
		filterParameterPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)), "Filter-Parameter", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelGazeFilterPrefs.add(filterParameterPrefsPanel, "2, 2, fill, fill");
		filterParameterPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		JLabel lblVelocityThresholdin = new JLabel("Velocity Threshold (in deg/sec; default: 20):");
		filterParameterPrefsPanel.add(lblVelocityThresholdin, "2, 2, right, default");
		
		txtGazeFilterVelocityThreshold = new JTextField();
		filterParameterPrefsPanel.add(txtGazeFilterVelocityThreshold, "4, 2, fill, default");
		txtGazeFilterVelocityThreshold.setColumns(10);
		
		JLabel lblDistanceThresholdin = new JLabel("Distance Threshold (in px; default: 35):");
		filterParameterPrefsPanel.add(lblDistanceThresholdin, "2, 4, right, default");
		
		txtGazeFilterDistanceThreshold = new JTextField();
		filterParameterPrefsPanel.add(txtGazeFilterDistanceThreshold, "4, 4, fill, default");
		txtGazeFilterDistanceThreshold.setColumns(10);
		
		JPanel panelHeatMapGeneratorPrefs = new JPanel();
		tabbedPane.addTab("Heatmap-Generator", null, panelHeatMapGeneratorPrefs, null);
		panelHeatMapGeneratorPrefs.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		JPanel heatMapGeneratorPrefsPanel = new JPanel();
		heatMapGeneratorPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)), "Generator-Parameter", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelHeatMapGeneratorPrefs.add(heatMapGeneratorPrefsPanel, "2, 2, fill, fill");
		heatMapGeneratorPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		chckbxHeatMapGeneratorSkipPercentage = new JCheckBox("\u00DCberspringe Prozentsatz der Events");
		chckbxHeatMapGeneratorSkipPercentage.setBorder(null);
		heatMapGeneratorPrefsPanel.add(chckbxHeatMapGeneratorSkipPercentage, "2, 2");
		
		JLabel lblDieseOptionBeschleunigt = new JLabel("(Schnellere Generierung bei gro\u00DFen Datens\u00E4tzen. Verringert Genauigkeit.)");
		heatMapGeneratorPrefsPanel.add(lblDieseOptionBeschleunigt, "4, 2, left, default");
		
		JLabel lblProzentsatzin = new JLabel("Prozentsatz (in %; default: 0.05):");
		heatMapGeneratorPrefsPanel.add(lblProzentsatzin, "2, 4, right, default");
		
		txtHeatMapGeneratorSkipPercentage = new JTextField();
		heatMapGeneratorPrefsPanel.add(txtHeatMapGeneratorSkipPercentage, "4, 4, fill, default");
		txtHeatMapGeneratorSkipPercentage.setColumns(10);
		
		JLabel lblHitzeradiusinPx = new JLabel("Hitzeradius (in px; default: 80):");
		heatMapGeneratorPrefsPanel.add(lblHitzeradiusinPx, "2, 6, right, default");
		
		txtHeatMapGeneratorHeatRadius = new JTextField();
		heatMapGeneratorPrefsPanel.add(txtHeatMapGeneratorHeatRadius, "4, 6, fill, default");
		txtHeatMapGeneratorHeatRadius.setColumns(10);
		
		chckbxHeatMapGeneratorGenFromFrequencyInstead = new JCheckBox("Aggregiere nach H\u00E4ufung statt Dauer");
		chckbxHeatMapGeneratorGenFromFrequencyInstead.setBorder(null);
		heatMapGeneratorPrefsPanel.add(chckbxHeatMapGeneratorGenFromFrequencyInstead, "2, 8");
		
		JLabel lblnderungenTretenErst = new JLabel("\u00C4nderungen werden erst bei der n\u00E4chsten (manuellen) Generierung wirksam.");
		panelHeatMapGeneratorPrefs.add(lblnderungenTretenErst, "2, 3, center, default");
		
		JPanel panelTempSyncPrefs = new JPanel();
		tabbedPane.addTab("Datensatz-Synchronisation", null, panelTempSyncPrefs, null);
		panelTempSyncPrefs.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("fill:default:grow"),}));
		
		JPanel histogramCompPrefsPanel = new JPanel();
		histogramCompPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(128, 128, 128)), "Zeitliche Synchronisation \u00FCber Histogramm-Vergleich", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelTempSyncPrefs.add(histogramCompPrefsPanel, "2, 2, fill, fill");
		histogramCompPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:default"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		JLabel lblGittergre = new JLabel("Gittergr\u00F6\u00DFe (x * x; default: 16; 16 <= x <= 20):");
		histogramCompPrefsPanel.add(lblGittergre, "2, 2, right, default");
		
		spinnerHistogramGridSize = new JSpinner();
		spinnerHistogramGridSize.setModel(new SpinnerNumberModel(16, 16, 20, 1));
		JComponent editor = spinnerHistogramGridSize.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
			spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
		}
		histogramCompPrefsPanel.add(spinnerHistogramGridSize, "4, 2, fill, default");
		
		JLabel lblDeviationThreshold = new JLabel("Correlation Threshold (in %; default: 40):");
		histogramCompPrefsPanel.add(lblDeviationThreshold, "2, 4, right, default");
		
		txtHistogramCorrelationThreshold = new JTextField();
		histogramCompPrefsPanel.add(txtHistogramCorrelationThreshold, "4, 4, fill, default");
		txtHistogramCorrelationThreshold.setColumns(10);
		
		JLabel lblAmountThreshold = new JLabel("Anzahl abweichender Zellen Threshold (default: 1):");
		histogramCompPrefsPanel.add(lblAmountThreshold, "2, 6, right, default");
		
		txtHistogramDeviatingCellsThreshold = new JTextField();
		histogramCompPrefsPanel.add(txtHistogramDeviatingCellsThreshold, "4, 6, fill, default");
		txtHistogramDeviatingCellsThreshold.setColumns(10);
		
		JLabel lblMetrik = new JLabel("Metrik:");
		histogramCompPrefsPanel.add(lblMetrik, "2, 8, right, default");
		
		JLabel lblZweiBilderSind = new JLabel("Zwei Bilder sind ungleich, wenn die Korrelation von mindestens X Zellen <= Y% betr\u00E4gt.");
		histogramCompPrefsPanel.add(lblZweiBilderSind, "4, 8");
		
		JLabel lblberDieseMetrik = new JLabel("\u00DCber diese Metrik wird der erste Frame eines gegebenen Zustandes / Zuges gesucht.");
		histogramCompPrefsPanel.add(lblberDieseMetrik, "4, 10");
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		JButton btnOKButton = new JButton("      OK      ");
		btnOKButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// Einstellungen speichern
				writeElementsToPrefs();
				PreferencesFrame.this.setVisible(false);
				PreferencesFrame.this.dispose();
			}
		});
		buttonPanel.add(btnOKButton);
		
		JButton btnCancelButton = new JButton("Abbrechen");
		btnCancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				PreferencesFrame.this.setVisible(false);
				PreferencesFrame.this.dispose();
			}
		});
		buttonPanel.add(btnCancelButton);
		
		readPrefsIntoElements();
		
		this.pack();
		this.setMinimumSize(new Dimension((int) Math.max(500, this.getSize().getWidth()), (int) Math.max(380, this.getSize().getHeight())));
		setLocationRelativeTo(null);
	}

	private void readPrefsIntoElements() {
		
		Preferences prefs = Project.currentProject().getPreferences();
		
		txtVisibilityDuration.setText(Integer.toString(prefs.getFixationOverlayTimeSpan()));
		txtGazeFilterVelocityThreshold.setText(Integer.toString(prefs.getFilterVelocityThreshold()));
		txtGazeFilterDistanceThreshold.setText(Integer.toString(prefs.getFilterDistanceThreshold()));
		txtMinDistPlotDist.setText(Integer.toString(prefs.getMinDistPlotMinDist()));
		txtHeatMapGeneratorSkipPercentage.setText(Float.toString(prefs.getHeatMapGenSkipPercentage()));
		txtHeatMapGeneratorHeatRadius.setText(Integer.toString(prefs.getHeatMapGenHeatRadius()));
		chckbxHeatMapGeneratorSkipPercentage.setSelected(prefs.getHeatMapGenSkipEvents());
		comboBoxOverlayHeatMapSource.setSelectedIndex(Math.min(comboBoxOverlayHeatMapSource.getItemCount() - 1, prefs.getHeatMapOverlayPlayer()));
		chckbxEnableHeatMapPlot.setSelected(prefs.getEnableHeatMapOverlay());
		chckbxEnableRawDataPlot.setSelected(prefs.getEnableRawDataOverlay());
		chckbxEnableFixationPlot.setSelected(prefs.getEnableFixationOverlay());
		comboBoxMinDistPlotSource1.setSelectedIndex(Math.min(comboBoxMinDistPlotSource1.getItemCount() - 1, prefs.getMinDistPlotPlayer1()));
		comboBoxMinDistPlotSource2.setSelectedIndex(Math.min(comboBoxMinDistPlotSource2.getItemCount() - 1, prefs.getMinDistPlotPlayer2()));
		chckbxHeatMapGeneratorGenFromFrequencyInstead.setSelected(prefs.getHeatMapGenGenFromFrequencyInstead());
		txtHistogramCorrelationThreshold.setText(Integer.toString(prefs.getHistogramCorrelationThreshold()));
		txtHistogramDeviatingCellsThreshold.setText(Integer.toString(prefs.getHistogramDeviatingCellsThreshold()));
		spinnerHistogramGridSize.setValue(prefs.getHistogramGridSize());
	}
	
	private void writeElementsToPrefs() {
		
		Preferences prefs = Project.currentProject().getPreferences();
		
		if (!txtVisibilityDuration.getText().isEmpty())
			prefs.setFixationOverlayTimeSpan(Integer.parseInt(txtVisibilityDuration.getText()));
		if (!txtGazeFilterVelocityThreshold.getText().isEmpty())
			prefs.setFilterVelocityThreshold(Integer.parseInt(txtGazeFilterVelocityThreshold.getText()));
		if (!txtGazeFilterDistanceThreshold.getText().isEmpty())
			prefs.setFilterDistanceThreshold(Integer.parseInt(txtGazeFilterDistanceThreshold.getText()));
		if (!txtMinDistPlotDist.getText().isEmpty())
			prefs.setMinDistPlotMinDist(Integer.parseInt(txtMinDistPlotDist.getText()));
		if (!txtHeatMapGeneratorSkipPercentage.getText().isEmpty())
			prefs.setHeatMapGenSkipPercentage(Float.parseFloat(txtHeatMapGeneratorSkipPercentage.getText()));
		if (!txtHeatMapGeneratorHeatRadius.getText().isEmpty())
			prefs.setHeatMapGenHeatRadius(Integer.parseInt(txtHeatMapGeneratorHeatRadius.getText()));
		prefs.setHeatMapGenSkipEvents(chckbxHeatMapGeneratorSkipPercentage.isSelected());
		if (comboBoxOverlayHeatMapSource.getSelectedIndex() >= 0)
			prefs.setHeatMapOverlayPlayer(comboBoxOverlayHeatMapSource.getSelectedIndex());
		prefs.setEnableHeatMapOverlay(chckbxEnableHeatMapPlot.isSelected());
		prefs.setEnableRawDataOverlay(chckbxEnableRawDataPlot.isSelected());
		prefs.setEnableFixationOverlay(chckbxEnableFixationPlot.isSelected());
		if (comboBoxMinDistPlotSource1.getSelectedIndex() >= 0)
			prefs.setMinDistPlotPlayer1(comboBoxMinDistPlotSource1.getSelectedIndex());
		if (comboBoxMinDistPlotSource2.getSelectedIndex() >= 0)
			prefs.setMinDistPlotPlayer2(comboBoxMinDistPlotSource2.getSelectedIndex());
		prefs.setHeatMapGenGenFromFrequencyInstead(chckbxHeatMapGeneratorGenFromFrequencyInstead.isSelected());
		if (!txtHistogramCorrelationThreshold.getText().isEmpty())
			prefs.setHistogramCorrelationThreshold(Integer.parseInt(txtHistogramCorrelationThreshold.getText()));
		if (!txtHistogramDeviatingCellsThreshold.getText().isEmpty())
			prefs.setHistogramDeviatingCellsThreshold(Integer.parseInt(txtHistogramDeviatingCellsThreshold.getText()));
		prefs.setHistogramGridSize((int) spinnerHistogramGridSize.getValue());
		
		
		for (OverlayGazeProjector p : vidFrame.getPanel().getProjectors()) {
			IVTFilter.filterRecording(p.getRecording(), prefs.getFilterVelocityThreshold(), prefs.getFilterDistanceThreshold());
			p.transformFilteredPointsToTarget(vidFrame.getHostProjector().getRecording());
		}
		
		vidFrame.updateQuickSettingsToolbar();
		vidFrame.getPanel().repaint();
		
		System.gc();
	}
}
