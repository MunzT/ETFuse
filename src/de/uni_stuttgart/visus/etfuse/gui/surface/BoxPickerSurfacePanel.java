package de.uni_stuttgart.visus.etfuse.gui.surface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import org.opencv.videoio.Videoio;

import de.uni_stuttgart.visus.etfuse.media.OverlayGazeProjector;

public class BoxPickerSurfacePanel extends VideoSurfacePanel {

    @Override
    protected void paintComponent(Graphics g) {
        
        Graphics2D g2 = (Graphics2D) g;
        
        int mediaWidth = (int) this.camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int mediaHeight = (int) this.camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        
        AffineTransform saveAT = g2.getTransform();
        saveAT.scale((double) panelWidth / (double) mediaWidth, (double) panelHeight / (double) mediaHeight);
        g2.setTransform(saveAT);
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int step = 0;
        
        g2.drawImage(image, null, 0, 0);

        if (camera == null)
            return;
        
        if (projectors == null || projectors.size() < 1)
            return;
        
        for (OverlayGazeProjector projector : this.projectors) {
            
            g2.setColor(Color.red);
            
            if (projector.getRecording().getFramePoint1() != null) {
                
                step = 1;
                
                java.awt.Point p1 = projector.getRecording().getFramePoint1();
                
                g2.setStroke(new BasicStroke(3));
                Line2D block = new Line2D.Double(p1.x, p1.y, p1.x, p1.y);
                g2.draw(block);
                g2.setStroke(new BasicStroke(1));
                Line2D line1 = new Line2D.Double(p1.x, p1.y, p1.x + 20, p1.y);
                g2.draw(line1);
                Line2D line2 = new Line2D.Double(p1.x, p1.y, p1.x, p1.y + 20);
                g2.draw(line2);
            }
            
            if (projector.getRecording().getFramePoint2() != null) {
                
                step = 0;
                
                java.awt.Point p2 = projector.getRecording().getFramePoint2();
                
                g2.setStroke(new BasicStroke(3));
                Line2D block = new Line2D.Double(p2.x, p2.y, p2.x, p2.y);
                g2.draw(block);
                g2.setStroke(new BasicStroke(1));
                Line2D line1 = new Line2D.Double(p2.x, p2.y, p2.x - 20, p2.y);
                g2.draw(line1);
                Line2D line2 = new Line2D.Double(p2.x, p2.y, p2.x, p2.y - 20);
                g2.draw(line2);
            }
        }
        
        Point mousePos = this.getMousePosition();
        
        if (mousePos == null)
            return;
        
        g2.setColor(Color.blue);
        
        if (step == 0) {
            g2.setStroke(new BasicStroke(3));
            Line2D block = new Line2D.Double(mousePos.x, mousePos.y, mousePos.x, mousePos.y);
            g2.draw(block);
            g2.setStroke(new BasicStroke(1));
            Line2D line1 = new Line2D.Double(mousePos.x, mousePos.y, mousePos.x + 20, mousePos.y);
            g2.draw(line1);
            Line2D line2 = new Line2D.Double(mousePos.x, mousePos.y, mousePos.x, mousePos.y + 20);
            g2.draw(line2);
        }
        else if (step == 1) {
            g2.setStroke(new BasicStroke(3));
            Line2D block = new Line2D.Double(mousePos.x, mousePos.y, mousePos.x, mousePos.y);
            g2.draw(block);
            g2.setStroke(new BasicStroke(1));
            Line2D line1 = new Line2D.Double(mousePos.x, mousePos.y, mousePos.x - 20, mousePos.y);
            g2.draw(line1);
            Line2D line2 = new Line2D.Double(mousePos.x, mousePos.y, mousePos.x, mousePos.y - 20);
            g2.draw(line2);
        }
    }
}
