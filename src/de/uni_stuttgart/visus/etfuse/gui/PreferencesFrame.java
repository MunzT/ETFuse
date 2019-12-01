package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

import org.opencv.imgproc.Imgproc;

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
    private JComboBox comboBoxMinDistTemporalSubdivision;
    private JSpinner spinnerSubdivisionIntervalSize;
    private JTextField txtMinDistPlotDist;
    private JComboBox comboBoxHeatmapSource;
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
    private JComboBox comboBoxHeatmapColors;
    private JSpinner spinnerHeatmapTransparency;
    private ColorChooserButton colorPickerButtonFixationsPlayer1;
    private ColorChooserButton colorPickerButtonFixationsPlayer2;
    private ColorChooserButton colorPickerButtonMinDistPlotClose;
    private ColorChooserButton colorPickerButtonMinDistPlotFar;
    private ColorChooserButton colorPickerButtonMinDistPlotOutsideBoard;
    private ColorChooserButton colorPickerButtonMinDistPlotOutsideScreen;
    private ColorChooserButton colorPickerButtonHeatmapPlayer1;
    private ColorChooserButton colorPickerButtonHeatmapPlayer2;
    private JCheckBox chckbxAdditionalEventsForMinDistPlot;
    private JCheckBox chckbxPlayer1EventsForMinDistPlot;
    private JCheckBox chckbxPlayer2EventsForMinDistPlot;
    private JCheckBox chckbxAdditionalEventsTicks;
    private JCheckBox chckbxPlayer1Ticks;
    private JCheckBox chckbxPlayer2Ticks;
    private JCheckBox chckbxAdditionalEventsForHeatmap;
    private JCheckBox chckbxPlayer1EventsForHeatmap;
    private JCheckBox chckbxPlayer2EventsForHeatmap;

    Map<Integer, Integer> colorMapMapping;
    Map<Integer, Integer> colorMapIndexMapping;

    private VideoFrame vidFrame = null;

    public PreferencesFrame(VideoFrame vidFrame) {

        super(vidFrame, "Settings", true);

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
        fixationPrefsPanel.setBorder(new TitledBorder(null, "Fixations", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
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

        chckbxEnableFixationPlot = new JCheckBox("Activate plot");
        chckbxEnableFixationPlot.setBorder(null);
        chckbxEnableFixationPlot.setAlignmentY(Component.TOP_ALIGNMENT);
        fixationPrefsPanel.add(chckbxEnableFixationPlot, "2, 2, left, top");

        JLabel labelVisibilityDuration = new JLabel(
                "Visibility of completed fixations (in ms, default: 500):");
        fixationPrefsPanel.add(labelVisibilityDuration, "2, 4, fill, fill");

        txtVisibilityDuration = new JTextField();
        txtVisibilityDuration.setHorizontalAlignment(SwingConstants.LEFT);
        fixationPrefsPanel.add(txtVisibilityDuration, "4, 4, fill, center");
        txtVisibilityDuration.setColumns(10);

        JPanel rawDataPrefsPanel = new JPanel();
        rawDataPrefsPanel.setBorder(new TitledBorder(null, "Raw Data", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        panelOverlayPrefs.add(rawDataPrefsPanel, "1, 2, fill, center");
        rawDataPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("left:default"),
                ColumnSpec.decode("default:grow"),},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        chckbxEnableRawDataPlot = new JCheckBox("Activate plot");
        chckbxEnableRawDataPlot.setBorder(null);
        rawDataPrefsPanel.add(chckbxEnableRawDataPlot, "2, 2, fill, top");

        JPanel heatMapPrefsPanel = new JPanel();
        heatMapPrefsPanel.setBorder(new TitledBorder(null, "Attention Map", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
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

        chckbxEnableHeatMapPlot = new JCheckBox("Activate plot");
        chckbxEnableHeatMapPlot.setBorder(null);
        heatMapPrefsPanel.add(chckbxEnableHeatMapPlot, "2, 2, fill, top");

        JLabel lblQuelldatensatz = new JLabel("Source data set:");
        heatMapPrefsPanel.add(lblQuelldatensatz, "2, 4, right, default");

        ArrayList<String> playerList = new ArrayList<String>();
        for (int i = 1; i <= vidFrame.getPanel().getProjectors().size(); i++) {
            playerList.add("Player " + i);
        }

        if (playerList.size() < 1)
            playerList.add("No data loaded");

        ArrayList<String> playerListForHeatmaps =  new ArrayList<>(playerList);
        if (playerList.size() >= 2)
            playerListForHeatmaps.add("Player 1 + 2"); // TODO make it more generalized

        comboBoxOverlayHeatMapSource = new JComboBox();
        comboBoxOverlayHeatMapSource.setModel(new DefaultComboBoxModel(playerListForHeatmaps.toArray(new String[0])));
        heatMapPrefsPanel.add(comboBoxOverlayHeatMapSource, "4, 4, fill, default");

        JPanel panelMinDistPrefs = new JPanel();
        tabbedPane.addTab("Minimum Distance Plot", null, panelMinDistPrefs, null);
        panelMinDistPrefs.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                RowSpec.decode("default:grow"),}));

        JPanel minDistPrefsPanel = new JPanel();
        minDistPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED,
                new Color(255, 255, 255), new Color(128, 128, 128)), "Minimum Distance Plot Parameters",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
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
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        JLabel minDistTemporalSubdivision = new JLabel("Temporal subdivision:");
        minDistPrefsPanel.add(minDistTemporalSubdivision, "2, 2, right, default");

        String[] minDistTemporalSubdivisionList = { "Minimum areas", "Mouse clicks/events", "Intervals" };

        comboBoxMinDistTemporalSubdivision = new JComboBox();
        comboBoxMinDistTemporalSubdivision.setModel(new DefaultComboBoxModel(minDistTemporalSubdivisionList));
        minDistPrefsPanel.add(comboBoxMinDistTemporalSubdivision, "4, 2, fill, default");

        JLabel lblEventsForMinDistPlot = new JLabel("Event types (if mouse clicks/events is selected):");
        minDistPrefsPanel.add(lblEventsForMinDistPlot, "2, 4, right, default");

        chckbxAdditionalEventsForMinDistPlot = new JCheckBox("Additional Events");
        chckbxAdditionalEventsForMinDistPlot.setBorder(null);
        minDistPrefsPanel.add(chckbxAdditionalEventsForMinDistPlot, "4, 4, fill, top");

        chckbxPlayer1EventsForMinDistPlot = new JCheckBox("Player 1");
        chckbxPlayer1EventsForMinDistPlot.setBorder(null);
        minDistPrefsPanel.add(chckbxPlayer1EventsForMinDistPlot, "4, 6, fill, top");

        chckbxPlayer2EventsForMinDistPlot = new JCheckBox("Player 2");
        chckbxPlayer2EventsForMinDistPlot.setBorder(null);
        minDistPrefsPanel.add(chckbxPlayer2EventsForMinDistPlot, "4, 8, fill, top");

        JLabel lblTimelineIntervalSize = new JLabel("Subdivision interval size (if Intervals is selected):");
        minDistPrefsPanel.add(lblTimelineIntervalSize, "2, 10, right, default");

        spinnerSubdivisionIntervalSize = new JSpinner();
        spinnerSubdivisionIntervalSize.setModel(new SpinnerNumberModel(1000, 0, 1000000, 1));
        JComponent editor3 = spinnerSubdivisionIntervalSize.getEditor();
        if (editor3 instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor3;
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
        minDistPrefsPanel.add(spinnerSubdivisionIntervalSize, "4, 10, fill, default");

        JLabel label = new JLabel("Minimum distance (in px; default: 200):");
        minDistPrefsPanel.add(label, "2, 12, right, default");

        txtMinDistPlotDist = new JTextField();
        txtMinDistPlotDist.setHorizontalAlignment(SwingConstants.LEFT);
        txtMinDistPlotDist.setColumns(10);
        minDistPrefsPanel.add(txtMinDistPlotDist, "4, 12, fill, default");

        JLabel lblQuelldatensatz_1 = new JLabel("Source data set 1:");
        minDistPrefsPanel.add(lblQuelldatensatz_1, "2, 14, right, default");

        comboBoxMinDistPlotSource1 = new JComboBox();
        comboBoxMinDistPlotSource1.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
        minDistPrefsPanel.add(comboBoxMinDistPlotSource1, "4, 14, fill, default");

        JLabel lblQuelldatensatz_2 = new JLabel("Source data set 2:");
        minDistPrefsPanel.add(lblQuelldatensatz_2, "2, 16, right, default");

        comboBoxMinDistPlotSource2 = new JComboBox();
        comboBoxMinDistPlotSource2.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
        minDistPrefsPanel.add(comboBoxMinDistPlotSource2, "4, 16, fill, default");

        JLabel lblTicks = new JLabel("Ticks:");
        minDistPrefsPanel.add(lblTicks, "2, 18, right, default");

        chckbxAdditionalEventsTicks = new JCheckBox("Additional Events");
        chckbxAdditionalEventsTicks.setBorder(null);
        minDistPrefsPanel.add(chckbxAdditionalEventsTicks, "4, 18, fill, top");

        chckbxPlayer1Ticks = new JCheckBox("Player 1");
        chckbxPlayer1Ticks.setBorder(null);
        minDistPrefsPanel.add(chckbxPlayer1Ticks, "4, 20, fill, top");

        chckbxPlayer2Ticks = new JCheckBox("Player 2");
        chckbxPlayer2Ticks.setBorder(null);
        minDistPrefsPanel.add(chckbxPlayer2Ticks, "4, 22, fill, top");

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
        filterParameterPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED,
                new Color(255, 255, 255), new Color(128, 128, 128)), "Filter Parameters",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
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
        tabbedPane.addTab("Heatmap Generator", null, panelHeatMapGeneratorPrefs, null);
        panelHeatMapGeneratorPrefs.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                RowSpec.decode("default:grow"),}));

        JPanel heatMapGeneratorPrefsPanel = new JPanel();
        heatMapGeneratorPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED,
                new Color(255, 255, 255), new Color(128, 128, 128)), "Generator Parameters",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
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
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        JLabel heatmapSource = new JLabel("Heatmap source:");
        heatMapGeneratorPrefsPanel.add(heatmapSource, "2, 2, right, default");

        String[] heatmapSourceList = { "Userdefined region", "Mouse clicks/events" };

        comboBoxHeatmapSource = new JComboBox();
        comboBoxHeatmapSource.setModel(new DefaultComboBoxModel(heatmapSourceList));
        heatMapGeneratorPrefsPanel.add(comboBoxHeatmapSource, "4, 2, fill, default");

        JLabel lblEventsForHeatmap = new JLabel("Event types (if mouse clicks/events is selected):");
        heatMapGeneratorPrefsPanel.add(lblEventsForMinDistPlot, "2, 4, right, default");

        chckbxAdditionalEventsForHeatmap = new JCheckBox("Additional Events");
        chckbxAdditionalEventsForHeatmap.setBorder(null);
        heatMapGeneratorPrefsPanel.add(chckbxAdditionalEventsForHeatmap, "4, 4, fill, top");

        chckbxPlayer1EventsForHeatmap = new JCheckBox("Player 1");
        chckbxPlayer1EventsForHeatmap.setBorder(null);
        heatMapGeneratorPrefsPanel.add(chckbxPlayer1EventsForHeatmap, "4, 6, fill, top");

        chckbxPlayer2EventsForHeatmap = new JCheckBox("Player 2");
        chckbxPlayer2EventsForHeatmap.setBorder(null);
        heatMapGeneratorPrefsPanel.add(chckbxPlayer2EventsForHeatmap, "4, 8, fill, top");

        chckbxHeatMapGeneratorSkipPercentage = new JCheckBox("Skip percentage of events");
        chckbxHeatMapGeneratorSkipPercentage.setBorder(null);
        heatMapGeneratorPrefsPanel.add(chckbxHeatMapGeneratorSkipPercentage, "2, 10");

        JLabel lblDieseOptionBeschleunigt = new JLabel("(Faster generation of large datasets. Decreases accuracy.)");
        heatMapGeneratorPrefsPanel.add(lblDieseOptionBeschleunigt, "4, 10, left, default");

        JLabel lblProzentsatzin = new JLabel("Percentage (in %; default: 0.05):");
        heatMapGeneratorPrefsPanel.add(lblProzentsatzin, "2, 12, right, default");

        txtHeatMapGeneratorSkipPercentage = new JTextField();
        heatMapGeneratorPrefsPanel.add(txtHeatMapGeneratorSkipPercentage, "4, 12, fill, default");
        txtHeatMapGeneratorSkipPercentage.setColumns(10);

        JLabel lblHitzeradiusinPx = new JLabel("Heat radius (in px; default: 80):");
        heatMapGeneratorPrefsPanel.add(lblHitzeradiusinPx, "2, 14, right, default");

        txtHeatMapGeneratorHeatRadius = new JTextField();
        heatMapGeneratorPrefsPanel.add(txtHeatMapGeneratorHeatRadius, "4, 14, fill, default");
        txtHeatMapGeneratorHeatRadius.setColumns(10);

        chckbxHeatMapGeneratorGenFromFrequencyInstead = new JCheckBox("Aggregate by frequency instead of duration");
        chckbxHeatMapGeneratorGenFromFrequencyInstead.setBorder(null);
        heatMapGeneratorPrefsPanel.add(chckbxHeatMapGeneratorGenFromFrequencyInstead, "2, 16");

        JPanel panelTempSyncPrefs = new JPanel();
        tabbedPane.addTab("Data Set Synchronisation", null, panelTempSyncPrefs, null);
        panelTempSyncPrefs.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                RowSpec.decode("fill:default:grow"),}));

        JPanel histogramCompPrefsPanel = new JPanel();
        histogramCompPrefsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED,
                new Color(255, 255, 255), new Color(128, 128, 128)),
                "Temporal Synchronization via Histogram Comparison", TitledBorder.LEADING,
                TitledBorder.TOP, null, new Color(0, 0, 0)));
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

        JLabel lblGittergre = new JLabel("Grid size (x * x; default: 16; 16 <= x <= 20):");
        histogramCompPrefsPanel.add(lblGittergre, "2, 2, right, default");

        spinnerHistogramGridSize = new JSpinner();
        spinnerHistogramGridSize.setModel(new SpinnerNumberModel(16, 16, 20, 1));
        JComponent editor = spinnerHistogramGridSize.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
        histogramCompPrefsPanel.add(spinnerHistogramGridSize, "4, 2, fill, default");

        JLabel lblDeviationThreshold = new JLabel("Correlation threshold (in %; default: 40):");
        histogramCompPrefsPanel.add(lblDeviationThreshold, "2, 4, right, default");

        txtHistogramCorrelationThreshold = new JTextField();
        histogramCompPrefsPanel.add(txtHistogramCorrelationThreshold, "4, 4, fill, default");
        txtHistogramCorrelationThreshold.setColumns(10);

        JLabel lblAmountThreshold = new JLabel("Number of deviating cells threshold (default: 1):");
        histogramCompPrefsPanel.add(lblAmountThreshold, "2, 6, right, default");

        txtHistogramDeviatingCellsThreshold = new JTextField();
        histogramCompPrefsPanel.add(txtHistogramDeviatingCellsThreshold, "4, 6, fill, default");
        txtHistogramDeviatingCellsThreshold.setColumns(10);

        JLabel lblMetrik = new JLabel("Metric:");
        histogramCompPrefsPanel.add(lblMetrik, "2, 8, right, default");

        JLabel lblZweiBilderSind = new JLabel("Two images are unequal if the correlation of at least X cells is <= Y%.");
        histogramCompPrefsPanel.add(lblZweiBilderSind, "4, 8");

        JLabel lblberDieseMetrik = new JLabel("This metric is used to search for the first frame of a given state / move.");
        histogramCompPrefsPanel.add(lblberDieseMetrik, "4, 10");

        JPanel panelColorsPrefs = new JPanel();
        tabbedPane.addTab("Colors", null, panelColorsPrefs, null);
        panelColorsPrefs.setLayout(new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("default:grow"),},
            new RowSpec[] {
                RowSpec.decode("fill:default"),
                RowSpec.decode("fill:default"),
                RowSpec.decode("fill:default"),}));

        JPanel fixationsColorPrefsPanel = new JPanel();
        fixationsColorPrefsPanel.setBorder(new TitledBorder(null, "Fixations", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        panelColorsPrefs.add(fixationsColorPrefsPanel, "1, 1, fill, center");
        fixationsColorPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
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

        JLabel fixationsPlayer1ColorLabel = new JLabel("Player 1:");
        fixationsColorPrefsPanel.add(fixationsPlayer1ColorLabel, "2, 2, right, default");

        colorPickerButtonFixationsPlayer1 = new ColorChooserButton();
        fixationsColorPrefsPanel.add(colorPickerButtonFixationsPlayer1, "4, 2, fill, default");

        JLabel fixationsPlayer2ColorLabel = new JLabel("Player 2:");
        fixationsColorPrefsPanel.add(fixationsPlayer2ColorLabel, "2, 4, right, default");

        colorPickerButtonFixationsPlayer2 = new ColorChooserButton();
        fixationsColorPrefsPanel.add(colorPickerButtonFixationsPlayer2, "4, 4, fill, default");

        JPanel minDistPlotColorPrefsPanel = new JPanel();
        minDistPlotColorPrefsPanel.setBorder(new TitledBorder(null, "MinDist Plot", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        panelColorsPrefs.add(minDistPlotColorPrefsPanel, "1, 2, fill, center");
        minDistPlotColorPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
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
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        JLabel colorPickerMinDistPlotCloseLabel = new JLabel("Small distance between gaze points:");
        minDistPlotColorPrefsPanel.add(colorPickerMinDistPlotCloseLabel, "2, 2, right, default");

        colorPickerButtonMinDistPlotClose =new ColorChooserButton();
        minDistPlotColorPrefsPanel.add(colorPickerButtonMinDistPlotClose, "4, 2, fill, default");

        JLabel colorPickerMinDistPlotFarLabel = new JLabel("Larger distance between gaze points:");
        minDistPlotColorPrefsPanel.add(colorPickerMinDistPlotFarLabel, "2, 4, right, default");

        colorPickerButtonMinDistPlotFar = new ColorChooserButton();
        minDistPlotColorPrefsPanel.add(colorPickerButtonMinDistPlotFar, "4, 4, fill, default");

        JLabel colorPickerMinDistPlotOutsideBoardLabel = new JLabel("Gaze points outside the board:");
        minDistPlotColorPrefsPanel.add(colorPickerMinDistPlotOutsideBoardLabel, "2, 6, right, default");

        colorPickerButtonMinDistPlotOutsideBoard = new ColorChooserButton();
        minDistPlotColorPrefsPanel.add(colorPickerButtonMinDistPlotOutsideBoard, "4, 6, fill, default");

        JLabel colorPickerMinDistPlotOutsideScreenLabel = new JLabel("Missing gaze points:");
        minDistPlotColorPrefsPanel.add(colorPickerMinDistPlotOutsideScreenLabel, "2, 8, right, default");

        colorPickerButtonMinDistPlotOutsideScreen = new ColorChooserButton();
        minDistPlotColorPrefsPanel.add(colorPickerButtonMinDistPlotOutsideScreen, "4, 8, fill, default");

        JPanel heatmapColorPrefsPanel = new JPanel();
        heatmapColorPrefsPanel.setBorder(new TitledBorder(null, "Heatmap", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        panelColorsPrefs.add(heatmapColorPrefsPanel, "1, 3, fill, center");
        heatmapColorPrefsPanel.setLayout(new FormLayout(new ColumnSpec[] {
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
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        JLabel heatmapColors = new JLabel("Color map (for one player):");
        heatmapColorPrefsPanel.add(heatmapColors, "2, 2, right, default");

        int[] colorMaps = {
                Imgproc.COLORMAP_AUTUMN,
                Imgproc.COLORMAP_BONE,
                Imgproc.COLORMAP_JET,
                Imgproc.COLORMAP_WINTER,
                Imgproc.COLORMAP_RAINBOW,
                Imgproc.COLORMAP_OCEAN,
                Imgproc.COLORMAP_SUMMER,
                Imgproc.COLORMAP_SPRING,
                Imgproc.COLORMAP_COOL,
                Imgproc.COLORMAP_HSV,
                Imgproc.COLORMAP_PINK,
                Imgproc.COLORMAP_HOT,
                Imgproc.COLORMAP_PARULA,
                Imgproc.COLORMAP_MAGMA,
                Imgproc.COLORMAP_INFERNO,
                Imgproc.COLORMAP_PLASMA,
                Imgproc.COLORMAP_VIRIDIS,
                Imgproc.COLORMAP_CIVIDIS,
                Imgproc.COLORMAP_TWILIGHT,
                Imgproc.COLORMAP_TWILIGHT_SHIFTED };

        String[] colorMapsStrings = {
                "AUTUMN",
                "BONE",
                "JET",
                "WINTER",
                "RAINBOW",
                "OCEAN",
                "SUMMER",
                "SPRING",
                "COOL",
                "HSV",
                "PINK",
                "HOT",
                "PARULA",
                "MAGMA",
                "INFERNO",
                "PLASMA",
                "VIRIDIS",
                "CIVIDIS",
                "TWILIGHT",
                "TWILIGHT_SHIFTED" };

        colorMapMapping = new HashMap<Integer, Integer>();
        colorMapIndexMapping = new HashMap<Integer, Integer>();
        for (int i = 0; i < colorMapsStrings.length; i++) {
            colorMapMapping.put(i, colorMaps[i]);
            colorMapIndexMapping.put(colorMaps[i], i);
        }

        comboBoxHeatmapColors = new JComboBox();
        comboBoxHeatmapColors.setModel(new DefaultComboBoxModel(colorMapsStrings));
        heatmapColorPrefsPanel.add(comboBoxHeatmapColors, "4, 2, fill, default");

        JLabel heatmapTransparencyLabel = new JLabel("Heatmap transparency (for one player):");
        heatmapColorPrefsPanel.add(heatmapTransparencyLabel, "2, 4, right, default");

        spinnerHeatmapTransparency = new JSpinner();
        spinnerHeatmapTransparency.setModel(new SpinnerNumberModel(128, 0, 256, 1));
        JComponent editor2 = spinnerHeatmapTransparency.getEditor();
        if (editor2 instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor2;
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
        heatmapColorPrefsPanel.add(spinnerHeatmapTransparency, "4, 4, fill, default");

        JLabel heatmapPlayer1ColorLabel = new JLabel("Player 1 (multiple heatmaps):");
        heatmapColorPrefsPanel.add(heatmapPlayer1ColorLabel, "2, 6, right, default");

        colorPickerButtonHeatmapPlayer1 = new ColorChooserButton();
        heatmapColorPrefsPanel.add(colorPickerButtonHeatmapPlayer1, "4, 6, fill, default");

        JLabel heatmapPlayer2ColorLabel = new JLabel("Player 2 (multiple heatmaps):");
        heatmapColorPrefsPanel.add(heatmapPlayer2ColorLabel, "2, 8, right, default");

        colorPickerButtonHeatmapPlayer2 = new ColorChooserButton();
        heatmapColorPrefsPanel.add(colorPickerButtonHeatmapPlayer2, "4, 8, fill, default");

        JPanel buttonPanel = new JPanel();
        FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        JButton btnOKButton = new JButton("      OK      ");
        btnOKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Einstellungen speichern
                writeElementsToPrefs();
                PreferencesFrame.this.setVisible(false);
                PreferencesFrame.this.dispose();
            }
        });
        buttonPanel.add(btnOKButton);

        JButton btnCancelButton = new JButton("Abort");
        btnCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PreferencesFrame.this.setVisible(false);
                PreferencesFrame.this.dispose();
            }
        });
        buttonPanel.add(btnCancelButton);

        readPrefsIntoElements();

        this.pack();
        this.setMinimumSize(new Dimension((int) Math.max(500, this.getSize().getWidth()),
                (int) Math.max(380, this.getSize().getHeight())));
        setLocationRelativeTo(null);
    }

    private void readPrefsIntoElements() {

        Preferences prefs = Project.currentProject().getPreferences();

        txtVisibilityDuration.setText(Integer.toString(prefs.getFixationOverlayTimeSpan()));
        txtGazeFilterVelocityThreshold.setText(Integer.toString(prefs.getFilterVelocityThreshold()));
        txtGazeFilterDistanceThreshold.setText(Integer.toString(prefs.getFilterDistanceThreshold()));
        txtMinDistPlotDist.setText(Integer.toString(prefs.getMinDistPlotMinDist()));
        comboBoxHeatmapSource.setSelectedIndex(Math.min(comboBoxHeatmapSource.getItemCount() - 1,
                prefs.getHeatMapSource().ordinal()));
        txtHeatMapGeneratorSkipPercentage.setText(Float.toString(prefs.getHeatMapGenSkipPercentage()));
        txtHeatMapGeneratorHeatRadius.setText(Integer.toString(prefs.getHeatMapGenHeatRadius()));
        chckbxHeatMapGeneratorSkipPercentage.setSelected(prefs.getHeatMapGenSkipEvents());
        comboBoxOverlayHeatMapSource.setSelectedIndex(Math.min(comboBoxOverlayHeatMapSource.getItemCount() - 1,
                prefs.getHeatMapOverlayPlayer()));
        chckbxEnableHeatMapPlot.setSelected(prefs.getEnableHeatMapOverlay());
        chckbxEnableRawDataPlot.setSelected(prefs.getEnableRawDataOverlay());
        chckbxEnableFixationPlot.setSelected(prefs.getEnableFixationOverlay());
        comboBoxMinDistTemporalSubdivision.setSelectedIndex(Math.min(
                comboBoxMinDistTemporalSubdivision.getItemCount() - 1,
                prefs.getMinDistSubdivision().ordinal()));
        spinnerSubdivisionIntervalSize.setValue(prefs.getMinDistSubdivisionInterval());
        comboBoxMinDistPlotSource1.setSelectedIndex(Math.min(comboBoxMinDistPlotSource1.getItemCount() - 1,
                prefs.getMinDistPlotPlayer1()));
        comboBoxMinDistPlotSource2.setSelectedIndex(Math.min(comboBoxMinDistPlotSource2.getItemCount() - 1,
                prefs.getMinDistPlotPlayer2()));
        chckbxHeatMapGeneratorGenFromFrequencyInstead.setSelected(prefs.getHeatMapGenGenFromFrequencyInstead());
        txtHistogramCorrelationThreshold.setText(Integer.toString(prefs.getHistogramCorrelationThreshold()));
        txtHistogramDeviatingCellsThreshold.setText(Integer.toString(prefs.getHistogramDeviatingCellsThreshold()));
        spinnerHistogramGridSize.setValue(prefs.getHistogramGridSize());

        colorPickerButtonFixationsPlayer1.setBackground(prefs.getColorPlayer1());
        colorPickerButtonFixationsPlayer2.setBackground(prefs.getColorPlayer2());
        colorPickerButtonMinDistPlotClose.setBackground(prefs.getColorMinDistClose());
        colorPickerButtonMinDistPlotFar.setBackground(prefs.getColorMinDistFarAway());
        colorPickerButtonMinDistPlotOutsideBoard.setBackground(prefs.getColorMinDistOutsideBoard());
        colorPickerButtonMinDistPlotOutsideScreen.setBackground(prefs.getColorMinDistOutsideDisplay());
        spinnerHeatmapTransparency.setValue(prefs.getHeatmapTransparency());

        comboBoxHeatmapColors.setSelectedIndex(Math.min(comboBoxHeatmapColors.getItemCount() - 1,
                colorMapIndexMapping.get(prefs.getColorMap())));

        colorPickerButtonHeatmapPlayer1.setBackground(prefs.getHeatmapColorPlayer1());
        colorPickerButtonHeatmapPlayer2.setBackground(prefs.getHeatmapColorPlayer2());

        chckbxAdditionalEventsForMinDistPlot.setSelected(prefs.getUseAdditionalEventForMinDistPlot());
        chckbxPlayer1EventsForMinDistPlot.setSelected(prefs.getPlayerEventsForMinDistPlot().contains(0));
        chckbxPlayer2EventsForMinDistPlot.setSelected(prefs.getPlayerEventsForMinDistPlot().contains(1));
        chckbxAdditionalEventsTicks.setSelected(prefs.getShowAdditionalEventTicks());
        chckbxPlayer1Ticks.setSelected(prefs.getShowPlayerEventTicks().contains(0));
        chckbxPlayer2Ticks.setSelected(prefs.getShowPlayerEventTicks().contains(1));
        chckbxAdditionalEventsForHeatmap.setSelected(prefs.getUseAdditionalEventForHeatmaps());
        chckbxPlayer1EventsForHeatmap.setSelected(prefs.getPlayerEventsForHeatmaps().contains(0));
        chckbxPlayer2EventsForHeatmap.setSelected(prefs.getPlayerEventsForHeatmaps().contains(1));
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
        if (comboBoxHeatmapSource.getSelectedIndex() >= 0)
            prefs.setHeatMapSource(Preferences.HeatMapTimeSource.values()[
                comboBoxHeatmapSource.getSelectedIndex()]);
        if (comboBoxOverlayHeatMapSource.getSelectedIndex() >= 0)
            prefs.setHeatMapOverlayPlayer(comboBoxOverlayHeatMapSource.getSelectedIndex());
        prefs.setEnableHeatMapOverlay(chckbxEnableHeatMapPlot.isSelected());
        prefs.setEnableRawDataOverlay(chckbxEnableRawDataPlot.isSelected());
        prefs.setEnableFixationOverlay(chckbxEnableFixationPlot.isSelected());
        if (comboBoxMinDistTemporalSubdivision.getSelectedIndex() >= 0)
            prefs.setMinDistSubdivision(Preferences.MinDistSubdivision.values()[
                comboBoxMinDistTemporalSubdivision.getSelectedIndex()]);
        prefs.setMinDistSubdivisionInterval((int) spinnerSubdivisionIntervalSize.getValue());
        if (comboBoxMinDistPlotSource1.getSelectedIndex() >= 0)
            prefs.setMinDistPlotPlayer1(comboBoxMinDistPlotSource1.getSelectedIndex());
        if (comboBoxMinDistPlotSource2.getSelectedIndex() >= 0)
            prefs.setMinDistPlotPlayer2(comboBoxMinDistPlotSource2.getSelectedIndex());
        if (!txtHistogramCorrelationThreshold.getText().isEmpty())
            prefs.setHistogramCorrelationThreshold(Integer.parseInt(txtHistogramCorrelationThreshold.getText()));
        if (!txtHistogramDeviatingCellsThreshold.getText().isEmpty())
            prefs.setHistogramDeviatingCellsThreshold(Integer.parseInt(txtHistogramDeviatingCellsThreshold.getText()));
        prefs.setHistogramGridSize((int) spinnerHistogramGridSize.getValue());
        prefs.setColorPlayer1(colorPickerButtonFixationsPlayer1.getBackground());
        prefs.setColorPlayer2(colorPickerButtonFixationsPlayer2.getBackground());
        if (vidFrame.getPanel().getProjectors().size() >= 1)
            vidFrame.getPanel().getProjector(0).getRecording().preferredGazeColor = prefs.getColorPlayer1();
        if (vidFrame.getPanel().getProjectors().size() >= 2)
            vidFrame.getPanel().getProjector(1).getRecording().preferredGazeColor = prefs.getColorPlayer2();
        prefs.setColorMinDistClose(colorPickerButtonMinDistPlotClose.getBackground());
        prefs.setColorMinDistFarAway(colorPickerButtonMinDistPlotFar.getBackground());
        prefs.setColorMinDistOutsideBoard(colorPickerButtonMinDistPlotOutsideBoard.getBackground());
        prefs.setColorMinDistOutsideDisplay(colorPickerButtonMinDistPlotOutsideScreen.getBackground());

        prefs.setShowAdditionalEventTicks(chckbxAdditionalEventsTicks.isSelected());
        prefs.setUseAdditionalEventForMinDistPlot(chckbxAdditionalEventsForMinDistPlot.isSelected());

        ArrayList<Integer> playersForTicks = new ArrayList<Integer>();
        if (chckbxPlayer1Ticks.isSelected())
            playersForTicks.add(0);
        if (chckbxPlayer2Ticks.isSelected())
            playersForTicks.add(1);

        ArrayList<Integer> playersForMinDistPlots = new ArrayList<Integer>();
        if (chckbxPlayer1EventsForMinDistPlot.isSelected())
            playersForMinDistPlots.add(0);
        if (chckbxPlayer2EventsForMinDistPlot.isSelected())
            playersForMinDistPlots.add(1);

        ArrayList<Integer> playersForHeatmaps = new ArrayList<Integer>();
        if (chckbxPlayer1EventsForHeatmap.isSelected())
            playersForHeatmaps.add(0);
        if (chckbxPlayer2EventsForHeatmap.isSelected())
            playersForHeatmaps.add(1);

        prefs.setShowPlayerEventTicks(playersForTicks);
        prefs.setPlayerEventsForMinDistPlot(playersForMinDistPlots);

        int prevColormapId = prefs.getColorMap();
        int prevTransparency = prefs.getHeatmapTransparency();
        boolean prevHeatMapGenSkipEvents = prefs.getHeatMapGenSkipEvents();
        float prevHeatMapGenSkipPercentage = prefs.getHeatMapGenSkipPercentage();
        int prevHeatMapGenHeatRadius = prefs.getHeatMapGenHeatRadius();
        boolean prevHeatMapGenGenFromFrequencyInstead = prefs.getHeatMapGenGenFromFrequencyInstead();
        Color prevHeatmapColorPlayer1 = prefs.getHeatmapColorPlayer1();
        Color prevHeatmapColorPlayer2 = prefs.getHeatmapColorPlayer2();
        Boolean prevUseAdditionalEventForHeatmaps = prefs.getUseAdditionalEventForHeatmaps();
        ArrayList<Integer> prevPlayersForHeatmaps = prefs.getPlayerEventsForHeatmaps();

        if (comboBoxHeatmapColors.getSelectedIndex() >= 0)
            prefs.setColorMap(colorMapMapping.get(comboBoxHeatmapColors.getSelectedIndex()));
        prefs.setHeatmapTransparency((int) spinnerHeatmapTransparency.getValue());
        prefs.setHeatMapGenSkipEvents(chckbxHeatMapGeneratorSkipPercentage.isSelected());
        if (!txtHeatMapGeneratorSkipPercentage.getText().isEmpty())
            prefs.setHeatMapGenSkipPercentage(Float.parseFloat(txtHeatMapGeneratorSkipPercentage.getText()));
        if (!txtHeatMapGeneratorHeatRadius.getText().isEmpty())
            prefs.setHeatMapGenHeatRadius(Integer.parseInt(txtHeatMapGeneratorHeatRadius.getText()));
        prefs.setHeatMapGenGenFromFrequencyInstead(chckbxHeatMapGeneratorGenFromFrequencyInstead.isSelected());
        prefs.setHeatmapColorPlayer1(colorPickerButtonHeatmapPlayer1.getBackground());
        prefs.setHeatmapColorPlayer2(colorPickerButtonHeatmapPlayer2.getBackground());
        prefs.setUseAdditionalEventForHeatmaps(chckbxAdditionalEventsForHeatmap.isSelected());
        prefs.setPlayerEventsForHeatmaps(playersForHeatmaps);

        if (prevUseAdditionalEventForHeatmaps != prefs.getUseAdditionalEventForHeatmaps()
                || !playersForHeatmaps.equals(prevPlayersForHeatmaps)) {
            vidFrame.getPanel().updateHeatmapEvents();
        }

        if (prevColormapId != prefs.getColorMap() || prevTransparency != prefs.getHeatmapTransparency()
                || prevHeatMapGenSkipEvents != prefs.getHeatMapGenSkipEvents()
                || prevHeatMapGenSkipPercentage != prefs.getHeatMapGenSkipPercentage()
                || prevHeatMapGenHeatRadius != prefs.getHeatMapGenHeatRadius()
                || prevHeatMapGenGenFromFrequencyInstead != prefs.getHeatMapGenGenFromFrequencyInstead()
                || prevHeatmapColorPlayer1 != prefs.getHeatmapColorPlayer1()
                || prevHeatmapColorPlayer2 != prefs.getHeatmapColorPlayer2()
                || prevUseAdditionalEventForHeatmaps != prefs.getUseAdditionalEventForHeatmaps()
                || !playersForHeatmaps.equals(prevPlayersForHeatmaps)) {
            vidFrame.getPanel().setRepaintHeatMap();
        }

        for (OverlayGazeProjector p : vidFrame.getPanel().getProjectors()) {
            IVTFilter.filterRecording(p.getRecording(), prefs.getFilterVelocityThreshold(),
                    prefs.getFilterDistanceThreshold());
            p.transformFilteredPointsToTarget(vidFrame.getHostProjector().getRecording());
        }

        vidFrame.updateQuickSettingsToolbar();
        vidFrame.getPanel().repaint();

        System.gc();
    }
}
