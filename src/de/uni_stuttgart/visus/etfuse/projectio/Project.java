package de.uni_stuttgart.visus.etfuse.projectio;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_stuttgart.visus.etfuse.misc.Preferences;

public class Project implements Serializable {

    //private static final long serialVersionUID = -2082609050833935516L;
    private static final long serialVersionUID = -2615634893322224238L;

    public String hostVidPath = "";
    public String hostDatasetPath = "";
    public Rectangle hostFrame = null;

    public ArrayList<String> guestVidPaths = null;
    public ArrayList<String> guestDatasetPaths = null;
    public ArrayList<Rectangle> guestFrames = null;
    public ArrayList<Long> guestTimeShiftOffsets = null;

    static Project curProj = null;

    private Preferences preferences = new Preferences();

    public Project() {

        guestVidPaths = new ArrayList<String>();
        guestDatasetPaths = new ArrayList<String>();
        guestFrames = new ArrayList<Rectangle>();
        guestTimeShiftOffsets = new ArrayList<Long>();
    }

    public static Project currentProject() {

        if (curProj == null)
            curProj = new Project();

        return curProj;
    }

    public Preferences getPreferences() {

        return preferences;
    }

    public Project loadProjectFromIniFile(File path) {

        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while((line = br.readLine()) != null) {
               // split for "="
                String[] parts = line.split("=", 2);

                if (parts[0].equals(ProjectIOStrings.hostVidPathString)) {
                    hostVidPath = parts[1];
                }
                else if (parts[0].equals(ProjectIOStrings.hostDatasetPathString)) {
                    hostDatasetPath = parts[1];
                }
                else if (parts[0].equals(ProjectIOStrings.hostFrameString)) {
                    Rectangle rectValue = getRect(parts[1]);
                    if (rectValue != null)
                        hostFrame = rectValue;
                }
                else if (parts[0].equals(ProjectIOStrings.guestVidPathsString)) {
                    String[] subParts = parts[1].substring(1, parts[1].length() - 1).split(", ");
                    for (int i = 0; i < subParts.length; i++) {
                        guestVidPaths.add(subParts[i]);
                    }
                }
                else if (parts[0].equals(ProjectIOStrings.guestDatasetPathsString)) {
                    String[] subParts = parts[1].substring(1, parts[1].length() - 1).split(", ");
                    for (int i = 0; i < subParts.length; i++) {
                        guestDatasetPaths.add(subParts[i]);
                    }
                }
                else if (parts[0].equals(ProjectIOStrings.guestFramesString)) {
                    String[] subParts = parts[1].substring(1, parts[1].length() - 1).split(", ");
                    for (int i = 0; i < subParts.length; i++) {
                        Rectangle rectValue = getRect(subParts[i]);
                        if (rectValue != null)
                            guestFrames.add(rectValue);
                    }
                }
                else if (parts[0].equals(ProjectIOStrings.guestTimeShiftOffsetsString)) {
                    String[] subParts = parts[1].substring(1, parts[1].length() - 1).split(", ");
                    for (int i = 0; i < subParts.length; i++) {
                        Long longValue = getLong(subParts[i]);
                        if (longValue != null)
                            guestTimeShiftOffsets.add(longValue);
                    }
                }

                else if (parts[0].equals(ProjectIOStrings.enableHeatMapOverlayString)) {
                    Boolean boolValue = getBool(parts[1]);
                    if (boolValue != null)
                        getPreferences().setEnableHeatMapOverlay(boolValue);
                }
                else if (parts[0].equals(ProjectIOStrings.heatMapOverlayPlayerString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setHeatMapOverlayPlayer(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.enableFixationOverlayString)) {
                    Boolean boolValue = getBool(parts[1]);
                    if (boolValue != null)
                        getPreferences().setEnableFixationOverlay(boolValue);
                }
                else if (parts[0].equals(ProjectIOStrings.fixationOverlayTimeSpanString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setFixationOverlayTimeSpan(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.enableRawDataOverlayString)) {
                    Boolean boolValue = getBool(parts[1]);
                    if (boolValue != null)
                        getPreferences().setEnableRawDataOverlay(boolValue);
                }
                else if (parts[0].equals(ProjectIOStrings.minDistPlotPlayer1String)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setMinDistPlotPlayer1(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.minDistPlotPlayer2String)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setMinDistPlotPlayer2(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.minDistPlotMinDistString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setMinDistPlotMinDist(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.minDistSubdivisionString)) {
                  Preferences.MinDistSubdivision subValue = getMinDistSubdivision(parts[1]);
                  if (subValue != null)
                      getPreferences().setMinDistSubdivision(subValue);
                }
                else if (parts[0].equals(ProjectIOStrings.minDistSubdivisionIntervalString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setMinDistSubdivisionInterval(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.filterVelocityThresholdString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setFilterVelocityThreshold(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.filterDistanceThresholdString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setFilterDistanceThreshold(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.heatMapGenSkipEventsString)) {
                    Boolean boolValue = getBool(parts[1]);
                    if (boolValue != null)
                        getPreferences().setHeatMapGenSkipEvents(boolValue);
                }
                else if (parts[0].equals(ProjectIOStrings.heatMapGenSkipPercentageString)) {
                    Float floatValue = getFloat(parts[1]);
                    if (floatValue != null)
                        getPreferences().setHeatMapGenSkipPercentage(floatValue);
                }
                else if (parts[0].equals(ProjectIOStrings.heatMapGenHeatRadiusString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setHeatMapGenHeatRadius(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.heatMapGenGenFromFrequencyInsteadString)) {
                    Boolean boolValue = getBool(parts[1]);
                    if (boolValue != null)
                        getPreferences().setHeatMapGenGenFromFrequencyInstead(boolValue);
                }
                else if (parts[0].equals(ProjectIOStrings.heatMapSourceString)) {
                    Preferences.HeatMapTimeSource heatmapValue = getHeatMapTimeSource(parts[1]);
                    if (heatmapValue != null)
                        getPreferences().setHeatMapSource(heatmapValue);
                }
                else if (parts[0].equals(ProjectIOStrings.heatmapTransparencyString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setHeatmapTransparency(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.histogramGridSizeString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setHistogramGridSize(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.histogramCorrelationThresholdString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setHistogramCorrelationThreshold(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.histogramDeviatingCellsThresholdString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setHistogramDeviatingCellsThreshold(intValue);
                }
                else if (parts[0].equals(ProjectIOStrings.prevFileDirectoryString)) {
                    getPreferences().setFileDirectory(parts[1]);
                }
                else if (parts[0].equals(ProjectIOStrings.colorPlayer1String)) {
                    Color colorValue = getColor(parts[1]);
                    if (colorValue != null)
                        getPreferences().setColorPlayer1(colorValue);
                }
                else if (parts[0].equals(ProjectIOStrings.colorPlayer2String)) {
                    Color colorValue = getColor(parts[1]);
                    if (colorValue != null)
                        getPreferences().setColorPlayer2(colorValue);
                }
                else if (parts[0].equals(ProjectIOStrings.colorMinDistOutsideDisplayString)) {
                    Color colorValue = getColor(parts[1]);
                    if (colorValue != null)
                        getPreferences().setColorMinDistOutsideDisplay(colorValue);
                }
                else if (parts[0].equals(ProjectIOStrings.colorMinDistOutsideBoardString)) {
                    Color colorValue = getColor(parts[1]);
                    if (colorValue != null)
                        getPreferences().setColorMinDistOutsideBoard(colorValue);
                }
                else if (parts[0].equals(ProjectIOStrings.colorMinDistCloseString)) {
                    Color colorValue = getColor(parts[1]);
                    if (colorValue != null)
                        getPreferences().setColorMinDistClose(colorValue);
                }
                else if (parts[0].equals(ProjectIOStrings.colorMinDistFarAwayString)) {
                    Color colorValue = getColor(parts[1]);
                    if (colorValue != null)
                        getPreferences().setColorMinDistFarAway(colorValue);
                }
                else if (parts[0].equals(ProjectIOStrings.colorMapString)) {
                    Integer intValue = getInt(parts[1]);
                    if (intValue != null)
                        getPreferences().setColorMap(intValue);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return curProj;
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

    private Float getFloat(String str) {
        float i;
        try {
            i = Float.parseFloat(str.trim());
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

    private Long getLong(String str) {
        long i;
        try {
            i = Long.parseLong(str.trim());
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

    private Boolean getBool(String str) {
        boolean i;
        try {
            i = Boolean.valueOf(str.trim());
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

    private Preferences.MinDistSubdivision getMinDistSubdivision(String str) {
        Preferences.MinDistSubdivision i;
        try {
            i = Preferences.MinDistSubdivision.valueOf(str.trim());
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

    private Preferences.HeatMapTimeSource getHeatMapTimeSource(String str) {
        Preferences.HeatMapTimeSource i;
        try {
            i = Preferences.HeatMapTimeSource.valueOf(str.trim());
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

    private Rectangle getRect(String str) {
        Rectangle i = null;
        try {
            Matcher m = Pattern.compile("\\d++").matcher(str); // java.awt.Rectangle[x=2,y=67,width=1010,height=1010]
            List<Integer> numbers = new ArrayList<Integer>();
            while(m.find()) {
                numbers.add(Integer.parseInt(m.group()));
            }
            i = new Rectangle(numbers.get(0), numbers.get(1), numbers.get(2), numbers.get(3));
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
            Matcher m = Pattern.compile("\\d++").matcher(str); // java.awt.Color[r=240,g=240,b=240]
            List<Integer> numbers = new ArrayList<Integer>();
            while(m.find()) {
                numbers.add(Integer.parseInt(m.group()));
            }
            i = new Color(numbers.get(0), numbers.get(1), numbers.get(2));
        }
        catch (Exception e)
        {
           return null;
        }
        return i;
    }

    public void saveProjectToIniFile(File file) {

        FileWriter writer;
        try {
            writer = new FileWriter(file, false);
            PrintWriter printer = new PrintWriter(writer);

            // save everything from project
            printer.append(ProjectIOStrings.hostVidPathString + "="
                    + hostVidPath + "\n");
            printer.append(ProjectIOStrings.hostDatasetPathString + "="
                    + hostDatasetPath + "\n");
            printer.append(ProjectIOStrings.hostFrameString + "="
                    + hostFrame + "\n");
            printer.append(ProjectIOStrings.guestVidPathsString + "="
                    + guestVidPaths + "\n");
            printer.append(ProjectIOStrings.guestDatasetPathsString + "="
                    + guestDatasetPaths + "\n");
            printer.append(ProjectIOStrings.guestFramesString + "="
                    + guestFrames + "\n");
            printer.append(ProjectIOStrings.guestTimeShiftOffsetsString + "="
                    + guestTimeShiftOffsets + "\n");

            // save everything from preferences
            printer.append(ProjectIOStrings.enableHeatMapOverlayString + "="
                    + getPreferences().getEnableHeatMapOverlay() + "\n");
            printer.append(ProjectIOStrings.heatMapOverlayPlayerString + "="
                    + getPreferences().getHeatMapOverlayPlayer() + "\n");
            printer.append(ProjectIOStrings.enableFixationOverlayString + "="
                    + getPreferences().getEnableFixationOverlay() + "\n");
            printer.append(ProjectIOStrings.fixationOverlayTimeSpanString + "="
                    + getPreferences().getFixationOverlayTimeSpan() + "\n");
            printer.append(ProjectIOStrings.enableRawDataOverlayString + "="
                    + getPreferences().getEnableRawDataOverlay() + "\n");
            printer.append(ProjectIOStrings.minDistPlotPlayer1String + "="
                    + getPreferences().getMinDistPlotPlayer1() + "\n");
            printer.append(ProjectIOStrings.minDistPlotPlayer2String + "="
                    + getPreferences().getMinDistPlotPlayer2() + "\n");
            printer.append(ProjectIOStrings.minDistPlotMinDistString + "="
                    + getPreferences().getMinDistPlotMinDist() + "\n");
            printer.append(ProjectIOStrings.minDistSubdivisionString + "="
                    + getPreferences().getMinDistSubdivision() + "\n");
            printer.append(ProjectIOStrings.minDistSubdivisionIntervalString + "="
                    + getPreferences().getMinDistSubdivisionInterval() + "\n");
            printer.append(ProjectIOStrings.filterVelocityThresholdString + "="
                    + getPreferences().getFilterVelocityThreshold() + "\n");
            printer.append(ProjectIOStrings.filterDistanceThresholdString + "="
                    + getPreferences().getFilterDistanceThreshold() + "\n");
            printer.append(ProjectIOStrings.heatMapGenSkipEventsString + "="
                    + getPreferences().getHeatMapGenSkipEvents() + "\n");
            printer.append(ProjectIOStrings.heatMapGenSkipPercentageString + "="
                    + getPreferences().getHeatMapGenSkipPercentage() + "\n");
            printer.append(ProjectIOStrings.heatMapGenHeatRadiusString + "="
                    + getPreferences().getHeatMapGenHeatRadius() + "\n");
            printer.append(ProjectIOStrings.heatMapGenGenFromFrequencyInsteadString + "="
                    + getPreferences().getHeatMapGenGenFromFrequencyInstead() + "\n");
            printer.append(ProjectIOStrings.heatMapSourceString + "="
                    + getPreferences().getHeatMapSource() + "\n");
            printer.append(ProjectIOStrings.heatmapTransparencyString + "="
                    + getPreferences().getHeatmapTransparency() + "\n");
            printer.append(ProjectIOStrings.histogramGridSizeString + "="
                    + getPreferences().getHistogramGridSize() + "\n");
            printer.append(ProjectIOStrings.histogramCorrelationThresholdString + "="
                    + getPreferences().getHistogramCorrelationThreshold() + "\n");
            printer.append(ProjectIOStrings.histogramDeviatingCellsThresholdString + "="
                    + getPreferences().getHistogramDeviatingCellsThreshold() + "\n");
            printer.append(ProjectIOStrings.prevFileDirectoryString + "="
                    + getPreferences().getFileDirectory() + "\n");
            printer.append(ProjectIOStrings.colorPlayer1String + "="
                    + getPreferences().getColorPlayer1() + "\n");
            printer.append(ProjectIOStrings.colorPlayer2String + "="
                    + getPreferences().getColorPlayer2() + "\n");
            printer.append(ProjectIOStrings.colorMinDistOutsideDisplayString + "="
                    + getPreferences().getColorMinDistOutsideDisplay() + "\n");
            printer.append(ProjectIOStrings.colorMinDistOutsideBoardString + "="
                    + getPreferences().getColorMinDistOutsideBoard() + "\n");
            printer.append(ProjectIOStrings.colorMinDistCloseString + "="
                    + getPreferences().getColorMinDistClose() + "\n");
            printer.append(ProjectIOStrings.colorMinDistFarAwayString + "="
                    + getPreferences().getColorMinDistFarAway() + "\n");
            printer.append(ProjectIOStrings.colorMapString + "="
                    + getPreferences().getColorMap() + "\n");

            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Project loadProjectFromFile(File path) {

        Project proj = new Project();
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);
            proj = (Project) ois.readObject();
        } catch (InvalidClassException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        curProj = proj;
        return proj;
    }

    public static void saveProjectToFile(File path) {

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(curProj);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
