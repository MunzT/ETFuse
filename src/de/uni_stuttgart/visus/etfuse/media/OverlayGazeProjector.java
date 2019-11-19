package de.uni_stuttgart.visus.etfuse.media;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import org.opencv.core.Mat;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.misc.Utils;

public class OverlayGazeProjector {

    private EyeTrackerRecording recording;
    private int lastSearchPointer;
    private long lastSearchTimestamp;
    private long timeSyncOffset;
    private Mat rawHeatMap;
    private Mat standardHeatMap;
    private Mat transparentHeatMap;
    private Boolean heatMapIsBeingGenerated = false;

    public OverlayGazeProjector(EyeTrackerRecording recording) {

        this.recording = recording;

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

        Line2D sourceFrame = new Line2D.Double(this.recording.getFramePoint1().x, this.recording.getFramePoint1().y, this.recording.getFramePoint2().x, this.recording.getFramePoint2().y);
        Line2D targetFrame = new Line2D.Double(target.getFramePoint1().x, target.getFramePoint1().y, target.getFramePoint2().x, target.getFramePoint2().y);

        for (EyeTrackerEyeEvent event : this.recording.getRawEyeEvents()) {

            Point eventPoint = new Point(event.fixationPointX, event.fixationPointY);
            Utils.transformCoordinate(eventPoint, sourceFrame, targetFrame);

            event.fixationPointX = eventPoint.x;
            event.fixationPointY = eventPoint.y;
        }

        System.out.println("<OverlayGazeProjector> Rohdaten-Koordinaten transformiert");

        return 0;
    }

    public int transformFilteredPointsToTarget(EyeTrackerRecording target)  {

        if (target == null || target == this.recording)
            return -1;

        Line2D sourceFrame = new Line2D.Double(this.recording.getFramePoint1().x, this.recording.getFramePoint1().y, this.recording.getFramePoint2().x, this.recording.getFramePoint2().y);
        Line2D targetFrame = new Line2D.Double(target.getFramePoint1().x, target.getFramePoint1().y, target.getFramePoint2().x, target.getFramePoint2().y);

        for (EyeTrackerEyeEvent event : this.recording.getFilteredEyeEvents()) {

            Point eventPoint = new Point(event.fixationPointX, event.fixationPointY);
            Utils.transformCoordinate(eventPoint, sourceFrame, targetFrame);

            event.fixationPointX = eventPoint.x;
            event.fixationPointY = eventPoint.y;
        }

        System.out.println("<OverlayGazeProjector> Fixationen-Koordinaten transformiert");

        return 0;
    }

    public ArrayList<EyeTrackerEyeEvent> xEventsBeforeShiftedTimestamp(int amount, long timestamp, Boolean fromRawData, Boolean includeEyesNotFound) {

        long projectedTime = timestamp + this.timeSyncOffset;
        return this.getRecording().xEventsBeforeTimestamp(amount, projectedTime, fromRawData, includeEyesNotFound);
    }

    public ArrayList<EyeTrackerEyeEvent> eventsBetweenShiftedTimestamps(long timestampStart, long timestampEnd, Boolean fromRawData, Boolean includeEyesNotFound) {

        long projectedStart = timestampStart + this.timeSyncOffset;
        long projectedEnd = timestampEnd + this.timeSyncOffset;

        return this.getRecording().eventsBetweenTimestamps(projectedStart, projectedEnd, fromRawData, includeEyesNotFound);
    }

    public void setRawHeatMap(Mat heatMap) {

        this.rawHeatMap = heatMap;
    }

    public Mat getRawHeatMap() {

        return this.rawHeatMap;
    }

    public void setNormalizedHeatMap(Mat heatMap) {

        this.standardHeatMap = heatMap;
    }

    public Mat getNormalizedHeatMap() {

        return this.standardHeatMap;
    }

    public void setTransparentHeatMap(Mat heatMap) {

        this.transparentHeatMap = heatMap;
    }

    public Mat getTransparentHeatMap() {

        return this.transparentHeatMap;
    }

    public Boolean isHeatMapBeingGenerated() {
        return heatMapIsBeingGenerated;
    }

    public void setIsHeatMapBeingGenerated(Boolean heatMapIsBeingGenerated) {
        this.heatMapIsBeingGenerated = heatMapIsBeingGenerated;
    }
}
