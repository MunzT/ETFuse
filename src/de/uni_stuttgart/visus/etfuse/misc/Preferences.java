package de.uni_stuttgart.visus.etfuse.misc;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import org.opencv.imgproc.Imgproc;

public class Preferences implements Serializable {

    private static final long serialVersionUID = -3052803564035377548L;

    // Overlay
    private Boolean enableHeatMapOverlay = true;
    private int heatMapOverlayPlayer = 0;
    private Boolean enableFixationOverlay = true;
    private int fixationOverlayTimeSpan = 500;
    private Boolean enableRawDataOverlay = true;

    // MinDist-Plot
    private int minDistPlotPlayer1 = 0;
    private int minDistPlotPlayer2 = 0;
    private int minDistPlotMinDist = 200;

    public enum MinDistSubdivision
    {
       MINAREAS, CLICKS, TIMEINTERVALS;
    }
    private MinDistSubdivision minDistSubdivision = MinDistSubdivision.CLICKS;
    private int minDistSubdivisionInterval = 1000;

    // Gaze-Filter
    private int filterVelocityThreshold = 20;
    private int filterDistanceThreshold = 35;

    // Heatmap-Generator
    private Boolean heatMapGenSkipEvents = false;
    private float heatMapGenSkipPercentage = 0.05f;
    private int heatMapGenHeatRadius = 80;
    private Boolean heatMapGenGenFromFrequencyInstead = false;

    public enum HeatMapTimeSource
    {
       USERDEFINED, CLICKS, TIMEINTERVALS; // TODO TIMEINTERVALS not used yet
    }
    private HeatMapTimeSource heatMapSource = HeatMapTimeSource.CLICKS;
    private int heatmapTransparency = 50;
    private int videoTransparency = 50;

    // Temp Sync
    private int histogramGridSize = 16;
    private int histogramCorrelationThreshold = 40;
    private int histogramDeviatingCellsThreshold = 1;

    // File path for file dialog
    private String prevFileDirectory = "";

    // Colors for fixations
    private Color colorPlayer1 = new Color(0, 0, 0);
    private Color colorPlayer2 = new Color(240, 240, 240);

    // Colors for MinDist plot
    private Color colorMinDistOutsideDisplay = new Color(218, 218, 218);
    private Color colorMinDistOutsideBoard = new Color(166, 166, 166);
    private Color colorMinDistClose = new Color(255, 195, 0);
    private Color colorMinDistFarAway = new Color(255, 255, 167);

    // Colormap
    private int colorMap = Imgproc.COLORMAP_INFERNO;

    // Events
    private Boolean showAdditionalEventTicks = true;
    private ArrayList<Integer> showPlayerEventTicks = new ArrayList<Integer>();
    private Boolean useAdditionalEventForMinDistPlot = true;
    private ArrayList<Integer> playerEventsForMinDistPlot = new ArrayList<Integer>();
    private Boolean useAdditionalEventForHeatmaps = true;
    private ArrayList<Integer> playerEventsForHeatmaps = new ArrayList<Integer>();

    private Color heatmapColorPlayer1 = new Color(0, 0, 255);
    private Color heatmapColorPlayer2 = new Color(255, 0, 0);

    public Boolean getEnableHeatMapOverlay() {
        return enableHeatMapOverlay;
    }
    public void setEnableHeatMapOverlay(Boolean enableHeatMapOverlay) {
        this.enableHeatMapOverlay = enableHeatMapOverlay;
    }
    public int getHeatMapOverlayPlayer() {
        return heatMapOverlayPlayer;
    }
    public void setHeatMapOverlayPlayer(int heatMapOverlayPlayer) {
        this.heatMapOverlayPlayer = heatMapOverlayPlayer;
    }
    public Boolean getEnableFixationOverlay() {
        return enableFixationOverlay;
    }
    public void setEnableFixationOverlay(Boolean enableFixationOverlay) {
        this.enableFixationOverlay = enableFixationOverlay;
    }
    public int getFixationOverlayTimeSpan() {
        return fixationOverlayTimeSpan;
    }
    public void setFixationOverlayTimeSpan(int fixationOverlayTimeSpan) {
        this.fixationOverlayTimeSpan = fixationOverlayTimeSpan;
    }
    public Boolean getEnableRawDataOverlay() {
        return enableRawDataOverlay;
    }
    public void setEnableRawDataOverlay(Boolean enableRawDataOverlay) {
        this.enableRawDataOverlay = enableRawDataOverlay;
    }
    public int getMinDistPlotPlayer1() {
        return minDistPlotPlayer1;
    }
    public void setMinDistPlotPlayer1(int minDistPlotPlayer1) {
        this.minDistPlotPlayer1 = minDistPlotPlayer1;
    }
    public int getMinDistPlotPlayer2() {
        return minDistPlotPlayer2;
    }
    public void setMinDistPlotPlayer2(int minDistPlotPlayer2) {
        this.minDistPlotPlayer2 = minDistPlotPlayer2;
    }
    public int getMinDistPlotMinDist() {
        return minDistPlotMinDist;
    }
    public void setMinDistPlotMinDist(int minDistPlotMinDist) {
        this.minDistPlotMinDist = minDistPlotMinDist;
    }
    public MinDistSubdivision getMinDistSubdivision() {
        return minDistSubdivision;
    }
    public void setMinDistSubdivision(MinDistSubdivision minDistSubdivision) {
        this.minDistSubdivision = minDistSubdivision;
    }
    public int getMinDistSubdivisionInterval() {
        return minDistSubdivisionInterval;
    }
    public void setMinDistSubdivisionInterval(int minDistSubdivisionInterval) {
        this.minDistSubdivisionInterval = minDistSubdivisionInterval;
    }
    public int getFilterVelocityThreshold() {
        return filterVelocityThreshold;
    }
    public void setFilterVelocityThreshold(int filterVelocityThreshold) {
        this.filterVelocityThreshold = filterVelocityThreshold;
    }
    public int getFilterDistanceThreshold() {
        return filterDistanceThreshold;
    }
    public void setFilterDistanceThreshold(int filterDistanceThreshold) {
        this.filterDistanceThreshold = filterDistanceThreshold;
    }
    public Boolean getHeatMapGenSkipEvents() {
        return heatMapGenSkipEvents;
    }
    public void setHeatMapGenSkipEvents(Boolean heatMapGenSkipEvents) {
        this.heatMapGenSkipEvents = heatMapGenSkipEvents;
    }
    public float getHeatMapGenSkipPercentage() {
        return heatMapGenSkipPercentage;
    }
    public void setHeatMapGenSkipPercentage(float heatMapGenSkipPercentage) {
        this.heatMapGenSkipPercentage = heatMapGenSkipPercentage;
    }
    public int getHeatMapGenHeatRadius() {
        return heatMapGenHeatRadius;
    }
    public void setHeatMapGenHeatRadius(int heatMapGenHeatRadius) {
        this.heatMapGenHeatRadius = heatMapGenHeatRadius;
    }
    public Boolean getHeatMapGenGenFromFrequencyInstead() {
        return heatMapGenGenFromFrequencyInstead;
    }
    public void setHeatMapGenGenFromFrequencyInstead(Boolean heatMapGenGenFromFrequencyInstead) {
        this.heatMapGenGenFromFrequencyInstead = heatMapGenGenFromFrequencyInstead;
    }
    public HeatMapTimeSource getHeatMapSource() {
        return heatMapSource;
    }
    public void setHeatMapSource(HeatMapTimeSource heatMapSource) {
        this.heatMapSource = heatMapSource;
    }
    public void setHeatmapTransparency(int heatmapTransparency) {
        this.heatmapTransparency = heatmapTransparency;
    }
    public int getHeatmapTransparency() {
        return this.heatmapTransparency;
    }
    public void setVideoTransparency(int videoTransparency) {
        this.videoTransparency = videoTransparency;
    }
    public int getVideoTransparency() {
        return this.videoTransparency;
    }
    public int getHistogramGridSize() {
        return histogramGridSize;
    }
    public void setHistogramGridSize(int histogramGridSize) {
        this.histogramGridSize = histogramGridSize;
    }
    public int getHistogramCorrelationThreshold() {
        return histogramCorrelationThreshold;
    }
    public void setHistogramCorrelationThreshold(int histogramCorrelationThreshold) {
        this.histogramCorrelationThreshold = histogramCorrelationThreshold;
    }
    public int getHistogramDeviatingCellsThreshold() {
        return histogramDeviatingCellsThreshold;
    }
    public void setHistogramDeviatingCellsThreshold(int histogramDeviatingCellsThreshold) {
        this.histogramDeviatingCellsThreshold = histogramDeviatingCellsThreshold;
    }
    public String getFileDirectory() {
        return this.prevFileDirectory;
    }
    public void setFileDirectory(String prevFileDirectory) {
        this.prevFileDirectory = prevFileDirectory;
    }
    public Color getColorPlayer1() {
        return this.colorPlayer1;
    }
    public void setColorPlayer1(Color colorPlayer1) {
        this.colorPlayer1 = colorPlayer1;
    }
    public Color getColorPlayer2() {
        return this.colorPlayer2;
    }
    public void setColorPlayer2(Color colorPlayer2) {
        this.colorPlayer2 = colorPlayer2;
    }
    public Color getColorMinDistOutsideDisplay() {
        return this.colorMinDistOutsideDisplay;
    }
    public void setColorMinDistOutsideDisplay(Color colorMinDistOutsideDisplay) {
        this.colorMinDistOutsideDisplay = colorMinDistOutsideDisplay;
    }
    public Color getColorMinDistOutsideBoard() {
        return this.colorMinDistOutsideBoard;
    }
    public void setColorMinDistOutsideBoard(Color colorMinDistOutsideBoard) {
        this.colorMinDistOutsideBoard = colorMinDistOutsideBoard;
    }
    public Color getColorMinDistClose() {
        return this.colorMinDistClose;
    }
    public void setColorMinDistClose(Color colorMinDistClose) {
        this.colorMinDistClose = colorMinDistClose;
    }
    public Color getColorMinDistFarAway() {
        return this.colorMinDistFarAway;
    }
    public void setColorMinDistFarAway(Color colorMinDistFarAway) {
        this.colorMinDistFarAway = colorMinDistFarAway;
    }
    public int getColorMap() {
        return this.colorMap;
    }
    public void setColorMap(int colorMap) {
        this.colorMap = colorMap;
    }

    public Boolean getShowAdditionalEventTicks() {
        return this.showAdditionalEventTicks;
    }
    public void setShowAdditionalEventTicks(Boolean showAdditionalEventTicks) {
        this.showAdditionalEventTicks = showAdditionalEventTicks;
    }
    public ArrayList<Integer> getShowPlayerEventTicks() {
        return this.showPlayerEventTicks;
    }
    public void setShowPlayerEventTicks(ArrayList<Integer> showPlayerEventTicks) {
        this.showPlayerEventTicks = showPlayerEventTicks;
    }
    public Boolean getUseAdditionalEventForMinDistPlot() {
        return this.useAdditionalEventForMinDistPlot;
    }
    public void setUseAdditionalEventForMinDistPlot(Boolean useAdditionalEventForMinDistPlot) {
        this.useAdditionalEventForMinDistPlot = useAdditionalEventForMinDistPlot;
    }
    public ArrayList<Integer> getPlayerEventsForMinDistPlot() {
        return this.playerEventsForMinDistPlot;
    }
    public void setPlayerEventsForMinDistPlot(ArrayList<Integer> playerEventsForMinDistPlot) {
        this.playerEventsForMinDistPlot = playerEventsForMinDistPlot;
    }
    public Boolean getUseAdditionalEventForHeatmaps() {
        return this.useAdditionalEventForHeatmaps;
    }
    public void setUseAdditionalEventForHeatmaps(Boolean useAdditionalEventForHeatmaps) {
        this.useAdditionalEventForHeatmaps = useAdditionalEventForHeatmaps;
    }
    public ArrayList<Integer> getPlayerEventsForHeatmaps() {
        return this.playerEventsForHeatmaps;
    }
    public void setPlayerEventsForHeatmaps(ArrayList<Integer> playerEventsForHeatmaps) {
        this.playerEventsForHeatmaps = playerEventsForHeatmaps;
    }
    public Color getHeatmapColorPlayer1() {
        return this.heatmapColorPlayer1;
    }
    public void setHeatmapColorPlayer1(Color heatmapColorPlayer1) {
        this.heatmapColorPlayer1 = heatmapColorPlayer1;
    }
    public Color getHeatmapColorPlayer2() {
        return this.heatmapColorPlayer2;
    }
    public void setHeatmapColorPlayer2(Color heatmapColorPlayer2) {
        this.heatmapColorPlayer2 = heatmapColorPlayer2;
    }

}
