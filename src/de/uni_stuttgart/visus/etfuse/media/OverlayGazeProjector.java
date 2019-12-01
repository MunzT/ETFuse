package de.uni_stuttgart.visus.etfuse.media;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.opencv.core.Mat;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.gui.surface.VideoSurfacePanel;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.misc.Preferences.HeatMapTimeSource;
import de.uni_stuttgart.visus.etfuse.misc.Utils;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class OverlayGazeProjector {

    private EyeTrackerRecording recording;
    private VideoSurfacePanel vidFramePanel = null;
    private int lastSearchPointer;
    private long lastSearchTimestamp;
    private long timeSyncOffset;

    // Map for heatmaps:
    // outer map: heatmap type: region, click, intervals
    // inner map: id of map
    private HashMap<HeatMapTimeSource, HashMap<Long, Mat> >rawHeatMaps =
            new HashMap<HeatMapTimeSource, HashMap<Long, Mat>>();
    private HashMap<HeatMapTimeSource, HashMap<Long, Mat>> standardHeatMaps =
            new HashMap<HeatMapTimeSource, HashMap<Long, Mat>>();
    private HashMap<HeatMapTimeSource, HashMap<Long, Mat>> transparentHeatMaps =
            new HashMap<HeatMapTimeSource, HashMap<Long, Mat>>();
    private HashMap<HeatMapTimeSource, HashMap<Long, Mat>> uniColorTansparentHeatMaps =
            new HashMap<HeatMapTimeSource, HashMap<Long, Mat>>();
    private Boolean heatMapIsBeingGenerated = false;

    public OverlayGazeProjector(EyeTrackerRecording recording, VideoSurfacePanel vidFramePanel) {

        this.recording = recording;
        this.vidFramePanel = vidFramePanel;

        lastSearchPointer = 0;
        lastSearchTimestamp = 0;
        timeSyncOffset = 0; // milliseconds

        // prepare array lists for region, clicks and intervals
        rawHeatMaps.put(HeatMapTimeSource.USERDEFINED, new HashMap<Long, Mat>());
        rawHeatMaps.put(HeatMapTimeSource.CLICKS, new HashMap<Long, Mat>());
        rawHeatMaps.put(HeatMapTimeSource.TIMEINTERVALS, new HashMap<Long, Mat>());
        standardHeatMaps.put(HeatMapTimeSource.USERDEFINED, new HashMap<Long, Mat>());
        standardHeatMaps.put(HeatMapTimeSource.CLICKS, new HashMap<Long, Mat>());
        standardHeatMaps.put(HeatMapTimeSource.TIMEINTERVALS, new HashMap<Long, Mat>());
        transparentHeatMaps.put(HeatMapTimeSource.USERDEFINED, new HashMap<Long, Mat>());
        transparentHeatMaps.put(HeatMapTimeSource.CLICKS, new HashMap<Long, Mat>());
        transparentHeatMaps.put(HeatMapTimeSource.TIMEINTERVALS, new HashMap<Long, Mat>());
        uniColorTansparentHeatMaps.put(HeatMapTimeSource.USERDEFINED, new HashMap<Long, Mat>());
        uniColorTansparentHeatMaps.put(HeatMapTimeSource.CLICKS, new HashMap<Long, Mat>());
        uniColorTansparentHeatMaps.put(HeatMapTimeSource.TIMEINTERVALS, new HashMap<Long, Mat>());
    }

    public EyeTrackerRecording getRecording() {

        return this.recording;
    }

    public long getTimeSyncOffset() {

        return this.timeSyncOffset;
    }

    public void setTimeSyncShift(long shift) {

        this.timeSyncOffset = shift;
    }

    // Transform Points
    public int transformRawPointsToTarget(EyeTrackerRecording target)  {

        if (target == null || target == this.recording)
            return -1;

        Line2D sourceFrame = new Line2D.Double(this.recording.getFramePoint1().x,
                this.recording.getFramePoint1().y, this.recording.getFramePoint2().x,
                this.recording.getFramePoint2().y);
        Line2D targetFrame = new Line2D.Double(target.getFramePoint1().x, target.getFramePoint1().y,
                target.getFramePoint2().x, target.getFramePoint2().y);

        for (EyeTrackerEyeEvent event : this.recording.getRawEyeEvents()) {

            Point eventPoint = new Point(event.fixationPointX, event.fixationPointY);
            Utils.transformCoordinate(eventPoint, sourceFrame, targetFrame);

            event.fixationPointX = eventPoint.x;
            event.fixationPointY = eventPoint.y;
        }

        System.out.println("<OverlayGazeProjector> Raw data coordinates transformed");

        return 0;
    }

    public int transformFilteredPointsToTarget(EyeTrackerRecording target)  {

        if (target == null || target == this.recording)
            return -1;

        Line2D sourceFrame = new Line2D.Double(this.recording.getFramePoint1().x,
                this.recording.getFramePoint1().y, this.recording.getFramePoint2().x,
                this.recording.getFramePoint2().y);
        Line2D targetFrame = new Line2D.Double(target.getFramePoint1().x, target.getFramePoint1().y,
                target.getFramePoint2().x, target.getFramePoint2().y);

        for (EyeTrackerEyeEvent event : this.recording.getFilteredEyeEvents()) {

            Point eventPoint = new Point(event.fixationPointX, event.fixationPointY);
            Utils.transformCoordinate(eventPoint, sourceFrame, targetFrame);

            event.fixationPointX = eventPoint.x;
            event.fixationPointY = eventPoint.y;
        }

        System.out.println("<OverlayGazeProjector> fixation coordinates transformed");

        return 0;
    }

    public ArrayList<EyeTrackerEyeEvent> xEventsBeforeShiftedTimestamp(int amount, long timestamp,
            Boolean fromRawData, Boolean includeEyesNotFound) {

        long projectedTime = timestamp + this.timeSyncOffset;
        return this.getRecording().xEventsBeforeTimestamp(amount, projectedTime, fromRawData,
                includeEyesNotFound);
    }

    public ArrayList<EyeTrackerEyeEvent> eventsBetweenShiftedTimestamps(long timestampStart,
            long timestampEnd, Boolean fromRawData, Boolean includeEyesNotFound) {

        long projectedStart = timestampStart + this.timeSyncOffset;
        long projectedEnd = timestampEnd + this.timeSyncOffset;

        return this.getRecording().eventsBetweenTimestamps(projectedStart, projectedEnd,
                fromRawData, includeEyesNotFound);
    }

    public void setRawHeatMap(Mat heatMap, long id, HeatMapTimeSource type) {
        this.rawHeatMaps.get(type).put(id, heatMap);
    }

    public Mat getCurrentRawHeatMap() {
        return getRawHeatMap(determineCurrentHeatmapId(),
                Project.currentProject().getPreferences().getHeatMapSource());
    }

    public Mat getRawHeatMap(long id, HeatMapTimeSource type) {
        if (this.rawHeatMaps.containsKey(type))
            return this.rawHeatMaps.get(type).get(id);
        return null;
    }

    public void setNormalizedHeatMap(Mat heatMap, long id, HeatMapTimeSource type) {
        this.standardHeatMaps.get(type).put(id, heatMap);
    }

    public Mat getCurrentNormalizedHeatMap() {
        return getNormalizedHeatMap(determineCurrentHeatmapId(),
                Project.currentProject().getPreferences().getHeatMapSource());
    }

    public Mat getNormalizedHeatMap(long id, HeatMapTimeSource type) {
        if (this.standardHeatMaps.containsKey(type))
            return this.standardHeatMaps.get(type).get(id);
        return null;
    }

    public void setTransparentHeatMap(Mat heatMap, long id, HeatMapTimeSource type) {
        this.transparentHeatMaps.get(type).put(id, heatMap);
    }

    public Mat getTransparentHeatMap(long id, HeatMapTimeSource type) {
        if (this.transparentHeatMaps.containsKey(type))
            return this.transparentHeatMaps.get(type).get(id);
        return null;
    }

    public Mat getCurrentTransparentHeatMap() {
        return getTransparentHeatMap(determineCurrentHeatmapId(),
                Project.currentProject().getPreferences().getHeatMapSource());
    }

    public void setUniColorTransparentHeatMap(Mat heatMap, long id, HeatMapTimeSource type) {
        this.uniColorTansparentHeatMaps.get(type).put(id, heatMap);
    }

    public Mat getUniColorTransparentHeatMap(long id, HeatMapTimeSource type) {
        if (this.uniColorTansparentHeatMaps.containsKey(type))
            return this.uniColorTansparentHeatMaps.get(type).get(id);
        return null;
    }

    public Mat getCurrentUniColorTransparentHeatMap() {
        return getUniColorTransparentHeatMap(determineCurrentHeatmapId(),
                Project.currentProject().getPreferences().getHeatMapSource());
    }

    public void setIsHeatMapBeingGenerated(Boolean heatMapIsBeingGenerated) {
        this.heatMapIsBeingGenerated = heatMapIsBeingGenerated;
    }

    public Boolean isHeatMapBeingGenerated() {
        return this.heatMapIsBeingGenerated;
    }

    private int determineCurrentHeatmapId() {
        if (Project.currentProject().getPreferences().getHeatMapSource()
                == Preferences.HeatMapTimeSource.USERDEFINED) {
            return 0;
        }
        else { // depends on mouse click
            long currentTime = this.vidFramePanel.getCurrentTime();
            ArrayList<Long> clicks = this.vidFramePanel.getHeatmapEvents();

            int mapIndex = 0;
            for (int i = 0; i < clicks.size(); i++)
            {
                if (currentTime >= clicks.get(i)) {
                    mapIndex++;
                }
                else {
                    return mapIndex;
                }
            }
            return mapIndex;
        }
    }
}
