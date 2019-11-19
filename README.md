![Application](application.png?raw=true)

# ETFuse - Synchronization System for Eye Tracking Data

Our system supports eye tracking researchers in analyzing and comparing eye movement behavior of two people that view the same content on different monitors and whose eye movements were recorded with Tobii Pro Eye Trackers.
For instance, the system can be used to explore the eye movement of two people playing an online game against each other. It is possible to explore the focus of the different persons and the distance of their eye gaze.

## Getting Started

Our System is implemented in Java using Java SE 11.<br/>
We implemented and deployed our system with eclipse 4.13.0

TODO? We provide a jar file, which can be used as follows:<br/>
`java.exe -jar ETFuse.jar`

TODO There is an example data set available in the `exampleData` directory. Two people were playing the board game Go online against each other and data was recorded with a Tobii Pro Spectrum 1200 and a Tobii T60XL.

To load a new data set you have to select *New Project* on the welcome screen. Then, you will be asked to provide a video file from the data set (this video file will be the one shown in the application). Next, under *File > Load host eye tracking data* you will be able to specify the file path to the corresponding eye tracking data file. Using the menu entry *Show Guest Recording* a wizard will be opend and guide you to load the eye tracking data of the second participant, and to specify the positions on the screens which should be used for position mapping between two participants (we supply a preset for our data set). For temporal synchronization, different algorithms can be chosen, one of them specifically designed for our type of data sets. More information on the different options are available in the help of the wizard.

Now, you are ready to explore the eye tracking data of both participants simultaneously using different visualizations.

Please note that in the first few seconds after loading data for a new project or loading a saved project the heat maps are generated in a background process. While it is possible to already use the application to explore the data set, heat maps might not be ready yet.

Once you loaded a data set, you can save a project, which can be reloaded later.

## Dependencies

Our system uses following libraries/classes:

* [OpenCV](https://docs.opencv.org/3.4.7/d1/dfb/intro.html)
* [JIDE-OSS](https://github.com/jidesoft/jide-oss)
* [JGoodies Common and Forms](http://www.jgoodies.com/downloads/libraries/)
* FFmpeg as part of OpenCV
* [LAB class from C3 (Categorical Color Components)](https://github.com/StanfordHCI/c3/blob/master/java/src/edu/stanford/vis/color/LAB.java)

All libraries/classes are already included in this project to readily use it.


## Input Data

As input, our system uses data and video files created with Tobii Pro Lab and/or Tobii Studio.

### Eye Tracking Data

The eye tracking data has to be saved as .tsv files and require the data export of all data.

For *Tobii Studio*, the following columns are used by our system:<br/>
* 0 (Timestamp)
* 3 (Number)
* 8 (DistanceLeft)
* 15 (DistanceRight)
* 21 (Event)
* 28 (MediaWidth)
* 29 (MediaHeight)
* 32 (MappedFixationPointX)
* 33 (MappedFixationPointY).

For *Tobii Pro Lab*:<br/>
* 0 (Recording timestamp)
* 11 (Recording resolution height)
* 12 (Recording resolution width)
* 21 (Event)
* 22 (Event value)
* 23 (Gaze point X)
* 24 (Gaze point Y)
* 39 (Eye position left X (DACSmm))
* 40 (Eye position left Y (DACSmm))
* 41 (Eye position left Z (DACSmm))
* 42 (Eye position right X (DACSmm))
* 43 (Eye position right Y (DACSmm))
* 44 (Eye position right Z (DACSmm))
* 63 (Eye movement type).

You may easily adapt the parser files if your data provides the information in different columns or if your data was created with another eye tracking software.

### Videos

Video data should be preferably exported without eye tracking data.
For Tobii Studio, video files can be exported without any fixations/raw data visible on the stimulus.
When Using Tobii Pro Lab, the original file located in the project in the folder `TODO` has to be used (as it is not possible to disable visibility of gaze information when exporting a video with the software).
