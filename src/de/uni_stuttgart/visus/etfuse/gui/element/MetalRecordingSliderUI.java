package de.uni_stuttgart.visus.etfuse.gui.element;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalSliderUI;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class MetalRecordingSliderUI extends MetalSliderUI {

    private OverlayGazeProjector hostProjForMinDist = null;
    private OverlayGazeProjector guestProjForMinDist = null;
    private VideoFrame vidFrame = null;
    private int minDistance = 0;
    private int safeLength = 0;

    ArrayList<Long> clickPositions1;
    ArrayList<Long> clickPositions2;
    ArrayList<Long> eventPositions;
    ArrayList<Color> eventColors;

    enum GazeDistanceState
    {
       CLOSE, FARAWAY, OUTSIDEBOARD, OUTSIDESCREEN;
    };

    public MetalRecordingSliderUI() {

        super();
        this.safeLength = ((Integer) UIManager.get("Slider.majorTickLength")).intValue();
    }

    protected void setMinDistanceRecordingsToDraw(VideoFrame vidFrame,
            OverlayGazeProjector hostProjector, OverlayGazeProjector guestProjector, int minDistance) {

        this.vidFrame = vidFrame;
        this.hostProjForMinDist = hostProjector;
        this.guestProjForMinDist = guestProjector;
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

        if (hostProjForMinDist == null || guestProjForMinDist == null ||
            hostProjForMinDist.getRecording().getFilteredEyeEvents().size() < 1 ||
            guestProjForMinDist.getRecording().getFilteredEyeEvents().size() < 1 ||
            hostProjForMinDist.getRecording().getRawEyeEvents().size() < 1 ||
            guestProjForMinDist.getRecording().getRawEyeEvents().size() < 1 ||
            hostFramePoint1 == null || hostFramePoint2 == null)
            return;

        long startTS = vidFrame.getHostProjector().getRecording().getRawEyeEvents().get(0).timestamp;
        long endTS =  vidFrame.getHostProjector().getRecording().getRawEyeEvents().get(
                vidFrame.getHostProjector().getRecording().getRawEyeEvents().size() - 1).timestamp;

        Rectangle tickBounds = tickRect;

        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            g.translate(0, tickBounds.y);

            GazeDistanceState currentState = GazeDistanceState.OUTSIDESCREEN;

            // timestamp position on slider for both players
            clickPositions1 = new ArrayList<Long>();
            clickPositions2 = new ArrayList<Long>();
            for (int i = 0; i < this.vidFrame.getPanel().getProjectors().size(); i++) {
                if (prefs.getShowPlayerEventTicks().contains(i)) {
                    OverlayGazeProjector proj = this.vidFrame.getPanel().getProjectors().get(i);

                    ArrayList<Long> tempClicks = proj.getRecording().getClicks();
                    ArrayList<Long> clickPositions = new ArrayList<Long>();
                    for (int j = 0; j < tempClicks.size(); ++j) {
                        long c =  Math.round(((((float)(tempClicks.get(j)
                                - proj.getTimeSyncOffset() - startTS) * trackRect.width
                                / (endTS - startTS)) + trackRect.x)));
                        clickPositions.add(c);
                    }

                    if (i == 0)
                        clickPositions1 = clickPositions;
                    else
                        clickPositions2 = clickPositions;
                }
            }

            OverlayGazeProjector hostProj = this.vidFrame.getHostProjector();

            eventPositions = new ArrayList<Long>();
            eventColors = new ArrayList<Color>();

            if (prefs.getShowAdditionalEventTicks()) {
                for (Long key : this.vidFrame.getPanel().getCustomEvents().keySet()) {
                    long c =  Math.round(((((float)(key
                            - hostProj.getTimeSyncOffset() - startTS) * trackRect.width
                            / (endTS - startTS)) + trackRect.x)));
                    eventPositions.add(c);
                    eventColors.add(this.vidFrame.getPanel().getCustomEvents().get(key));
                }
            }

            // all timestamps for clicks
            ArrayList<Long> clicksTS = new ArrayList<Long>();
            for (int i = 0; i < prefs.getPlayerEventsForMinDistPlot().size(); i++) {
                OverlayGazeProjector proj = this.vidFrame.getPanel().getProjectors().get(prefs.getPlayerEventsForMinDistPlot().get(i));
                ArrayList<Long> tempClicks = proj.getRecording().getClicks();
                for (int j = 0; j < tempClicks.size(); ++j) {
                    long c =  tempClicks.get(j) - proj.getTimeSyncOffset();
                    clicksTS.add(c);
                }
            }
            if (prefs.getUseAdditionalEventForMinDistPlot()) {
                clicksTS.addAll(this.vidFrame.getPanel().getCustomEvents().keySet());
            }
            Collections.sort(clicksTS);

            // for click method
            int currentClickId = -1;

            // for interval method
            int currentInterval = 0;

            // for click and interval method
            Boolean recalcultaeTimeRange = true;
            Long startTime = (long) 0;
            Long endTime = (long) 0;

            // lists are sorted from large to small
            ArrayList<EyeTrackerEyeEvent> hostEvents = null;
            ArrayList<EyeTrackerEyeEvent> guestEvents = null;

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

                if (prefs.getMinDistSubdivision() == Preferences.MinDistSubdivision.MINAREAS) {

                    hostEvents =
                            hostProjForMinDist.eventsBetweenShiftedTimestamps(progressStartTS,
                                    progressEndTS, true, false);
                    guestEvents =
                            guestProjForMinDist.eventsBetweenShiftedTimestamps(progressStartTS,
                                    progressEndTS, true, false);

                    if (hostEvents == null || hostEvents.size() < 1
                            || guestEvents == null || guestEvents.size() < 1)
                        currentState = GazeDistanceState.OUTSIDESCREEN;
                    else {

                        currentState = getDistanceStateForAsynchroneData(hostEvents, guestEvents,
                        		hostProjForMinDist, guestProjForMinDist);
                    }
                }
                else if (prefs.getMinDistSubdivision() == Preferences.MinDistSubdivision.CLICKS) {

                    if (clicksTS.size() <= 0 || (currentClickId != -1 && (currentClickId >= clicksTS.size()
                            || progressEndTS < clicksTS.get(currentClickId)))) {
                        // no nothing and draw same as before
                    }
                    else {
                        currentClickId++;
                        if (currentClickId == 0) { // area before first click
                            startTime = startTS;
                            endTime = clicksTS.get(currentClickId);
                        }
                        else if (currentClickId == clicksTS.size()) { // area after last click
                            startTime = clicksTS.get(currentClickId - 1);
                            endTime = endTS;
                        }
                        else {
                            startTime = clicksTS.get(currentClickId - 1);
                            endTime = clicksTS.get(currentClickId);
                        }

                        hostEvents = hostProjForMinDist.eventsBetweenShiftedTimestamps(startTime, endTime, true, false);
                        guestEvents = guestProjForMinDist.eventsBetweenShiftedTimestamps(startTime, endTime, true, false);

                        if (hostEvents == null || hostEvents.size() < 1
                                || guestEvents == null || guestEvents.size() < 1) {
                            currentState = GazeDistanceState.OUTSIDESCREEN;
                        }
                        else {
                            currentState = getDistanceStateForAsynchroneData(hostEvents, guestEvents,
                            		hostProjForMinDist, guestProjForMinDist);
                        }
                    }
                }

                else if (prefs.getMinDistSubdivision() == Preferences.MinDistSubdivision.TIMEINTERVALS) {

                    if (recalcultaeTimeRange) {
                        startTime = startTS + currentInterval * prefs.getMinDistSubdivisionInterval();
                        endTime = startTS + (currentInterval + 1) * prefs.getMinDistSubdivisionInterval() - 1;

                        hostEvents = hostProjForMinDist.eventsBetweenShiftedTimestamps(startTime, endTime, true, false);
                        guestEvents = guestProjForMinDist.eventsBetweenShiftedTimestamps(startTime, endTime, true, false);

                        recalcultaeTimeRange = false;
                    }

                    if (hostEvents == null || hostEvents.size() < 1
                            || guestEvents == null || guestEvents.size() < 1) {
                        currentState = GazeDistanceState.OUTSIDESCREEN;
                    }
                    else {
                        currentState = getDistanceStateForAsynchroneData(hostEvents, guestEvents,
                        		hostProjForMinDist, guestProjForMinDist);
                    }

                    if (progressEndTS > endTime + (progressEndTS - progressStartTS) / 2) {
                        currentInterval++;
                        recalcultaeTimeRange = true;
                    }
                }

                if (currentState == GazeDistanceState.CLOSE) {
                    g.setColor(prefs.getColorMinDistClose());
                }
                else if (currentState == GazeDistanceState.FARAWAY) {
                    g.setColor(prefs.getColorMinDistFarAway());
                }
                else if (currentState == GazeDistanceState.OUTSIDEBOARD) {
                    g.setColor(prefs.getColorMinDistOutsideBoard());
                }
                else if (currentState == GazeDistanceState.OUTSIDESCREEN) {
                    g.setColor(prefs.getColorMinDistOutsideDisplay());
                }

                paintMajorTickForHorizSlider(g, tickBounds, xPos);
            }

            g.translate( 0, -tickBounds.y);
        }
        else {
            // slider has to be horizontal
        }
    }


    private GazeDistanceState getDistanceStateForAsynchroneData(ArrayList<EyeTrackerEyeEvent> hostEvents,
            ArrayList<EyeTrackerEyeEvent> guestEvents, OverlayGazeProjector host, OverlayGazeProjector guest) {
        int belowMinDistanceCounter = 0;
        int aboveMinDistanceCounter = 0;
        int notContainedInRectCounter = 0;
        int guestIndex = 0;

        GazeDistanceState currentState = GazeDistanceState.OUTSIDESCREEN;

        // outer loop should be the smaller list: swap lists
        OverlayGazeProjector tempGuestProj = guest;
        OverlayGazeProjector tempHosttProj = host;
        if (hostEvents.size() > guestEvents.size()) {
            ArrayList<EyeTrackerEyeEvent> tempEvents;
            tempEvents = guestEvents;
            guestEvents = hostEvents;
            hostEvents = tempEvents;

            tempGuestProj = host;
            tempHosttProj = guest;
        }

        Point hostFramePoint1 = host.getRecording().getFramePoint1();
        Point hostFramePoint2 = host.getRecording().getFramePoint2();

        int index = -1;
        for (EyeTrackerEyeEvent hostEvent : hostEvents) {

            EyeTrackerEyeEvent nextHostEvent = (index == -1 ? null : hostEvents.get(index));

            Boolean notContained = false;
            if (!hostEvent.containedInRecFrame(hostFramePoint1, hostFramePoint2)) {
                notContainedInRectCounter++;
                notContained = true;
            }

            long tsHost = hostEvent.timestamp - tempHosttProj.getTimeSyncOffset();

            while (guestIndex < guestEvents.size()) {
                EyeTrackerEyeEvent guestEvent = guestEvents.get(guestIndex);

                // TODO: nicht immer mit allen gast-events vergleichen
                if ((guestEvent.timestamp - tempGuestProj.getTimeSyncOffset())
                        < tsHost - (nextHostEvent == null ? 0 :
                            (nextHostEvent.timestamp - hostEvent.timestamp) / 2)) { // TODO?
                    break;
                }

                if (!guestEvent.containedInRecFrame(hostFramePoint1, hostFramePoint2)) {
                    if (!notContained)
                        notContainedInRectCounter++;
                }
                else {
                    Point hostPoint = new Point(hostEvent.fixationPointX,
                            hostEvent.fixationPointY);
                    Point guestPoint = new Point(guestEvent.fixationPointX,
                            guestEvent.fixationPointY);

                    if (hostPoint.distance(guestPoint) > minDistance)
                        aboveMinDistanceCounter++;
                    else
                        belowMinDistanceCounter++;
                }

                guestIndex++;
            }

            index++;
        }

        if (belowMinDistanceCounter >= aboveMinDistanceCounter
                && belowMinDistanceCounter >= notContainedInRectCounter)
            currentState = GazeDistanceState.CLOSE;
        else if (aboveMinDistanceCounter >= belowMinDistanceCounter
                && aboveMinDistanceCounter > notContainedInRectCounter)
            currentState = GazeDistanceState.FARAWAY;
        else if (notContainedInRectCounter >= belowMinDistanceCounter
                && notContainedInRectCounter >= aboveMinDistanceCounter)
            currentState = GazeDistanceState.OUTSIDEBOARD;

        return currentState;
    }


    @Override
    protected void paintMajorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x ) {
        g.drawLine(x, TICK_BUFFER + (safeLength - 1) * 1/8, x, TICK_BUFFER + (safeLength - 1) );

        if (clickPositions1.contains((long)x)) {
            g.setColor(changeColorBrightness(Project.currentProject().
                    getPreferences().getColorPlayer1()));
            g.drawLine(x, TICK_BUFFER - 5, x, TICK_BUFFER + (safeLength - 1) * 1/8 - 2);
        }

        if (clickPositions2.contains((long)x)) {
            g.setColor(changeColorBrightness(Project.currentProject().
                    getPreferences().getColorPlayer2()));
            g.drawLine(x, TICK_BUFFER - 5, x, TICK_BUFFER + (safeLength - 1) * 1/8 - 2);
        }

        if (eventPositions.contains((long)x)) {
            g.setColor(eventColors.get(eventPositions.indexOf((long)x)));
            g.drawLine(x, TICK_BUFFER - 5, x, TICK_BUFFER + (safeLength - 1) * 1/8 - 4);
        }
    }
    Color changeColorBrightness(Color c) {
        if (c.getRed() + c.getGreen() + c.getBlue() < 350) {
            c = c.brighter();
        }
        else {
            c = c.darker();
        }
        return c;
    }


    @Override
    protected void paintMajorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        g.drawLine( 0, y, safeLength, y );
    }
}
