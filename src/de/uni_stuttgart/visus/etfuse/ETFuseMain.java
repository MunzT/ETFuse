package de.uni_stuttgart.visus.etfuse;

import javax.swing.JOptionPane;

import org.opencv.core.Core;

import de.uni_stuttgart.visus.etfuse.gui.MainFrame;

public class ETFuseMain {

    public static void main(String[] args) {

        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.loadLibrary("opencv_ffmpeg346_64");
        } catch (UnsatisfiedLinkError e) {
            JOptionPane.showMessageDialog(null, "Konnte nötige Programmbibliotheken (opencv_java346.dll, opencv_ffmpeg346_64.dll) nicht im aktuellen Arbeitsverzeichnis (" + System.getProperty("user.dir") + ") finden. Programm wird geschlossen.");
            return;
        }

        MainFrame mainFrame = new MainFrame("ETFuse");
        mainFrame.setVisible(true);
    }
}
