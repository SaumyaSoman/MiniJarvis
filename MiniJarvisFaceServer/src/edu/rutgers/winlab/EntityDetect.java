package edu.rutgers.winlab;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

/*
 * Detects faces in an image, draws boxes around them, and writes the results
 * to "faceDetection.png".
 */
public class EntityDetect {
	
	private String entity=null;
	public String run(String fileName) throws IOException {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("\nRunning DetectFaceDemo");

		// Create a face detCore.NATIVE_LIBRARY_NAMEector from the cascade file in the resources
		// directory.

		String folderName="E:\\winlab\\cascadexmls";
		File[] files = new File(folderName).listFiles();
		boolean face=false;
		for (File file : files) {
			if (file.isFile()) {
				System.out.println(file.getName());
				System.out.println(fileName);
				CascadeClassifier faceDetector = new CascadeClassifier(folderName+"\\"+file.getName());
				Mat image = Highgui.imread(fileName);
				// Detect faces in the image.
				// MatOfRect is a special container class for Rect.
				MatOfRect faceDetections = new MatOfRect();
				faceDetector.detectMultiScale(image, faceDetections);
				if(faceDetections.toArray().length>0){
					face=true;
					System.out.println(String.format("Detected %s faces",
							faceDetections.toArray().length));
					// Draw a bounding box around each face.
					for (Rect rect : faceDetections.toArray()) {
						Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
								+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
					}

					// Save the visualized detection.
					String filename = "E:\\game\\faceDetection.jpg";
					Highgui.imwrite(filename, image);
				}
				recognizeEntity(face,fileName);

			}
		}
		
		
		return entity;

	}

	private void recognizeEntity(boolean face, String fileName) throws IOException {
		// TODO Auto-generated method stub
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
				System.out.println(folderName+"\\"+file.getName());
				Mat image = Highgui.imread(fileName);
				// Detect faces in the image.
				// MatOfRect is a special container class for Rect.
				MatOfRect faceDetections = new MatOfRect();
				faceDetector.detectMultiScale(image, faceDetections);
				if(faceDetections.toArray().length>0){
					
					System.out.println(String.format("Recognized %s entities",
							faceDetections.toArray().length));
					entity=file.getName().substring(0, file.getName().lastIndexOf('.'));
					// Draw a bounding box around each face.
					Rect rect =faceDetections.toArray()[0];
						if(!face){
							System.out.println("inside here");
							System.out.println(rect.x+" "+ rect.y+" "+rect.x+ rect.width+" "+rect.y + rect.height);
							ColorExtract z=new ColorExtract();
							String color =z.analyseColors(fileName,rect.x, rect.y,rect.x+ rect.width, rect.y + rect.height);
							entity=color+" "+entity;
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