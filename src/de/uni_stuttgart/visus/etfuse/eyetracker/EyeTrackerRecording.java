package de.uni_stuttgart.visus.etfuse.eyetracker;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class EyeTrackerRecording {

    private ArrayList<EyeTrackerEyeEvent> rawEyeEvents = null;
    private ArrayList<EyeTrackerEyeEvent> filteredEyeEvents = null;
    private ArrayList<Long> clicks = null;
    private Point framePoint1, framePoint2;
    private Rectangle2D screenResolution = null;
    private double displayPPI = 0.0;

    public long recordingStartTS = 0;
    private int samplingFrequency = 0;

    public Color preferredGazeColor = null;

    public EyeTrackerRecording() {

        this.rawEyeEvents = new ArrayList<EyeTrackerEyeEvent>();
        this.filteredEyeEvents = new ArrayList<EyeTrackerEyeEvent>();
        this.clicks = new ArrayList<Long>();
    }

    public void addEyeEvent(EyeTrackerEyeEvent event) {

        rawEyeEvents.add(event);
    }

    public ArrayList<EyeTrackerEyeEvent> getRawEyeEvents() {

        return rawEyeEvents;
    }

    public ArrayList<EyeTrackerEyeEvent> getFilteredEyeEvents() {
        return filteredEyeEvents;
    }

    public void setFilteredEyeEvents(ArrayList<EyeTrackerEyeEvent> filteredEyeEvents) {
        this.filteredEyeEvents = filteredEyeEvents;
    }

    public void addClick(long ts) {

        clicks.add(ts);
    }

    public ArrayList<Long> getClicks() {

        return clicks;
    }

    public void setFrame(Point p1, Point p2) {

        this.framePoint1 = p1;
        this.framePoint2 = p2;
    }

    public Point getFramePoint1() {

        return this.framePoint1;
    }

    public Point getFramePoint2() {

        return this.framePoint2;
    }

    public void setSamplingFrequency(int frequency) {

        this.samplingFrequency = frequency;
    }

    public int getSamplingFrequency() {

        return this.samplingFrequency;
    }

    public void setScreenResolution(int screenWidth, int screenHeight) {

        this.screenResolution = new Rectangle2D.Double(0, 0, screenWidth, screenHeight);
    }

    public void setScreenResolution(Rectangle2D screenResolution) {

        this.screenResolution = screenResolution;
    }

    public Rectangle2D getScreenResolution() {

        return this.screenResolution;
    }

    public double getDisplayPPI() {

        return this.displayPPI;
    }

    public void setDisplayPPI(double displayPPI) {
        this.displayPPI = displayPPI;
    }

    public ArrayList<EyeTrackerEyeEvent> xEventsBeforeTimestamp(int amount, long timestamp,
                                                 Boolean fromRawData, Boolean includeEyesNotFound) {

        ArrayList<EyeTrackerEyeEvent> eventList = new ArrayList<EyeTrackerEyeEvent>();

        if ((fromRawData && this.getRawEyeEvents().size() < 1)
                || (!fromRawData && this.getFilteredEyeEvents().size() < 1))
            return eventList;

        long firstTS = this.getRawEyeEvents().get(0).timestamp;
        long lastTS = this.getRawEyeEvents().get(this.getRawEyeEvents().size() - 1).timestamp;

        if (timestamp < firstTS || timestamp > lastTS)
            return eventList;

        ArrayList<EyeTrackerEyeEvent> events = null;

        if (fromRawData)
            events = this.getRawEyeEvents();
        else
            events = this.getFilteredEyeEvents();

        double factor = (double) timestamp / (double) lastTS;
        int pivot = (int) Math.floor(factor * events.size());

        if (pivot >= events.size())
            pivot = events.size() - 1;

        for (;;) {

            if (events.get(pivot).timestamp == timestamp)
                break;

            if (events.get(pivot).timestamp < timestamp) {

                pivot++;

                if (pivot >= events.size() || events.get(pivot).timestamp > timestamp) {

                    pivot--;
                    break;
                }
                else if (events.get(pivot).timestamp == timestamp) {

                    break;
                }
            }

            if (events.get(pivot).timestamp > timestamp) {

                pivot--;

                if (pivot < 0 || events.get(pivot).timestamp <= timestamp) {
                    break;
                }
            }
        }

        if (pivot < 0 || pivot >= events.size())
            return eventList;

        EyeTrackerEyeEvent match = events.get(pivot);

        if (match != null)
            if (!match.eyesNotFound || includeEyesNotFound)
                eventList.add(match);

        int counter = 0;

        while (pivot > 0 && counter < amount) {

            if (!events.get(pivot).eyesNotFound || includeEyesNotFound)
                eventList.add(events.get(pivot));

            counter++;
            pivot--;
        }

        return eventList;
    }

    public ArrayList<EyeTrackerEyeEvent> eventsBetweenTimestamps(long timestampBegin, long timestampEnd,
                                                 Boolean fromRawData, Boolean includeEyesNotFound) {

        ArrayList<EyeTrackerEyeEvent> eventList = new ArrayList<EyeTrackerEyeEvent>();

        if ((fromRawData && this.getRawEyeEvents().size() < 1)
                || (!fromRawData && this.getFilteredEyeEvents().size() < 1))
            return eventList;

        ArrayList<EyeTrackerEyeEvent> events = null;

        if (fromRawData)
            events = this.getRawEyeEvents();
        else
            events = this.getFilteredEyeEvents();

        long firstTS = events.get(0).timestamp;
        long lastTS = events.get(events.size() - 1).timestamp;

        if (timestampEnd < firstTS)
            return eventList;

        if (timestampEnd > lastTS)
            timestampEnd = lastTS;

        double factor = (double) timestampEnd / (double) lastTS;
        int pivot = (int) Math.floor(factor * events.size());

        if (pivot >= events.size())
            pivot = events.size() - 1;

        for (;;) {

            if (events.get(pivot).timestamp == timestampEnd)
                break;

            if (events.get(pivot).timestamp < timestampEnd) {

                pivot++;

                if (events.get(pivot).timestamp > timestampEnd) {

                    pivot--;
                    break;
                }
                else if (events.get(pivot).timestamp == timestampEnd) {

                    break;
                }
            }

            if (events.get(pivot).timestamp > timestampEnd) {

                pivot--;

                if (events.get(pivot).timestamp <= timestampEnd) {

                    break;
                }
            }
        }

        if (pivot < 0 || pivot >= events.size())
            return eventList;

        EyeTrackerEyeEvent match = events.get(pivot);

        if (match != null)
            if (!match.eyesNotFound || includeEyesNotFound)
                eventList.add(match);

        while (pivot > 0) {

            if (events.get(pivot).timestamp < timestampBegin)
                break;

            if (!events.get(pivot).eyesNotFound || includeEyesNotFound)
                eventList.add(events.get(pivot));

            pivot--;
        }

        return eventList;
    }
}
