package de.uni_stuttgart.visus.etfuse.gui.surface;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.opencv.core.Mat;

import de.uni_stuttgart.visus.etfuse.misc.Utils;

public class HeatMapImagePanel extends JPanel {
	
	private Mat heatMap = null;
	private BufferedImage bufImgHeatMap = null;
	
	public void setHeatMap(Mat heatMap) {
		
		this.heatMap = heatMap;
		this.bufImgHeatMap = Utils.Mat2BufferedImage(heatMap);
	}
	
	public Mat getHeatMap() {
		
		return this.heatMap;
	}

	@Override
	protected void paintComponent(Graphics g) {
		
		if (bufImgHeatMap == null)
			return;
		
		Graphics2D g2 = (Graphics2D) g;
		
		int mediaWidth = (int) this.heatMap.cols();
		int mediaHeight = (int) this.heatMap.rows();
		int panelWidth = (int) this.getPreferredSize().getWidth();
		int panelHeight = this.getHeight();
				
		AffineTransform saveAT = g2.getTransform();
		saveAT.scale((double) panelWidth / (double) mediaWidth, (double) panelHeight / (double) mediaHeight);
		g2.setTransform(saveAT);
		
		g2.drawImage(bufImgHeatMap, null, 0, 0);
	}
}
