package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import de.uni_stuttgart.visus.etfuse.eyetracker.EyeTrackerRecording;
import de.uni_stuttgart.visus.etfuse.gui.element.RecordingSlider;
import de.uni_stuttgart.visus.etfuse.gui.surface.VideoSurfacePanel;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Utils;

public class FramePickerFrame extends JDialog implements ChangeListener {

    private VideoSurfacePanel panel;
    private JPanel panelContainer;
    private JPanel playbackPanel;
    private RecordingSlider progressSlider;
    private JButton acceptButton;
    private Boolean progressUpdate;

    private VideoCapture camera;
    private EyeTrackerRecording rec;
    private NotificationListener callbackTarget;

    public FramePickerFrame(JDialog parentFrame, VideoCapture camera, EyeTrackerRecording rec, NotificationListener callbackTarget) {

        super(parentFrame, "Select frame", true);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Utils.resizePanelToRetainAspectRatio(panel, panelContainer);
            }
        });

        FramePickerFrame myself = this;

        this.camera = camera;
        this.rec = rec;
        this.callbackTarget = callbackTarget;

        this.setSize(0, 0);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.panelContainer = new JPanel(new GridBagLayout());

        this.panel = new VideoSurfacePanel();
        this.panel.setToolTipText("Current frame. Can be selected via \\\"Select this frame\\\".");
        this.panel.attachCamera(camera);

        OverlayGazeProjector projector = new OverlayGazeProjector(rec);
        this.panel.attachProjector(projector);

        this.panel.setPreferredSize(new Dimension((int) Math.ceil(camera.get(Videoio.CAP_PROP_FRAME_WIDTH)), (int) Math.ceil(camera.get(Videoio.CAP_PROP_FRAME_HEIGHT))));
        this.panelContainer.setPreferredSize(this.panel.getPreferredSize());
        this.panelContainer.add(this.panel);
        this.add(this.panelContainer, BorderLayout.CENTER);

        this.playbackPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        this.playbackPanel.setLayout(layout);

        this.acceptButton = new JButton("Select this frame");
        this.acceptButton.setToolTipText("Select currently visible frame");
        this.acceptButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Integer frame = (int) camera.get(Videoio.CV_CAP_PROP_POS_FRAMES);
                Notification notif = new Notification("framepickerresult", myself, 0);

                if (callbackTarget != null)
                    callbackTarget.handleNotification(notif, frame);

                myself.setVisible(false);
                myself.dispose();
            }

        });
        this.playbackPanel.add(this.acceptButton, BorderLayout.EAST);
        layout.putConstraint(SpringLayout.NORTH, this.acceptButton, 3,
                SpringLayout.NORTH, this.playbackPanel);
        layout.putConstraint(SpringLayout.SOUTH, this.acceptButton, -3,
                SpringLayout.SOUTH, this.playbackPanel);
        layout.putConstraint(SpringLayout.EAST, this.acceptButton, -5,
                SpringLayout.EAST, this.playbackPanel);

        this.progressSlider = new RecordingSlider(JSlider.HORIZONTAL, 0,
                (int) camera.get(Videoio.CAP_PROP_FRAME_COUNT), 0);
        this.progressSlider.setToolTipText("Use to select a frame that shows a move during the game");
        this.progressSlider.addChangeListener(this);
        this.progressSlider.setPaintTicks(false);
        this.progressSlider.setPaintLabels(false);
        this.playbackPanel.add(this.progressSlider, BorderLayout.CENTER);
        layout.putConstraint(SpringLayout.WEST, this.progressSlider, 5,
                SpringLayout.WEST, this.playbackPanel);
        layout.putConstraint(SpringLayout.NORTH, this.progressSlider, 3,
                SpringLayout.NORTH, this.playbackPanel);
        layout.putConstraint(SpringLayout.SOUTH, this.progressSlider, 3,
                SpringLayout.SOUTH, this.playbackPanel);
        layout.putConstraint(SpringLayout.EAST, this.progressSlider, -5,
                SpringLayout.WEST, this.acceptButton);

        this.add(this.playbackPanel, BorderLayout.PAGE_END);

        this.playbackPanel.setPreferredSize(new Dimension(this.panel.getPreferredSize().width, 25));

        drawFrame();

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.setResizable(true);
        this.pack();

        this.setLocationRelativeTo(null);
    }

    public void drawFrame() {

        Mat newMatrix = new Mat();

        if (camera.read(newMatrix)) {
            panel.setImage(Utils.Mat2BufferedImage(newMatrix));
            panel.repaint();
            this.progressUpdate = true;
            progressSlider.setValue((int) camera.get(Videoio.CAP_PROP_POS_FRAMES));
            this.progressUpdate = false;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        if (!this.progressUpdate) {

            JSlider source = (JSlider) e.getSource();

            if (source.getValueIsAdjusting()) {
                camera.set(Videoio.CV_CAP_PROP_POS_FRAMES, source.getValue());
                drawFrame();
            }
        }
    }
}
