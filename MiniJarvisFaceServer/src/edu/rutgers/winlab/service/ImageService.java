package edu.rutgers.winlab.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import edu.rutgers.winlab.Constants;
import edu.rutgers.winlab.javafaces.DetectedFaces;
import edu.rutgers.winlab.javafaces.FaceDetect;
import edu.rutgers.winlab.javafaces.FaceRecognition;
import edu.rutgers.winlab.javafaces.MatchResult;
import edu.rutgers.winlab.response.Annotations;
import edu.rutgers.winlab.response.WSResponse;


@Path("/image")

/**
 * REST web service which receives image, does face/object detection and recognition, color extraction and google search.
 * The response is text(object/person) and search results.
 * @author Saumya
 *
 */
public class ImageService {

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json")
	public WSResponse uploadFile(@FormDataParam("image") InputStream uploadedInputStream,
			@FormDataParam("image") FormDataContentDisposition fileDetail) {

		//save the received image with this name
		
		System.out.println(Constants.FILE_LOCATION);
		String uploadedFileLocation = Constants.FILE_LOCATION+ fileDetail.getFileName();

		WSResponse response=new WSResponse();
		try{
			long startTime=System.currentTimeMillis();
			saveToFile(uploadedInputStream, uploadedFileLocation);
			UUID requestId = UUID.randomUUID();
			FaceDetect faceDetect=new FaceDetect();
			HashMap<String, ArrayList<DetectedFaces>> faceList=faceDetect.run(requestId.toString(),uploadedFileLocation);
			FaceRecognition recognition= new FaceRecognition();
			HashMap<String, MatchResult> names=recognition.recognize(requestId.toString(),faceList);
			String output="";
			if(names==null){
				output= "Cannot be recognized";
			}else{
				//if face is identified, do google search
				ArrayList<Annotations> annotations=new ArrayList<Annotations>();
				for (Entry<String, MatchResult> name : names.entrySet()) {
					Annotations annotation=new Annotations();
					//ImageSearch search=new ImageSearch();
					//ArrayList<SearchResponse> searchResp=search.getSearchResults(uploadedFileLocation,output);
					System.out.println(name);
					annotation.setText(name.getKey());
					if(name.getValue()!=null){
						DetectedFaces face=name.getValue().getFace();
						annotation.setX(face.getX());
						annotation.setY(face.getY());
						annotation.setHeight(face.getHeight());
						annotation.setWidth(face.getWidth());
					}					
					annotations.add(annotation);
				}
				response.setAnnotations(annotations);
			}
			response.setResult(output);
			
			long endTime=System.currentTimeMillis();
			 
			System.out.println("\ntotal time taken="+(endTime-startTime)+" millisecs");
		}catch(Exception e){
			e.printStackTrace();
		}
		return response;

	}


	/**
	 * Method to save uploaded file to new location
	 * @param uploadedInputStream InputStream
	 * @param uploadedFileLocation string
	 */
	private void saveToFile(InputStream uploadedInputStream,String uploadedFileLocation) {

		try {
			OutputStream out = null;
			int read = 0;
			byte[] bytes = new byte[1024]; 
			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
	
	
	//public static void main(String[] args) {
//	File[] files=new File("E:\\Software\\eclipse-jee-juno-SR1-win32-x86_64\\test").listFiles();
//	for(int i=0;i<files.length;i++){
//		String uploadedFileLocation = "E:\\Software\\eclipse-jee-juno-SR1-win32-x86_64\\test\\" + files[i].getName();
//		WSResponse response=new WSResponse();
//		try{
//			long startTime=System.currentTimeMillis();
//			UUID requestId = UUID.randomUUID();
//			FaceDetect faceDetect=new FaceDetect();
//			HashMap<String, ArrayList<DetectedFaces>> faceList=faceDetect.run(requestId.toString(),uploadedFileLocation);
//			FaceRecognition recognition= new FaceRecognition();
//			HashMap<String, MatchResult> names=recognition.recognize(requestId.toString(),faceList);
//			String output="";
//			StringBuffer sb=new StringBuffer();
//			if(names==null && names.size()<=0){
//				output= "Cannot be recognized";
//			}else{
//				//if face is identified, do google search
//				ArrayList<Annotations> annotations=new ArrayList<Annotations>();
//				
//				for (Entry<String, MatchResult> name : names.entrySet()) {
//					Annotations annotation=new Annotations();
//					//ImageSearch search=new ImageSearch();
//					//ArrayList<SearchResponse> searchResp=search.getSearchResults(uploadedFileLocation,output);
//					System.out.println(name);
//					annotation.setText(name.getKey());
//					sb.append(name.getKey()+",");
//					if(name.getValue()!=null){
//						DetectedFaces face=name.getValue().getFace();
//						annotation.setX(face.getX());
//						annotation.setY(face.getY());
//						annotation.setHeight(face.getHeight());
//						annotation.setWidth(face.getWidth());
//					}					
//					annotations.add(annotation);
//				}
//				response.setAnnotations(annotations);
//			}
//			response.setResult(output);
//			
//			long endTime=System.currentTimeMillis();
//			
//			System.out.println(uploadedFileLocation+"..."+sb.toString());
//			System.out.println("\ntotal time taken="+(endTime-startTime)+" millisecs");
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//	
//}

}

