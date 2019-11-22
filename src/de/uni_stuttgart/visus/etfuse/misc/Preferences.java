package de.uni_stuttgart.visus.etfuse.misc;

import java.awt.Color;
import java.io.Serializable;

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
       MINAREAS, CLICKS;
    }
    private MinDistSubdivision minDistSubdivision = MinDistSubdivision.CLICKS;

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
       USERDEFINED, CLICKS;
    }
    private HeatMapTimeSource heatMapSource = HeatMapTimeSource.CLICKS;
    private int heatmapTransparency = 128;

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
    private Color colorMinDistOutsideDisplay = new Color(150, 150, 150);
    private Color colorMinDistOutsideBoard = new Color(61, 1, 76);
    private Color colorMinDistClose = new Color(255, 234, 0);
    private Color colorMinDistFarAway = new Color(40, 161, 151);

    // Colormap
    private int colorMap = Imgproc.COLORMAP_MAGMA;

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
}
