package de.uni_stuttgart.visus.etfuse.fileimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.swing.SwingWorker;

import de.uni_stuttgart.visus.etfuse.database.EyeTrackerRecordingCollector;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.fileimport.parsers.ParserController;

public class ImportTask extends SwingWorker implements PropertyChangeListener {

    String filePath = "";
    File file = null;
    ParserController pc = null;

    public ImportTask(String filePath) {

        super();

        this.filePath = filePath;
        this.pc = new ParserController();
    }

    public ImportTask(File file) {

        super();

        this.file = file;
        this.pc = new ParserController();
    }

    @Override
    protected Object doInBackground() throws Exception {
        if (this.file != null) {
            return this.importFilePath(this.file.getAbsolutePath());
        }
        else if (this.filePath.length() > 0) {
            return this.importFilePath(this.filePath);
        }

        return null;
    }

    private EyeTrackerRecording importFilePath(String path) {
        if (this.file == null)
            this.file = new File(path);

        String[] parts = path.split("\\.");
        if (parts[parts.length - 1].toString().equals("tsv")) {
            return this.importFile(this.file);
        }
        else if (parts.length >= 3 && (parts[parts.length - 2].toString() + "." + parts[parts.length - 1].toString()).equals("tsv.gz")) {
            return this.importGzipFile(this.file);
        }
        return null;
    }

    public EyeTrackerRecording importFile(File file) {

        this.filePath = file.getPath();

        ArrayList<String> fileContents = new ArrayList<String>();

        FileInputStream inputStream = null;
        Scanner sc = null;

        try {

            inputStream = new FileInputStream(file);
            sc = new Scanner(inputStream, "UTF-8");

            String sCurrentLine;

            this.setProgress(0);
            long totalLength = file.length();
            long readLength = 0;
            double lengthPerPercent = 100.0 / totalLength;

            while (sc.hasNextLine()) {
                sCurrentLine = sc.nextLine();
                fileContents.add(sCurrentLine);

                readLength += sCurrentLine.length();
                this.setProgress((int) (Math.round(lengthPerPercent * readLength)));
            }

            // scanner unterdrückt exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }

        } catch (IOException ex) {

            ex.printStackTrace();

        } finally {

            try {

                if (inputStream != null)
                    inputStream.close();

                if (sc != null)
                    sc.close();

            } catch (IOException ex) {

                ex.printStackTrace();
            }
        }

        //parse

        this.setProgress(100);

        System.out.println("<ImportTask> Datei gelesen. Parse Inhalt...");

        pc.addProgressEventListener(this);
        EyeTrackerRecording rec = pc.parseDataUsingBestParser(fileContents);

        fileContents.clear();
        System.gc();

        EyeTrackerRecordingCollector.sharedInstance().addRecording(rec);

        return rec;
    }

    public EyeTrackerRecording importGzipFile(File file) {

        this.filePath = file.getPath();

        ArrayList<String> fileContents = new ArrayList<String>();

        FileInputStream inputStream = null;
        BufferedReader buffered = null;

        try {
            inputStream = new FileInputStream(file);

            GZIPInputStream gzipStream = new GZIPInputStream(inputStream);
            InputStreamReader decoder = new InputStreamReader(gzipStream, "UTF-8");
            buffered = new BufferedReader(decoder);

            String sCurrentLine;

            this.setProgress(0);

            while ((sCurrentLine = buffered.readLine()) != null) {
                fileContents.add(sCurrentLine);
            }

        } catch (IOException ex) {

            ex.printStackTrace();

        } finally {

            try {

                if (inputStream != null)
                    inputStream.close();

                if (buffered != null)
                    buffered.close();

            } catch (IOException ex) {

                ex.printStackTrace();
            }
        }

        //parse

        this.setProgress(100);

        System.out.println("<ImportTask> Datei gelesen. Parse Inhalt...");

        pc.addProgressEventListener(this);
        EyeTrackerRecording rec = pc.parseDataUsingBestParser(fileContents);

        fileContents.clear();
        System.gc();

        EyeTrackerRecordingCollector.sharedInstance().addRecording(rec);

        return rec;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        this.setProgress(pc.getProgress());
    }

}
