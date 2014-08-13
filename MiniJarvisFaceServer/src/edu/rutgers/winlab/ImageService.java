package edu.rutgers.winlab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import edu.rutgers.winlab.EntityDetect;


@Path("/image")

/**
 * 
 * @author Saumya
 *
 */
public class ImageService {

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json")
	public WSResponse uploadFile(@FormDataParam("image") InputStream uploadedInputStream,
			@FormDataParam("image") FormDataContentDisposition fileDetail) {

		String uploadedFileLocation = "e://game/" + fileDetail.getFileName();
		WSResponse response=new WSResponse();
		// save it
		try{
			saveToFile(uploadedInputStream, uploadedFileLocation);
			String output=new EntityDetect().run(uploadedFileLocation);		
			if(output==null){
				output= "Cannot be recognized";
			}else{
				ImageSearch search=new ImageSearch();
				ArrayList<SearchResponse> searchResp=search.getSearchResults(uploadedFileLocation,output);
				response.setResponses(searchResp);
			}
			System.out.println(output);
			response.setText(output);
		}catch(Exception e){
			e.printStackTrace();
		}
		return response;

	}

	// save uploaded file to new location
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

}

