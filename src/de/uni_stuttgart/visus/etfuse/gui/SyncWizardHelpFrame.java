package de.uni_stuttgart.visus.etfuse.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class SyncWizardHelpFrame extends JDialog {

    public SyncWizardHelpFrame(SyncWizardFrame parentFrame) {

        super(parentFrame, "Instructions", true);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea txtrInstructions = new JTextArea();
        txtrInstructions.setWrapStyleWord(true);
        txtrInstructions.setEditable(false);
        txtrInstructions.setLineWrap(true);
        txtrInstructions.setText("Step 1 : Loading the guest record\n"
            + "In the first step, the data set of the guest recording is loaded.\n"
            + "This dataset has to be in TSV format.\n" + "\n"
            + "Step 2 : Defining the board area in the guest recording\n"
            + "In the second step the area of the video in the guest recording is selected which "
            + "will be affected by the coordinate transformation. "
            + "The user can choose "
            + "between two options: "
            + "Presets or a manual selection. Among the presets, there are two pre-built areas "
            + "that fit the recordings made with the two devices used during this work. "
            + "Alternatively, the area can be set manually. For this the screen recording of the "
            + "guest recording is required. "
            + "If this option is selected, a file selection dialog opens where the video can be "
            + "selected. "
            + "If the user selects a valid file, another window opens with the video of the guest "
            + "recording, in which the upper left and lower right corners of the board can be "
            + "marked by mouse click. "
            + "If this is done, the wizard moves to the next step.\n"
            + "\n"
            + "Step 3 : Defining the playing field area in the host recording\n"
            + "Analogous to step 2, in the third step the board area in the host recording is "
            + "determined. "
            + "Here, the same presets are available as from the previous step. Alternatively, the "
            + "area can also be defined manually. "
            + "It is not necessary to load a video file in this step, since the host recording is "
            + "already loaded and taken into account by the wizard. "
            + "Once this step has been completed, the wizard moves to the last step.\n"
            + "\n"
            + "Step 4 : Selecting the synchronization method\n"
            + "In the last step, the user chooses between three options for temporal "
            + "synchronization of the recordings. "
            + "The choice is between an inaccurate, fast method based only on the system time "
            + "stamps in the recordings, and two more precise methods. "
            + "If the inaccurate method is selected, the configuration is complete and the guest "
            + "recording is added. "
            + "If, on the other hand, one of the other two methods is selected, additional "
            + "information is required to serve as guide values for each method, and the screen "
            + "recording for the guest data set is required. "
            + "For the synchronization via a specific (experimental) method the position of the "
            + "brick on the board has to be determined, which which is then used by the system. "
            + "For the synchronization via histogram comparison, the user is asked to select a "
            + "start frame from the host screen recording that lies between two moves in the game. "
            + "To do this, the host screen recording opens in a separate window. "
            + "Once the information has been entered, the wizard is ready to add the guest recording.");
        getContentPane().add(txtrInstructions, BorderLayout.CENTER);

        this.setMinimumSize(new Dimension(550, 700));

        this.pack();

        setLocationRelativeTo(null);
    }
}
