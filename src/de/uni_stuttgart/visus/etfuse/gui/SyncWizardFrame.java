package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.videoio.VideoCapture;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.fileimport.ImportTask;
import de.uni_stuttgart.visus.etfuse.media.HeatMapGenerator;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.media.RecTempSynchronizer;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class SyncWizardFrame extends JDialog implements PropertyChangeListener, NotificationListener {

    private static final long serialVersionUID = 5770808289901431468L;
    
    JButton cancelButton, nextButton, backButton, loadSourceDataButton, loadSourceVideoButton, videoTargetButton, helpButton;
    JLabel stepOneLabel, stepTwoLabel, stepThreeLabel, stepFourLabel, videoSourceLabel, videoTargetLabel;
    JCheckBox predefinedSourceCheckBox, predefinedTargetCheckBox;
    JRadioButton customTimeSyncRadio, histogramTimeSyncRadio, estimatedTimeSyncRadio;
    ButtonGroup timeSyncGroup;
    JComboBox<String> predefinedSourceFrameCombo, predefinedTargetFrameCombo;
        
    ArrayList<JDialog> childFrames;
    
    int state;
    
    // --- IMPORT DATA ---
    VideoFrame parentFrame;
    EyeTrackerRecording sourceData;
    VideoCapture sourceVideo;
    
    int histogramOrientationFrame = 0;
    Point customSyncStoneCoord = null;
    // -------------------
    
    public SyncWizardFrame(VideoFrame parentFrame) {
        
        super(parentFrame, "Gast-Recording einbinden", true);
        
        childFrames = new ArrayList<JDialog>();
        
        this.parentFrame = parentFrame;
        
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                
        cancelButton = new JButton("Abbrechen");
        cancelButton.setToolTipText("Vorgang abbrechen und Wizard schließen");
        nextButton = new JButton("Weiter");
        nextButton.setToolTipText("Zum nächsten Schritt fortfahren");
        backButton = new JButton("Zurück");
        backButton.setToolTipText("Zum vorherigen Schritt zurückkehren");
        loadSourceDataButton = new JButton("Quell-Daten laden");
        loadSourceDataButton.setToolTipText("Datensatz des einzubindenden Recordings auswählen");
        loadSourceVideoButton = new JButton("Video-Datei öffnen");
        loadSourceVideoButton.setToolTipText("Screen-Recording zum einzubindenden Datensatz auswählen");
        helpButton = new JButton("?");
        helpButton.setToolTipText("Hilfe anzeigen");
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeFrame();
            }
        });
        
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextStep();
            }
        });
        
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousStep();
            }
        });
        
        loadSourceDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                loadSourceDataButton.setEnabled(false);
                
                // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
                //Create a file chooser
                final JFileChooser fc = new JFileChooser();

                fc.addChoosableFileFilter(new FileNameExtensionFilter("TSV-Datei", "tsv"));
                fc.setAcceptAllFileFilterUsed(false);

                //In response to a button click:
                int returnVal = fc.showOpenDialog((Component) e.getSource());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    
                    // datei ausgewählt; fahre fort

                    final File chosenFile = fc.getSelectedFile();

                    Project.currentProject().guestDatasetPaths.add(chosenFile.getAbsolutePath());
                    ImportTask importTask = new ImportTask(chosenFile);
                    importTask.addPropertyChangeListener(SyncWizardFrame.this);
                    importTask.execute();
                }
            }
        });
        
        loadSourceVideoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                                
                File chosenFile = null;

                // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
                //Create a file chooser
                final JFileChooser fc = new JFileChooser();

                fc.addChoosableFileFilter(new FileNameExtensionFilter("Video-Datei", "mp4", "avi", "mkv", "flv", "mpeg"));
                fc.setAcceptAllFileFilterUsed(false);

                //In response to a button click:
                int returnVal = fc.showOpenDialog((Component) e.getSource());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // datei ausgewählt; fahre fort

                    chosenFile = fc.getSelectedFile();
                    
                    Project.currentProject().guestVidPaths.add(chosenFile.getAbsolutePath());
                    VideoCapture sourceCamera = new VideoCapture(chosenFile.getAbsolutePath());
                    sourceVideo = sourceCamera;
                    BoxPickerFrame sourceBpf = new BoxPickerFrame(SyncWizardFrame.this, sourceCamera, sourceData, (NotificationListener) SyncWizardFrame.this);
                    childFrames.add(sourceBpf);
                    sourceBpf.setVisible(true);
                }
            }
        });
        
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                SyncWizardHelpFrame helpFrame = new SyncWizardHelpFrame(SyncWizardFrame.this);
                helpFrame.setVisible(true);
            }
        });
        
        String predefinedSourceFramesArray[] = {"Win10 1920x1080", "Win7+Taskbar 1920x1200"};
        predefinedSourceFrameCombo = new JComboBox<String>(predefinedSourceFramesArray);
        predefinedSourceFrameCombo.setSelectedIndex(1);
        String predefinedTargetFramesArray[] = {"Win10 1920x1080", "Win7+Taskbar 1920x1200"};
        predefinedTargetFrameCombo = new JComboBox<String>(predefinedTargetFramesArray);
        predefinedTargetFrameCombo.setSelectedIndex(0);
        
        stepOneLabel = new JLabel("1. Quell-Daten laden:");
        stepTwoLabel = new JLabel("2. Quell-Frame festlegen:");
        stepThreeLabel = new JLabel("3. Ziel-Frame festlegen:");
        stepFourLabel = new JLabel("4. Zeitliche Synchronisierung:");
        
        predefinedSourceCheckBox = new JCheckBox("Preset nutzen:");
        videoSourceLabel = new JLabel("Oder im Video manuell festlegen:");
        predefinedTargetCheckBox = new JCheckBox("Preset nutzen:");
        videoTargetLabel = new JLabel("Oder im Video manuell festlegen:");
        videoTargetButton = new JButton("Manuell auswählen");
        videoTargetButton.setToolTipText("Spielfeldbereich manuell im Screen-Recording bestimmen");
        
        timeSyncGroup = new ButtonGroup();
        
        customTimeSyncRadio = new JRadioButton("Präziserer Time-Sync (experimentell; nur GO-Spiel)");
        histogramTimeSyncRadio = new JRadioButton("Präziserer Time-Sync über Histogramme (empfohlen)");
        estimatedTimeSyncRadio = new JRadioButton("Nutze Schätzwert für Time-Sync (ungenau)");
        
        timeSyncGroup.add(customTimeSyncRadio);
        timeSyncGroup.add(histogramTimeSyncRadio);
        timeSyncGroup.add(estimatedTimeSyncRadio);
        
        estimatedTimeSyncRadio.setSelected(true);
        
        predefinedSourceCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (predefinedSourceCheckBox.isSelected()) {
                    nextButton.setEnabled(true);
                    loadSourceVideoButton.setEnabled(false);
                }
                else {
                    nextButton.setEnabled(false);
                    loadSourceVideoButton.setEnabled(true);
                }
            }
        });
        
        predefinedTargetCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (predefinedTargetCheckBox.isSelected()) {
                    nextButton.setEnabled(true);
                    videoTargetButton.setEnabled(false);
                }
                else {
                    nextButton.setEnabled(false);
                    videoTargetButton.setEnabled(true);
                }
            }
        });
        
        videoTargetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                VideoCapture targetCamera = parentFrame.getPanel().getCamera();
                EyeTrackerRecording targetData = parentFrame.hostProjector.getRecording();
                
                BoxPickerFrame targetBpf = new BoxPickerFrame(SyncWizardFrame.this, targetCamera, targetData, (NotificationListener) SyncWizardFrame.this);
                childFrames.add(targetBpf);
                targetBpf.setVisible(true);
            }
        });
        
        customTimeSyncRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                if (customTimeSyncRadio.isSelected()) {
                    
                    if (sourceVideo == null) {
                        
                        JOptionPane.showMessageDialog(SyncWizardFrame.this, "Ein Screen-Recording der Gast-Daten wird hierfür benötigt!");
                        
                        File chosenFile = null;

                        // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
                        //Create a file chooser
                        final JFileChooser fc = new JFileChooser();

                        fc.addChoosableFileFilter(new FileNameExtensionFilter("Video-Datei", "mp4", "avi", "mkv", "flv", "mpeg"));
                        fc.setAcceptAllFileFilterUsed(false);

                        //In response to a button click:
                        int returnVal = fc.showOpenDialog((Component) e.getSource());

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            // datei ausgewählt; fahre fort

                            chosenFile = fc.getSelectedFile();
                            
                            Project.currentProject().guestVidPaths.add(chosenFile.getAbsolutePath());
                            VideoCapture sourceCamera = new VideoCapture(chosenFile.getAbsolutePath());
                            sourceVideo = sourceCamera;
                        }
                        else {
                            estimatedTimeSyncRadio.setSelected(true);
                            return;
                        }
                    }

                    VideoCapture targetCamera = parentFrame.getPanel().getCamera();
                    EyeTrackerRecording targetData = parentFrame.hostProjector.getRecording();
                    
                    StonePickerFrame stonePicker = new StonePickerFrame(SyncWizardFrame.this, targetCamera, targetData, (NotificationListener) SyncWizardFrame.this);
                    stonePicker.setVisible(true);
                }
            }
        });
        
        histogramTimeSyncRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                if (histogramTimeSyncRadio.isSelected()) {
                    
                    if (sourceVideo == null) {
                        
                        JOptionPane.showMessageDialog(SyncWizardFrame.this, "Ein Screen-Recording der Gast-Daten wird hierfür benötigt!");
                        
                        File chosenFile = null;

                        // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
                        //Create a file chooser
                        final JFileChooser fc = new JFileChooser();

                        fc.addChoosableFileFilter(new FileNameExtensionFilter("Video-Datei", "mp4", "avi", "mkv", "flv", "mpeg"));
                        fc.setAcceptAllFileFilterUsed(false);

                        //In response to a button click:
                        int returnVal = fc.showOpenDialog((Component) e.getSource());

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            // datei ausgewählt; fahre fort

                            chosenFile = fc.getSelectedFile();
                            
                            Project.currentProject().guestVidPaths.add(chosenFile.getAbsolutePath());
                            VideoCapture sourceCamera = new VideoCapture(chosenFile.getAbsolutePath());
                            sourceVideo = sourceCamera;
                        }
                        else {
                            estimatedTimeSyncRadio.setSelected(true);
                            return;
                        }
                    }
                    
                    VideoCapture targetCamera = parentFrame.getPanel().getCamera();
                    EyeTrackerRecording targetData = parentFrame.hostProjector.getRecording();
                    
                    FramePickerFrame framePicker = new FramePickerFrame(SyncWizardFrame.this, targetCamera, targetData, (NotificationListener) SyncWizardFrame.this);
                    framePicker.setVisible(true);
                }
            }
        });
        
        SpringLayout layout = new SpringLayout();
        Container contentPane = this.getContentPane();

        layout.putConstraint(SpringLayout.WEST, stepOneLabel, 15, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, loadSourceDataButton, 40, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, stepTwoLabel, 15, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, predefinedSourceCheckBox, 20, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, predefinedSourceFrameCombo, 40, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, videoSourceLabel, 20, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, loadSourceVideoButton, 40, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, stepThreeLabel, 15, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, predefinedTargetCheckBox, 20, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, predefinedTargetFrameCombo, 40, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, videoTargetLabel, 20, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, videoTargetButton, 40, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, stepFourLabel, 15, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, customTimeSyncRadio, 20, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, histogramTimeSyncRadio, 20, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.WEST, estimatedTimeSyncRadio, 20, SpringLayout.WEST, contentPane);


        layout.putConstraint(SpringLayout.NORTH, stepOneLabel, 15, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.NORTH, loadSourceDataButton, 5, SpringLayout.SOUTH, stepOneLabel);
        layout.putConstraint(SpringLayout.NORTH, stepTwoLabel, 15, SpringLayout.SOUTH, loadSourceDataButton);
        layout.putConstraint(SpringLayout.NORTH, predefinedSourceCheckBox, 5, SpringLayout.SOUTH, stepTwoLabel);
        layout.putConstraint(SpringLayout.NORTH, predefinedSourceFrameCombo, 5, SpringLayout.SOUTH, predefinedSourceCheckBox);
        layout.putConstraint(SpringLayout.NORTH, videoSourceLabel, 5, SpringLayout.SOUTH, predefinedSourceFrameCombo);
        layout.putConstraint(SpringLayout.NORTH, loadSourceVideoButton, 5, SpringLayout.SOUTH, videoSourceLabel);
        layout.putConstraint(SpringLayout.NORTH, stepThreeLabel, 15, SpringLayout.SOUTH, loadSourceVideoButton);
        layout.putConstraint(SpringLayout.NORTH, predefinedTargetCheckBox, 5, SpringLayout.SOUTH, stepThreeLabel);
        layout.putConstraint(SpringLayout.NORTH, predefinedTargetFrameCombo, 5, SpringLayout.SOUTH, predefinedTargetCheckBox);
        layout.putConstraint(SpringLayout.NORTH, videoTargetLabel, 5, SpringLayout.SOUTH, predefinedTargetFrameCombo);
        layout.putConstraint(SpringLayout.NORTH, videoTargetButton, 5, SpringLayout.SOUTH, videoTargetLabel);
        layout.putConstraint(SpringLayout.NORTH, stepFourLabel, 15, SpringLayout.SOUTH, videoTargetButton);
        layout.putConstraint(SpringLayout.NORTH, customTimeSyncRadio, 5, SpringLayout.SOUTH, stepFourLabel);
        layout.putConstraint(SpringLayout.NORTH, histogramTimeSyncRadio, 5, SpringLayout.SOUTH, customTimeSyncRadio);
        layout.putConstraint(SpringLayout.NORTH, estimatedTimeSyncRadio, 5, SpringLayout.SOUTH, histogramTimeSyncRadio);

        
        layout.putConstraint(SpringLayout.EAST, cancelButton, -5, SpringLayout.EAST, contentPane);
        layout.putConstraint(SpringLayout.EAST, nextButton, -5, SpringLayout.WEST, cancelButton);
        layout.putConstraint(SpringLayout.EAST, backButton, -5, SpringLayout.WEST, nextButton);
        layout.putConstraint(SpringLayout.EAST, helpButton, -5, SpringLayout.WEST, backButton);

        layout.putConstraint(SpringLayout.SOUTH, cancelButton, -5, SpringLayout.SOUTH, contentPane);
        layout.putConstraint(SpringLayout.SOUTH, nextButton, -5, SpringLayout.SOUTH, contentPane);
        layout.putConstraint(SpringLayout.SOUTH, backButton, -5, SpringLayout.SOUTH, contentPane);
        layout.putConstraint(SpringLayout.SOUTH, helpButton, -5, SpringLayout.SOUTH, contentPane);
        
        contentPane.add(cancelButton);
        contentPane.add(nextButton);
        contentPane.add(backButton);
        contentPane.add(helpButton);
        contentPane.add(loadSourceDataButton);
        contentPane.add(loadSourceVideoButton);
        contentPane.add(stepOneLabel);
        contentPane.add(stepTwoLabel);
        contentPane.add(stepThreeLabel);
        contentPane.add(stepFourLabel);
        contentPane.add(predefinedSourceCheckBox);
        contentPane.add(videoSourceLabel);
        contentPane.add(predefinedTargetCheckBox);
        contentPane.add(videoTargetLabel);
        contentPane.add(videoTargetButton);
        contentPane.add(predefinedSourceFrameCombo);
        contentPane.add(predefinedTargetFrameCombo);
        contentPane.add(customTimeSyncRadio);
        contentPane.add(histogramTimeSyncRadio);
        contentPane.add(estimatedTimeSyncRadio);
                
        contentPane.setLayout(layout);
        
        setAssistantState(0);
        
        this.pack();
        this.setMinimumSize(new Dimension(400, 600));
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }
    
    private void closeFrame() {
        
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    private void setPresetFrame(EyeTrackerRecording rec, int preset) {
        
        // Preset 1 (win10): x:2 y:67 bis x:1012 y:1077
        // Preset 2 (win7): x:2 y:66 bis x:1093 y:1157
                
        switch (preset) {
            
            case 0:
                rec.setFrame(new Point(2, 67), new Point(1012, 1077));
                break;
                
            case 1:
                rec.setFrame(new Point(2, 66), new Point(1093, 1157));
                break;
                
            default:
                // ;_;
        }
    }
    
    private void nextStep() {
        
        EyeTrackerRecording targetData = this.parentFrame.hostProjector.getRecording();
        
        if (state == 1) {
            if (predefinedSourceCheckBox.isSelected()) {
                setPresetFrame(sourceData, predefinedSourceFrameCombo.getSelectedIndex());
            }
        }
        
        else if (state == 2) {
            if (predefinedTargetCheckBox.isSelected()) {
                setPresetFrame(targetData, predefinedTargetFrameCombo.getSelectedIndex());
            }
        }
        
        if (state < 4)
            setAssistantState(++state);
        else {
            // fertigstellen
            
            OverlayGazeProjector newProjector = new OverlayGazeProjector(sourceData);
            newProjector.transformRawPointsToTarget(targetData);
            newProjector.transformFilteredPointsToTarget(targetData);
            
            long timeShift = 0;
            
            if (customTimeSyncRadio.isSelected()) {
                if (this.customSyncStoneCoord != null)
                    timeShift = RecTempSynchronizer.computeTimeOffsetCustom(targetData, sourceData, this.parentFrame.getPanel().getCamera(), this.sourceVideo, this.customSyncStoneCoord);
                else
                    timeShift = RecTempSynchronizer.computeTimestampOffset(targetData, sourceData);
            }
            else if (histogramTimeSyncRadio.isSelected()) {
                if (this.histogramOrientationFrame != 0)
                    timeShift = RecTempSynchronizer.computeTimeOffsetHistogram(targetData, sourceData, this.parentFrame.getPanel().getCamera(), this.sourceVideo, this.histogramOrientationFrame);
                else
                    timeShift = RecTempSynchronizer.computeTimestampOffset(targetData, sourceData);
            }
            else
                timeShift = RecTempSynchronizer.computeTimestampOffset(targetData, sourceData);
            
            
            Project proj = Project.currentProject();
            proj.guestTimeShiftOffsets.add(timeShift);
            proj.guestFrames.add(new Rectangle(sourceData.getFramePoint1().x, sourceData.getFramePoint1().y, (sourceData.getFramePoint2().x - sourceData.getFramePoint1().x), (sourceData.getFramePoint2().y - sourceData.getFramePoint1().y)));
            proj.hostFrame = new Rectangle(targetData.getFramePoint1().x, targetData.getFramePoint1().y, (targetData.getFramePoint2().x - targetData.getFramePoint1().x), (targetData.getFramePoint2().y - targetData.getFramePoint1().y));
            
            newProjector.setTimeSyncShift(timeShift);
                        
            HeatMapGenerator mapGen = new HeatMapGenerator(newProjector);
            mapGen.attachVideoFrameForTitleUpdate(this.parentFrame);
            mapGen.execute();
            
            this.parentFrame.getPanel().attachProjector(newProjector);
            
            Preferences prefs = proj.getPreferences();
            prefs.setMinDistPlotPlayer2(this.parentFrame.getPanel().getProjectors().size() - 1);
                        
            this.parentFrame.updateQuickSettingsToolbar();
            this.parentFrame.progressSlider.setValueIsAdjusting(true);
            this.parentFrame.progressSlider.setValue(0);
            this.parentFrame.progressSlider.setValueIsAdjusting(false);
            
            this.setVisible(false);
            this.dispose();
        }
    }
    
    private void previousStep() {
        
        if (state > 0)
            state--;
        
        if (state == 1)
            predefinedSourceCheckBox.setSelected(false);
        
        if (state == 2)
            predefinedTargetCheckBox.setSelected(false);
        
        setAssistantState(state);
    }
    
    private void setAssistantState(int state) {
        
        /*
         * 0: Step 1 active
         * 1: Step 2 active
         * 2: Step 3 active
         */
        
        this.state = state;
        
        nextButton.setEnabled(false);
        backButton.setEnabled(false);
        loadSourceDataButton.setEnabled(false);
        loadSourceVideoButton.setEnabled(false);
        
        predefinedSourceCheckBox.setEnabled(false);
        predefinedTargetCheckBox.setEnabled(false);
        videoTargetButton.setEnabled(false);
        
        predefinedSourceFrameCombo.setEnabled(false);
        predefinedTargetFrameCombo.setEnabled(false);
        
        stepOneLabel.setForeground(Color.lightGray);
        stepTwoLabel.setForeground(Color.lightGray);
        stepThreeLabel.setForeground(Color.lightGray);
        stepFourLabel.setForeground(Color.lightGray);
        videoSourceLabel.setForeground(Color.lightGray);
        videoTargetLabel.setForeground(Color.lightGray);
        
        customTimeSyncRadio.setEnabled(false);
        histogramTimeSyncRadio.setEnabled(false);
        estimatedTimeSyncRadio.setEnabled(false);
        
        nextButton.setText("Weiter");
        
        switch (state) {
                
            case 0:
                stepOneLabel.setForeground(Color.black);
                loadSourceDataButton.setEnabled(true);
                break;
                
            case 1:
                stepTwoLabel.setForeground(Color.black);
                predefinedSourceCheckBox.setEnabled(true);
                videoSourceLabel.setForeground(Color.black);
                predefinedSourceFrameCombo.setEnabled(true);
                loadSourceVideoButton.setEnabled(true);
                backButton.setEnabled(true);
                break;
                
            case 2:
                stepThreeLabel.setForeground(Color.black);
                predefinedTargetCheckBox.setEnabled(true);
                videoTargetLabel.setForeground(Color.black);
                videoTargetButton.setEnabled(true);
                predefinedTargetFrameCombo.setEnabled(true);
                backButton.setEnabled(true);
                break;
                
            case 3:
                stepFourLabel.setForeground(Color.black);
                customTimeSyncRadio.setEnabled(true);
                histogramTimeSyncRadio.setEnabled(true);
                estimatedTimeSyncRadio.setEnabled(true);
                nextButton.setEnabled(true);
                backButton.setEnabled(true);
                break;
                
            case 4:
                nextButton.setText("Fertig");
                nextButton.setEnabled(true);
                backButton.setEnabled(true);
                break;
            
            default:
                // something probably went wrong...
                break;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        ImportTask importTask = (ImportTask) evt.getSource();
        
        if (evt.getPropertyName().contains("state"))
            if (importTask.getState() == SwingWorker.StateValue.DONE) {
                
                EyeTrackerRecording rec = null;
                
                try {
                    rec = (EyeTrackerRecording) importTask.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (rec != null) {
                    
                    if (this.parentFrame.getPanel().getProjectors().size() == 1)
                        rec.preferredGazeColor = Color.red;
                    else if (this.parentFrame.getPanel().getProjectors().size() == 2)
                        rec.preferredGazeColor = Color.orange;
                    else {
                        Random rand = new Random();
                        float r = rand.nextFloat();
                        float g = rand.nextFloat();
                        float b = rand.nextFloat();
                        rec.preferredGazeColor = new Color(r, g, b);
                    }
                    sourceData = rec;
                    setCursor(Cursor.getDefaultCursor());
                        
                    JOptionPane.showMessageDialog(this, "Fertig. Es wurden " + rec.getRawEyeEvents().size() + " EyeTracker-Events eingelesen und " + rec.getFilteredEyeEvents().size() + " Fixationen ermittelt.");
                    
                    nextStep();
                }
            }
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        
        // frame set
        
        if (notification != null) {
            if (notification.getType().contains("boxpickerresult")) {
                /* */
            }
            else if (notification.getType().contains("stonepickerresult")) {
                System.out.println("<SyncWizardFrame> StonePickerResult erhalten");
                this.customSyncStoneCoord = (Point) handback;
            }
            else if (notification.getType().contains("framepickerresult")) {
                System.out.println("<SyncWizardFrame> FramePickerResult erhalten");
                this.histogramOrientationFrame = (int) handback;
            }
        }
        
        nextStep();
    }
    
    @Override
    public void dispose() {
        
        setVisible(false);
        
        for (JDialog child : childFrames) {
            if (child != null)
                child.setVisible(false);
                child.dispose();
        }
        
        super.dispose();
    }
}
