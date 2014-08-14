package edu.rutgers.winlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.winlab.response.SearchResponse;

public class ImageSearch {

	/**
	 * Method to get search results using Google Custom search API
	 * @param filepath String required for reverse image search in later stage
	 * @param text the annotation (object/person)
	 * @return ArrayList<SearchResponse>
	 */
	public ArrayList<SearchResponse> getSearchResults(String filepath, String text){

		ArrayList<SearchResponse> respList=new ArrayList<SearchResponse>();	
		File file = new File(filepath); //required later for reverse image search
		String fileName = file.getName();
		try {

			//call the API. The API key has to be generated. Please generate keys for ### parameters

			URL url = new URL("https://www.googleapis.com/customsearch/v1?key=###&cx=###&q="+ URLEncoder.encode(text));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			//add request header
			con.setRequestMethod("GET");
			InputStream is=con.getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
			StringBuilder responseStrBuilder = new StringBuilder();
			String respString;
			while ((respString = streamReader.readLine()) != null)
				responseStrBuilder.append(respString);
			JSONObject resp= new JSONObject(responseStrBuilder.toString());

			// parse json
			JSONArray jsonMainNode = resp.optJSONArray("items");
			int lengthJsonArr = jsonMainNode.length(); 	

			for(int i=0; i < lengthJsonArr; i++)
			{
				JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
				SearchResponse data=new SearchResponse();
				data.setTitle(jsonChildNode.optString("title"));
				data.setLink(jsonChildNode.optString("link"));
				data.setSnippet(jsonChildNode.optString("snippet"));
				respList.add(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return respList;		

	}
}
