package de.uni_stuttgart.visus.etfuse.projectio;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import de.uni_stuttgart.visus.etfuse.misc.Preferences;

public class Project implements Serializable {

    //private static final long serialVersionUID = -2082609050833935516L;
    private static final long serialVersionUID = -2615634893322224238L;

    public String hostVidPath = "";
    public String hostDatasetPath = "";
    public Rectangle hostFrame = null;

    public ArrayList<String> guestVidPaths = null;
    public ArrayList<String> guestDatasetPaths = null;
    public ArrayList<Rectangle> guestFrames = null;
    public ArrayList<Long> guestTimeShiftOffsets = null;

    static Project curProj = null;

    private Preferences preferences = new Preferences();

    public Project() {

        guestVidPaths = new ArrayList<String>();
        guestDatasetPaths = new ArrayList<String>();
        guestFrames = new ArrayList<Rectangle>();
        guestTimeShiftOffsets = new ArrayList<Long>();
    }

    public static Project currentProject() {

        if (curProj == null)
            curProj = new Project();

        return curProj;
    }

    public Preferences getPreferences() {

        return preferences;
    }

    public static Project loadProjectFromFile(File path) {

        Project proj = new Project();
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);
            proj = (Project) ois.readObject();
        } catch (InvalidClassException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        curProj = proj;
        return proj;
    }

    public static void saveProjectToFile(File path) {

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(curProj);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
