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

import javax.swing.JPanel;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.media.HeatMapGenerator;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.misc.Utils;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class VideoSurfacePanel extends JPanel {

	static private final long serialVersionUID = 1617960366446486220L;
	static private VideoSurfacePanel lastInstance;
	protected BufferedImage image;
	protected VideoCapture camera;
	protected ArrayList<OverlayGazeProjector> projectors;
	private Boolean paintHeatMap = false;
	private Boolean repaintHeatMap = false;
	private Mat compositeHeatMap = null;
	private Boolean paintGazePlot = false;
	private Boolean paintRawDataPlot = false;
	private VideoFrame parentVideoFrame = null;

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

	@Override
	protected void paintComponent(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;
		
		int mediaWidth = (int) this.camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int mediaHeight = (int) this.camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		int panelWidth = this.getWidth();
		int panelHeight = this.getHeight();
		
		AffineTransform saveAT = g2.getTransform();
		saveAT.scale((double) panelWidth / (double) mediaWidth, (double) panelHeight / (double) mediaHeight);
		g2.setTransform(saveAT);
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.drawImage(image, null, 0, 0);
		
		Preferences prefs = Project.currentProject().getPreferences();
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

		long currentTime = (long) Math.floor((camera.get(Videoio.CV_CAP_PROP_POS_FRAMES) / camera.get(Videoio.CAP_PROP_FPS)) * 1000);

		for (OverlayGazeProjector projector : this.projectors) {

			ArrayList<EyeTrackerEyeEvent> lastEvents = projector.xEventsBeforeShiftedTimestamp(1, currentTime, true, false);

			if (lastEvents == null || lastEvents.size() < 1)
				continue;

			g2.setColor(Color.gray);
			g2.setStroke(new BasicStroke(10));

			for (int i = lastEvents.size() - 1; i > -1; i--) {
				
				if (projector.getRecording().preferredGazeColor == Color.blue)
					g2.setColor(Color.cyan);
				else if (projector.getRecording().preferredGazeColor == Color.red)
					g2.setColor(Color.pink);
				else if (projector.getRecording().preferredGazeColor == Color.orange)
					g2.setColor(Color.yellow);

				EyeTrackerEyeEvent curEvent = lastEvents.get(i);

				if (curEvent == null)
					continue;

				Ellipse2D ellipse = new Ellipse2D.Double(curEvent.fixationPointX - 5, curEvent.fixationPointY - 5, 10, 10);
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

		long currentTime = (long) Math.floor((camera.get(Videoio.CV_CAP_PROP_POS_FRAMES) / camera.get(Videoio.CAP_PROP_FPS)) * 1000);

		for (OverlayGazeProjector projector : this.projectors) {
			

			ArrayList<EyeTrackerEyeEvent> lastEvents = projector.eventsBetweenShiftedTimestamps(currentTime - (msRange + fadeDuration), currentTime, false, false);

			if (lastEvents == null || lastEvents.size() < 1) // fallback nach sehr langer fixation
				lastEvents = projector.xEventsBeforeShiftedTimestamp(10, currentTime, false, false);
			
			if (lastEvents == null || lastEvents.size() < 1)
				continue;

			for (int i = lastEvents.size() - 1; i > -1; i--) {
				
				g2.setStroke(new BasicStroke(10));
				g2.setColor(Color.gray);
				
				EyeTrackerEyeEvent curEvent = lastEvents.get(i);

				if (curEvent == null)
					continue;
				
				if (i == 0 && currentTime < (curEvent.timestamp - projector.getTimeSyncOffset()) + curEvent.fixationDuration)
					g2.setColor(projector.getRecording().preferredGazeColor);

				Ellipse2D ellipse = new Ellipse2D.Double(curEvent.fixationPointX - 15, curEvent.fixationPointY - 15, 30, 30);
				Ellipse2D outer = new Ellipse2D.Double(curEvent.fixationPointX - 20, curEvent.fixationPointY - 20, 40, 40);
				
				Composite former = g2.getComposite();
				g2.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.f, Math.min(1.f, 1.f - ((float) (currentTime - ((curEvent.timestamp - projector.getTimeSyncOffset()) + curEvent.fixationDuration + msRange)) / (float) (fadeDuration))))));
				g2.draw(ellipse);
				
				g2.setColor(projector.getRecording().preferredGazeColor);
				g2.setStroke(new BasicStroke(2));
				g2.draw(outer);
				
				g2.setColor(Color.gray);
				
				if (i > 0) {

					EyeTrackerEyeEvent lastEvent = lastEvents.get(i - 1);

					if (lastEvent == null)
						continue;

					Line2D line = new Line2D.Double(lastEvent.fixationPointX, lastEvent.fixationPointY, curEvent.fixationPointX, curEvent.fixationPointY);
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
				HeatMapGenerator mapGen = new HeatMapGenerator(proj);
				mapGen.attachVideoFrameForTitleUpdate(this.parentVideoFrame);
				mapGen.execute();
			}
			
			repaintHeatMap = false;
		}
		
		int heatMapToPaint = Project.currentProject().getPreferences().getHeatMapOverlayPlayer();
		
		if (this.projectors.size() > heatMapToPaint)
			this.compositeHeatMap = this.projectors.get(heatMapToPaint).getTransparentHeatMap();
		else
			return;
				
		if (this.compositeHeatMap != null) {
			
			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(Utils.Mat2BufferedImage(this.compositeHeatMap), null, 0, 0);
		}
	}

	private void setRepaintHeatMap() {
		
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
}