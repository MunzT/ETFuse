package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.uni_stuttgart.visus.etfuse.gui.surface.HeatMapImagePanel;
import de.uni_stuttgart.visus.etfuse.gui.surface.VideoSurfacePanel;
import de.uni_stuttgart.visus.etfuse.media.HeatMapGenerator;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Utils;

public class HeatMapComparisonFrame extends JFrame {

    private final JPanel singleHeatMapPanelContainer = new JPanel();
    private VideoSurfacePanel pane = null;
    private JPanel heatMapViewContainer;
    private JSplitPane splitHeatMapPanel;
    private JPanel leftHeatMapPanelContainer;
    private HeatMapImagePanel leftHeatMapPanel;
    private JPanel rightHeatMapPanelContainer;
    private HeatMapImagePanel rightHeatMapPanel;
    private HeatMapImagePanel singleHeatMapPanel;
    private JPanel controlPanel;
    private JList heatMapRightList;
    private JList heatMapLeftList;
    private JCheckBox chckbxGetrenntNormalisieren;
    private JCheckBox chckbxNurSpielfeldbereich;

    private int mode = 0; // 0: standard, 1: links - rechts, 2: rechts - links

    public HeatMapComparisonFrame(VideoSurfacePanel pane) {

        this.pane = pane;
        setTitle("Attention map comparison");

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("min:grow"),},
                new RowSpec[] {
                        RowSpec.decode("default:grow"),
                        FormSpecs.MIN_ROWSPEC,}));

        chckbxNurSpielfeldbereich = new JCheckBox("Board area only");
        chckbxGetrenntNormalisieren = new JCheckBox("Normalize separately");

        heatMapViewContainer = new JPanel();
        getContentPane().add(heatMapViewContainer, "1, 1, fill, fill");
        SpringLayout sl_heatMapViewContainer = new SpringLayout();
        sl_heatMapViewContainer.putConstraint(SpringLayout.NORTH, singleHeatMapPanelContainer, 3, SpringLayout.NORTH, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.WEST, singleHeatMapPanelContainer, 3, SpringLayout.WEST, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.SOUTH, singleHeatMapPanelContainer, -3, SpringLayout.SOUTH, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.EAST, singleHeatMapPanelContainer, -3, SpringLayout.EAST, heatMapViewContainer);
        heatMapViewContainer.setLayout(sl_heatMapViewContainer);

        splitHeatMapPanel = new JSplitPane();
        splitHeatMapPanel.setResizeWeight(0.5);
        sl_heatMapViewContainer.putConstraint(SpringLayout.NORTH, splitHeatMapPanel, 3, SpringLayout.NORTH, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.WEST, splitHeatMapPanel, 3, SpringLayout.WEST, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.SOUTH, splitHeatMapPanel, -3, SpringLayout.SOUTH, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.EAST, splitHeatMapPanel, -3, SpringLayout.EAST, heatMapViewContainer);
        heatMapViewContainer.add(splitHeatMapPanel);

        leftHeatMapPanelContainer = new JPanel();
        splitHeatMapPanel.setLeftComponent(leftHeatMapPanelContainer);
        GridBagLayout gbl_leftHeatMapPanelContainer = new GridBagLayout();
        leftHeatMapPanelContainer.setLayout(gbl_leftHeatMapPanelContainer);

        leftHeatMapPanel = new HeatMapImagePanel();
        leftHeatMapPanel.setLayout(null);
        leftHeatMapPanelContainer.add(leftHeatMapPanel);

        rightHeatMapPanelContainer = new JPanel();
        splitHeatMapPanel.setRightComponent(rightHeatMapPanelContainer);
        GridBagLayout gbl_rightHeatMapPanelContainer = new GridBagLayout();
        rightHeatMapPanelContainer.setLayout(gbl_rightHeatMapPanelContainer);

        rightHeatMapPanel = new HeatMapImagePanel();
        rightHeatMapPanel.setLayout(null);
        rightHeatMapPanelContainer.add(rightHeatMapPanel);

        leftHeatMapPanelContainer.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                Utils.resizePanelToRetainAspectRatio(leftHeatMapPanel, leftHeatMapPanelContainer);
                Utils.resizePanelToRetainAspectRatio(rightHeatMapPanel, rightHeatMapPanelContainer);
            }
        });
        singleHeatMapPanelContainer.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {

                Utils.resizePanelToRetainAspectRatio(singleHeatMapPanel, singleHeatMapPanelContainer);
            }
        });
        singleHeatMapPanelContainer.setVisible(false);

        heatMapViewContainer.add(singleHeatMapPanelContainer);
        GridBagLayout gbl_singleHeatMapPanelContainer = new GridBagLayout();
        singleHeatMapPanelContainer.setLayout(gbl_singleHeatMapPanelContainer);

        singleHeatMapPanel = new HeatMapImagePanel();
        singleHeatMapPanel.setHeatMap(new Mat(1, 1, CvType.CV_8UC1));
        singleHeatMapPanelContainer.add(singleHeatMapPanel);
        sl_heatMapViewContainer.putConstraint(SpringLayout.NORTH, singleHeatMapPanel, 3, SpringLayout.NORTH, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.WEST, singleHeatMapPanel, 3, SpringLayout.WEST, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.SOUTH, singleHeatMapPanel, -3, SpringLayout.SOUTH, heatMapViewContainer);
        sl_heatMapViewContainer.putConstraint(SpringLayout.EAST, singleHeatMapPanel, -3, SpringLayout.EAST, heatMapViewContainer);

        controlPanel = new JPanel();
        getContentPane().add(controlPanel, "1, 2, fill, bottom");
        controlPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
        controlPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.UNRELATED_GAP_COLSPEC,
                ColumnSpec.decode("left:min"),
                ColumnSpec.decode("14dlu"),
                ColumnSpec.decode("left:min"),
                ColumnSpec.decode("14dlu:grow"),
                FormSpecs.DEFAULT_COLSPEC,
                ColumnSpec.decode("14dlu"),
                FormSpecs.DEFAULT_COLSPEC,
                ColumnSpec.decode("14dlu"),
                FormSpecs.DEFAULT_COLSPEC,
                ColumnSpec.decode("14dlu:grow(2)"),
                ColumnSpec.decode("left:min"),
                FormSpecs.UNRELATED_GAP_COLSPEC,},
                new RowSpec[] {
                        FormSpecs.UNRELATED_GAP_ROWSPEC,
                        FormSpecs.MIN_ROWSPEC,
                        FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
                        RowSpec.decode("116px:grow"),
                        FormSpecs.UNRELATED_GAP_ROWSPEC,}));

        JLabel lblHeatmapLinks = new JLabel("Left heatmap:");
        controlPanel.add(lblHeatmapLinks, "2, 2, default, bottom");

        JLabel lblDarstellung = new JLabel("Representation:");
        controlPanel.add(lblDarstellung, "4, 2");

        JLabel lblHeatmapRechts = new JLabel("Right heatmap:");
        controlPanel.add(lblHeatmapRechts, "12, 2, right, bottom");

        ArrayList<String> valuesList = new ArrayList<String>();
        for (int i = 1; i <= this.pane.getProjectors().size(); i++) {
            valuesList.add("Player " + i);
        }

        heatMapRightList = new JList();
        heatMapLeftList = new JList();
        heatMapLeftList.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
        heatMapLeftList.setModel(new AbstractListModel() {
            String[] values = valuesList.toArray(new String[0]);
            @Override
            public int getSize() {
                return values.length;
            }
            @Override
            public Object getElementAt(int index) {
                return values[index];
            }
        });
        heatMapLeftList.setSelectedIndex(0);
        controlPanel.add(heatMapLeftList, "2, 4, fill, fill");

        JPanel prefContainer = new JPanel();
        controlPanel.add(prefContainer, "4, 4, default, top");
        prefContainer.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.DEFAULT_COLSPEC,},
                new RowSpec[] {
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        prefContainer.add(chckbxNurSpielfeldbereich, "1, 1, default, center");
        prefContainer.add(chckbxGetrenntNormalisieren, "1, 2, default, center");

        JButton btnLinksRechts = new JButton("Difference left minus right");
        btnLinksRechts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                mode = 1;

                redrawHeatMaps();

                splitHeatMapPanel.setVisible(false);
                singleHeatMapPanelContainer.setVisible(true);
                chckbxGetrenntNormalisieren.setEnabled(false);
            }
        });
        controlPanel.add(btnLinksRechts, "6, 4, center, top");

        JButton btnStandardansicht = new JButton("Default view");
        btnStandardansicht.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                mode = 0;

                redrawHeatMaps();

                singleHeatMapPanelContainer.setVisible(false);
                splitHeatMapPanel.setVisible(true);
                chckbxGetrenntNormalisieren.setEnabled(true);
            }
        });
        controlPanel.add(btnStandardansicht, "8, 4, center, top");

        JButton btnRechtsLinks = new JButton("Difference right minus left");
        btnRechtsLinks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                mode = 2;

                redrawHeatMaps();

                splitHeatMapPanel.setVisible(false);
                singleHeatMapPanelContainer.setVisible(true);
                chckbxGetrenntNormalisieren.setEnabled(false);
            }
        });
        controlPanel.add(btnRechtsLinks, "10, 4, center, top");

        heatMapRightList.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
        controlPanel.add(heatMapRightList, "12, 4, fill, fill");
        heatMapRightList.setModel(new AbstractListModel() {
            String[] values = valuesList.toArray(new String[0]);
            @Override
            public int getSize() {
                return values.length;
            }
            @Override
            public Object getElementAt(int index) {
                return values[index];
            }
        });
        heatMapRightList.setSelectedIndex(0);

        int otherStartingMapIndex = 0;
        if (pane.getProjectors().size() > 1)
            otherStartingMapIndex = 1;

        heatMapLeftList.setSelectedIndex(0);
        heatMapRightList.setSelectedIndex(otherStartingMapIndex);

        redrawHeatMaps();

        chckbxGetrenntNormalisieren.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                redrawHeatMaps();
            }
        });
        chckbxNurSpielfeldbereich.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                redrawHeatMaps();
            }
        });
        heatMapLeftList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                redrawHeatMaps();
            }
        });
        heatMapRightList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                redrawHeatMaps();
            }
        });

        OverlayGazeProjector proj = pane.getProjector(0);
        Point p1 = proj.getRecording().getFramePoint1();
        Point p2 = proj.getRecording().getFramePoint2();

        if (p1 == null || p2 == null)
            chckbxNurSpielfeldbereich.setEnabled(false);

        this.pack();
        this.setMinimumSize(new Dimension((int) Math.max(873, this.getSize().getWidth()), (int) Math.max(500, this.getSize().getHeight())));
        setLocationRelativeTo(null);
    }

    private void redrawHeatMaps() {

        switch (mode) {

        case 0: // Standardansicht

            Mat hm1 = pane.getProjector(heatMapLeftList.getSelectedIndex()).getRawHeatMap();
            Mat hm2 = pane.getProjector(heatMapRightList.getSelectedIndex()).getRawHeatMap();

            Rect roi = new Rect(0, 0, hm1.cols(), hm1.rows());

            if (chckbxNurSpielfeldbereich.isSelected()) {

                OverlayGazeProjector proj = pane.getProjector(0);
                Point p1 = proj.getRecording().getFramePoint1();
                Point p2 = proj.getRecording().getFramePoint2();
                roi = new Rect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
            }

            if (chckbxGetrenntNormalisieren.isSelected()) {

                hm1 = pane.getProjector(heatMapLeftList.getSelectedIndex()).getNormalizedHeatMap();
                hm2 = pane.getProjector(heatMapRightList.getSelectedIndex()).getNormalizedHeatMap();
            }
            else {

                Mat[] maps = HeatMapGenerator.processHeatMapsForComparison(hm1, hm2);
                hm1 = maps[0];
                hm2 = maps[1];
            }

            hm1 = hm1.submat(roi);
            hm2 = hm2.submat(roi);

            leftHeatMapPanel.setHeatMap(hm1);
            rightHeatMapPanel.setHeatMap(hm2);

            Utils.resizePanelToRetainAspectRatio(leftHeatMapPanel, leftHeatMapPanelContainer);
            Utils.resizePanelToRetainAspectRatio(rightHeatMapPanel, rightHeatMapPanelContainer);

            leftHeatMapPanel.repaint();
            rightHeatMapPanel.repaint();
            break;

        case 1: // Links minus rechts

            Mat hml1 = pane.getProjector(heatMapLeftList.getSelectedIndex()).getRawHeatMap();
            Mat hml2 = pane.getProjector(heatMapRightList.getSelectedIndex()).getRawHeatMap();
            Mat hml3 = HeatMapGenerator.processDiffedHeatMap(hml1, hml2);

            if (chckbxNurSpielfeldbereich.isSelected()) {

                OverlayGazeProjector proj = pane.getProjector(0);
                Point p1 = proj.getRecording().getFramePoint1();
                Point p2 = proj.getRecording().getFramePoint2();
                Rect roiL = new Rect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);

                hml3 = hml3.submat(roiL);
            }

            singleHeatMapPanel.setHeatMap(hml3);

            Utils.resizePanelToRetainAspectRatio(singleHeatMapPanel, singleHeatMapPanelContainer);

            singleHeatMapPanel.repaint();
            break;

        case 2: // Rechts minus links

            Mat hmr1 = pane.getProjector(heatMapLeftList.getSelectedIndex()).getRawHeatMap();
            Mat hmr2 = pane.getProjector(heatMapRightList.getSelectedIndex()).getRawHeatMap();
            Mat hmr3 = HeatMapGenerator.processDiffedHeatMap(hmr2, hmr1);

            if (chckbxNurSpielfeldbereich.isSelected()) {

                OverlayGazeProjector proj = pane.getProjector(0);
                Point p1 = proj.getRecording().getFramePoint1();
                Point p2 = proj.getRecording().getFramePoint2();
                Rect roiR = new Rect(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);

                hmr3 = hmr3.submat(roiR);
            }

            singleHeatMapPanel.setHeatMap(hmr3);

            Utils.resizePanelToRetainAspectRatio(singleHeatMapPanel, singleHeatMapPanelContainer);

            singleHeatMapPanel.repaint();
            break;

        default:
            return;
        }
    }
}
