# Tutorial: Loading the example data set

## Starting the Application

Make sure that Java is installed on your machine; our system requires at least Java SE 11.

Start the application:<br/>
`java.exe -jar ETFuse.jar`

Instead, it may be possible to use double click to start the application.

## Host Data

Use `New Project` to select the video file of the first player (Tobii_Spectrum1200_R1.mpg).

Next, use the menu entry `File > Load host eye tracking data...` to select the corresponding eye tracking data file (Tobii_Spectrum1200_R1.tsv.gz).

Now, the application might be a little bit slow, as a few heatmaps are being created.

## Guest Data

Select `Show guest recording` to load the data of the second player.
First, with `Load source data`, you have to select the eye tracking data of the second player (Tobii_T60XL_R1.tsv.gz).

Now, the board areas of the two players have to be mapped.
If you loaded the data of the two players as described before, you could twice check `Use preset` and press the button `Continue`. If you loaded the data of the second player as host data, you could swap the presets first. It is also possible that you use `Open video file` to specify the board areas yourself on the videos; you will have to provide the video file of the second player (Tobii_T60XL_R1.avi).

Finally, you have to select a method for temporal synchronization. You can use the inaccurate method that synchronizes videos according to system time stamps. Usually, the results are more precise when using the histogram method (you will have to provide the file path to the corresponding video file of player two if not done yet (Tobii_T60XL_R1.avi). Additionally, a frame that lies between two moves in the game has to be selected. Another option would be a method specifically designed for our data set that also requires the second video file (Tobii_T60XL_R1.avi), and you will have to select the position of a stone on the board. However, this method might occasionally fail but is more precise if it does not.

## Events

You can load an additional file containing event time stamps using `File > Add events...` (e.g. Spectrum1200_startAndEnd_R1.csv).

## Loading a Project

We also provide a project file (R1.etp) that may be use instead of creating a new Project. After starting the application, use `Load Project` instead of `New Project`. As this file contains file paths for the files mentioned above, you may have to provide the correct location of these expected files.
