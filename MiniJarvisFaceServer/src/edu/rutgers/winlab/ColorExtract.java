package edu.rutgers.winlab;

import com.google.common.util.concurrent.AtomicLongMap;
import org.imgscalr.Scalr;
import org.opencv.core.Core;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * To extract the color of a recognized object in an image
 * Inspired by an blog post of Jared Allen (http://chironexsoftware.com/blog/?p=60) and Niko Schmuck
 * @author Saumya
 *
 */
public class ColorExtract {
	
    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(new EntityDetect().run("C:\\Users\\Saumya\\Pictures\\picture1.jpg"));        
    }
    
    /**
     * Method to analyze colors in an image
     * @param filename image filename
     * @param rectx
     * @param recty
     * @param w
     * @param h
     * @return String most dominant color
     * @throws IOException
     */
	public String analyseColors(String filename, int rectx, int recty, int w, int h) throws IOException {
    	
		//read the image
    	File file = new File(filename);
        ImageInputStream is = ImageIO.createImageInputStream(file);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(is);
        ImageReader imageReader = iter.next();
        imageReader.setInput(is);

        //get subimage of the image, i.e. the portion enclosed within the rectangle
        BufferedImage image = imageReader.read(0);
        BufferedImage subImage=image.getSubimage(rectx, recty, w-rectx, h-recty);
        
        //rescale to 32X32 pixels to spped up the process. Used Scalr library.
        BufferedImage thumbnail = Scalr.resize(subImage, 32, 32);
        int height = thumbnail.getHeight();
        int width = thumbnail.getWidth();

        Map<Color, Double> colorDist = new HashMap<Color, Double>();
        AtomicLongMap<Color> colorCount = AtomicLongMap.create();

        final int alphaThershold = 10;
        long pixelCount = 0;
        long avgAlpha = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x <width; x++) {
                int rgb = thumbnail.getRGB(x, y);
                int[] rgbArr = getRGBArr(rgb);

                if (rgbArr[0] <= alphaThershold)
                   continue; //ignore

                avgAlpha += rgbArr[0];

                Color clr = new Color(rgbArr[1], rgbArr[2], rgbArr[3]);
                colorCount.getAndIncrement(clr);
                if (!colorDist.containsKey(clr)) {
                    double dist = 0.0d;

                    for (int y2 = 0; y2 < height; y2++) {
                        for (int x2 = 0; x2 < width; x2++) {
                            int rgb2 = thumbnail.getRGB(x2, y2);
                            int[] rgbArr2 = getRGBArr(rgb2);

                            if (rgbArr2[0] <= alphaThershold)
                                continue; //ignore

                            dist += Math.sqrt(Math.pow((double) (rgbArr2[1] - rgbArr[1]), 2) +
                                              Math.pow((double) (rgbArr2[2] - rgbArr[2]), 2) +
                                              Math.pow((double) (rgbArr2[3] - rgbArr[3]), 2));
                        } // for-x2
                    } // inner for-y2 loop

                    colorDist.put(clr, dist);
                }
                pixelCount++;
            } // for-x
        } // outer for-y loop

        // clamp alpha
        avgAlpha = avgAlpha / pixelCount;
        if (avgAlpha >= (255 - alphaThershold))
            avgAlpha = 255;

        // sort RGB distances
        ValueComparator bvc = new ValueComparator(colorDist);
        TreeMap<Color, Double> sorted_map = new TreeMap<Color, Double>(bvc);
        sorted_map.putAll(colorDist);

        // take weighted average of top 2% colors
        double threshold = 0.02;
        int nrToThreshold = Math.max(1, (int)(colorDist.size() * threshold));
        int mostThreshold = Math.max(1, (int)(colorDist.size() * 0.8));
        Map<Color, Double> clrsDist = new HashMap<Color, Double>();
        java.util.List<Double> topDist = new ArrayList<Double>();
        java.util.List<Double> mostDist = new ArrayList<Double>();
        java.util.List<Double> allDist = new ArrayList<Double>();
        int i = 0;
        for (Map.Entry<Color, Double> e : sorted_map.entrySet()) {
            Double distance = 1.0d / Math.max(1.0, e.getValue());
            if (i < nrToThreshold) {
                Color clr = e.getKey();
                clrsDist.put(clr, distance);
                topDist.add(e.getValue());
            }
            if (i < mostThreshold) {
                mostDist.add(e.getValue());
            }
            allDist.add(e.getValue());
            i++;
        }

        double sumDist = 0.0d;
        double sumR = 0.0d;
        double sumG = 0.0d;
        double sumB = 0.0d;
        for (Map.Entry<Color,Double> e : clrsDist.entrySet()) {
            sumR += e.getKey().getRed() * e.getValue();
            sumG += e.getKey().getGreen() * e.getValue();
            sumB += e.getKey().getBlue() * e.getValue();
            sumDist += e.getValue();
        }
        Color dominantColor = new Color((int) (sumR / sumDist),
                                        (int) (sumG / sumDist),
                                        (int) (sumB / sumDist));

        return getColorNameFromColor(dominantColor);
    }





    private static int[] getRGBArr(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[]{alpha, red, green, blue};
    }



    static class ValueComparator implements Comparator {

        Map<Color, Double> base;

        public ValueComparator(Map<Color, Double> base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
            if (base.get(a) < base.get(b)) {
                return -1;
            } else if (base.get(a) == base.get(b)) {
                return 0;
            } else {
                return 1;
            }
        }
    }
    
    // list of colors
    private ArrayList<ColorName> initColorList() {
        ArrayList<ColorName> colorList = new ArrayList<ColorName>();
        colorList.add(new ColorName("aqua", 0x00, 0xFF, 0xFF));
        colorList.add(new ColorName("beige", 0xF5, 0xF5, 0xDC));
        colorList.add(new ColorName("black", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("blue", 0x00, 0x00, 0xFF));
        colorList.add(new ColorName("brown", 0xA5, 0x2A, 0x2A));
        colorList.add(new ColorName("Crimson", 0xDC, 0x14, 0x3C));
        colorList.add(new ColorName("Cyan", 0x00, 0xFF, 0xFF));
        colorList.add(new ColorName("dark blue", 0x00, 0x00, 0x8B));
        colorList.add(new ColorName("dark green", 0x00, 0x64, 0x00));
        colorList.add(new ColorName("dark red", 0x8B, 0x00, 0x00));
        colorList.add(new ColorName("green", 0x00, 0x80, 0x00));
        colorList.add(new ColorName("light blue", 0xAD, 0xD8, 0xE6));
        colorList.add(new ColorName("magenta", 0xFF, 0x00, 0xFF));
        colorList.add(new ColorName("maroon", 0x80, 0x00, 0x00));
        colorList.add(new ColorName("orange", 0xFF, 0xA5, 0x00));
        colorList.add(new ColorName("pink", 0xFF, 0xC0, 0xCB));
        colorList.add(new ColorName("purple", 0x80, 0x00, 0x80));
        colorList.add(new ColorName("red", 0xFF, 0x00, 0x00));
        colorList.add(new ColorName("violet", 0xEE, 0x82, 0xEE));
        colorList.add(new ColorName("white", 0xFF, 0xFF, 0xFF));
        colorList.add(new ColorName("yellow", 0xFF, 0xFF, 0x00));
        colorList.add(new ColorName("yellow green", 0x9A, 0xCD, 0x32));
        colorList.add(new ColorName("light yellow", 0xFF, 0xFF, 0xE0));

        return colorList;
    }
    
    public String getColorNameFromColor(Color color) {
        return getColorNameFromRgb(color.getRed(), color.getGreen(),
                color.getBlue());
    }
    public String getColorNameFromRgb(int r, int g, int b) {
        ArrayList<ColorName> colorList = initColorList();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return closestMatch.getName();
        } else {
            return "No matched color name.";
        }
    }
    public class ColorName {
        public int r, g, b;
        public String name;

        public ColorName(String name, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        public int computeMSE(int pixR, int pixG, int pixB) {
            return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3);
        }

        public int getR() {
            return r;
        }

        public int getG() {
            return g;
        }

        public int getB() {
            return b;
        }

        public String getName() {
            return name;
        }
    }
}