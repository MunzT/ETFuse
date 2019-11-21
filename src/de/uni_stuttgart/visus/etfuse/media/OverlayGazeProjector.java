package de.uni_stuttgart.visus.etfuse.media;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Mat;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.gui.surface.VideoSurfacePanel;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.misc.Utils;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class OverlayGazeProjector {

    private EyeTrackerRecording recording;
    private VideoSurfacePanel vidFramePanel = null;
    private int lastSearchPointer;
    private long lastSearchTimestamp;
    private long timeSyncOffset;

    // Lists of heatmaps:
    // index 0: for selected region (defined by slider)
    // from index 1: heatmaps for regions defined by mousen clicks
    // index 1: region before first click
    // index 1 - #clicks + 1: region between two clicks
    // index #clicks + 2: region after last click
    private Map<Long, Mat> rawHeatMaps = new HashMap<Long, Mat>();
    private Map<Long, Mat> standardHeatMaps = new HashMap<Long, Mat>();
    private Map<Long, Mat> transparentHeatMaps = new HashMap<Long, Mat>();
    private Boolean heatMapIsBeingGenerated = false;

    public OverlayGazeProjector(EyeTrackerRecording recording, VideoSurfacePanel vidFramePanel) {

        this.recording = recording;
        this.vidFramePanel = vidFramePanel;

        lastSearchPointer = 0;
        lastSearchTimestamp = 0;
        timeSyncOffset = 0; // milliseconds
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

    public void setRawHeatMap(Mat heatMap, long id) {
        this.rawHeatMaps.put(id, heatMap);
    }

    public Mat getCurrentRawHeatMap() {
        return getRawHeatMap(determineCurrentHeatmapId());
    }

    public Mat getRawHeatMap(long id) {
        if (this.rawHeatMaps.containsKey(id))
            return this.rawHeatMaps.get(id);
        return null;
    }

    public void setNormalizedHeatMap(Mat heatMap, long id) {
        this.standardHeatMaps.put(id, heatMap);
    }

    public Mat getCurrentNormalizedHeatMap() {
        return getNormalizedHeatMap(determineCurrentHeatmapId());
    }

    public Mat getNormalizedHeatMap(long id) {
        if (this.standardHeatMaps.containsKey(id))
            return this.standardHeatMaps.get(id);
        return null;
    }

    public void setTransparentHeatMap(Mat heatMap, long id) {
        this.transparentHeatMaps.put(id, heatMap);
    }

    public Mat getTransparentHeatMap(long id) {
        if (this.transparentHeatMaps.containsKey(id))
            return this.transparentHeatMaps.get(id);
        return null;
    }

    public Mat getCurrentTransparentHeatMap() {
        return getTransparentHeatMap(determineCurrentHeatmapId());
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
            ArrayList<Long> clicks = this.vidFramePanel.getClicks();

            int mapIndex = 1;
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
