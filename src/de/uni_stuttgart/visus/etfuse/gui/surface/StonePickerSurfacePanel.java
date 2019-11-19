package de.uni_stuttgart.visus.etfuse.gui.surface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import org.opencv.videoio.Videoio;

public class StonePickerSurfacePanel extends VideoSurfacePanel {

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

        g2.drawImage(image, null, 0, 0);

        if (camera == null)
            return;

        Point mousePos = this.getMousePosition();

        if (mousePos == null)
            return;

        g2.setColor(Color.red);

        Point left = new Point(mousePos.x - 15, mousePos.y - 15);
        Point right = new Point(mousePos.x + 15, mousePos.y + 15);

        Ellipse2D leftPoint = new Ellipse2D.Double(left.x - 1, left.y - 1, 2, 2);
        Ellipse2D rightPoint = new Ellipse2D.Double(right.x - 1, right.y - 1, 2, 2);
        Ellipse2D centerPoint = new Ellipse2D.Double(mousePos.x - 2, mousePos.y - 2, 4, 4);

        g2.draw(leftPoint);
        g2.draw(centerPoint);
        g2.draw(rightPoint);
    }
}
