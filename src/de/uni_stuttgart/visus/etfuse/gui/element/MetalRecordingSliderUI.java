package de.uni_stuttgart.visus.etfuse.gui.element;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalSliderUI;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class MetalRecordingSliderUI extends MetalSliderUI {

    private OverlayGazeProjector hostProj = null;
    private OverlayGazeProjector guestProj = null;
    private VideoFrame vidFrame = null;
    private int minDistance = 0;
    private int safeLength = 0;

    public MetalRecordingSliderUI() {

        super();
        this.safeLength = ((Integer) UIManager.get("Slider.majorTickLength")).intValue();
    }

    protected void setMinDistanceRecordingsToDraw(VideoFrame vidFrame,
            OverlayGazeProjector hostProjector, OverlayGazeProjector guestProjector, int minDistance) {

        this.vidFrame = vidFrame;
        this.hostProj = hostProjector;
        this.guestProj = guestProjector;
        this.minDistance = minDistance;
    }

    @Override
    public void paintTicks(Graphics g) {

        Preferences prefs = Project.currentProject().getPreferences();

        if (vidFrame == null)
            return;

        if (vidFrame.getHostProjector() == null)
            return;

        if (vidFrame.getHostProjector().getRecording() == null)
            return;

        Point hostFramePoint1 = vidFrame.getHostProjector().getRecording().getFramePoint1();
        Point hostFramePoint2 = vidFrame.getHostProjector().getRecording().getFramePoint2();

        if (hostProj == null || guestProj == null ||
            hostProj.getRecording().getFilteredEyeEvents().size() < 1 ||
            guestProj.getRecording().getFilteredEyeEvents().size() < 1 ||
            hostProj.getRecording().getRawEyeEvents().size() < 1 ||
            guestProj.getRecording().getRawEyeEvents().size() < 1 ||
            hostFramePoint1 == null || hostFramePoint2 == null)
            return;

        long startTS = vidFrame.getHostProjector().getRecording().getRawEyeEvents().get(0).timestamp;
        long endTS =  vidFrame.getHostProjector().getRecording().getRawEyeEvents().get(
                vidFrame.getHostProjector().getRecording().getRawEyeEvents().size() - 1).timestamp;

        Rectangle tickBounds = tickRect;

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            g.translate(0, tickBounds.y);

            for (int x = 0; x < trackRect.width; x++) {

                int xPos = x + trackRect.x;
                double progress = ((double) x) / trackRect.width;
                long progressTS = Math.round(startTS + (progress * (endTS - startTS)));

                // progressStartTS und progressEndTS markieren den Bereich aller Punkte, die am nächsten an progressTS liegen
                // .     -----.-----ooooo.ooooo     .     # - bzw. o markiert Bereich, . ist progressTS für Pixel des Tracks
                long progressStartTS = startTS;
                if (x > 0) {
                    progressStartTS = Math.round(startTS + ((((double) x - 1) / trackRect.width)
                            * (endTS - startTS)));
                    progressStartTS = Math.round((progressStartTS + progressTS) * 0.5);
                }

                long progressEndTS = endTS;
                if (x < trackRect.width - 1) {
                    progressEndTS = Math.round(startTS + ((((double) x + 1) / trackRect.width)
                            * (endTS - startTS)));
                    progressEndTS = Math.round((progressEndTS + progressTS) * 0.5);
                }

                ArrayList<EyeTrackerEyeEvent> hostEvents =
                        hostProj.eventsBetweenShiftedTimestamps(progressStartTS, progressEndTS, true, false);
                ArrayList<EyeTrackerEyeEvent> guestEvents =
                        guestProj.eventsBetweenShiftedTimestamps(progressStartTS, progressEndTS, true, false);

                if (hostEvents == null || hostEvents.size() < 1 || guestEvents == null || guestEvents.size() < 1)
                    g.setColor(prefs.getColorMinDistOutsideDisplay());
                else {

                    int belowMinDistanceCounter = 0;
                    int aboveMinDistanceCounter = 0;
                    int notContainedInRectCounter = 0;
                    int guestIndex = 0;

                    for (EyeTrackerEyeEvent hostEvent : hostEvents) {

                        if (!hostEvent.containedInRecFrame(hostFramePoint1, hostFramePoint2)) {
                            notContainedInRectCounter++;
                            continue;
                        }

                        long tsHost = hostEvent.timestamp - hostProj.getTimeSyncOffset();

                        while (guestIndex < guestEvents.size()) {

                            EyeTrackerEyeEvent guestEvent = guestEvents.get(guestIndex);

                            // TODO: nicht immer mit allen gast-events vergleichen
                            if ((guestEvent.timestamp - guestProj.getTimeSyncOffset()) < tsHost)
                                break;

                            if (!guestEvent.containedInRecFrame(hostFramePoint1, hostFramePoint2)) {
                                notContainedInRectCounter++;
                            }
                            else {
                                Point hostPoint = new Point(hostEvent.fixationPointX, hostEvent.fixationPointY);
                                Point guestPoint = new Point(guestEvent.fixationPointX, guestEvent.fixationPointY);

                                if (hostPoint.distance(guestPoint) > minDistance)
                                    aboveMinDistanceCounter++;
                                else
                                    belowMinDistanceCounter++;
                            }

                            guestIndex++;
                        }
                    }

                    if (notContainedInRectCounter > belowMinDistanceCounter && notContainedInRectCounter > aboveMinDistanceCounter)
                        g.setColor(prefs.getColorMinDistOutsideBoard());
                    else if (belowMinDistanceCounter > aboveMinDistanceCounter && belowMinDistanceCounter > notContainedInRectCounter)
                        g.setColor(prefs.getColorMinDistClose());
                    else if (aboveMinDistanceCounter > belowMinDistanceCounter && aboveMinDistanceCounter > notContainedInRectCounter)
                        g.setColor(prefs.getColorMinDistFarAway());
                }

                paintMajorTickForHorizSlider(g, tickBounds, xPos);
            }

            g.translate( 0, -tickBounds.y);
        }
        else {
            g.translate(tickBounds.x, 0);

            for (int y = 0; y < trackRect.height; y++) {

                int yPos = y + trackRect.x;
                double progress = ((double) y) / trackRect.height;
                long progressTS = (long) (startTS + (progress * (endTS - startTS)));

                long progressStartTS = startTS;
                if (y > 0) {
                    progressStartTS = Math.round(startTS + ((((double) y - 1) / trackRect.width)
                            * (endTS - startTS)));
                    progressStartTS = Math.round((progressStartTS + progressTS) * 0.5);
                }

                long progressEndTS = endTS;
                if (y < trackRect.height - 1) {
                    progressEndTS = Math.round(startTS + ((((double) y + 1) / trackRect.width)
                            * (endTS - startTS)));
                    progressEndTS = Math.round((progressEndTS + progressTS) * 0.5);
                }

                ArrayList<EyeTrackerEyeEvent> hostEvents =
                        hostProj.eventsBetweenShiftedTimestamps(progressStartTS, progressEndTS, true, false);
                ArrayList<EyeTrackerEyeEvent> guestEvents =
                        guestProj.eventsBetweenShiftedTimestamps(progressStartTS, progressEndTS, true, false);

                if (hostEvents == null || hostEvents.size() < 1 || guestEvents == null || guestEvents.size() < 1)
                    g.setColor(prefs.getColorMinDistOutsideDisplay());
                else {

                    int belowMinDistanceCounter = 0;
                    int aboveMinDistanceCounter = 0;
                    int notContainedInRectCounter = 0;
                    int guestIndex = 0;

                    for (EyeTrackerEyeEvent hostEvent : hostEvents) {

                        if (!hostEvent.containedInRecFrame(hostFramePoint1, hostFramePoint2)) {
                            notContainedInRectCounter++;
                            continue;
                        }

                        long tsHost = hostEvent.timestamp - hostProj.getTimeSyncOffset();

                        while (guestIndex < guestEvents.size()) {

                            EyeTrackerEyeEvent guestEvent = guestEvents.get(guestIndex);

                            if ((guestEvent.timestamp - guestProj.getTimeSyncOffset()) < tsHost)
                                break;

                            if (!guestEvent.containedInRecFrame(hostFramePoint1, hostFramePoint2)) {
                                notContainedInRectCounter++;
                            }
                            else {
                                Point hostPoint = new Point(hostEvent.fixationPointX, hostEvent.fixationPointY);
                                Point guestPoint = new Point(guestEvent.fixationPointX, guestEvent.fixationPointY);

                                if (hostPoint.distance(guestPoint) > minDistance)
                                    aboveMinDistanceCounter++;
                                else
                                    belowMinDistanceCounter++;
                            }

                            guestIndex++;
                        }
                    }


                    if (notContainedInRectCounter > belowMinDistanceCounter && notContainedInRectCounter > aboveMinDistanceCounter)
                        g.setColor(prefs.getColorMinDistOutsideBoard());
                    else if (belowMinDistanceCounter > aboveMinDistanceCounter && belowMinDistanceCounter > notContainedInRectCounter)
                        g.setColor(prefs.getColorMinDistFarAway());
                    else if (aboveMinDistanceCounter > belowMinDistanceCounter && aboveMinDistanceCounter > notContainedInRectCounter)
                        g.setColor(prefs.getColorMinDistClose());
                }

                paintMajorTickForVertSlider(g, tickBounds, yPos);
            }

            g.translate(-tickBounds.x, 0);
        }
    }

    @Override
    protected void paintMajorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
        g.drawLine( x, TICK_BUFFER , x, TICK_BUFFER + (safeLength - 1) );
    }

    @Override
    protected void paintMajorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        g.drawLine( 0, y, safeLength, y );
    }
}
