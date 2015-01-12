package edu.rutgers.winlab.javafaces;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;


import edu.rutgers.winlab.Constants;

public class FaceDetect {

	public HashMap<String, ArrayList<DetectedFaces>> run(String requestId,String fileName)  {

		HashMap<String, ArrayList<DetectedFaces>> detectedFaces=new HashMap<String, ArrayList<DetectedFaces>>();
		try {
			MBFImage image = ImageUtilities.readMBF(new FileInputStream(fileName));
			FaceDetector<DetectedFace,FImage> fd = new HaarCascadeDetector(80);
			List<DetectedFace> faces = fd. detectFaces (Transforms.calculateIntensity(image));
			System.out.println("Found "+faces.size()+" faces");
			int i=1;
			ArrayList<DetectedFaces> dfaces=new ArrayList<DetectedFaces>();
			for (DetectedFace face : faces) {
				try {
					Rectangle rectangle=face.getBounds();
					BufferedImage bi = ImageIO.read(new File(fileName));
					BufferedImage subImage=bi.getSubimage((int)rectangle.x,(int)rectangle.y,(int)rectangle.width,(int)rectangle.height);
					BufferedImage thumbnail = new BufferedImage(125, 150, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = thumbnail.createGraphics();
					g.drawImage(subImage, 0, 0,125, 150, null);
					g.dispose();

					makeNewFolder(Constants.PROBE);
					ImageIO.write(thumbnail, "png", new File(Constants.PROBE+requestId+"_"+i+".png"));
					DetectedFaces aFace=new DetectedFaces();
					aFace.setFileName(Constants.PROBE+requestId+"_"+i+".png");
					aFace.setX((int)rectangle.x);
					aFace.setY((int)rectangle.y);
					aFace.setWidth((int)rectangle.width);
					aFace.setHeight((int)rectangle.height); 
					dfaces.add(aFace);        
					i++;
				} catch (IOException e) {              
					e.printStackTrace();
				}
			}
			detectedFaces.put(requestId,dfaces);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
		return detectedFaces;
	}

	private void makeNewFolder(String fldr){
		File folder=new File(fldr);
		if (!folder.isDirectory()){
			folder.mkdir();
		}
	}
	public static void main(String[] args){
		FaceDetect f=new FaceDetect();
		f.run("a","C:\\Users\\Saumya\\Pictures\\pics1\\family" +
				".jpg");
	}
}
