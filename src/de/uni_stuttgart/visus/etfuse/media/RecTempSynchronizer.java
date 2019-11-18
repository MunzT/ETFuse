package de.uni_stuttgart.visus.etfuse.media;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.misc.Utils;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class RecTempSynchronizer {
	
	private static class ColorStamp { 
		  public final int color;
		  public final int frame;
		  public ColorStamp(int color, int frame) { 
		    this.color = color; 
		    this.frame = frame; 
		  } 
	}

	private static long computePreciseOffset_histogram(EyeTrackerRecording host, EyeTrackerRecording guest, VideoCapture hostCam, VideoCapture guestCam, long roughGuess, int orientationFrame) {

		Preferences prefs = Project.currentProject().getPreferences();
		
		int histGridSize = prefs.getHistogramGridSize();
		int histCorrelThreshold = prefs.getHistogramCorrelationThreshold();
		int histDeviatingCellsThreshold = prefs.getHistogramDeviatingCellsThreshold();
		
		// Zwischen 16 und 20, da Rechenaufwand mindestens n * quadratisch hierzu zunimmt
		/*int kernelSize = 16;
		double correlationThreshold = 0.4;
		int numDifferThreshold = 1;*/
		
		int kernelSize = histGridSize;
		double correlationThreshold = ((double) histCorrelThreshold) / 100.0;
		int numDifferThreshold = histDeviatingCellsThreshold;

		hostCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, 0.0);
		guestCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, 0.0);

		long timeShift = roughGuess;

		Mat imageHost = new Mat();
		Mat imageHostPlayingField = new Mat();
		Mat[] histHost;
		Mat imageGuest = new Mat();
		Mat imageGuestPlayingField = new Mat();
		Mat[] histGuest;
		BufferedImage bufImgHost = null;
		BufferedImage bufImgGuest = null;

		float[] histogramRanges = {0, 180, 0, 256};
		int[] histogramSize = {50, 60};
		int[] histogramChannels = {0, 1};

		int click = (int) Math.floor((host.getClicks().size() * 0.5));
		long sampleClickTS = (long) Math.floor(host.getClicks().get(click) + (host.getClicks().get(click + 1) - host.getClicks().get(click)) * 0.15);

		long lowerFrameEstimate = (long) Math.floor(((double) sampleClickTS / 1000.0) * hostCam.get(Videoio.CV_CAP_PROP_FPS));
		if (lowerFrameEstimate < 2) lowerFrameEstimate = 2;
		//lowerFrameEstimate = 1920; // DEBUG
		if (orientationFrame != 0)
			lowerFrameEstimate = orientationFrame;
		hostCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, lowerFrameEstimate);

		if (hostCam.read(imageHost)) {					
			ArrayList<Mat> imgList = new ArrayList<Mat>();
			Imgproc.cvtColor(imageHost.submat(new Rect(host.getFramePoint1().x, host.getFramePoint1().y, host.getFramePoint2().x - host.getFramePoint1().x, host.getFramePoint2().y - host.getFramePoint1().y)), imageHostPlayingField, Imgproc.COLOR_BGR2HSV);

			Imgproc.resize(imageHostPlayingField, imageHostPlayingField, new Rect(guest.getFramePoint1().x, guest.getFramePoint1().y, guest.getFramePoint2().x - guest.getFramePoint1().x, guest.getFramePoint2().y - guest.getFramePoint1().y).size());

			Mat[] splitPlayingField = Utils.splitMatrixIntoCells(imageHostPlayingField, kernelSize);
			histHost = new Mat[splitPlayingField.length];

			for (int i = 0; i < splitPlayingField.length; i++) {

				imgList.clear();
				imgList.add(splitPlayingField[i]);
				histHost[i] = new Mat();
				Imgproc.calcHist(imgList, new MatOfInt(histogramChannels), new Mat(), histHost[i], new MatOfInt(histogramSize), new MatOfFloat(histogramRanges), false);
			}
		}
		else
			return 0;

		bufImgHost = Utils.Mat2BufferedImage(imageHostPlayingField);


		// finde erstes �ber x% abweichendes frame
		
		Mat[] histHostCur;
		int firstNewFrameHost = 0;
		int foundCounter = 0;
		
		while (hostCam.read(imageHost)) {
			ArrayList<Mat> imgList = new ArrayList<Mat>();
			Imgproc.cvtColor(imageHost.submat(new Rect(host.getFramePoint1().x, host.getFramePoint1().y, host.getFramePoint2().x - host.getFramePoint1().x, host.getFramePoint2().y - host.getFramePoint1().y)), imageHostPlayingField, Imgproc.COLOR_BGR2HSV);

			Imgproc.resize(imageHostPlayingField, imageHostPlayingField, new Rect(guest.getFramePoint1().x, guest.getFramePoint1().y, guest.getFramePoint2().x - guest.getFramePoint1().x, guest.getFramePoint2().y - guest.getFramePoint1().y).size());

			Mat[] splitPlayingField = Utils.splitMatrixIntoCells(imageHostPlayingField, kernelSize);
			histHostCur = new Mat[splitPlayingField.length];

			for (int i = 0; i < splitPlayingField.length; i++) {

				imgList.clear();
				imgList.add(splitPlayingField[i]);
				histHostCur[i] = new Mat();
				Imgproc.calcHist(imgList, new MatOfInt(histogramChannels), new Mat(), histHostCur[i], new MatOfInt(histogramSize), new MatOfFloat(histogramRanges), false);
			}

			double[] correlation = new double[histHostCur.length];

			for (int i = 0; i < correlation.length; i++) {

				correlation[i] = Imgproc.compareHist(histHostCur[i], histHost[i], Imgproc.CV_COMP_CORREL);
				
				if (correlation[i] <= correlationThreshold) {
					firstNewFrameHost = (int) (hostCam.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1);
					System.out.println("<RecTempSynchronizer> StartingFrame: " + lowerFrameEstimate + " Host Frame: " + firstNewFrameHost);
					histHost = histHostCur;
					foundCounter++;
				}
			}
			
			if (foundCounter >= numDifferThreshold)
				break;
		}
		
		bufImgHost = Utils.Mat2BufferedImage(imageHostPlayingField);

		sampleClickTS = (int) (firstNewFrameHost / hostCam.get(Videoio.CV_CAP_PROP_FPS) * 1000);

		int startingFrameGuess = (int) ((sampleClickTS + roughGuess) / 1000 * guestCam.get(Videoio.CV_CAP_PROP_FPS)) - 150;
		if (startingFrameGuess < 0) startingFrameGuess = 0;
		guestCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, startingFrameGuess);

		double biggestCorrelation = 0.0;
		int biggestCorrelationFrame = 0;
		int iterations = 0;
		Mat biggestCorrelationImg = null;

		histGuest = new Mat[0];
		Mat[] histGuestBiggest = new Mat[0];

		while (guestCam.read(imageGuest)) {
			ArrayList<Mat> imgList = new ArrayList<Mat>();
			Imgproc.cvtColor(imageGuest.submat(new Rect(guest.getFramePoint1().x, guest.getFramePoint1().y, guest.getFramePoint2().x - guest.getFramePoint1().x, guest.getFramePoint2().y - guest.getFramePoint1().y)), imageGuestPlayingField, Imgproc.COLOR_BGR2HSV);
			
			Mat[] splitPlayingField = Utils.splitMatrixIntoCells(imageGuestPlayingField, kernelSize);
			histGuest = new Mat[splitPlayingField.length];

			for (int i = 0; i < splitPlayingField.length; i++) {

				imgList.clear();
				imgList.add(splitPlayingField[i]);
				histGuest[i] = new Mat();
				Imgproc.calcHist(imgList, new MatOfInt(histogramChannels), new Mat(), histGuest[i], new MatOfInt(histogramSize), new MatOfFloat(histogramRanges), false);
			}

			double[] correlation = new double[histGuest.length];

			for (int i = 0; i < correlation.length; i++) {

				correlation[i] = Imgproc.compareHist(histGuest[i], histHost[i], Imgproc.CV_COMP_CORREL);
			}

			double sum = Utils.getArraySum(correlation);
			if (sum > biggestCorrelation) {
				biggestCorrelation = sum;
				biggestCorrelationFrame = (int) (guestCam.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1);
				biggestCorrelationImg = imageGuestPlayingField;
				bufImgGuest = Utils.Mat2BufferedImage(biggestCorrelationImg);
				histGuestBiggest = histGuest;
			}

			if ((iterations % 100) == 0) {

				System.gc();
			}

			iterations++;

			if (iterations > 299)
				break;
		}

		// finde erstes vorhergehendes �ber 9% abweichendes frame

		Mat[] histGuestCur;
		int firstNewFrameGuest = 0;
		foundCounter = 0;

		for (int minus = 1; minus < 300; minus++) {
			guestCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, biggestCorrelationFrame - minus);

			if (minus > biggestCorrelationFrame)
				break;

			if (!guestCam.read(imageGuest))
				break;

			ArrayList<Mat> imgList = new ArrayList<Mat>();
			Imgproc.cvtColor(imageGuest.submat(new Rect(guest.getFramePoint1().x, guest.getFramePoint1().y, guest.getFramePoint2().x - guest.getFramePoint1().x, guest.getFramePoint2().y - guest.getFramePoint1().y)), imageGuestPlayingField, Imgproc.COLOR_BGR2HSV);
			
			Mat[] splitPlayingField = Utils.splitMatrixIntoCells(imageGuestPlayingField, kernelSize);
			histGuestCur = new Mat[splitPlayingField.length];

			for (int i = 0; i < splitPlayingField.length; i++) {

				imgList.clear();
				imgList.add(splitPlayingField[i]);
				histGuestCur[i] = new Mat();
				Imgproc.calcHist(imgList, new MatOfInt(histogramChannels), new Mat(), histGuestCur[i], new MatOfInt(histogramSize), new MatOfFloat(histogramRanges), false);
			}

			double[] correlation = new double[histGuestCur.length];

			for (int i = 0; i < correlation.length; i++) {

				correlation[i] = Imgproc.compareHist(histGuestCur[i], histGuestBiggest[i], Imgproc.CV_COMP_CORREL);
				
				if (correlation[i] <= correlationThreshold) {
					
					firstNewFrameGuest = (int) (guestCam.get(Videoio.CV_CAP_PROP_POS_FRAMES));
					System.out.println("<RecTempSynchronizer> StartingFrame: " + biggestCorrelationFrame + " Gesuchtes Guest-Frame: " + firstNewFrameGuest);
					bufImgGuest = Utils.Mat2BufferedImage(imageGuestPlayingField);
					
					foundCounter++;
				}
			}
			
			if (foundCounter >= numDifferThreshold)
				break;
		}

		long tsGuest = (int) (firstNewFrameGuest / guestCam.get(Videoio.CV_CAP_PROP_FPS) * 1000);
		long tsHost = (int) (firstNewFrameHost / hostCam.get(Videoio.CV_CAP_PROP_FPS) * 1000);
		timeShift = tsGuest - sampleClickTS;

		System.out.println("<RecTempSynchronizer> Guess: " + roughGuess + " Host TS: " + tsHost + " Guest TS: " + tsGuest + " Diff: " + timeShift + " HFrame: " + firstNewFrameHost + " GFrame: " + firstNewFrameGuest/* + " Correlation: " + biggestCorrelation + " Frame: " + biggestCorrelationFrame*/);

		System.gc();

		return timeShift;
	}

	private static long computePreciseOffset_custom(EyeTrackerRecording host, EyeTrackerRecording guest, VideoCapture hostCam, VideoCapture guestCam, long roughGuess, Point stoneCoord) {

		hostCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, 0.0);
		guestCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, 0.0);

		long timeShift = roughGuess;

		int xHr = 15 + host.getFramePoint1().x + ((host.getFramePoint2().x - host.getFramePoint1().x) / 2);
		int yHr = 15 + host.getFramePoint1().y + ((host.getFramePoint2().y - host.getFramePoint1().y) / 2);
		int xHl = -15 + host.getFramePoint1().x + ((host.getFramePoint2().x - host.getFramePoint1().x) / 2);
		int yHl = -15 + host.getFramePoint1().y + ((host.getFramePoint2().y - host.getFramePoint1().y) / 2);
		
		if (stoneCoord != null) {
			xHr = 15 + stoneCoord.x;
			yHr = 15 + stoneCoord.y;
			xHl = -15 + stoneCoord.x;
			yHl = -15 + stoneCoord.y;
		}

		Point transformedCoordr = Utils.transformCoordinate(new Point(xHr, yHr), new Line2D.Double(host.getFramePoint1(), host.getFramePoint2()), new Line2D.Double(guest.getFramePoint1(), guest.getFramePoint2()));
		int xGr = transformedCoordr.x;
		int yGr = transformedCoordr.y;
		Point transformedCoordl = Utils.transformCoordinate(new Point(xHl, yHl), new Line2D.Double(host.getFramePoint1(), host.getFramePoint2()), new Line2D.Double(guest.getFramePoint1(), guest.getFramePoint2()));
		int xGl = transformedCoordl.x;
		int yGl = transformedCoordl.y;

		Mat imageHost = new Mat();
		Mat imageGuest = new Mat();
		BufferedImage bufImgHost = null;
		BufferedImage bufImgGuest = null;

		ArrayList<ArrayList<ColorStamp>> historyHostr = new ArrayList<ArrayList<ColorStamp>>();
		ArrayList<ArrayList<ColorStamp>> historyHostl = new ArrayList<ArrayList<ColorStamp>>();
		ArrayList<ArrayList<ColorStamp>> historyGuestr = new ArrayList<ArrayList<ColorStamp>>();
		ArrayList<ArrayList<ColorStamp>> historyGuestl = new ArrayList<ArrayList<ColorStamp>>();

		// 17 proben zeitlich um jeden klick herum sammeln
		for (long clickTS : host.getClicks()) {

			long lowerFrameEstimate = (long) Math.floor(((double) clickTS / 1000.0) * hostCam.get(Videoio.CV_CAP_PROP_FPS)) - 8;
			hostCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, lowerFrameEstimate);

			ArrayList<ColorStamp> colorsAroundClickr = new ArrayList<ColorStamp>();
			ArrayList<ColorStamp> colorsAroundClickl = new ArrayList<ColorStamp>();

			for (int i = 0; i < 17; i++) {
				if (hostCam.read(imageHost)) {					
					bufImgHost = Utils.Mat2BufferedImage(imageHost);
					int newColorr = bufImgHost.getRGB(xHr, yHr);
					colorsAroundClickr.add(new ColorStamp(newColorr, (int) (hostCam.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1.0)));
					int newColorl = bufImgHost.getRGB(xHl, yHl);
					colorsAroundClickl.add(new ColorStamp(newColorl, (int) (hostCam.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1.0)));
				}
			}

			if (colorsAroundClickr.size() > 0)
				historyHostr.add(colorsAroundClickr);

			if (colorsAroundClickl.size() > 0)
				historyHostl.add(colorsAroundClickl);
		}

		if (historyHostr.size() != historyHostl.size()) {
			System.out.println("<RecTempSynchronizer> Ungleiche Anzahl der Samples. Breche ab.");
			return timeShift;
		}

		// finde letztes vorkommen einer beigen farbe
		//Color beige = new Color(215, 172, 87);
		//Color beige = new Color(148, 123, 82);
		Color beige = new Color(0xb0, 0x8b, 0x31);			

		Boolean found = false;
		ColorStamp firstStampWithStone = null;

		VideoCapture activePlayer = null;
		VideoCapture passivePlayer = null;

		int xPr = 0;
		int yPr = 0;
		int xPl = 0;
		int yPl = 0;

		// schauen, ob host-cam den stein setzt (bzw der stein in der n�he eines klicks auftaucht)

		for (int i = 0; i < historyHostr.size(); i++) {

			ArrayList<ColorStamp> stampsr = historyHostr.get(i);
			ArrayList<ColorStamp> stampsl = historyHostl.get(i);

			ColorStamp firstStampr = stampsr.get(0);
			ColorStamp firstStampl = stampsl.get(0);


			if (14.0 <= Utils.computeSRGBDistance(firstStampr.color, beige.getRGB()) && 14.0 <= Utils.computeSRGBDistance(firstStampl.color, beige.getRGB()))
				continue; // vor dem klick sind beide nicht beige -> uninteressant, stein wird nicht gesetzt

			// wir haben (mind.) eine beige farbe

			for (int j = 1; j < stampsr.size(); j++) {

				if (14.0 <= Utils.computeSRGBDistance(stampsr.get(j).color, beige.getRGB()) && 14.0 <= Utils.computeSRGBDistance(stampsl.get(j).color, beige.getRGB())) {

					firstStampWithStone = stampsr.get(j);
					activePlayer = hostCam;
					passivePlayer = guestCam;

					xPr = xGr;
					yPr = yGr;
					xPl = xGl;
					yPl = yGl;

					found = true;

					System.out.println("<RecTempSynchronizer> Host setzt Stein.");

					break;
				}
			}

			if (found)
				break;
		}

		if (!found) {
			// wiederholen mit guest, falls host-cam scheinbar den stein nicht setzt

			// samplen...
			for (long clickTS : guest.getClicks()) {

				long lowerFrameEstimate = (long) Math.floor(((double) clickTS / 1000.0) * guestCam.get(Videoio.CV_CAP_PROP_FPS)) - 8;
				guestCam.set(Videoio.CV_CAP_PROP_POS_FRAMES, lowerFrameEstimate);

				ArrayList<ColorStamp> colorsAroundClickr = new ArrayList<ColorStamp>();
				ArrayList<ColorStamp> colorsAroundClickl = new ArrayList<ColorStamp>();

				for (int i = 0; i < 17; i++) {
					if (guestCam.read(imageGuest)) {
						bufImgGuest = Utils.Mat2BufferedImage(imageGuest);
						int newColorr = bufImgGuest.getRGB(xGr, yGr);
						colorsAroundClickr.add(new ColorStamp(newColorr, (int) (guestCam.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1.0)));
						int newColorl = bufImgGuest.getRGB(xGl, yGl);
						colorsAroundClickl.add(new ColorStamp(newColorl, (int) (guestCam.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1.0)));
					}
				}

				if (colorsAroundClickr.size() > 0)
					historyGuestr.add(colorsAroundClickr);

				if (colorsAroundClickl.size() > 0)
					historyGuestl.add(colorsAroundClickl);
			}

			if (historyGuestr.size() != historyGuestl.size()) {
				System.out.println("<RecTempSynchronizer> Ungleiche Anzahl der Samples. Breche ab.");
				return timeShift;
			}

			for (int i = 0; i < historyGuestr.size(); i++) {

				ArrayList<ColorStamp> stampsr = historyGuestr.get(i);
				ArrayList<ColorStamp> stampsl = historyGuestl.get(i);

				ColorStamp firstStampr = stampsr.get(0);
				ColorStamp firstStampl = stampsl.get(0);

				if (14.0 <= Utils.computeSRGBDistance(firstStampr.color, beige.getRGB()) && 14.0 <= Utils.computeSRGBDistance(firstStampl.color, beige.getRGB()))
					continue; // vor dem klick ist nicht beige -> uninteressant, stein wird nicht gesetzt

				// wir haben eine beige farbe

				for (int j = 1; j < stampsr.size(); j++) {

					if (14.0 <= Utils.computeSRGBDistance(stampsr.get(j).color, beige.getRGB()) && 14.0 <= Utils.computeSRGBDistance(stampsl.get(j).color, beige.getRGB())) {

						firstStampWithStone = stampsr.get(j);
						activePlayer = guestCam;
						passivePlayer = hostCam;

						xPr = xHr;
						yPr = yHr;
						xPl = xHl;
						yPl = yHl;

						found = true;

						roughGuess *= -1;

						System.out.println("<RecTempSynchronizer> Gast setzt Stein.");

						break;
					}
				}

				if (found)
					break;
			}
		}

		if (!found) {
			System.out.println("<RecTempSynchronizer> Keiner der beiden Spieler scheint den Stein zu setzen. Nutze Sch�tzwert.");
			return timeShift;
		}

		long timeStamp = (int) (firstStampWithStone.frame / activePlayer.get(Videoio.CV_CAP_PROP_FPS) * 1000);

		int startingFrameGuess = (int) ((timeStamp + roughGuess) / 1000 * passivePlayer.get(Videoio.CV_CAP_PROP_FPS)) - 10;
		int endingFrameGuess = startingFrameGuess + 20;

		passivePlayer.set(Videoio.CV_CAP_PROP_POS_FRAMES, startingFrameGuess);

		Mat imagePassive = new Mat();
		BufferedImage bufImagePassive = null;

		ArrayList<ColorStamp> histogramPassiver = new ArrayList<ColorStamp>();
		ArrayList<ColorStamp> histogramPassivel = new ArrayList<ColorStamp>();

		while (passivePlayer.read(imagePassive)) {

			bufImagePassive = Utils.Mat2BufferedImage(imagePassive);

			int newColorr = bufImagePassive.getRGB(xPr, yPr);
			ColorStamp newStampr = new ColorStamp(newColorr, (int) (passivePlayer.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1.0));
			histogramPassiver.add(newStampr);

			int newColorl = bufImagePassive.getRGB(xPl, yPl);
			ColorStamp newStampl = new ColorStamp(newColorl, (int) (passivePlayer.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1.0));
			histogramPassivel.add(newStampl);

			if (newStampl.frame >= endingFrameGuess)
				break;
		}

		int s = histogramPassiver.size() - 1;		
		while (s >= 0) {

			int colorr = histogramPassiver.get(s).color;
			int colorl = histogramPassivel.get(s).color;

			// finde beige von hinten
			if (14.0 > Utils.computeSRGBDistance(beige.getRGB(), colorr) || 14.0 > Utils.computeSRGBDistance(beige.getRGB(), colorl))
				break;

			s--;
		}
		
		if (s < 0)
			s = 0;

		passivePlayer.set(Videoio.CV_CAP_PROP_POS_FRAMES, histogramPassiver.get(s).frame);

		int firstBlackColorPassive = 0;

		while (passivePlayer.read(imagePassive)) {

			bufImagePassive = Utils.Mat2BufferedImage(imagePassive);
			int newColorr = bufImagePassive.getRGB(xPr, yPr);
			int newColorl = bufImagePassive.getRGB(xPl, yPl);

			if (14.0 <= Utils.computeSRGBDistance(beige.getRGB(), newColorr) && 14.0 <= Utils.computeSRGBDistance(beige.getRGB(), newColorl)) {

				firstBlackColorPassive = newColorr;
				break;
			}
		}

		int firstFrameWithStonePassive = (int) ((passivePlayer.get(Videoio.CV_CAP_PROP_POS_FRAMES) - 1.0));
		long timeStampPassive = (int) (firstFrameWithStonePassive / passivePlayer.get(Videoio.CV_CAP_PROP_FPS) * 1000);

		System.out.println("<RecTempSynchronizer> Active (" + firstStampWithStone.frame + ") | Color: " + firstStampWithStone.color + " distance: " + Utils.computeSRGBDistance(beige.getRGB(), firstStampWithStone.color) + " frame: " + firstStampWithStone.frame);
		System.out.println("<RecTempSynchronizer> Passive | Color: " + firstBlackColorPassive + " distance: " + Utils.computeSRGBDistance(beige.getRGB(), firstBlackColorPassive) + " frame: " + firstFrameWithStonePassive);

		int activeTS = (int) (firstStampWithStone.frame / activePlayer.get(Videoio.CV_CAP_PROP_FPS) * 1000);
		int passiveTS = (int) (firstFrameWithStonePassive / passivePlayer.get(Videoio.CV_CAP_PROP_FPS) * 1000);

		timeShift = passiveTS - activeTS;

		if (activePlayer.equals(guestCam)) { // zur�ckdrehen
			roughGuess *= -1;
			timeShift *= -1;
		}

		System.out.println("<RecTempSynchronizer> Shift: " + timeShift + ", Guess war: " + roughGuess);

		return timeShift;
	}

	public static long computeTimestampOffset(EyeTrackerRecording host, EyeTrackerRecording guest) {

		return host.recordingStartTS - guest.recordingStartTS;
	}

	public static long computeTimeOffsetCustom(EyeTrackerRecording host, EyeTrackerRecording guest, VideoCapture hostCam, VideoCapture guestCam, Point stoneCoord) {

		long timeShift = computeTimestampOffset(host, guest);

		if (guestCam == null)
			return timeShift;

		timeShift = computePreciseOffset_custom(host, guest, hostCam, guestCam, timeShift, stoneCoord);

		return timeShift;
	}
	
	public static long computeTimeOffsetHistogram(EyeTrackerRecording host, EyeTrackerRecording guest, VideoCapture hostCam, VideoCapture guestCam, int orientationFrame) {

		long timeShift = computeTimestampOffset(host, guest);

		if (guestCam == null)
			return timeShift;

		timeShift = computePreciseOffset_histogram(host, guest, hostCam, guestCam, timeShift, orientationFrame);

		return timeShift;
	}
}
