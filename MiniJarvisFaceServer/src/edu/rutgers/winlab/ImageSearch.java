package edu.rutgers.winlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageSearch {

	public ArrayList<SearchResponse> getSearchResults(String filepath, String text){
		
		ArrayList<SearchResponse> respList=new ArrayList<SearchResponse>();	
//		File file = new File("C:\\Users\\Saumya\\Pictures\\hmm.jpg");
//		String fileName = file.getName();
		try {
			URL url = new URL("https://www.googleapis.com/customsearch/v1?key=AIzaSyC9fo9wv4ZMfpStZv0gyJGKNkRjLN-l99g&cx=004023995457250620417:jmuhwnphnrw&q="+ URLEncoder.encode(text));
			//URL url = new URL("http://images.google.com/searchbyimage?site=search&image_url="+fileName);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
	 
			//add reuqest header
			con.setRequestMethod("GET");
			InputStream is=con.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		    StringBuilder responseStrBuilder = new StringBuilder();
		    String respString;
		    while ((respString = streamReader.readLine()) != null)
		        responseStrBuilder.append(respString);
		    JSONObject resp= new JSONObject(responseStrBuilder.toString());
		    System.out.println(resp.toString());
		    
		    // parse json
		    JSONArray jsonMainNode = resp.optJSONArray("items");

			/*********** Process each JSON Node ************/

			int lengthJsonArr = jsonMainNode.length(); 
			
			
			for(int i=0; i < lengthJsonArr; i++)
			{
				/****** Get Object for each JSON node.***********/
				JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
				SearchResponse data=new SearchResponse();
				/******* Fetch node values **********/
				data.setTitle(jsonChildNode.optString("title"));
				data.setLink(jsonChildNode.optString("link"));
				data.setSnippet(jsonChildNode.optString("snippet"));
				respList.add(data);
			}
		} catch (IOException e) {
			System.out.println("Auuup"+e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return respList;		
		
	}
}
