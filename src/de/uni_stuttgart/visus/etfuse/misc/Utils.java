package de.uni_stuttgart.visus.etfuse.misc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.Videoio;

import de.uni_stuttgart.visus.etfuse.gui.surface.HeatMapImagePanel;
import de.uni_stuttgart.visus.etfuse.gui.surface.VideoSurfacePanel;

public class Utils {

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";
    public final static String tsv = "tsv";

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public static BufferedImage Mat2BufferedImage(Mat m) {
        //Method converts a Mat to a Buffered Image

        int type = BufferedImage.TYPE_BYTE_GRAY;

        if ( m.channels() > 1 )
            type = BufferedImage.TYPE_3BYTE_BGR;

        if ( m.channels() > 3 )
            type = BufferedImage.TYPE_4BYTE_ABGR;

        int bufferSize = m.channels() * m.cols() * m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return image;
    }

    // https://docs.opencv.org/2.4/doc/tutorials/highgui/video-input-psnr-ssim/video-input-psnr-ssim.html#videoinputpsnrmssim
    public static Scalar computeMSSIM(Mat i1, Mat i2) {

        double C1 = 6.5025, C2 = 58.5225;
        /***************************** INITS **********************************/
        int d = CvType.CV_32F;

        Mat I1 = new Mat();
        Mat I2 = new Mat();
        i1.convertTo(I1, d);            // cannot calculate on one byte large values
        i2.convertTo(I2, d);

        Mat I2_2   = I2.mul(I2);        // I2^2
        Mat I1_2   = I1.mul(I1);        // I1^2
        Mat I1_I2  = I1.mul(I2);        // I1 * I2

        /*************************** END INITS **********************************/

        Mat mu1 = new Mat();
        Mat mu2 = new Mat();                   // PRELIMINARY COMPUTING
        Imgproc.GaussianBlur(I1, mu1, new Size(11, 11), 1.5);
        Imgproc.GaussianBlur(I2, mu2, new Size(11, 11), 1.5);

        Mat mu1_2   =   mu1.mul(mu1);
        Mat mu2_2   =   mu2.mul(mu2);
        Mat mu1_mu2 =   mu1.mul(mu2);

        Mat sigma1_2 = new Mat();
        Mat sigma2_2 = new Mat();
        Mat sigma12 = new Mat();

        Imgproc.GaussianBlur(I1_2, sigma1_2, new Size(11, 11), 1.5);
        Core.subtract(sigma1_2, mu1_2, sigma1_2);

        Imgproc.GaussianBlur(I2_2, sigma2_2, new Size(11, 11), 1.5);
        Core.subtract(sigma2_2, mu2_2, sigma2_2);

        Imgproc.GaussianBlur(I1_I2, sigma12, new Size(11, 11), 1.5);
        Core.subtract(sigma12, mu1_mu2, sigma12);

        ///////////////////////////////// FORMULA ////////////////////////////////
        Mat t1 = new Mat();
        Mat t2 = new Mat();
        Mat t3 = new Mat();

        t1 = mu1_mu2.mul(new MatOfDouble(2));
        Core.add(t1, new MatOfDouble(C1), t1);

        t2 = sigma12.mul(new MatOfDouble(2));
        Core.add(t1, new MatOfDouble(C2), t2);
        t3 = t1.mul(t2);                 // t3 = ((2*mu1_mu2 + C1).*(2*sigma12 + C2))

        Core.add(mu1_2, mu2_2, t1);
        Core.add(t1, new MatOfDouble(C1), t1);
        Core.add(sigma1_2, sigma2_2, t2);
        Core.add(t2, new MatOfDouble(C2), t2);
        t1 = t1.mul(t2);                 // t1 =((mu1_2 + mu2_2 + C1).*(sigma1_2 + sigma2_2 + C2))

        Mat ssim_map = new Mat();
        Core.divide(t3, t1, ssim_map);        // ssim_map =  t3./t1;

        Scalar mssim = Core.mean(ssim_map);   // mssim = average of ssim map

        System.gc();

        return mssim;
    }

    public static Point transformCoordinate(Point p, Line2D sourceFrame, Line2D targetFrame) {

        Rectangle2D sourceRect = new Rectangle2D.Double(sourceFrame.getX1(), sourceFrame.getY1(),
                sourceFrame.getX2() - sourceFrame.getX1(), sourceFrame.getY2() - sourceFrame.getY1());
        Rectangle2D targetRect = new Rectangle2D.Double(targetFrame.getX1(), targetFrame.getY1(),
                targetFrame.getX2() - targetFrame.getX1(), targetFrame.getY2() - targetFrame.getY1());

        double fractionX = (p.x - sourceRect.getCenterX()) / (sourceRect.getWidth() / 2);
        double fractionY = (p.y - sourceRect.getCenterY()) / (sourceRect.getHeight() / 2);

        p.x = (int) Math.round(targetRect.getCenterX() + (fractionX * (targetRect.getWidth() / 2)));
        p.y = (int) Math.round(targetRect.getCenterY() + (fractionY * (targetRect.getHeight() / 2)));

        return p;
    }

    public static Mat[] splitMatrixIntoCells(Mat matrix, int sideNum) {

        int cellWidth = matrix.cols() / sideNum;
        int cellHeight = matrix.rows() / sideNum;

        Mat[] matrices = new Mat[sideNum * sideNum];

        for (int y = 0; y < sideNum; y++) {
            for (int x = 0; x < sideNum; x++) {

                int cellIndex = (y * sideNum) + x;
                matrices[cellIndex] = matrix.submat(y * cellHeight, (y + 1) * cellHeight,
                        x * cellWidth, (x + 1) * cellWidth);
            }
        }

        return matrices;
    }

    public static double getArraySum(double[] array) {

        double sum = 0.0;

        for (double val : array) {
            sum += val;
        }

        return sum;
    }

    public static double computeSRGBDistance(int color1, int color2) {

        Color rgb1 = new Color(color1);
        Color rgb2 = new Color(color2);

        LAB lab1 = LAB.fromRGB(rgb1.getRed(), rgb1.getGreen(), rgb1.getBlue(), 0);
        LAB lab2 = LAB.fromRGB(rgb2.getRed(), rgb2.getGreen(), rgb2.getBlue(), 0);

        return LAB.ciede2000_n(lab1, lab2);
    }

    public static void resizePanelToRetainAspectRatio(VideoSurfacePanel panel, JPanel panelContainer) {

        double mediaW = panel.getCamera().get(Videoio.CAP_PROP_FRAME_WIDTH);
        double mediaH = panel.getCamera().get(Videoio.CAP_PROP_FRAME_HEIGHT);

        resizePanelToRetainAspectRatio(mediaW, mediaH, panel, panelContainer);
    }

    public static void resizePanelToRetainAspectRatio(HeatMapImagePanel panel, JPanel panelContainer) {

        double mediaW = panel.getHeatMap().cols();
        double mediaH = panel.getHeatMap().rows();

        resizePanelToRetainAspectRatio(mediaW, mediaH, panel, panelContainer);
    }

    private static void resizePanelToRetainAspectRatio(double normalWidth, double normalHeight,
            JPanel panel, JPanel panelContainer) {

        double mediaW = normalWidth;
        double mediaH = normalHeight;
        double mediaWidthPerHeight = mediaW / mediaH;

        int w = panelContainer.getWidth();
        int h = panelContainer.getHeight();

        double panelW = w;
        double panelH = h;
        double panelWidthPerHeight = panelW / panelH;

        if (mediaWidthPerHeight > panelWidthPerHeight)
            h = (int) Math.floor(w * Math.pow(mediaWidthPerHeight, -1));
        else
            w = (int) Math.floor(h * mediaWidthPerHeight);

        panel.setPreferredSize(new Dimension(w, h));
        panelContainer.revalidate();
    }
}
