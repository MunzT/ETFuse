package de.uni_stuttgart.visus.etfuse.fileimport.parsers;

import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;

public class TSVParserT60XL extends TSVParser {

    private enum Columns {

        TIMESTAMP(0), NUMBER(3), EYE_DIST_LEFT(8), EYE_DIST_RIGHT(15), EVENT(21),
        SCREENWIDTH(28), SCREENHEIGHT(29), POINT_X(32), POINT_Y(33);

        private int index;

        Columns(int index) {
            this.index = index;
        }

        public int i() {
            return this.index;
        }
    }

    @Override
    public String parserDescription() { return "Tobii Pro T60XL"; }

    @Override
    public EyeTrackerRecording parseData(List<String> rawData) {

        if (rawData.size() < 1)
            return null;

        EyeTrackerRecording rec = new EyeTrackerRecording();

        ArrayList<String> map = (ArrayList<String>) rawData;

        int dataLinePointer = 0;

        while (dataLinePointer < map.size()) {

            if (map.get(dataLinePointer).split("\t")[0].equals("Timestamp"))
                break;

            dataLinePointer++;
        }

        dataLinePointer++;

        String recordingStartTime = map.get(dataLinePointer).split("\t")[1];
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");
        Date recordingStartDate = null;

        try {
            recordingStartDate = sdf.parse(recordingStartTime);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(recordingStartDate);

        while (dataLinePointer < map.size()) {

            if (map.get(dataLinePointer).split("\t")[Columns.EVENT.i()].contains("ScreenRecStarted"))
                break;

            dataLinePointer++;
        }

        dataLinePointer++;

        int screenWidth = 0;
        int screenHeight = 0;

        try {
            screenWidth = Integer.parseInt(map.get(dataLinePointer).split("\t")[Columns.SCREENWIDTH.i()]);
        } catch (NumberFormatException e) {}

        try {
            screenHeight = Integer.parseInt(map.get(dataLinePointer).split("\t")[Columns.SCREENHEIGHT.i()]);
        } catch (NumberFormatException e) {}

        Rectangle2D screenResolution = new Rectangle2D.Double(0, 0, screenWidth, screenHeight);
        rec.setScreenResolution(screenResolution);
        rec.setDisplayPPI(94.34);

        rec.setSamplingFrequency(60);

        this.setProgress(0);
        long linesToProcess = map.size() - dataLinePointer;
        long processedLines = 0;
        double linesPerPercent = 100.0 / linesToProcess;

        while (dataLinePointer < map.size()) {

            String[] line = map.get(dataLinePointer).split("\t");

            if (line[Columns.NUMBER.i()].length() > 0) {
                if (line[Columns.TIMESTAMP.i()].length() > 0) {

                    EyeTrackerEyeEvent e = new EyeTrackerEyeEvent();
                    e.number = Integer.parseInt(line[Columns.NUMBER.i()]);
                    e.timestamp = Long.parseLong(line[Columns.TIMESTAMP.i()]);

                    if (line[Columns.POINT_X.i()].length() > 0)
                        e.fixationPointX = Integer.parseInt(line[Columns.POINT_X.i()]);
                    else
                        e.eyesNotFound = true;

                    if (line[Columns.POINT_Y.i()].length() > 0)
                        e.fixationPointY = Integer.parseInt(line[Columns.POINT_Y.i()]);
                    else
                        e.eyesNotFound = true;

                    if (line[Columns.EYE_DIST_LEFT.i()].length() > 0
                            && !(line[Columns.EYE_DIST_LEFT.i()].contains("unendlich"))) {
                        e.eyePosLeftZ = Double.parseDouble(line[Columns.EYE_DIST_LEFT.i()].replace(",", "."));
                        e.eyePosLeftX = screenResolution.getCenterX();
                        e.eyePosLeftY = screenResolution.getCenterY();
                    }

                    if (line[Columns.EYE_DIST_RIGHT.i()].length() > 0
                            && !(line[Columns.EYE_DIST_RIGHT.i()].contains("unendlich"))) {
                        e.eyePosRightZ = Double.parseDouble(line[Columns.EYE_DIST_RIGHT.i()].replace(",", "."));
                        e.eyePosRightX = screenResolution.getCenterX();
                        e.eyePosRightY = screenResolution.getCenterY();
                    }

                    if (e.eyesNotFound) {
                        e.fixationPointX = -10000;
                        e.fixationPointY = -10000;
                    }

                    e.realFixationPointX = e.fixationPointX;
                    e.realFixationPointY = e.fixationPointY;

                    e.fixationDuration = 1000.0 / rec.getSamplingFrequency();

                    rec.addEyeEvent(e);
                }
            }
            else if (line[Columns.EVENT.i()].contains("LeftMouseClick")) {

                long ts = Long.parseLong(line[Columns.TIMESTAMP.i()]);
                rec.addClick(ts);
            }
            else if (line[Columns.EVENT.i()].contains("ScreenRecStopped"))
                break;

            processedLines++;

            setProgress((int) (Math.round(linesPerPercent * processedLines)));

            dataLinePointer++;
        }

        setProgress(100);

        rec.recordingStartTS = timeCal.getTimeInMillis();

        map = null;

        return rec;
    }

    @Override
    public double canParseDataConfidence(List<String> rawData) {

        String firstToken = rawData.get(0).split("\t")[0];

        if (firstToken.contains("System Properties:"))
            return 0.5;

        return 0.0;
    }

}
