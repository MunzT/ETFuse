package de.uni_stuttgart.visus.etfuse.fileimport;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class EventsImporter {

    private VideoFrame vidFrame;

    public EventsImporter(VideoFrame vidFrame) {
        this.vidFrame = vidFrame;
    }

    public HashMap<Long, Color> loadEvents() {
        final JFileChooser fc =
                new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

        fc.addChoosableFileFilter(new FileNameExtensionFilter("Events file (.csv)", "csv"));
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showOpenDialog(vidFrame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File chosenFile = fc.getSelectedFile();

            Project.currentProject().eventsPath = chosenFile.getAbsolutePath();
            return load(chosenFile.getAbsolutePath());
        }
        return null;
    }

    public HashMap<Long, Color> load(String path) {
        HashMap<Long, Color> events = new HashMap<Long, Color>();
        if(path != null && !path.isEmpty()) {
            try(BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while((line = br.readLine()) != null) {
                   // split for "="
                    String[] parts = line.split(",", 2);

                    if (parts[0] != null && parts[1] != null) {
                        Integer f = getInt(parts[0]);
                        Color c = getColor(parts[1]);
                        if (f != null && c != null) {

                            // frame to timestamp
                            long ts = vidFrame.getPanel().getTimeForFrame(f);
                            events.put(ts, c);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return events;
    }

    private Integer getInt(String str) {
        int i;
        try {
            i = Integer.parseInt(str.trim());
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

    private Color getColor(String str) {
        Color i = null;
        try {
            i = Color.decode(str);
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

}