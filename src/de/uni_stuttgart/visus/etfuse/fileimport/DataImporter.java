package de.uni_stuttgart.visus.etfuse.fileimport;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class DataImporter implements ActionListener, PropertyChangeListener {

    private JProgressBar linkedBar = null;
    private VideoFrame parentFrame = null;
    private ImportTask importTask = null;

    public DataImporter(VideoFrame parentFrame, JProgressBar linkedBar) {

        this.linkedBar = linkedBar;
        this.parentFrame = parentFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
        //Create a file chooser
        final JFileChooser fc = new JFileChooser();

        fc.addChoosableFileFilter(new FileNameExtensionFilter("TSV-Datei", "tsv"));
        fc.setAcceptAllFileFilterUsed(false);

        //In response to a button click:
        int returnVal = fc.showOpenDialog((Component) e.getSource());

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            this.parentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // datei ausgewählt; fahre fort

            final File chosenFile = fc.getSelectedFile();

            Project.currentProject().hostDatasetPath = chosenFile.getAbsolutePath();
            this.importTask = new ImportTask(chosenFile);
            this.importTask.addPropertyChangeListener(this);
            this.importTask.execute();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName().contains("state"))
            if (this.importTask.getState() == SwingWorker.StateValue.DONE) {

                EyeTrackerRecording rec = null;

                try {
                    rec = (EyeTrackerRecording) this.importTask.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (rec != null) {

                    rec.preferredGazeColor = Project.currentProject().getPreferences().getColorPlayer1();
                    this.parentFrame.setHostRecording(rec);
                    this.parentFrame.setCursor(Cursor.getDefaultCursor());
                    this.parentFrame.updateQuickSettingsToolbar();
                    JOptionPane.showMessageDialog(this.parentFrame,
                            "Finished. " + rec.getRawEyeEvents().size()
                            + " eye tracking events were read and "
                                    + rec.getFilteredEyeEvents().size() + " fixations determined.");
                }
            }

        if (this.linkedBar != null) {

            this.linkedBar.setValue(importTask.getProgress());

            if (importTask.getProgress() == 100)
                this.linkedBar.setVisible(false);

            if (importTask.getProgress() < 100)
                this.linkedBar.setVisible(true);
        }

        if (this.parentFrame != null) {

            if (importTask.getProgress() == 100)
                this.parentFrame.setTitleWithProgress("Importiere Eye-Tracker-Daten (id "
                    + importTask.hashCode() + ")", -1);
            else
                this.parentFrame.setTitleWithProgress("Importiere Eye-Tracker-Daten (id "
                    + importTask.hashCode() + ")", importTask.getProgress());
        }
    }
}
