# Tutorial: Loading the example data set

## Starting the Application

Start the application:<br/>
`java.exe -jar ETFuse.jar`

## Host Data

Use `New Project` to select the video file of the first player (exampleData/Tobii_Spectrum1200_R3.mpg).

Next, use the menu entry `File > Load host eye tracking data...` to select the corresponding eye tracking data file (exampleData/Tobii_Spectrum1200_R3.tsv).

The application might be a little bit slow, now, as a few heatmaps are being created.

## Guest Data

Select `Show guest recording` to load the data of the second player.
First, with `Load source data`, you have to select the eye tracking data of the second player (exampleData/Tobii_T60XL_R3.tsv).

Now, the board areas of the two players have to be mapped.
If you loaded the data of the two players as described before, you could twice check `Use preset` and press the button `Continue`. If you loaded the data of the second player as host data, you could swap the presets first. It is also possible that you use `Open video file` to specify the board area yourself on the video; you will have to provide the video file of the second player (exampleData/Tobii_T60XL_R3.avi).

Finally, you have to select a method for temporal synchronization. You can use the inaccurate method that synchronizes videos according to system time stamps. Usually, the results are more precise when using the histogram method (you will have to provide the file path to the corresponding video file of player two if not done yet (exampleData/Tobii_T60XL_R3.avi), and a frame that lies between two moves in the game has to be selected. Another option would be a method specifically designed for our data set that also requires the second video file (exampleData/Tobii_T60XL_R3.avi), and you will have to select the position of a stone on the board. However, this method might occasionally also fail but is more precise if it does not.
