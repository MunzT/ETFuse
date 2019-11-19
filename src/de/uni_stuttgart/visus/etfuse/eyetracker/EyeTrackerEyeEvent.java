package de.uni_stuttgart.visus.etfuse.eyetracker;

import java.awt.Point;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EyeTrackerEyeEvent {

    // Attention! Raw Gaze Data points are also stored as EyeTrackerEyeEvent
    // "fixationPointX/Y" as well as "fixationDuration" may be misleading in that instance
    // fixationDuration for raw data points is derived from the sampling frequency of the eye tracker

    public int number = -1;
    public long timestamp = 0;
    public int fixationPointX = 0;
    public int fixationPointY = 0;
    public int realFixationPointX = 0;
    public int realFixationPointY = 0;
    public double eyePosLeftX = 0;
    public double eyePosLeftY = 0;
    public double eyePosLeftZ = 0;
    public double eyePosRightX = 0;
    public double eyePosRightY = 0;
    public double eyePosRightZ = 0;
    public double fixationDuration = 0;
    public boolean eyesNotFound = false;

    public Boolean containedInRecFrame(Point framePoint1, Point framePoint2) {

        Point eventPoint = new Point(this.fixationPointX, this.fixationPointY);
        Rectangle frame = new Rectangle(framePoint1.x, framePoint1.y,
                                        framePoint2.x - framePoint1.x,
                                        framePoint2.y - framePoint1.y);

        return frame.contains(eventPoint);
    }

    public void printDesc() {

        Date date = new Date(this.timestamp);
        DateFormat formatter = new SimpleDateFormat("mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateFormatted = formatter.format(date);

        System.out.println("<" + this.number + "@" + dateFormatted + "> " + "("
                           + this.fixationPointX + ", " + this.fixationPointY + "), eyes found: "
                           + !this.eyesNotFound);
    }
}
