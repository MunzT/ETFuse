package de.uni_stuttgart.visus.etfuse.database;

import java.util.ArrayList;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;

public class EyeTrackerRecordingCollector {

	static private EyeTrackerRecordingCollector sharedInstance;
	private ArrayList<EyeTrackerRecording> collection;

	static public EyeTrackerRecordingCollector sharedInstance() {

		if (sharedInstance == null)
			sharedInstance = new EyeTrackerRecordingCollector();

		return sharedInstance;
	}

	public EyeTrackerRecordingCollector() {

		collection = new ArrayList<EyeTrackerRecording>();
	}

	public void addRecording(EyeTrackerRecording recording) {

		collection.add(recording);
	}
}
