package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.management.Notification;
import javax.management.NotificationListener;
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
import de.uni_stuttgart.visus.etfuse.gui.surface.BoxPickerSurfacePanel;
import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;
import de.uni_stuttgart.visus.etfuse.misc.Utils;

public class BoxPickerFrame extends JDialog implements ChangeListener, MouseListener, MouseMotionListener {

    private BoxPickerSurfacePanel panel;
    private JPanel panelContainer;
    private JPanel playbackPanel;
    private JSlider progressSlider;
    private Boolean progressUpdate;

    private int boxPickerStep;
    private Point framePoint1, framePoint2;

    private VideoCapture camera;
    private EyeTrackerRecording rec;
    private NotificationListener callbackTarget;

    public BoxPickerFrame(JDialog parentFrame, VideoCapture camera, EyeTrackerRecording rec, NotificationListener callbackTarget) {

        super(parentFrame, "Select area", true);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Utils.resizePanelToRetainAspectRatio(panel, panelContainer);
            }
        });

        this.camera = camera;
        this.rec = rec;
        this.callbackTarget = callbackTarget;

        this.setSize(0, 0);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.panelContainer = new JPanel(new GridBagLayout());

        this.panel = new BoxPickerSurfacePanel();
        this.panel.setToolTipText("Click to define upper left and lower right corner of the board");
        this.panel.attachCamera(camera);
        this.panel.setPaintGazePlot(true);

        OverlayGazeProjector projector = new OverlayGazeProjector(rec);
        this.panel.attachProjector(projector);

        this.panel.addMouseListener(this);
        this.panel.addMouseMotionListener(this);
        this.panel.setPreferredSize(new Dimension((int) Math.ceil(camera.get(Videoio.CAP_PROP_FRAME_WIDTH)), (int) Math.ceil(camera.get(Videoio.CAP_PROP_FRAME_HEIGHT))));
        this.panelContainer.setPreferredSize(this.panel.getPreferredSize());
        this.panelContainer.add(this.panel);
        this.add(this.panelContainer, BorderLayout.CENTER);

        this.playbackPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        this.playbackPanel.setLayout(layout);

        this.progressSlider = new JSlider(JSlider.HORIZONTAL, 0, (int) camera.get(Videoio.CAP_PROP_FRAME_COUNT), 0);
        this.progressSlider.addChangeListener(this);
        this.progressSlider.setPaintTicks(false);
        this.progressSlider.setPaintLabels(false);
        this.playbackPanel.add(this.progressSlider, BorderLayout.CENTER);
        layout.putConstraint(SpringLayout.WEST, this.progressSlider, 5, SpringLayout.WEST, this.playbackPanel);
        layout.putConstraint(SpringLayout.NORTH, this.progressSlider, 3, SpringLayout.NORTH, this.playbackPanel);
        layout.putConstraint(SpringLayout.SOUTH, this.progressSlider, 3, SpringLayout.SOUTH, this.playbackPanel);
        layout.putConstraint(SpringLayout.EAST, this.progressSlider, -5, SpringLayout.EAST, this.playbackPanel);

        this.add(this.playbackPanel, BorderLayout.PAGE_END);

        this.playbackPanel.setPreferredSize(new Dimension(this.panel.getPreferredSize().width, 25));

        drawFrame();

        this.setResizable(false);
        this.pack();

        this.setLocationRelativeTo(null);

        this.boxPickerStep = 1;
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

    @Override
    public void mouseClicked(MouseEvent e) {

        if (boxPickerStep == 1) {

            framePoint1 = e.getPoint();
            rec.setFrame(framePoint1, null);
            boxPickerStep = 2;
        }
        else if (boxPickerStep == 2) {

            framePoint2 = e.getPoint();
            rec.setFrame(framePoint1, framePoint2);
            Notification notif = new Notification("boxpickerresult", this, 0);

            if (callbackTarget != null)
                callbackTarget.handleNotification(notif, null);

            boxPickerStep = 1;

            this.setVisible(false);
            this.dispose();
        }

        this.panel.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

        this.panel.repaint();
    }
}
