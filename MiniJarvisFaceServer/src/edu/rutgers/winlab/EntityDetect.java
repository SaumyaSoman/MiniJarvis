package edu.rutgers.winlab;
import java.io.File;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Detects and recognizes an object/face in an image, draws boxes around it, and writes the results to "faceDetection.png".
 * If an object, color extraction is done.
 * @author Saumya
 *
 */
public class EntityDetect {

	private String annotation=null;

	/**
	 * Method to detect and recognize object/face in an image
	 * @param fileName String
	 * @return String the name or object identified
	 * @throws IOException
	 */
	public String run(String fileName) throws IOException {

		//loads the opencv library. The opencv dll should be set in java library path
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Face is detected by using cascade files in the opencv/sources

		String folderName="E:\\winlab\\cascadexmls"; //path were the cascade cmls are stored
		File[] files = new File(folderName).listFiles();
		boolean face=false;
		for (File file : files) {
			if (file.isFile()) {
				CascadeClassifier faceDetector = new CascadeClassifier(folderName+"\\"+file.getName());
				Mat image = Highgui.imread(fileName);
				// Detect faces in the image.
				// MatOfRect is a special container class for Rect.
				MatOfRect faceDetections = new MatOfRect();
				faceDetector.detectMultiScale(image, faceDetections);
				if(faceDetections.toArray().length>0){
					face=true;
					// Draw a bounding box around each face.
					for (Rect rect : faceDetections.toArray()) {
						Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
								+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
					}
					// Save the visualized detection.
					String filename = "E:\\game\\faceDetection.jpg";
					Highgui.imwrite(filename, image);
				}
				recognizeannotation(face,fileName);
			}
		}		
		return annotation;
	}

	/**
	 * Recognizes the object/face using trained cascade xmls
	 * To train for an object/person refer to http://coding-robin.de/2013/07/22/train-your-own-opencv-haar-classifier.html
	 * @param face boolean indicates whether its a face/object
	 * @param fileName String image location
	 * @throws IOException
	 */
	private void recognizeannotation(boolean face, String fileName) throws IOException {

		String folderName="";
		if(face){
			folderName="E:\\winlab\\cascadexmls\\facexmls";
		}else{
			folderName="E:\\winlab\\cascadexmls\\objectxmls";
		}
		File[] files = new File(folderName).listFiles();
		for (File file : files) {
			if (file.isFile()) {
				CascadeClassifier faceDetector = new CascadeClassifier(folderName+"\\"+file.getName());
				System.out.println(face+"..face");
				Mat image = Highgui.imread(fileName);
				// Detect faces in the image.
				// MatOfRect is a special container class for Rect.
				MatOfRect faceDetections = new MatOfRect();
				faceDetector.detectMultiScale(image, faceDetections);
				if(faceDetections.toArray().length>0){
					annotation=file.getName().substring(0, file.getName().lastIndexOf('.'));
					// Draw a bounding box around each face.
					Rect rect =faceDetections.toArray()[0];
					if(!face){
						//if its an object, the color is object is found out and added to the annotation
						ColorExtract z=new ColorExtract();
						String color =z.analyseColors(fileName,rect.x, rect.y,rect.x+ rect.width, rect.y + rect.height);
						annotation=color+" "+annotation; 
						Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
								+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
					}

					// Save the visualized detection.
					String filename = "E:\\game\\faceDetection.jpg";
					System.out.println(String.format("Writing %s", filename));
					Highgui.imwrite(filename, image);
				}	                       
			}
		}		
	}
}