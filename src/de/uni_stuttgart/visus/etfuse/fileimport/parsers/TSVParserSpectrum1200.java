package de.uni_stuttgart.visus.etfuse.fileimport.parsers;

import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerEyeEvent;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;

public class TSVParserSpectrum1200 extends TSVParser {

	private enum Columns {

		TIMESTAMP(0), SCREENHEIGHT(11), SCREENWIDTH(12), EVENT(21), EVENTVALUE(22), POINT_X(23), POINT_Y(24), EYE_POS_LEFT_X(39),
		EYE_POS_LEFT_Y(40), EYE_POS_LEFT_Z(41), EYE_POS_RIGHT_X(42), EYE_POS_RIGHT_Y(43), 
		EYE_POS_RIGHT_Z(44), EYE_MOVEMENT_TYPE(63);

		private int index;

		Columns(int index) {
			this.index = index;
		}

		public int i() {
			return this.index;
		}
	}

	public String parserDescription() { return "Tobii Pro Spectrum 1200"; }

	public EyeTrackerRecording parseData(List<String> rawData) {
		
		if (rawData.size() < 1)
			return null;

		EyeTrackerRecording rec = new EyeTrackerRecording();
		
		ArrayList<String> map = (ArrayList<String>) rawData;
		
		int dataLinePointer = 0;

		while (dataLinePointer < map.size()) {

			if (map.get(dataLinePointer).split("\t")[0].contains("Recording timestamp"))
				break;

			dataLinePointer++;
		}
		
		String recordingStartTime = map.get(dataLinePointer + 1).split("\t")[6];
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss.SSS");
		Date recordingStartDate = null;
		
		try {
			recordingStartDate = sdf.parse(recordingStartTime);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(recordingStartDate);
		
		if (!(map.size() > dataLinePointer + 2))
			return rec;
		
		long timeStampDiv = 1; // 1: Datei enthält Timestamps in Millisekunden-Auflösung; 1000: Datei enthält Timestamps in Mikrosekunden-Auflösung
		if (Long.parseLong(map.get(dataLinePointer + 2).split("\t")[Columns.TIMESTAMP.i()]) > 10000)
			timeStampDiv = 1000;
		
		while (dataLinePointer < map.size()) {

			if (map.get(dataLinePointer).split("\t")[Columns.EVENT.i()].contains("ScreenRecordingStart"))
				break;

			dataLinePointer++;
		}

		dataLinePointer++;
		
		int screenWidth = 0;
		int screenHeight = 0;
		
		try {
			screenWidth = Integer.parseInt(map.get(dataLinePointer).split("\t")[Columns.SCREENWIDTH.i()]);
		} catch (NumberFormatException e) {}
		
		try {
			screenHeight = Integer.parseInt(map.get(dataLinePointer).split("\t")[Columns.SCREENHEIGHT.i()]);
		} catch (NumberFormatException e) {}
		
		Rectangle2D screenResolution = new Rectangle2D.Double(0, 0, screenWidth, screenHeight);
		rec.setScreenResolution(screenResolution);
		rec.setDisplayPPI(91.79);

		this.setProgress(0);
		long linesToProcess = map.size() - dataLinePointer;
		long processedLines = 0;
		double linesPerPercent = 100.0 / linesToProcess;

		int number = 0;
		
		long baseTimeStamp = Long.parseLong(map.get(dataLinePointer).split("\t")[Columns.TIMESTAMP.i()]) / timeStampDiv;
		
		while (dataLinePointer < map.size()) {

			String[] line = map.get(dataLinePointer).split("\t");

			if (line[Columns.EVENT.i()].length() == 0) {
				if (line[Columns.TIMESTAMP.i()].length() > 0) {

					EyeTrackerEyeEvent e = new EyeTrackerEyeEvent();
					e.number = number;
					e.timestamp = (Long.parseLong(line[Columns.TIMESTAMP.i()]) / timeStampDiv) - baseTimeStamp;
					
					if (line[Columns.EYE_MOVEMENT_TYPE.i()].contains("EyesNotFound"))
						e.eyesNotFound = true;

					if (line[Columns.POINT_X.i()].length() > 0)
						e.fixationPointX = Integer.parseInt(line[Columns.POINT_X.i()]);
					else
						e.eyesNotFound = true;

					if (line[Columns.POINT_Y.i()].length() > 0)
						e.fixationPointY = Integer.parseInt(line[Columns.POINT_Y.i()]);
					else
						e.eyesNotFound = true;
					
					if (line[Columns.EYE_POS_LEFT_X.i()].length() > 0)
						e.eyePosLeftX = Double.parseDouble(line[Columns.EYE_POS_LEFT_X.i()]);

					if (line[Columns.EYE_POS_RIGHT_X.i()].length() > 0)
						e.eyePosRightX = Double.parseDouble(line[Columns.EYE_POS_RIGHT_X.i()]);

					if (line[Columns.EYE_POS_LEFT_Y.i()].length() > 0)
						e.eyePosLeftY = Double.parseDouble(line[Columns.EYE_POS_LEFT_Y.i()]);

					if (line[Columns.EYE_POS_RIGHT_Y.i()].length() > 0)
						e.eyePosRightY = Double.parseDouble(line[Columns.EYE_POS_RIGHT_Y.i()]);
					
					if (line[Columns.EYE_POS_LEFT_Z.i()].length() > 0)
						e.eyePosLeftZ = Double.parseDouble(line[Columns.EYE_POS_LEFT_Z.i()]);

					if (line[Columns.EYE_POS_RIGHT_Z.i()].length() > 0)
						e.eyePosRightZ = Double.parseDouble(line[Columns.EYE_POS_RIGHT_Z.i()]);

					if (e.eyesNotFound) {
						e.fixationPointX = -10000;
						e.fixationPointY = -10000;
					}
					
					e.realFixationPointX = e.fixationPointX;
					e.realFixationPointY = e.fixationPointY;
										
					rec.addEyeEvent(e);
					number++;
				}
			}
			else if (line[Columns.EVENT.i()].contains("MouseEvent") && line[Columns.EVENTVALUE.i()].contains("Up, Left")) {
				
				long ts = (Long.parseLong(line[Columns.TIMESTAMP.i()]) / timeStampDiv) - baseTimeStamp;
				rec.addClick(ts);
			}
			else if (line[Columns.EVENT.i()].contains("ScreenRecordingEnd"))
				break;

			processedLines++;

			setProgress((int) (Math.round(linesPerPercent * processedLines)));

			dataLinePointer++;
		}

		setProgress(100);
		
		rec.recordingStartTS = timeCal.getTimeInMillis() + baseTimeStamp;
		
		int sampleIndex = (int) Math.floor(rec.getRawEyeEvents().size() / 2);
		long samplingTimeSample1 = rec.getRawEyeEvents().get(sampleIndex).timestamp;
		long samplingTimeSample2 = rec.getRawEyeEvents().get(sampleIndex + 1).timestamp;
		long sampleTSDiff = Math.abs(samplingTimeSample2 - samplingTimeSample1);
		if (sampleTSDiff < 1)
			sampleTSDiff = 1;
		int samplingFrequency = (int) Math.round(1000.0 / sampleTSDiff);
		int[] supportedFrequencies = {60, 120, 150, 300, 600, 1200};
		int dist = Math.abs(samplingFrequency - supportedFrequencies[0]);
		int idx = 0;
		for (int i = 0; i < supportedFrequencies.length; i++) {
			
			if (dist > Math.abs(samplingFrequency - supportedFrequencies[i])) {
				dist = Math.abs(samplingFrequency - supportedFrequencies[i]);
				idx = i;
			}
		}
		rec.setSamplingFrequency(supportedFrequencies[idx]);
		
		for (EyeTrackerEyeEvent e : rec.getRawEyeEvents()) {
			e.fixationDuration = (double) (1000.0 / rec.getSamplingFrequency());
		}
			
		map = null;
		
		return rec;
	}

	public double canParseDataConfidence(List<String> rawData) {		

		String firstToken = rawData.get(0).split("\t")[0];

		if (firstToken.contains("Recording timestamp"))
			return 0.5;

		return 0.0;
	}

}
