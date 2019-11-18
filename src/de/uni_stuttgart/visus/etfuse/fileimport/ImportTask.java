package de.uni_stuttgart.visus.etfuse.fileimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.SwingWorker;

import de.uni_stuttgart.visus.etfuse.database.EyeTrackerRecordingCollector;
import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.fileimport.parsers.ParserController;

public class ImportTask extends SwingWorker implements PropertyChangeListener {

	String filePath = "";
	File file = null;
	ParserController pc = null;

	public ImportTask(String filePath) {

		super();

		this.filePath = filePath;
		this.pc = new ParserController();
	}

	public ImportTask(File file) {

		super();

		this.file = file;
		this.pc = new ParserController();
	}

	@Override
	protected Object doInBackground() throws Exception {

		if (this.file != null)
			return this.importFile(this.file);
		else if (this.filePath.length() > 0)
			return this.importFilePath(this.filePath);

		return null;
	}

	public EyeTrackerRecording importFilePath(String path) {

		this.file = new File(path);
		return this.importFile(this.file);
	}

	public EyeTrackerRecording importFile(File file) {

		this.filePath = file.getPath();

		ArrayList<String> fileContents = new ArrayList<String>();

		FileInputStream inputStream = null;
		Scanner sc = null;

		try {

			inputStream = new FileInputStream(file);
			sc = new Scanner(inputStream, "UTF-8");

			String sCurrentLine;

			this.setProgress(0);
			long totalLength = file.length();
			long readLength = 0;
			double lengthPerPercent = 100.0 / totalLength;

			while (sc.hasNextLine()) {
				sCurrentLine = sc.nextLine();
				fileContents.add(sCurrentLine);

				readLength += sCurrentLine.length();
				this.setProgress((int) (Math.round(lengthPerPercent * readLength)));
			}

			// scanner unterdrückt exceptions
			if (sc.ioException() != null) {
				throw sc.ioException();
			}

		} catch (IOException ex) {

			ex.printStackTrace();

		} finally {

			try {

				if (inputStream != null)
					inputStream.close();

				if (sc != null)
					sc.close();

			} catch (IOException ex) {

				ex.printStackTrace();
			}
		}

		//parse

		this.setProgress(100);

		System.out.println("<ImportTask> Datei gelesen. Parse Inhalt...");

		pc.addProgressEventListener(this);
		EyeTrackerRecording rec = pc.parseDataUsingBestParser(fileContents);
		
		fileContents.clear();
		System.gc();
		
		EyeTrackerRecordingCollector.sharedInstance().addRecording(rec);
		
		return rec;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		this.setProgress(pc.getProgress());
	}

}
