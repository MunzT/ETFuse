package de.uni_stuttgart.visus.etfuse.projectio;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.fileimport.ImportTask;
import de.uni_stuttgart.visus.etfuse.gui.MainFrame;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.media.HeatMapGenerator;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Preferences.HeatMapTimeSource;

public class ProjectIO implements PropertyChangeListener {

    private EyeTrackerRecording lastRec = null;

    private VideoFrame vidFrame = null;
    private int curGuest = 0;

    private MainFrame mainFrame = null;

    public ProjectIO() {}
    public ProjectIO(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void loadProject() {

        if (mainFrame != null)
            mainFrame.setButtonEnabled(false);

        final JFileChooser fc =
                new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

        fc.addChoosableFileFilter(new FileNameExtensionFilter("Eye Tracker Project", "etproj"));
        fc.setAcceptAllFileFilterUsed(false);

        //In response to a button click:
        int returnVal = fc.showOpenDialog(fc);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            final File chosenFile = fc.getSelectedFile();
            Project.loadProjectFromFile(chosenFile);

            if (Project.curProj == null) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Loading project failed. File incompatible with this software version.");
                mainFrame.setButtonEnabled(true);
                return;
            }

            prepareProjectStep1();

            Project.currentProject().getPreferences().setFileDirectory(chosenFile.getAbsolutePath());
        }
        else
            if (mainFrame != null)
                mainFrame.setButtonEnabled(true);
    }

    private Boolean checkProjectIntegrity() {

        Project proj = Project.currentProject();

        if (proj.guestDatasetPaths.size() != proj.guestFrames.size() ||
            proj.guestVidPaths.size() != proj.guestTimeShiftOffsets.size() ||
            proj.guestDatasetPaths.size() != proj.guestTimeShiftOffsets.size()) {

            if (mainFrame == null)
                return false;

            JOptionPane.showMessageDialog(mainFrame, "Guest recordings data is corrupted. Abort...");
            return false;
        }

        File hostVidFile = new File(proj.hostVidPath);
        if (!hostVidFile.isFile()) {

            if (mainFrame == null)
                return false;

            JOptionPane.showMessageDialog(mainFrame, "Could not find host video \"" + proj.hostVidPath + "\"!");

            final JFileChooser fc =
                    new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

            fc.addChoosableFileFilter(new FileNameExtensionFilter("Video file", "mp4", "avi",
                                                                  "mkv", "flv", "mpeg"));
            fc.setAcceptAllFileFilterUsed(false);

            int returnVal = fc.showOpenDialog(mainFrame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                final File chosenFile = fc.getSelectedFile();
                proj.hostVidPath = chosenFile.getAbsolutePath();

                Project.currentProject().getPreferences().setFileDirectory(chosenFile.getAbsolutePath());
            }
            else
                return false;
        }

        File hostDatasetFile = new File(proj.hostDatasetPath);
        if (!hostDatasetFile.isFile()) {

            if (mainFrame == null)
                return false;

            JOptionPane.showMessageDialog(mainFrame,
                    "Could not find host eye tracking data set \"" + proj.hostDatasetPath + "\"!");

            final JFileChooser fc =
                    new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

            fc.addChoosableFileFilter(new FileNameExtensionFilter("TSV file", "tsv"));
            fc.setAcceptAllFileFilterUsed(false);

            int returnVal = fc.showOpenDialog(mainFrame);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                final File chosenFile = fc.getSelectedFile();
                proj.hostDatasetPath = chosenFile.getAbsolutePath();

                Project.currentProject().getPreferences().setFileDirectory(chosenFile.getAbsolutePath());
            }
            else
                return false;
        }

        /*for (int i = 0; i < proj.guestVidPaths.size(); i++) {

            String guestVidPath = proj.guestVidPaths.get(i);

            File guestVidFile = new File(guestVidPath);
            if (!guestVidFile.isFile()) {

                if (mainFrame == null)
                    return false;

                JOptionPane.showMessageDialog(mainFrame,
                        "Could not find guest video \"" + guestVidPath + "\"!");

                final JFileChooser fc =
                        new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

                fc.addChoosableFileFilter(
                        new FileNameExtensionFilter("Video file", "mp4", "avi", "mkv", "flv", "mpeg"));
                fc.setAcceptAllFileFilterUsed(false);

                int returnVal = fc.showOpenDialog(mainFrame);

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    final File chosenFile = fc.getSelectedFile();
                    proj.guestVidPaths.set(i, chosenFile.getAbsolutePath());

                    Project.currentProject().getPreferences().setFileDirectory(chosenFile.getAbsolutePath());
                }
                else
                    return false;
            }
        }*/

        for (int i = 0; i < proj.guestDatasetPaths.size(); i++) {

            String guestDatasetPath = proj.guestDatasetPaths.get(i);

            File guestDatasetFile = new File(guestDatasetPath);
            if (!guestDatasetFile.isFile()) {

                if (mainFrame == null)
                    return false;

                JOptionPane.showMessageDialog(mainFrame,
                        "Could not find guest eye tracking data set \"" + guestDatasetPath + "\"!");

                final JFileChooser fc =
                        new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

                fc.addChoosableFileFilter(new FileNameExtensionFilter("TSV file", "tsv"));
                fc.setAcceptAllFileFilterUsed(false);

                int returnVal = fc.showOpenDialog(mainFrame);

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    final File chosenFile = fc.getSelectedFile();
                    proj.guestDatasetPaths.set(i, chosenFile.getAbsolutePath());

                    Project.currentProject().getPreferences().setFileDirectory(chosenFile.getAbsolutePath());
                }
                else
                    return false;
            }
        }

        return true;
    }

    private void prepareProjectStep1() {

        if (!checkProjectIntegrity()) {
            if (mainFrame != null)
                mainFrame.setButtonEnabled(true);
            return;
        }

        if (mainFrame != null)
            mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Project proj = Project.currentProject();

        Path path = Paths.get(proj.hostVidPath);
        Path fileName = path.getFileName();

        vidFrame = new VideoFrame("ETFuse - " + fileName.toString(), proj.hostVidPath);
        vidFrame.setLocationRelativeTo(null);

        File hostDatasetFile = new File(proj.hostDatasetPath);

        ImportTask importTask = new ImportTask(hostDatasetFile);
        importTask.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().contains("state"))
                    if (importTask.getState() == SwingWorker.StateValue.DONE) {

                        EyeTrackerRecording rec = null;

                        try {
                            rec = (EyeTrackerRecording) importTask.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (rec != null) {
                            rec.preferredGazeColor =
                                Project.currentProject().getPreferences().getColorPlayer1();

                            vidFrame.setHostRecording(rec);
                            ProjectIO.this.prepareProjectStep2();
                        }
                    }
            }
        });
        importTask.execute();
    }

    private void prepareProjectStep2() {

        Project proj = Project.currentProject();

        if (proj.hostFrame != null)
            vidFrame.getHostProjector().getRecording().setFrame(new Point((int) proj.hostFrame.getMinX(),
                    (int) proj.hostFrame.getMinY()), new Point((int) proj.hostFrame.getMaxX(),
                            (int) proj.hostFrame.getMaxY()));

        prepareProjectStep3();
    }

    private void prepareProjectStep3() {

        Project proj = Project.currentProject();

        if (proj.guestDatasetPaths.size() <= curGuest || proj.guestFrames.size() <= curGuest
                || proj.guestTimeShiftOffsets.size() <= curGuest) {
            prepareProjectStep5();
            return;
        }

        String guestPath = proj.guestDatasetPaths.get(curGuest);
        File guestDatasetFile = new File(guestPath);
        ImportTask impTask = new ImportTask(guestDatasetFile);
        impTask.addPropertyChangeListener(this);
        impTask.execute();
    }

    private void prepareProjectStep4() {

        if (lastRec == null)
            return;

        Project proj = Project.currentProject();

        Rectangle guestFrame = proj.guestFrames.get(curGuest);
        lastRec.setFrame(new Point((int) guestFrame.getMinX(), (int) guestFrame.getMinY()),
                new Point((int) guestFrame.getMaxX(), (int) guestFrame.getMaxY()));
        OverlayGazeProjector guestProj = new OverlayGazeProjector(lastRec, vidFrame.getPanel());
        guestProj.transformRawPointsToTarget(vidFrame.getHostProjector().getRecording());
        guestProj.transformFilteredPointsToTarget(vidFrame.getHostProjector().getRecording());
        guestProj.setTimeSyncShift(proj.guestTimeShiftOffsets.get(curGuest));

        int previousClickNo = vidFrame.getPanel().getClicks().size();
        vidFrame.getPanel().attachProjector(guestProj);
        int newClickNo = vidFrame.getPanel().getClicks().size();

        HeatMapGenerator mapGen = new HeatMapGenerator(guestProj, 0, HeatMapTimeSource.USERDEFINED, vidFrame);
        mapGen.attachVideoFrameForTitleUpdate(vidFrame);
        mapGen.execute();
        for (int i = 0; i <= newClickNo; i++) {
            mapGen = new HeatMapGenerator(guestProj, i, HeatMapTimeSource.CLICKS, vidFrame);
            mapGen.attachVideoFrameForTitleUpdate(vidFrame);
            mapGen.execute();
        }

        if (newClickNo != previousClickNo) {
            // recalculate for host
            for (int i = 0; i < vidFrame.getPanel().getProjectors().size() - 1; i++) {
                mapGen = new HeatMapGenerator(vidFrame.getPanel().getProjector(i),
                        0, HeatMapTimeSource.USERDEFINED, vidFrame);
                mapGen.attachVideoFrameForTitleUpdate(vidFrame);
                mapGen.execute();

                for (int j = 1; j <= newClickNo; j++) {
                    mapGen = new HeatMapGenerator(vidFrame.getPanel().getProjector(i),
                            j, HeatMapTimeSource.CLICKS, vidFrame);
                    mapGen.attachVideoFrameForTitleUpdate(vidFrame);
                    mapGen.execute();
                }
            }
        }

        curGuest++;

        prepareProjectStep3();
    }

    private void prepareProjectStep5() {

        vidFrame.updateQuickSettingsToolbar();
        vidFrame.setVisible(true);

        if (mainFrame != null) {
            mainFrame.setCursor(Cursor.getDefaultCursor());
            mainFrame.setVisible(false);
            mainFrame.dispose();
        }
    }

    public void saveProject() {

        final JFileChooser fc =
                new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

        fc.addChoosableFileFilter(new FileNameExtensionFilter("Eye tracker project (.etproj)", "etproj"));
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showSaveDialog(fc);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File chosenFile = fc.getSelectedFile();
            if (!chosenFile.getAbsolutePath().endsWith(".etproj"))
                chosenFile = new File(chosenFile.getAbsolutePath().concat(".etproj"));
            Project.saveProjectToFile(chosenFile);

            Project.currentProject().getPreferences().setFileDirectory(chosenFile.getAbsolutePath());
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

                    if (vidFrame.getPanel().getProjectors().size() == 1)
                        rec.preferredGazeColor =
                            Project.currentProject().getPreferences().getColorPlayer2();

                    else if (vidFrame.getPanel().getProjectors().size() == 2)
                        rec.preferredGazeColor = Color.orange;
                    else {
                        Random rand = new Random();
                        float r = rand.nextFloat();
                        float g = rand.nextFloat();
                        float b = rand.nextFloat();
                        rec.preferredGazeColor = new Color(r, g, b);
                    }
                    lastRec = rec;
                    prepareProjectStep4();
                }
            }
    }
}
