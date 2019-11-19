package de.uni_stuttgart.visus.etfuse.misc;

import java.io.Serializable;

public class Preferences implements Serializable {

    private static final long serialVersionUID = -3052803564035377548L;
    
    // Overlay
    private Boolean enableHeatMapOverlay = false;
    private int heatMapOverlayPlayer = 0;
    private Boolean enableFixationOverlay = true;
    private int fixationOverlayTimeSpan = 500;
    private Boolean enableRawDataOverlay = false;

    // MinDist-Plot
    private int minDistPlotPlayer1 = 0;
    private int minDistPlotPlayer2 = 0;
    private int minDistPlotMinDist = 150;
    
    // Gaze-Filter
    private int filterVelocityThreshold = 20;
    private int filterDistanceThreshold = 35;
    
    // Heatmap-Generator
    private Boolean heatMapGenSkipEvents = false;
    private float heatMapGenSkipPercentage = 0.05f;
    private int heatMapGenHeatRadius = 80;
    private Boolean heatMapGenGenFromFrequencyInstead = false;
    
    // Temp Sync
    private int histogramGridSize = 16;
    private int histogramCorrelationThreshold = 40;
    private int histogramDeviatingCellsThreshold = 1;
    
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
}
