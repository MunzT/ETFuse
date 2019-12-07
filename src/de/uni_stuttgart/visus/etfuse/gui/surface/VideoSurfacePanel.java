package de.uni_stuttgart.visus.etfuse.gui.surface;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.media.HeatMapGenerator;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.misc.Preferences.HeatMapTimeSource;
import de.uni_stuttgart.visus.etfuse.misc.Utils;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class VideoSurfacePanel extends JPanel {

    static private final long serialVersionUID = 1617960366446486220L;
    static private VideoSurfacePanel lastInstance;
    protected BufferedImage image;
    protected VideoCapture camera;
    protected ArrayList<OverlayGazeProjector> projectors; // heatmaps?
    private Boolean paintHeatMap = false;
    private Boolean repaintHeatMap = false;
    private Mat compositeHeatMap = null;
    private Boolean paintGazePlot = false;
    private Boolean paintRawDataPlot = false;
    private VideoFrame parentVideoFrame = null;
    private ArrayList<Long> heatmapEvents = new ArrayList<Long>();
    private HashMap<Long, Color> customEvents = new HashMap<Long, Color>(); // contains frames

    static public VideoSurfacePanel lastInstanceIfExists() {

        return lastInstance;
    }

    public VideoSurfacePanel() {

        this.projectors = new ArrayList<OverlayGazeProjector>();
        setBackground(Color.black);
        setOpaque(true);

        lastInstance = this;
    }

    public void setParentVideoFrame(VideoFrame frame) {

        this.parentVideoFrame = frame;
    }

    public void attachCamera(VideoCapture camera) {

        this.camera = camera;
    }

    public VideoCapture getCamera() {

        return this.camera;
    }

    public void attachProjector(OverlayGazeProjector projector) {

        this.projectors.add(projector);

        Preferences prefs = Project.currentProject().getPreferences();

        if (!parentVideoFrame.getSkipWhileLoadingProject()) {
            ArrayList<Integer> temp = prefs.getPlayerEventsForMinDistPlot();
            temp.add(this.projectors.size() -1);
            prefs.setPlayerEventsForMinDistPlot(temp);

            temp = prefs.getPlayerEventsForHeatmaps();
            temp.add(this.projectors.size() -1);
            prefs.setPlayerEventsForHeatmaps(temp);

            temp = prefs.getShowPlayerEventTicks();
            temp.add(this.projectors.size() -1);
            prefs.setShowPlayerEventTicks(temp);

            updateHeatmapEvents();
        }
    }

    public OverlayGazeProjector getProjector(int index) {

        return this.projectors.get(index);
    }

    public ArrayList<OverlayGazeProjector> getProjectors() {

        return this.projectors;
    }

    public void setImage(BufferedImage image) {

        this.image = image;
    }

    public long getCurrentTime() {
        return (long) Math.floor((camera.get(Videoio.CV_CAP_PROP_POS_FRAMES)
                / camera.get(Videoio.CAP_PROP_FPS)) * 1000);
    }

    public long getTimeForFrame(int frame) {
        return (long) Math.floor((frame
                / camera.get(Videoio.CAP_PROP_FPS)) * 1000);
    }

    @Override
    protected void paintComponent(Graphics g) {

        Preferences prefs = Project.currentProject().getPreferences();
        Graphics2D g2 = (Graphics2D) g;

        int mediaWidth = (int) this.camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int mediaHeight = (int) this.camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();

        g2.setBackground(Color.BLACK);
        g2.clearRect(0, 0, mediaWidth, mediaHeight);

        AffineTransform saveAT = g2.getTransform();
        saveAT.scale((double) panelWidth / (double) mediaWidth, (double) panelHeight / (double) mediaHeight);
        g2.setTransform(saveAT);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Composite originalComposite = g2.getComposite();
        if (!paintHeatMap || Project.currentProject().getPreferences().getHeatMapOverlayPlayer()
                >= this.projectors.size()) { // heatmap for multiple players
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float)(prefs.getVideoTransparency() / 100.0)));
        }
        g2.drawImage(image, null, 0, 0);
        g2.setComposite(originalComposite);

        Boolean paintHeatMapPrefs = prefs.getEnableHeatMapOverlay();
        Boolean paintGazePlotPrefs = prefs.getEnableFixationOverlay();
        Boolean paintRawDataPlotPrefs = prefs.getEnableRawDataOverlay();

        if (paintHeatMap && paintHeatMapPrefs)
            paintHeatMap(g);

        if (paintRawDataPlot && paintRawDataPlotPrefs)
            paintRawGazeDot(g);

        if (paintGazePlot && paintGazePlotPrefs)
            paintGazePlot(g);
    }

    private void paintRawGazeDot(Graphics g) {

        if (camera == null)
            return;

        if (projectors == null || projectors.size() < 1)
            return;

        Graphics2D g2 = (Graphics2D) g;

        long currentTime = getCurrentTime();

        for (OverlayGazeProjector projector : this.projectors) {

            ArrayList<EyeTrackerEyeEvent> lastEvents =
                    projector.xEventsBeforeShiftedTimestamp(1, currentTime, true, false);

            if (lastEvents == null || lastEvents.size() < 1)
                continue;

            g2.setColor(Color.gray);
            g2.setStroke(new BasicStroke(10));

            for (int i = lastEvents.size() - 1; i > -1; i--) {

                // original color but with transparency and brighter/darker
                Color newColor;
                if (projector.getRecording().preferredGazeColor.getRed()
                        + projector.getRecording().preferredGazeColor.getGreen()
                        + projector.getRecording().preferredGazeColor.getBlue() < 350) {
                    newColor = projector.getRecording().preferredGazeColor.brighter();
                }
                else {
                    newColor = projector.getRecording().preferredGazeColor.darker();
                }
                g2.setColor(new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 100));

                EyeTrackerEyeEvent curEvent = lastEvents.get(i);

                if (curEvent == null)
                    continue;

                Ellipse2D ellipse = new Ellipse2D.Double(curEvent.fixationPointX - 5,
                                                         curEvent.fixationPointY - 5, 10, 10);
                g2.draw(ellipse);
            }
        }
    }

    private void paintGazePlot(Graphics g) {

        int msRange = Project.currentProject().getPreferences().getFixationOverlayTimeSpan();
        int fadeDuration = 500;

        if (camera == null)
            return;

        if (projectors == null || projectors.size() < 1)
            return;

        Graphics2D g2 = (Graphics2D) g;

        long currentTime = getCurrentTime();

        for (OverlayGazeProjector projector : this.projectors) {


            ArrayList<EyeTrackerEyeEvent> lastEvents =
                    projector.eventsBetweenShiftedTimestamps(currentTime - (msRange + fadeDuration),
                                                             currentTime, false, false);

            if (lastEvents == null || lastEvents.size() < 1) // fallback nach sehr langer fixation
                lastEvents = projector.xEventsBeforeShiftedTimestamp(10, currentTime, false, false);

            if (lastEvents == null || lastEvents.size() < 1)
                continue;

            for (int i = lastEvents.size() - 1; i > -1; i--) {

                g2.setStroke(new BasicStroke(10));

                g2.setColor(new Color(projector.getRecording().preferredGazeColor.getRed(),
                                      projector.getRecording().preferredGazeColor.getGreen(),
                                      projector.getRecording().preferredGazeColor.getBlue(), 100));

                EyeTrackerEyeEvent curEvent = lastEvents.get(i);

                if (curEvent == null)
                    continue;

                if (i == 0 && currentTime < (curEvent.timestamp - projector.getTimeSyncOffset())
                        + curEvent.fixationDuration)
                    g2.setColor(projector.getRecording().preferredGazeColor);

                Ellipse2D ellipse = new Ellipse2D.Double(curEvent.fixationPointX - 15,
                        curEvent.fixationPointY - 15, 30, 30);
                Ellipse2D outer = new Ellipse2D.Double(curEvent.fixationPointX - 20,
                        curEvent.fixationPointY - 20, 40, 40);

                Composite former = g2.getComposite();
                g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.f, Math.min(1.f,
                        1.f - ((float) (currentTime - ((curEvent.timestamp
                                - projector.getTimeSyncOffset()) + curEvent.fixationDuration
                                + msRange)) / (fadeDuration))))));
                g2.draw(ellipse);

                g2.setColor(projector.getRecording().preferredGazeColor);
                g2.setStroke(new BasicStroke(2));
                g2.draw(outer);

                g2.setColor(new Color(projector.getRecording().preferredGazeColor.getRed(),
                                      projector.getRecording().preferredGazeColor.getGreen(),
                                      projector.getRecording().preferredGazeColor.getBlue(), 100));

                if (i > 0) {

                    EyeTrackerEyeEvent lastEvent = lastEvents.get(i - 1);

                    if (lastEvent == null)
                        continue;

                    Line2D line = new Line2D.Double(lastEvent.fixationPointX, lastEvent.fixationPointY,
                                                    curEvent.fixationPointX, curEvent.fixationPointY);
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(line);
                    g2.setStroke(new BasicStroke(10));
                }

                g2.setComposite(former);
            }
        }
    }

    private void paintHeatMap(Graphics g) {

        if (camera == null)
            return;

        if (repaintHeatMap) {
            for (int i = 0; i < this.projectors.size(); i++) {
                OverlayGazeProjector proj = this.projectors.get(i);

                HeatMapGenerator mapGen = new HeatMapGenerator(i, 0, HeatMapTimeSource.USERDEFINED, this.parentVideoFrame);
                mapGen.attachVideoFrameForTitleUpdate(this.parentVideoFrame);
                mapGen.execute();
                for (int j = 0; j <= this.parentVideoFrame.getPanel().getHeatmapEvents().size(); j++) {
                    mapGen = new HeatMapGenerator(i, j, HeatMapTimeSource.CLICKS, this.parentVideoFrame);
                    mapGen.attachVideoFrameForTitleUpdate(this.parentVideoFrame);
                    mapGen.execute();
                }
            }

            repaintHeatMap = false;
        }

        if (Project.currentProject().getPreferences().getHeatMapOverlayPlayer() < this.projectors.size()) { // one player
            int heatMapToPaint = Project.currentProject().getPreferences().getHeatMapOverlayPlayer();

            if (this.projectors.size() > heatMapToPaint)
                this.compositeHeatMap = this.projectors.get(heatMapToPaint).getCurrentTransparentHeatMap();
            else
                return;

            if (this.compositeHeatMap != null) {

                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(Utils.Mat2BufferedImage(this.compositeHeatMap), null, 0, 0);
            }
        }
        else { // 2 playes
            for (int heatMapToPaint = 0; heatMapToPaint < 2; heatMapToPaint++) {
                if (this.projectors.size() > heatMapToPaint)
                    this.compositeHeatMap = this.projectors.get(heatMapToPaint).getCurrentUniColorTransparentHeatMap();
                else
                    return;

                if (this.compositeHeatMap != null) {

                    Graphics2D g2 = (Graphics2D) g;
                    g2.drawImage(Utils.Mat2BufferedImage(this.compositeHeatMap), null, 0, 0);
                }
            }
        }
    }

    public void setRepaintHeatMap() {
        repaintHeatMap = true;
    }

    public Boolean getPaintGazePlot() {
        return paintGazePlot;
    }

    public void setPaintGazePlot(Boolean paintGazePlot) {
        this.paintGazePlot = paintGazePlot;
    }

    public Boolean getPaintRawDataPlot() {
        return paintRawDataPlot;
    }

    public void setPaintRawDataPlot(Boolean paintRawDataPlot) {
        this.paintRawDataPlot = paintRawDataPlot;
    }

    public Boolean getPaintHeatMap() {
        return paintHeatMap;
    }

    public void setPaintHeatMap(Boolean paintHeatMap) {
        this.paintHeatMap = paintHeatMap;
    }

    public ArrayList<Long> getHeatmapEvents() {
        return heatmapEvents;
    }

    public void updateHeatmapEvents() {
        Preferences prefs = Project.currentProject().getPreferences();
        heatmapEvents = new ArrayList<Long>();
        for (int i = 0; i < prefs.getPlayerEventsForHeatmaps().size(); i++) {
            if (this.getProjectors().size() > prefs.getPlayerEventsForHeatmaps().get(i)) {
                OverlayGazeProjector proj = this.getProjectors().get(prefs.getPlayerEventsForHeatmaps().get(i));
                ArrayList<Long> tempEvents = proj.getRecording().getClicks();
                for (int j = 0; j < tempEvents.size(); ++j) {
                    long c =  tempEvents.get(j) - proj.getTimeSyncOffset();
                    heatmapEvents.add(c);
                }
            }
        }
        if (prefs.getUseAdditionalEventForHeatmaps()) {
            heatmapEvents.addAll(this.getCustomEvents().keySet());
        }
        Collections.sort(heatmapEvents);
    }

    public void setCustomEvents(HashMap<Long, Color> events) {
        this.customEvents = events;
        updateHeatmapEvents();
        this.setRepaintHeatMap();
    }

    public HashMap<Long, Color> getCustomEvents() {
        return this.customEvents;
    }
}
