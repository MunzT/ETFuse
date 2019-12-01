package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import de.uni_stuttgart.visus.etfuse.projectio.Project;
import de.uni_stuttgart.visus.etfuse.projectio.ProjectIO;

public class MainFrame extends JFrame {

    private JButton openVideoButton, openProjButton;
    private JLabel lblEtfuse;
    private JPanel buttonContainer;
    private JPanel hiThereGazePlot;

    public MainFrame(String title) {

        super(title);

        setSize(481, 315);
        setMinimumSize(this.getSize());
        setResizable(false);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.UNRELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(92dlu;default)"),
                FormSpecs.UNRELATED_GAP_COLSPEC,
                ColumnSpec.decode("right:default:grow"),
                FormSpecs.UNRELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormSpecs.UNRELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                RowSpec.decode("14dlu"),
                RowSpec.decode("default:grow"),}));

        lblEtfuse = new JLabel("Welcome!");
        lblEtfuse.setFont(new Font("Tahoma", Font.PLAIN, 25));
        getContentPane().add(lblEtfuse, "4, 2, center, default");

        buttonContainer = new JPanel();
        getContentPane().add(buttonContainer, "2, 4, fill, fill");
        buttonContainer.setLayout(new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("center:default:grow"),},
                new RowSpec[] {
                        FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.PARAGRAPH_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC,}));

        openVideoButton = new JButton("New Project");
        openVideoButton.setToolTipText("Create a new project");
        buttonContainer.add(openVideoButton, "1, 1, fill, default");

        openProjButton = new JButton("Load Project");
        openProjButton.setToolTipText("Load an existing project");
        buttonContainer.add(openProjButton, "1, 3, fill, default");

        hiThereGazePlot = new JPanel() {

            Color color1 = new Color(155, 0, 161);
            Color color2 = new Color(246, 189, 39);

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int[] x1 = {90, 180, 135};
                int[] y1 = {35, 55, 130};
                int[] x2 = {220, 90, 85};
                int[] y2 = {145, 180, 102};

                paintPlot(g, x1, y1, color1);
                paintPlot(g, x2, y2, color2);

            }

            private void paintPlot(Graphics g, int[] x, int[] y, Color color) {

                Graphics2D g2 = (Graphics2D) g;

                for (int i = x.length - 1; i >= 0; i--) {

                    g2.setStroke(new BasicStroke(10));
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));

                    if (i == 0)
                        g2.setColor(color);

                    Ellipse2D ellipse = new Ellipse2D.Double(x[i] - 15, y[i] - 15, 30, 30);
                    Ellipse2D outer = new Ellipse2D.Double(x[i] - 20, y[i] - 20, 40, 40);

                    g2.draw(ellipse);

                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(outer);

                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));

                    if (i > 0 && i < x.length) {

                        Line2D line = new Line2D.Double(x[i - 1], y[i - 1], x[i], y[i]);
                        g2.setStroke(new BasicStroke(2));
                        g2.draw(line);
                        g2.setStroke(new BasicStroke(10));
                    }
                }
            }
        };
        getContentPane().add(hiThereGazePlot, "4, 4, fill, fill");

        JProgressBar importProgress = new JProgressBar(0, 100);

        importProgress.setValue(0);
        importProgress.setStringPainted(true);

        importProgress.setVisible(false);

        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItemVideo;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("File context");
        menuBar.add(menu);

        ActionListener openVideoListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
                //Create a file chooser
                final JFileChooser fc =
                        new JFileChooser(Project.currentProject().getPreferences().getFileDirectory());

                fc.addChoosableFileFilter(new FileNameExtensionFilter("Video file", "mp4",
                                                                      "avi", "mkv", "flv", "mpeg"));
                fc.setAcceptAllFileFilterUsed(false);

                //In response to a button click:
                int returnVal = fc.showOpenDialog((Component) e.getSource());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // datei ausgewählt; fahre fort

                    final File chosenFile = fc.getSelectedFile();

                    Project.currentProject().hostVidPath = chosenFile.getAbsolutePath();

                    Path path = Paths.get(Project.currentProject().hostVidPath);
                    Path fileName = path.getFileName();

                    VideoFrame vidFrame = new VideoFrame("ETFuse - " + fileName.toString(),
                                                         chosenFile.getAbsolutePath());
                    vidFrame.setLocationRelativeTo(null);
                    vidFrame.setVisible(true);

                    MainFrame.this.setVisible(false);
                    MainFrame.this.dispose();

                    Project.currentProject().getPreferences().setFileDirectory(chosenFile.getAbsolutePath());
                }
            }
        };

        openProjButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                ProjectIO projIO = new ProjectIO(MainFrame.this);
                projIO.loadProject();
            }
        });
        openVideoButton.addActionListener(openVideoListener);

        //file menu item
        menuItemVideo = new JMenuItem("Open video...", KeyEvent.VK_V);

        menuItemVideo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        menuItemVideo.getAccessibleContext().setAccessibleDescription("Open video");
        menuItemVideo.addActionListener(openVideoListener);
        menu.add(menuItemVideo);

        setLocationRelativeTo(null);
    }

    public void setButtonEnabled(Boolean enable) {

        openVideoButton.setEnabled(enable);
        openProjButton.setEnabled(enable);
    }
}
