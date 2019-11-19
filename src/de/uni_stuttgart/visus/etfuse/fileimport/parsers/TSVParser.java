package de.uni_stuttgart.visus.etfuse.fileimport.parsers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;

public abstract class TSVParser {

    private int progress = 0;
    private PropertyChangeSupport changeSupp = null;

    public TSVParser() {

        super();

        this.changeSupp = new PropertyChangeSupport(this);
    }

    public int getProgress() {
        return this.progress;
    }

    protected void setProgress(int progress) {

        this.progress = progress;
        this.changeSupp.firePropertyChange("progress", 1, 2);
    }

    public void addProgressEventListener(PropertyChangeListener listener) {

        this.changeSupp.addPropertyChangeListener(listener);
    }

    abstract public String parserDescription();
    abstract public EyeTrackerRecording parseData(List<String> rawData);
    abstract public double canParseDataConfidence(List<String> rawData);
}
