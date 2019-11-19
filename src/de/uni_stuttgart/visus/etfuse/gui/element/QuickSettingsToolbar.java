package de.uni_stuttgart.visus.etfuse.gui.element;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.uni_stuttgart.visus.etfuse.gui.ColorInfoFrame;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class QuickSettingsToolbar extends JPanel {

    private VideoFrame parentFrame;
    private JComboBox comboBoxOverlayHeatMapSource;
    private JComboBox comboBoxMinDistPlotSource1;
    private JComboBox comboBoxMinDistPlotSource2;

    public QuickSettingsToolbar(VideoFrame parentFrame) {

        this.parentFrame = parentFrame;

        setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.UNRELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                ColumnSpec.decode("default:grow"),},
                new RowSpec[] {
                        RowSpec.decode("default:grow"),}));

        ActionListener minDist1SettingChangedListener = new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {

                Preferences prefs = Project.currentProject().getPreferences();

                if (comboBoxMinDistPlotSource1.getSelectedIndex() >= 0)
                    prefs.setMinDistPlotPlayer1(comboBoxMinDistPlotSource1.getSelectedIndex());

                parentFrame.drawSliderMinDistance();
            }
        };

        ActionListener minDist2SettingChangedListener = new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {

                Preferences prefs = Project.currentProject().getPreferences();

                if (comboBoxMinDistPlotSource2.getSelectedIndex() >= 0)
                    prefs.setMinDistPlotPlayer2(comboBoxMinDistPlotSource2.getSelectedIndex());

                parentFrame.drawSliderMinDistance();
            }
        };

        ActionListener attentionMapSettingChangedListener = new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {

                Preferences prefs = Project.currentProject().getPreferences();

                if (comboBoxOverlayHeatMapSource.getSelectedIndex() >= 0)
                    prefs.setHeatMapOverlayPlayer(comboBoxOverlayHeatMapSource.getSelectedIndex());

                parentFrame.getPanel().repaint();
            }
        };

        JLabel lblAttentionmapoverlayQuelle = new JLabel("Attention map overlay source:");
        add(lblAttentionmapoverlayQuelle, "2, 1, right, default");

        comboBoxOverlayHeatMapSource = new JComboBox();
        comboBoxOverlayHeatMapSource.setToolTipText("Select the source for the attention map overlay.");
        add(comboBoxOverlayHeatMapSource, "4, 1, fill, default");

        JLabel lblMindistplotQuellen = new JLabel("MinDist-Plot source:");
        add(lblMindistplotQuellen, "6, 1");

        JLabel label = new JLabel("1:");
        add(label, "8, 1, right, default");

        comboBoxMinDistPlotSource1 = new JComboBox();
        comboBoxMinDistPlotSource1.setToolTipText("Select first source for the MinDist plot");
        add(comboBoxMinDistPlotSource1, "10, 1, fill, default");

        JLabel label_1 = new JLabel("2:");
        add(label_1, "12, 1, right, default");

        comboBoxMinDistPlotSource2 = new JComboBox();
        comboBoxMinDistPlotSource2.setToolTipText("Select second source for the MinDist plot");
        add(comboBoxMinDistPlotSource2, "14, 1, fill, default");

        JLabel lblFarben = new JLabel("Farben:");
        add(lblFarben, "16, 1");

        JButton btnInfosZuSpielerfarben = new JButton("Information on player colors");
        btnInfosZuSpielerfarben.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<String> playerList = new ArrayList<String>();
                ArrayList<Color> colorList = new ArrayList<Color>();
                for (int i = 0; i < parentFrame.getPanel().getProjectors().size(); i++) {
                    playerList.add("Player " + (i + 1));
                    colorList.add(parentFrame.getPanel().getProjector(i).getRecording().preferredGazeColor);
                }

                ColorInfoFrame colInfo = new ColorInfoFrame(parentFrame, playerList, colorList);
                colInfo.setVisible(true);
            }
        });
        add(btnInfosZuSpielerfarben, "18, 1");

        JButton btnInfosZuMindistfarben = new JButton("Information on MinDist colors");
        btnInfosZuMindistfarben.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Preferences prefs = Project.currentProject().getPreferences();
                int minDist = prefs.getMinDistPlotMinDist();

                ArrayList<String> entityList = new ArrayList<String>();
                ArrayList<Color> colorList = new ArrayList<Color>();
                for (int i = 0; i < 4; i++) {

                    switch (i) {

                        case 0:
                            entityList.add("Distance between gaze points (raw data) of the players <= " + minDist + "px");
                            colorList.add(Color.CYAN);
                            break;

                        case 1:
                            entityList.add("Distance between gaze points (raw data) of the players > " + minDist + "px");
                            colorList.add(Color.RED);
                            break;

                        case 2:
                            entityList.add("Gaze point (raw data) of at least one player is outside the board");
                            colorList.add(Color.YELLOW);
                            break;

                        case 3:
                            entityList.add("Gaze data missing for at least one player");
                            colorList.add(Color.DARK_GRAY);
                            break;

                        default:
                            break;
                    }
                }

                ColorInfoFrame colInfo = new ColorInfoFrame(parentFrame, entityList, colorList);
                colInfo.setVisible(true);
            }
        });
        add(btnInfosZuMindistfarben, "20, 1");

        updateSelectableIndicesFromDatasets();
        updateSelectedIndicesFromPreferences();

        comboBoxOverlayHeatMapSource.addActionListener(attentionMapSettingChangedListener);
        comboBoxMinDistPlotSource1.addActionListener(minDist1SettingChangedListener);
        comboBoxMinDistPlotSource2.addActionListener(minDist2SettingChangedListener);
    }

    public void updateSelectedIndicesFromPreferences() {

        Preferences prefs = Project.currentProject().getPreferences();

        comboBoxOverlayHeatMapSource.setSelectedIndex(Math.min(
                comboBoxOverlayHeatMapSource.getItemCount() - 1, prefs.getHeatMapOverlayPlayer()));
        comboBoxMinDistPlotSource1.setSelectedIndex(Math.min(
                comboBoxMinDistPlotSource1.getItemCount() - 1, prefs.getMinDistPlotPlayer1()));
        comboBoxMinDistPlotSource2.setSelectedIndex(Math.min(
                comboBoxMinDistPlotSource2.getItemCount() - 1, prefs.getMinDistPlotPlayer2()));
    }

    public void updateSelectableIndicesFromDatasets() {

        ArrayList<String> playerList = new ArrayList<String>();
        for (int i = 1; i <= parentFrame.getPanel().getProjectors().size(); i++) {
            playerList.add("Player " + i);
        }

        if (playerList.size() < 1)
            playerList.add("Keine Daten geladen");

        comboBoxOverlayHeatMapSource.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
        comboBoxMinDistPlotSource1.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
        comboBoxMinDistPlotSource2.setModel(new DefaultComboBoxModel(playerList.toArray(new String[0])));
    }
}
