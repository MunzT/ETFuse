package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.jidesoft.swing.RangeSlider;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.fileimport.DataImporter;
import de.uni_stuttgart.visus.etfuse.gui.element.QuickSettingsToolbar;
import de.uni_stuttgart.visus.etfuse.gui.element.RecordingSlider;
import de.uni_stuttgart.visus.etfuse.gui.surface.VideoSurfacePanel;
import de.uni_stuttgart.visus.etfuse.media.HeatMapGenerator;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.misc.Utils;
import de.uni_stuttgart.visus.etfuse.projectio.Project;
import de.uni_stuttgart.visus.etfuse.projectio.ProjectIO;

public class VideoFrame extends JFrame implements ChangeListener {

    private VideoSurfacePanel panel;
    private JPanel panelContainer;
    protected OverlayGazeProjector hostProjector;

    private Timer frameTimer;
    private Boolean progressUpdate;
    private Mat newMatrix;
    private VideoCapture newCamera;

    private JMenuBar menuBar;
    private JMenu menuImport, menuPlayback, menuRecordingOptions, menuCompareHeatMaps, menuPreferences;
    private JMenuItem menuItemSaveProject, menuItemTrackerData, menuItemQuarterSpeed, menuItemHalfSpeed,
    menuItemNormalSpeed, menuItemDoubleSpeed, menuItemQuadrupleSpeed,
    menuItemAddGuest;

    private JPanel playbackPanel;
    private JButton playPauseButton;
    private JLabel frameCounterLabel;
    public RecordingSlider progressSlider;
    private RangeSlider heatMapSlider;

    private QuickSettingsToolbar qsPanel;

    private String idleTitle;
    private HashMap<String, Integer> activityProgress;

    private static final long serialVersionUID = 3372205698822928813L;

    public static VideoFrame lastInstance;

    public VideoFrame(String title, String videoFilePath) {

        super(title);

        idleTitle = title;

        lastInstance = this;

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Utils.resizePanelToRetainAspectRatio(panel, panelContainer);
            }
        });

        activityProgress = new HashMap<String, Integer>();

        this.newMatrix = new Mat();
        this.newCamera = new VideoCapture(videoFilePath);
        this.frameTimer = new Timer(0, null);

        this.setSize(0, 0);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.menuBar = new JMenuBar();

        this.menuImport = new JMenu("File");
        this.menuImport.setMnemonic(KeyEvent.VK_I);
        this.menuImport.getAccessibleContext().setAccessibleDescription("File import");
        this.menuBar.add(this.menuImport);

        this.menuPlayback = new JMenu("Playback speed");
        this.menuPlayback.setMnemonic(KeyEvent.VK_P);
        this.menuPlayback.getAccessibleContext().setAccessibleDescription("Specify video playback speed");
        this.menuBar.add(this.menuPlayback);

        this.menuRecordingOptions = new JMenu("Show guest recording");
        this.menuRecordingOptions.setMnemonic(KeyEvent.VK_P);
        this.menuRecordingOptions.getAccessibleContext().setAccessibleDescription("Show guest recording");
        this.menuBar.add(this.menuRecordingOptions);

        this.menuCompareHeatMaps = new JMenu("Compare attention maps");
        this.menuCompareHeatMaps.setMnemonic(KeyEvent.VK_H);
        this.menuCompareHeatMaps.getAccessibleContext().setAccessibleDescription("Attention map view");
        this.menuBar.add(this.menuCompareHeatMaps);

        this.menuPreferences = new JMenu("Settings");
        this.menuPreferences.setMnemonic(KeyEvent.VK_E);
        this.menuPreferences.getAccessibleContext().setAccessibleDescription("Change settings");
        this.menuBar.add(this.menuPreferences);

        ActionListener importListener = new DataImporter(this, null);
        this.menuItemTrackerData = new JMenuItem("Load host eye tracking data...", KeyEvent.VK_T);
        this.menuItemTrackerData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        this.menuItemTrackerData.getAccessibleContext().setAccessibleDescription("Import eye tracking data for the host recording");
        this.menuItemTrackerData.addActionListener(importListener);
        this.menuImport.add(this.menuItemTrackerData);

        this.menuItemSaveProject = new JMenuItem("Save project as...", KeyEvent.VK_S);
        this.menuItemSaveProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        this.menuItemSaveProject.getAccessibleContext().setAccessibleDescription("Save project");
        this.menuItemSaveProject.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (hostProjector == null || hostProjector.getRecording() == null) {

                    JOptionPane.showMessageDialog(null, "Please load host eye tracking data first!");
                    return;
                }

                ProjectIO projIO = new ProjectIO();
                projIO.saveProject();
            }
        });
        this.menuImport.add(this.menuItemSaveProject);

        this.menuItemQuarterSpeed = new JMenuItem("x0.25 speed");
        this.menuItemQuarterSpeed.getAccessibleContext().setAccessibleDescription("Play at 1/4 speed");
        this.menuItemQuarterSpeed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPlaybackSpeed(0.25);
            }
        });
        this.menuPlayback.add(this.menuItemQuarterSpeed);

        this.menuItemHalfSpeed = new JMenuItem("x0.5 speed");
        this.menuItemHalfSpeed.getAccessibleContext().setAccessibleDescription("Play at half speed");
        this.menuItemHalfSpeed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPlaybackSpeed(0.5);
            }
        });
        this.menuPlayback.add(this.menuItemHalfSpeed);

        this.menuItemNormalSpeed = new JMenuItem("x1.0 speed");
        this.menuItemNormalSpeed.getAccessibleContext().setAccessibleDescription("Play at normal speed");
        this.menuItemNormalSpeed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPlaybackSpeed(1.0);
            }
        });
        this.menuPlayback.add(this.menuItemNormalSpeed);

        this.menuItemDoubleSpeed = new JMenuItem("x2.0 speed", KeyEvent.VK_Y);
        this.menuItemDoubleSpeed.getAccessibleContext().setAccessibleDescription("Play at double speed");
        this.menuItemDoubleSpeed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPlaybackSpeed(2.0);
            }
        });
        this.menuPlayback.add(this.menuItemDoubleSpeed);

        this.menuItemQuadrupleSpeed = new JMenuItem("x4.0 speed");
        this.menuItemQuadrupleSpeed.getAccessibleContext().setAccessibleDescription("Play at four times the speed");
        this.menuItemQuadrupleSpeed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPlaybackSpeed(4.0);
            }
        });
        this.menuPlayback.add(this.menuItemQuadrupleSpeed);

        this.menuItemAddGuest = new JMenuItem("Show guest recording");
        this.menuItemAddGuest.getAccessibleContext().setAccessibleDescription("Transform and show data from another recording");

        this.menuRecordingOptions.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (hostProjector == null || hostProjector.getRecording() == null) {

                    // System.out.println("Kein Recording geladen!");
                    JOptionPane.showMessageDialog(null, "Please load host eye tracking data first!");
                    return;
                }

                pause();
                // fenster anzeigen
                SyncWizardFrame assistantFrame = new SyncWizardFrame(VideoFrame.this);
                assistantFrame.setVisible(true);
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        this.menuCompareHeatMaps.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (hostProjector == null || hostProjector.getRecording() == null) {

                    JOptionPane.showMessageDialog(null, "Please load host eye tracking data first!");
                    return;
                }

                for (OverlayGazeProjector p : VideoFrame.this.getPanel().getProjectors()) {

                    if (p.getRawHeatMap() == null || p.isHeatMapBeingGenerated()) {

                        JOptionPane.showMessageDialog(null, "Heatmap generation is not yet completed!");
                        return;
                    }
                }

                pause();
                // fenster anzeigen
                HeatMapComparisonFrame hmcFrame = new HeatMapComparisonFrame(VideoFrame.this.getPanel());
                hmcFrame.setVisible(true);
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        this.menuPreferences.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

                pause();
                // fenster anzeigen
                PreferencesFrame prefsFrame = new PreferencesFrame(VideoFrame.this);
                prefsFrame.setVisible(true);
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        this.setJMenuBar(this.menuBar);

        this.panelContainer = new JPanel(new GridBagLayout());
        this.panelContainer.setBackground(Color.BLACK);

        this.panel = new VideoSurfacePanel();
        this.panel.attachCamera(newCamera);
        this.panel.setPaintGazePlot(true);
        this.panel.setPaintRawDataPlot(true);
        this.panel.setPaintHeatMap(true);
        this.panel.setParentVideoFrame(this);

        this.panel.setPreferredSize(new Dimension((int) Math.ceil(newCamera.get(Videoio.CAP_PROP_FRAME_WIDTH)), (int) Math.ceil(newCamera.get(Videoio.CAP_PROP_FRAME_HEIGHT))));
        this.panelContainer.setPreferredSize(this.panel.getPreferredSize());
        this.panelContainer.add(this.panel);
        this.add(this.panelContainer, BorderLayout.CENTER);

        this.playbackPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        this.playbackPanel.setLayout(layout);

        this.playPauseButton = new JButton(">");
        this.playPauseButton.setToolTipText("Play / pause video");

        ActionListener playPauseListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frameTimer.isRunning()) {
                    // pause
                    pause();
                }
                else {
                    // play
                    play();
                }
            }
        };

        this.playPauseButton.addActionListener(playPauseListener);
        this.playPauseButton.setFocusable(false);
        this.playbackPanel.add(this.playPauseButton);
        layout.putConstraint(SpringLayout.WEST, this.playPauseButton, 3, SpringLayout.WEST, this.playbackPanel);
        layout.putConstraint(SpringLayout.NORTH, this.playPauseButton, 3, SpringLayout.NORTH, this.playbackPanel);

        this.progressSlider = new RecordingSlider(JSlider.HORIZONTAL, 0, (int) newCamera.get(Videoio.CAP_PROP_FRAME_COUNT), 0);
        this.progressSlider.setToolTipText("Timeline with MinDist plot");
        this.progressSlider.addChangeListener(this);
        this.progressSlider.setFocusable(false);

        this.progressUpdate = false;

        //Turn on labels at major tick marks.
        this.progressSlider.setPaintTicks(true);
        this.progressSlider.setPaintLabels(false);
        this.playbackPanel.add(this.progressSlider);
        layout.putConstraint(SpringLayout.WEST, this.progressSlider, 5, SpringLayout.EAST, this.playPauseButton);
        layout.putConstraint(SpringLayout.NORTH, this.progressSlider, 3, SpringLayout.NORTH, this.playbackPanel);
        layout.putConstraint(SpringLayout.EAST, this.progressSlider, -5, SpringLayout.EAST, this.playbackPanel);

        this.frameCounterLabel = new JLabel("0", SwingConstants.CENTER);
        this.frameCounterLabel.setToolTipText("Frame Counter - click to jump to a specific frame");
        this.frameCounterLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                String input = JOptionPane.showInputDialog("jump to frame:");
                Integer integ = 0;

                try {
                    integ = Integer.parseInt(input);
                } catch (Exception ex) {
                    return;
                }

                int min = 0;
                int max = (int) newCamera.get(Videoio.CAP_PROP_FRAME_COUNT) - 1;
                integ = Math.max(0, integ);
                integ = Math.min(max, integ);
                progressSlider.setValueIsAdjusting(true);
                progressSlider.setValue(integ);
                progressSlider.setValueIsAdjusting(false);
            }
        });
        layout.putConstraint(SpringLayout.WEST, this.frameCounterLabel, 3, SpringLayout.WEST, this.playbackPanel);
        layout.putConstraint(SpringLayout.NORTH, this.frameCounterLabel, 3, SpringLayout.SOUTH, this.playPauseButton);
        layout.putConstraint(SpringLayout.EAST, this.frameCounterLabel, -5, SpringLayout.WEST, this.progressSlider);
        this.playbackPanel.add(this.frameCounterLabel);

        this.heatMapSlider = new RangeSlider(0, (int) newCamera.get(Videoio.CAP_PROP_FRAME_COUNT), 0, (int) newCamera.get(Videoio.CAP_PROP_FRAME_COUNT));
        this.heatMapSlider.setToolTipText("Period of time over which the attention maps are generated.");
        this.heatMapSlider.putClientProperty("JSlider.isFilled", false);
        ChangeListener heatMapChangeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                if (!((RangeSlider) e.getSource()).getValueIsAdjusting()) {

                    HeatMapGenerator.killAllActiveGenerators();

                    for (OverlayGazeProjector proj : VideoFrame.this.getPanel().getProjectors()) {
                        HeatMapGenerator mapGen = new HeatMapGenerator(proj);
                        mapGen.attachVideoFrameForTitleUpdate(VideoFrame.this);
                        mapGen.execute();
                    }
                }
            }
        };
        this.heatMapSlider.addChangeListener(heatMapChangeListener);
        this.heatMapSlider.setFocusable(false);
        this.heatMapSlider.setOrientation(SwingConstants.HORIZONTAL);
        this.heatMapSlider.setRangeDraggable(true);
        this.playbackPanel.add(this.heatMapSlider);
        layout.putConstraint(SpringLayout.WEST, this.heatMapSlider, 5, SpringLayout.EAST, this.playPauseButton);
        layout.putConstraint(SpringLayout.NORTH, this.heatMapSlider, 3, SpringLayout.SOUTH, this.playPauseButton);
        layout.putConstraint(SpringLayout.EAST, this.heatMapSlider, -5, SpringLayout.EAST, this.playbackPanel);

        this.qsPanel = new QuickSettingsToolbar(this);
        this.playbackPanel.add(this.qsPanel);
        layout.putConstraint(SpringLayout.WEST, this.qsPanel, 5, SpringLayout.WEST, this.playbackPanel);
        layout.putConstraint(SpringLayout.NORTH, this.qsPanel, 3, SpringLayout.SOUTH, this.heatMapSlider);
        layout.putConstraint(SpringLayout.EAST, this.qsPanel, -5, SpringLayout.EAST, this.playbackPanel);
        layout.putConstraint(SpringLayout.SOUTH, this.qsPanel, -5, SpringLayout.SOUTH, this.playbackPanel);

        this.add(this.playbackPanel, BorderLayout.PAGE_END);

        this.playbackPanel.setPreferredSize(new Dimension(this.panel.getPreferredSize().width, this.playPauseButton.getPreferredSize().height * 3 + 6));

        this.setResizable(true);
        this.pack();

        Dimension playPauseSize = this.playPauseButton.getSize();
        playPauseSize.width = 50;
        this.playPauseButton.setPreferredSize(playPauseSize);
        this.playPauseButton.setMinimumSize(playPauseSize);
        this.playPauseButton.setMaximumSize(playPauseSize);

        this.playbackPanel.revalidate();

        this.setPlaybackSpeed(1.0);

        this.frameTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (newCamera.read(newMatrix)) {

                    panel.setImage(Utils.Mat2BufferedImage(newMatrix));
                    //Update the vidPanel in the JFrame
                    panel.repaint();
                    progressUpdate = true;
                    progressSlider.setValue((int) newCamera.get(Videoio.CAP_PROP_POS_FRAMES));
                    frameCounterLabel.setText(Integer.toString((int) newCamera.get(Videoio.CAP_PROP_POS_FRAMES) - 1));
                    progressUpdate = false;
                }
                else {
                    pause();
                }
            }
        });

        this.frameTimer.setRepeats(false); // Only execute once
        this.frameTimer.start(); // erstes bild zeichnen
    }

    public void setHostRecording(EyeTrackerRecording rec) {

        this.hostProjector = new OverlayGazeProjector(rec);
        this.panel.attachProjector(this.hostProjector);

        HeatMapGenerator mapGen = new HeatMapGenerator(this.hostProjector);
        mapGen.attachVideoFrameForTitleUpdate(this);
        mapGen.execute();
    }

    public void setPlaybackSpeed(double speed) {

        double fps = newCamera.get(Videoio.CAP_PROP_FPS);
        this.frameTimer.setDelay((int) Math.floor((1000 / fps) / speed));
    }

    public void pause() {

        frameTimer.setRepeats(false);
        playPauseButton.setText(">");
    }

    public void play() {

        frameTimer.setRepeats(true);
        frameTimer.start();
        playPauseButton.setText("II");
    }

    public VideoSurfacePanel getPanel() {

        return this.panel;
    }

    public int getHeatMapRangeLow() {

        return this.heatMapSlider.getLowValue();
    }

    public int getHeatMapRangeHigh() {

        return this.heatMapSlider.getHighValue();
    }

    public void drawSliderMinDistance() {

        Preferences prefs = Project.currentProject().getPreferences();

        int player1 = prefs.getMinDistPlotPlayer1();
        int player2 = prefs.getMinDistPlotPlayer2();
        int minDist = prefs.getMinDistPlotMinDist();

        if (this.panel.getProjectors().size() > player1 && this.panel.getProjectors().size() > player2) {

            OverlayGazeProjector proj1 = this.panel.getProjector(player1);
            OverlayGazeProjector proj2 = this.panel.getProjector(player2);

            this.progressSlider.drawMinDistanceTicks(this, proj1, proj2, minDist);
            this.progressSlider.repaint();
            this.progressSlider.setPaintTicks(true);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        if (!this.progressUpdate) {

            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {

                this.newCamera.set(Videoio.CV_CAP_PROP_POS_FRAMES, source.getValue());

                frameCounterLabel.setText(Integer.toString(Math.max(0, (int) newCamera.get(Videoio.CAP_PROP_POS_FRAMES) - 1)));

                if (!this.frameTimer.isRunning()) {
                    this.frameTimer.setRepeats(false);
                    this.frameTimer.start();
                }
            }
        }
    }

    public OverlayGazeProjector getHostProjector() {

        return this.hostProjector;
    }

    public void setTitleWithProgress(String activity, int progress) {

        if (progress > -1)
            this.activityProgress.put(activity, progress);
        else {
            this.activityProgress.remove(activity);
            panel.repaint();
        }

        String compositeTitle = this.idleTitle;

        for (String curAct : activityProgress.keySet()) {
            compositeTitle += " [" + curAct + ": " + activityProgress.get(curAct) + "%]";
        }

        this.setTitle(compositeTitle);
    }

    public void updateQuickSettingsToolbar() {

        this.qsPanel.updateSelectableIndicesFromDatasets();
        this.qsPanel.updateSelectedIndicesFromPreferences();
    }
}
