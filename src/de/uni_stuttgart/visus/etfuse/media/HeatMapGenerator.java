package de.uni_stuttgart.visus.etfuse.media;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.Videoio;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.misc.Preferences.HeatMapTimeSource;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class HeatMapGenerator extends SwingWorker {

    private OverlayGazeProjector proj;
    private int projId;
    private VideoFrame vidFrame;
    public int progress;
    private long minTime;
    private long maxTime;
    private long heatmapId;
    private HeatMapTimeSource heatmapType;

    private static ArrayList<HeatMapGenerator> allActiveGenerators = new ArrayList<HeatMapGenerator>();

    public static void killAllActiveGenerators() {

        ArrayList<HeatMapGenerator> currentlyRunningGenerators = (ArrayList<HeatMapGenerator>) allActiveGenerators.clone();
        allActiveGenerators.clear();

        for (int i = 0; i < currentlyRunningGenerators.size(); i++) {
            HeatMapGenerator current = currentlyRunningGenerators.get(i);
            current.abortHeatMapGeneration();
        }
    }

    public HeatMapGenerator(int projId, int heatmapId,
            HeatMapTimeSource heatmapType, VideoFrame vidFrame) {

        this.projId = projId;
        this.proj = vidFrame.getPanel().getProjectors().get(projId);
        this.heatmapId = heatmapId;
        this.heatmapType = heatmapType;
        this.vidFrame = vidFrame;
        int hostVidFps = (int) vidFrame.getPanel().getCamera().get(Videoio.CV_CAP_PROP_FPS);

        if (heatmapType == HeatMapTimeSource.USERDEFINED) {
            this.minTime = vidFrame.getHeatMapRangeLow();
            this.maxTime = vidFrame.getHeatMapRangeHigh();
        }
        else if (heatmapType == HeatMapTimeSource.CLICKS) {
            ArrayList<Long> events = this.vidFrame.getPanel().getHeatmapEvents();

            if (heatmapId == 0 && events.size() > 0) {// first click
                this.minTime = vidFrame.getHostProjector().getRecording().getRawEyeEvents().get(0).timestamp - vidFrame.getHostProjector().getTimeSyncOffset(); // beginning of video
                this.maxTime = events.get(0) * hostVidFps / 1000;
            }
            else if (heatmapId == events.size() && events.size() > 0) { // last click
                this.minTime = events.get(events.size() - 1)  * hostVidFps / 1000;;
                this.maxTime = vidFrame.getHostProjector().getRecording().getRawEyeEvents().get(
                        vidFrame.getHostProjector().getRecording().getRawEyeEvents().size() - 1).timestamp - vidFrame.getHostProjector().getTimeSyncOffset(); // end of video
            }
            else if (events.size() > 0) {
                this.minTime = events.get(heatmapId - 1) * hostVidFps / 1000;
                this.maxTime = events.get(heatmapId) * hostVidFps / 1000;
            }
        }
        else if (heatmapType == HeatMapTimeSource.TIMEINTERVALS) {
            // TODO not implemented
        }
    }

    @Override
    protected Object doInBackground() throws Exception {

        Preferences prefs = Project.currentProject().getPreferences();

        Mat heatMap = proj.getNormalizedHeatMap(heatmapId, heatmapType);

        if (vidFrame.getSkipHeatmapGeneration()) {
            return heatMap;
        }

        if (true) {//(!proj.isHeatMapBeingGenerated()) { // TODO
            proj.setIsHeatMapBeingGenerated(true);
            HeatMapGenerator.allActiveGenerators.add(this);
            Mat step1 = generateHeatMapRaw(proj);
            if (this.isCancelled())
                return heatMap;
            proj.setRawHeatMap(step1, heatmapId, heatmapType);
            Mat step2 = normalizeHeatMap(step1, 0, 255);

            Mat finalStep;

            // apply color map and
            Mat step3 = colorMapHeatMap(step2, prefs.getColorMap());
            proj.setNormalizedHeatMap(step3, heatmapId, heatmapType);

            // set everything equally transparent
            finalStep = makeHeatMapTransparentABGR(step3);
            proj.setTransparentHeatMap(finalStep, heatmapId, heatmapType);

            // colormap with just one color
            Color color = this.projId == 0 ? prefs.getHeatmapColorPlayer1() : prefs.getHeatmapColorPlayer2();
            finalStep = prepareUniColorMapHeatMap(step2);
            finalStep = makeHeatMapTransparentUniColorABGR(finalStep, color);
            proj.setUniColorTransparentHeatMap(finalStep, heatmapId, heatmapType);

            // TODO original version
            /*Mat step3 = colorMapHeatMap(step2, Imgproc.COLORMAP_HOT, -1);
            proj.setNormalizedHeatMap(step3, heatmapId, heatmapType);

            step3 = colorMapHeatMap(step2, prefs.getColorMap(), projId);

            finalStep = makeHeatMapTransparentABGR_removeBlue(step3);*/

            proj.setIsHeatMapBeingGenerated(false);
            HeatMapGenerator.allActiveGenerators.remove(this);

            if (vidFrame != null)
                vidFrame.setTitleWithProgress("Generate heatmap (id " + this.hashCode() + ")", -1);
        }

        return heatMap;
    }

    private void abortHeatMapGeneration() {

        this.cancel(true);
        this.proj.setIsHeatMapBeingGenerated(false);

        if (vidFrame != null)
            vidFrame.setTitleWithProgress("Generate heatmap (id " + this.hashCode() + ")", -1);
    }

    public void attachVideoFrameForTitleUpdate(VideoFrame vidFrame) {

        this.vidFrame = vidFrame;
    }

    public static Mat[] processHeatMapsForComparison(Mat heatMap1, Mat heatMap2) {

        Preferences prefs = Project.currentProject().getPreferences();

        Mat norm1 = heatMap1.clone();
        Mat norm2 = heatMap2.clone();

        MinMaxLocResult res1 = Core.minMaxLoc(norm1);
        MinMaxLocResult res2 = Core.minMaxLoc(norm2);
        double h1MaxVal = res1.maxVal;
        double h2MaxVal = res2.maxVal;
        double h1MinVal = res1.minVal;
        double h2MinVal = res2.minVal;

        double h1Factor = Math.max(0, Math.min(1, (h1MaxVal / h2MaxVal)));
        double h2Factor = Math.max(0, Math.min(1, (h2MaxVal / h1MaxVal)));

        int h1Max = (int) Math.max(0, Math.min(255, 255 * h1Factor));
        int h2Max = (int) Math.max(0, Math.min(255, 255 * h2Factor));
        int h1Min = (int) Math.round(Math.max(0, Math.min(255.0 / h1Factor, h1MinVal - h2MinVal)) * h1Factor);
        int h2Min = (int) Math.round(Math.max(0, Math.min(255.0 / h2Factor, h2MinVal - h1MinVal)) * h2Factor);

        norm1 = normalizeHeatMap(norm1, h1Min, h1Max);
        norm2 = normalizeHeatMap(norm2, h2Min, h2Max);

        norm1 = colorMapHeatMap(norm1, prefs.getColorMap());
        norm2 = colorMapHeatMap(norm2, prefs.getColorMap());

        Mat[] result = {norm1, norm2};
        return result;
    }

    public static Mat processDiffedHeatMap(Mat hm1, Mat hm2, Boolean absoluteDiff) {

        Mat hm3 = new Mat();

        if (absoluteDiff) {
            Core.absdiff(hm1, hm2, hm3);
        }
        else {
            Core.subtract(hm1, hm2, hm3);
        }

        hm3 = normalizeHeatMap(hm3, 0, 255);
        hm3 = colorMapHeatMap(hm3, Project.currentProject().getPreferences().getColorMap());


        return hm3;
    }

    public static Mat processAddedHeatMap(Mat hm1, Mat hm2) {

        Mat hm3 = new Mat();
        Core.add(hm1, hm2, hm3);

        hm3 = normalizeHeatMap(hm3, 0, 255);
        hm3 = colorMapHeatMap(hm3, Project.currentProject().getPreferences().getColorMap());

        return hm3;
    }

    public static Mat processIntersectedHeatMap(Mat hm1, Mat hm2) {

        Mat hm3 = new Mat();
        Core.min(hm1, hm2, hm3); // minimum values for intersection

        hm3 = normalizeHeatMap(hm3, 0, 255);
        hm3 = colorMapHeatMap(hm3, Project.currentProject().getPreferences().getColorMap());

        return hm3;
    }

    private static Mat makeHeatMapTransparentABGR(Mat heatMap) {
        // make everything 50% transparent
        Mat transparent = heatMap.clone();
        Imgproc.cvtColor(transparent, transparent, Imgproc.COLOR_BGR2BGRA);

        for (int x = 0; x < transparent.cols(); x++) {
            for (int y = 0; y < transparent.rows(); y++) {
                double[] pixel = transparent.get(y, x);

                pixel[3] = pixel[2];
                pixel[2] = pixel[1];
                pixel[1] = pixel[0];

                // alpha
                pixel[0] = Project.currentProject().getPreferences().getHeatmapTransparency() * 255.0 / 100.0;

                transparent.put(y, x, pixel);
            }
        }

        return transparent;
    }

    private static Mat makeHeatMapTransparentABGR_removeBlue(Mat heatMap) {

        Mat transparent = heatMap.clone();
        Imgproc.cvtColor(transparent, transparent, Imgproc.COLOR_BGR2BGRA);

        for (int x = 0; x < transparent.cols(); x++) {
            for (int y = 0; y < transparent.rows(); y++) {
                double[] pixel = transparent.get(y, x);
                // BGRA -> ABGR
                pixel[3] = pixel[2];
                pixel[2] = pixel[1];
                pixel[1] = pixel[0];
                //pixel[0] = 255;
                pixel[0] = 128 - pixel[1];
                // kaltes blau transparent machen
                /*if (pixel[1] == 128 && pixel[2] == 0 && pixel[3] == 0)
                    pixel[0] = 0;*/

                transparent.put(y, x, pixel);
            }
        }

        return transparent;
    }

    public static Mat makeHeatMapTransparentUniColorABGR(Mat heatMap, Color color) {

        Mat transparent = heatMap.clone();
        Imgproc.cvtColor(transparent, transparent, Imgproc.COLOR_BGR2BGRA);

        for (int x = 0; x < transparent.cols(); x++) {
            for (int y = 0; y < transparent.rows(); y++) {
                double[] pixel = transparent.get(y, x);

                // alpha pixel[0]
                pixel[3] = color.getRed();
                pixel[2] = color.getGreen();
                pixel[1] = color.getBlue();

                transparent.put(y, x, pixel);
            }
        }

        return transparent;
    }

    private static Mat normalizeHeatMap(Mat heatMap, int min, int max) {

        Mat norm = heatMap.clone();
        Core.normalize(norm, norm, max, min, Core.NORM_MINMAX);

        return norm;
    }

    public static Mat colorMapHeatMap(Mat heatMap, int colorMap) {

        Mat mapped = heatMap.clone();
        mapped.convertTo(mapped, CvType.CV_8UC3);
        Imgproc.applyColorMap(mapped, mapped, colorMap);

        return mapped;
    }

    public static Mat prepareUniColorMapHeatMap(Mat heatMap) {

        Mat mapped = heatMap.clone();

        mapped.convertTo(mapped, CvType.CV_8UC3);

        Mat userColor = new Mat(256,1,CvType.CV_8UC3);

        for(int r = 0; r < 256; ++r) {
            for(int c = 0; c < 1; ++c) {

                double[] newColor = new double[3];
                newColor[0] = r / 1.5;
                newColor[1] = r / 1.5;
                newColor[2] = r / 1.5;

                userColor.put(r, c, newColor);
            }
        }
        Imgproc.applyColorMap(mapped, mapped, userColor);

        return mapped;
    }

    private Mat generateHeatMapRaw(OverlayGazeProjector hostProj) {

        Preferences prefs = Project.currentProject().getPreferences();

        final int heatRadius = prefs.getHeatMapGenHeatRadius();
        final int pointRadius = 0;
        final Boolean skipPercentage = prefs.getHeatMapGenSkipEvents();
        final double percentageToSkip = prefs.getHeatMapGenSkipPercentage();
        final Boolean skipRepeateds = false;
        final Boolean skipBorderEvents = false;

        System.out.println("<HeatMapGenerator> Generate heatmap");

        Rectangle2D screenRes = hostProj.getRecording().getScreenResolution();

        if (vidFrame != null)
            screenRes = new Rectangle2D.Double(0, 0,
                    vidFrame.getPanel().getCamera().get(Videoio.CAP_PROP_FRAME_WIDTH),
                    vidFrame.getPanel().getCamera().get(Videoio.CAP_PROP_FRAME_HEIGHT));

        Mat heatMap = new Mat((int) screenRes.getHeight(), (int) screenRes.getWidth(),
                CvType.CV_64FC1, new Scalar(0));

        if (hostProj == null)
            return heatMap;

        Boolean save = true;

        Mat alpha = new Mat(2 * heatRadius, 2 * heatRadius, CvType.CV_32FC1);

        // create alpha
        for (int r = 0; r < alpha.rows(); r++) {
            for (int c = 0; c < alpha.cols(); c++) {
                double x = heatRadius - r;
                double y = heatRadius - c;
                double radius = Math.hypot(x, y);
                double[] pixel = alpha.get(r, c);

                if (radius > heatRadius)
                    pixel[0] = 0;                    // transparent
                else if (radius < pointRadius)
                    pixel[0] = 1.f /*255*/;                    // solid
                else
                    pixel[0] = /*Math.round(*/(1.f - ((radius - pointRadius)
                            / (heatRadius - pointRadius))) * 1.f /*255)*/; // partial

                alpha.put(r, c, pixel);
            }
        }

        int lastFixPointX = 0;
        int lastFixPointY = 0;
        long count = 0;

        ArrayList<EyeTrackerEyeEvent> eyeEvents = null;

        if (vidFrame != null) {
            int hostVidFps = (int) vidFrame.getPanel().getCamera().get(Videoio.CV_CAP_PROP_FPS);
            long startTS = (minTime / hostVidFps) * 1000;
            long stopTS = (maxTime / hostVidFps) * 1000;

            eyeEvents = hostProj.eventsBetweenShiftedTimestamps(startTS, stopTS, false, true);
        }
        else {
            eyeEvents = hostProj.getRecording().getFilteredEyeEvents();
        }

        int totalEvents = hostProj.getRecording().getFilteredEyeEvents().size();
        int numEvents = eyeEvents.size();
        long eventsToSkip = Math.round(numEvents / (100 / percentageToSkip));

        for (EyeTrackerEyeEvent e : eyeEvents) {

            if (this.isCancelled())
                return null;

            count++;

            if (count % 100 == 0) {
                progress = (int) (((double) count / (double) numEvents) * 100);
                if (vidFrame != null)
                    vidFrame.setTitleWithProgress("Generate heatmap (id " + this.hashCode() + ")", progress);
            }

            if (skipPercentage && eventsToSkip > 1)
                if (count % eventsToSkip != 0)
                    continue;

            if (e.eyesNotFound)
                continue;

            if (skipRepeateds) {
                if (e.fixationPointX == lastFixPointX && e.fixationPointY == lastFixPointY)
                    continue;

                lastFixPointX = e.fixationPointX;
                lastFixPointY = e.fixationPointY;
            }

            // composite
            int xOrigin = e.fixationPointX - heatRadius;
            int yOrigin = e.fixationPointY - heatRadius;
            Rect roiRect = new Rect(xOrigin, yOrigin, 2 * heatRadius, 2 * heatRadius);

            if (skipBorderEvents)
                if (roiRect.x < 0 || roiRect.y < 0 || roiRect.x + roiRect.width > heatMap.width()
                        || roiRect.y + roiRect.height > heatMap.height())
                    continue;

            for (int j = 0; j < alpha.rows(); j++) {
                if (j + yOrigin < 0 || j + yOrigin >= heatMap.rows())
                    continue;
                for (int i = 0; i < alpha.cols(); i++) {
                    if (i + xOrigin < 0 || i + xOrigin >= heatMap.cols())
                        continue;
                    //for (int c = 0; c < 1; c++) {   // iterate over channels, result = circle * alpha + (1 - alpha) * background
                        double[] bgCell = heatMap.get(j + yOrigin, i + xOrigin);
                        //double[] fgCell = circle.get(j, i);
                        double[] alphaCell = alpha.get(j, i);

                        //for (int idx = 0; idx < bgCell.length; idx++) {
                            // // += statt =
                            // // bgCell[idx] = (fgCell[idx] * alphaCell[0]) + ((1.0 - alphaCell[0]) * bgCell[idx]);
                            double num = bgCell[0];
                            double add = alphaCell[0];
                            if (!prefs.getHeatMapGenGenFromFrequencyInstead())
                                add *= e.fixationDuration;
                            num += add;
                            //num += /*((int) fgCell[0]) **/ alphaCell[0] * e.fixationDuration; // "* e.fixationDuration" auskommentieren, um nach häufigkeit und nicht nach dauer zu generieren
                            bgCell[0] = num;
                        //}

                        heatMap.put(j + yOrigin, i + xOrigin, bgCell);
                    //}
                }
            }
        }

        if (vidFrame != null)
            vidFrame.setTitleWithProgress("Generate heatmap (id " + this.hashCode() + ")", 99);

        return heatMap;
    }
}
