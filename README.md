![Application](application.png?raw=true)

# ETFuse - Synchronization System for Eye Tracking Data

Our system supports eye tracking researchers in analyzing and comparing eye movement behavior of two people that view the same content on different monitors and whose eye movements were recorded with Tobii Pro Eye Trackers.
For instance, the system can be used to explore the eye movement of two people playing an online game against each other. It is possible to explore the focus of the different persons and the distance of their eye gaze.

## Getting Started

Our system is implemented in Java using Java SE 11.<br/>
We implemented and deployed our system with eclipse 4.13.0

Along with the latest release, we provide a jar file, which can be used as follows:<br/>
`java.exe -jar ETFuse.jar`

Instead, it may be possible to use a double click to start the application.

An example data set is available in the `exampleData` directory along with a [description](exampleData/tutorial.md) of how to load this data set. Two people played the board game Go online against each other, and data was recorded with a Tobii Pro Spectrum 1200 and a Tobii T60XL.

To load a new data set, you have to select *New Project* on the welcome screen. You will then be asked to provide a video file from the data set (this video file will be the one shown in the application). Next, under *File > Load host eye tracking data*, you will be able to specify the file path to the corresponding eye tracking data file. Using the menu entry *Show Guest Recording*, a wizard will be opened and guide you by loading the second participant's eye tracking data and specifying the coordinates on the screens for position mapping between two participants (we supply a preset for our data set). For temporal synchronization, different algorithms can be chosen, one of them specifically designed for our type of data sets. More information on the different options are available using the help of the wizard.
You can load an optional additional file containing event time stamps using `File > Add events...`. These events will be shown on the timeline and can be used for heatmap generation and distance plot subdivision.

Now, you are ready to explore the eye tracking data of both participants simultaneously using different visualizations.

Please note that the heat maps are generated in a background process in the first few seconds after loading data for a new project or loading a saved project. While it is possible to use the application to explore the data set, heat maps might not be ready yet.

Once you loaded a data set, you can save a project, which can be reloaded later.


## Input Data

As input, our system uses data and video files created with Tobii Pro Lab and/or Tobii Studio.
Additionally, it is possible to load a file containing custom events. 

### Eye Tracking Data

The eye tracking data has to be saved as .tsv files and require the data export of all data (optionally, such a file can be compressed with gzip (.tar.gz)).

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

Video data should preferably be without eye tracking data.
For Tobii Studio, video files can be exported without any fixations/raw data visible on the stimulus (fixation/raw data overlay can be disabled in the overlay settings; we exported videos using the Microsoft Video 1 codec).
When Using Tobii Pro Lab, the original .mp4 files of the project located in the folder `Media` can be used (it is not possible to disable the visibility of gaze information when exporting a video with the software).

### Custom Events

In a separate csv file, it is possible to define custom events, which can be used as markers on the timeline, for subdividing the distance plot and generating heatmaps. In the first column, the frame numbers from the host video have to be specified, and the second column must contain the colors used for the marker in the timeline. One line might look like this: `100,#ff0000`


## Additional Material

The `materials` directory contains a demo video of our system and some more information about Go.


## Dependencies

Our system uses the following libraries/classes:

* [OpenCV](https://docs.opencv.org/3.4.7/d1/dfb/intro.html)
* FFmpeg as part of OpenCV
* [JIDE-OSS](https://github.com/jidesoft/jide-oss)
* [JGoodies Common and Forms](http://www.jgoodies.com/downloads/libraries/)
* [LAB class from C3 (Categorical Color Components)](https://github.com/StanfordHCI/c3/blob/master/java/src/edu/stanford/vis/color/LAB.java)

All libraries/classes are already included in this project to use it readily.


## License

Our project is licensed under the [MIT License](LICENSE.md).

Our system includes third-party open-source libraries and classes (see above), which are licensed under their own respective open-source [licenses](THIRD-PARTY-LICENSES.md).


## Citation

When referencing our work, please cite the paper *Comparative Visual Gaze Analysis for Virtual Board Games*.

T. Munz, N. Schäfer, T. Blascheck, K. Kurzhals, E. Zhang, and D. Weiskopf. Comparative Visual Gaze Analysis for Virtual Board Games. Proceedings of the 13th International Symposium on Visual Information Communication and Interaction (VINCI 2020), to appear. 2020.

```
@article{vinci2020boardGames,
  author    = {Munz, Tanja and Schäfer, Noel and Blascheck, Tanja and Kurzhals, Kuno and Zhang, Eugene and Weiskopf, Daniel},
  title     = {Comparative Visual Gaze Analysis for Virtual Board Games},
  journal   = {The 13th International Symposium on Visual Information Communication and Interaction (VINCI 2020), to appear},
  year      = {2020}
}
```