package de.uni_stuttgart.visus.etfuse.fileimport.parsers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.eyetracker.gazefilter.IVTFilter;
import de.uni_stuttgart.visus.etfuse.misc.Preferences;
import de.uni_stuttgart.visus.etfuse.projectio.Project;

public class ParserController implements PropertyChangeListener {

    private int progress = 0;
    private PropertyChangeSupport changeSupp = null;

    public ParserController() {

        super();

        this.changeSupp = new PropertyChangeSupport(this);
    }

    public void addProgressEventListener(PropertyChangeListener listener) {

        this.changeSupp.addPropertyChangeListener(listener);
    }

    static public List<Class<? extends TSVParser>> allParsers() {

        ArrayList<Class<? extends TSVParser>> list = new ArrayList<Class<? extends TSVParser>>();
        list.add(TSVParserSpectrum1200.class);
        list.add(TSVParserT60XL.class);

        return list;
    }

    public int getProgress() {

        return this.progress;
    }

    public EyeTrackerRecording parseDataUsingBestParser(List<String> rawData) {

        //...

        TSVParser parserToUse = null;
        double parserConfidence = 0.0;

        for (Class parserClass : ParserController.allParsers()) {

            TSVParser p = null;

            try {

                p = (TSVParser) parserClass.getConstructor(null).newInstance(null);

            } catch (Exception e) {

                e.printStackTrace();
            }

            if (p != null) {

                double confidence = p.canParseDataConfidence(rawData);

                if (confidence > parserConfidence) {

                    parserConfidence = confidence;
                    parserToUse = p;
                }
            }
        }

        if (parserToUse != null) {

            System.out.println("<ParserController> parse als " + parserToUse.parserDescription()
                + " (confidence: " + parserConfidence + ")");

            parserToUse.addProgressEventListener(this);

            Preferences prefs = Project.currentProject().getPreferences();
            int velocityThreshold = prefs.getFilterVelocityThreshold();
            int distanceThreshold = prefs.getFilterDistanceThreshold();

            return IVTFilter.filterRecording(parserToUse.parseData(rawData), velocityThreshold,
                                             distanceThreshold);
        }

        System.out.println("<ParserController> es wurde kein kompatibles ParserModul gefunden.");
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        this.progress = ((TSVParser) evt.getSource()).getProgress();
        this.changeSupp.firePropertyChange("progress", 1, 2);
    }
}
