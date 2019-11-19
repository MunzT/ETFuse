package de.uni_stuttgart.visus.etfuse.gui.element;

import javax.swing.JSlider;

import de.uni_stuttgart.visus.etfuse.gui.VideoFrame;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;

public class RecordingSlider extends JSlider {

    /**
     *
     */
    private static final long serialVersionUID = -2083295176874326801L;
    private MetalRecordingSliderUI ui = null;

    public RecordingSlider(int eins, int zwei, int drei, int vier) {

        super(eins, zwei, drei, vier);

        // Zur angeklickten Position springen
        // https://stackoverflow.com/questions/518471/jslider-question-position-after-leftclick

        this.ui = new MetalRecordingSliderUI() {
            @Override
            protected void scrollDueToClickInTrack(int direction) {

                // this is the default behaviour, let's comment that out
                //scrollByBlock(direction);

                if (RecordingSlider.this.getMousePosition() == null)
                    return;

                int value = RecordingSlider.this.getValue();

                if (RecordingSlider.this.getOrientation() == JSlider.HORIZONTAL)
                    value = this.valueForXPosition(RecordingSlider.this.getMousePosition().x);
                else if (RecordingSlider.this.getOrientation() == JSlider.VERTICAL)
                    value = this.valueForYPosition(RecordingSlider.this.getMousePosition().y);

                RecordingSlider.this.setValue(value);
            }
        };

        this.setUI(this.ui);
    }

    public void drawMinDistanceTicks(VideoFrame vidFrame, OverlayGazeProjector hostProjector,
                                     OverlayGazeProjector guestProjector, int minDistance) {

        this.ui.setMinDistanceRecordingsToDraw(vidFrame, hostProjector, guestProjector, minDistance);
    }
}
