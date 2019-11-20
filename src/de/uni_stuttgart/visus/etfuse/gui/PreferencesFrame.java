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
    private JComboBox comboBoxHeatmapColors;
    private ColorChooserButton colorPickerButtonFixationsPlayer1;
    private ColorChooserButton colorPickerButtonFixationsPlayer2;
    private ColorChooserButton colorPickerButtonMinDistPlotClose;
    private ColorChooserButton colorPickerButtonMinDistPlotFar;
    private ColorChooserButton colorPickerButtonMinDistPlotOutsideBoard;
    private ColorChooserButton colorPickerButtonMinDistPlotOutsideScreen;

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

        comboBoxOverlayHeatMapSource = new JComboBox();
        comboBoxOverlayHeatMapSource.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
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
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        JLabel label = new JLabel("Minimum distance (in px; default: 150):");
        minDistPrefsPanel.add(label, "2, 2, right, default");

        txtMinDistPlotDist = new JTextField();
        txtMinDistPlotDist.setHorizontalAlignment(SwingConstants.LEFT);
        txtMinDistPlotDist.setColumns(10);
        minDistPrefsPanel.add(txtMinDistPlotDist, "4, 2, fill, default");

        JLabel lblQuelldatensatz_1 = new JLabel("Source data set 1:");
        minDistPrefsPanel.add(lblQuelldatensatz_1, "2, 4, right, default");

        comboBoxMinDistPlotSource1 = new JComboBox();
        comboBoxMinDistPlotSource1.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
        minDistPrefsPanel.add(comboBoxMinDistPlotSource1, "4, 4, fill, default");

        JLabel lblQuelldatensatz_2 = new JLabel("Source data set 2:");
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
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        chckbxHeatMapGeneratorSkipPercentage = new JCheckBox("Skip percentage of events");
        chckbxHeatMapGeneratorSkipPercentage.setBorder(null);
        heatMapGeneratorPrefsPanel.add(chckbxHeatMapGeneratorSkipPercentage, "2, 2");

        JLabel lblDieseOptionBeschleunigt = new JLabel("(Faster generation of large datasets. Decreases accuracy.)");
        heatMapGeneratorPrefsPanel.add(lblDieseOptionBeschleunigt, "4, 2, left, default");

        JLabel lblProzentsatzin = new JLabel("Percentage (in %; default: 0.05):");
        heatMapGeneratorPrefsPanel.add(lblProzentsatzin, "2, 4, right, default");

        txtHeatMapGeneratorSkipPercentage = new JTextField();
        heatMapGeneratorPrefsPanel.add(txtHeatMapGeneratorSkipPercentage, "4, 4, fill, default");
        txtHeatMapGeneratorSkipPercentage.setColumns(10);

        JLabel lblHitzeradiusinPx = new JLabel("Heat radius (in px; default: 80):");
        heatMapGeneratorPrefsPanel.add(lblHitzeradiusinPx, "2, 6, right, default");

        txtHeatMapGeneratorHeatRadius = new JTextField();
        heatMapGeneratorPrefsPanel.add(txtHeatMapGeneratorHeatRadius, "4, 6, fill, default");
        txtHeatMapGeneratorHeatRadius.setColumns(10);

        chckbxHeatMapGeneratorGenFromFrequencyInstead = new JCheckBox("Aggregate by frequency instead of duration");
        chckbxHeatMapGeneratorGenFromFrequencyInstead.setBorder(null);
        heatMapGeneratorPrefsPanel.add(chckbxHeatMapGeneratorGenFromFrequencyInstead, "2, 8");

        JLabel lblnderungenTretenErst = new JLabel("Changes are effective only with the next (manual) generation.");
        panelHeatMapGeneratorPrefs.add(lblnderungenTretenErst, "2, 3, center, default");

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
                FormSpecs.RELATED_GAP_ROWSPEC,}));

        JLabel heatmapColors = new JLabel("Color:");
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
                "COLORMAP_AUTUMN",
                "COLORMAP_BONE",
                "COLORMAP_JET",
                "COLORMAP_WINTER",
                "COLORMAP_RAINBOW",
                "COLORMAP_OCEAN",
                "COLORMAP_SUMMER",
                "COLORMAP_SPRING",
                "COLORMAP_COOL",
                "COLORMAP_HSV",
                "COLORMAP_PINK",
                "COLORMAP_HOT",
                "COLORMAP_PARULA",
                "COLORMAP_MAGMA",
                "COLORMAP_INFERNO",
                "COLORMAP_PLASMA",
                "COLORMAP_VIRIDIS",
                "COLORMAP_CIVIDIS",
                "COLORMAP_TWILIGHT",
                "COLORMAP_TWILIGHT_SHIFTED" };

        colorMapMapping = new HashMap<Integer, Integer>();
        colorMapIndexMapping = new HashMap<Integer, Integer>();
        for (int i = 0; i < colorMapsStrings.length; i++) {
            colorMapMapping.put(i, colorMaps[i]);
            colorMapIndexMapping.put(colorMaps[i], i);
        }

        comboBoxHeatmapColors = new JComboBox();
        comboBoxHeatmapColors.setModel(new DefaultComboBoxModel(colorMapsStrings));
        heatmapColorPrefsPanel.add(comboBoxHeatmapColors, "4, 2, fill, default");

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
        txtHeatMapGeneratorSkipPercentage.setText(Float.toString(prefs.getHeatMapGenSkipPercentage()));
        txtHeatMapGeneratorHeatRadius.setText(Integer.toString(prefs.getHeatMapGenHeatRadius()));
        chckbxHeatMapGeneratorSkipPercentage.setSelected(prefs.getHeatMapGenSkipEvents());
        comboBoxOverlayHeatMapSource.setSelectedIndex(Math.min(comboBoxOverlayHeatMapSource.getItemCount() - 1,
                prefs.getHeatMapOverlayPlayer()));
        chckbxEnableHeatMapPlot.setSelected(prefs.getEnableHeatMapOverlay());
        chckbxEnableRawDataPlot.setSelected(prefs.getEnableRawDataOverlay());
        chckbxEnableFixationPlot.setSelected(prefs.getEnableFixationOverlay());
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

        comboBoxHeatmapColors.setSelectedIndex(Math.min(comboBoxHeatmapColors.getItemCount() - 1,
                colorMapIndexMapping.get(prefs.getColorMap())));
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
        prefs.setColorPlayer1(colorPickerButtonFixationsPlayer1.getBackground());
        prefs.setColorPlayer2(colorPickerButtonFixationsPlayer2.getBackground());
        vidFrame.getPanel().getProjector(0).getRecording().preferredGazeColor = prefs.getColorPlayer1();
        vidFrame.getPanel().getProjector(1).getRecording().preferredGazeColor = prefs.getColorPlayer2();
        prefs.setColorMinDistClose(colorPickerButtonMinDistPlotClose.getBackground());
        prefs.setColorMinDistFarAway(colorPickerButtonMinDistPlotFar.getBackground());
        prefs.setColorMinDistOutsideBoard(colorPickerButtonMinDistPlotOutsideBoard.getBackground());
        prefs.setColorMinDistOutsideDisplay(colorPickerButtonMinDistPlotOutsideScreen.getBackground());
        if (comboBoxHeatmapColors.getSelectedIndex() >= 0)
            prefs.setColorMap(colorMapMapping.get(comboBoxHeatmapColors.getSelectedIndex())); // TODO updates only for the next heatmap generation

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
