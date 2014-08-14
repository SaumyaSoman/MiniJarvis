package edu.rutgers.winlab.response;


import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
/**
 * Web service response  
 * @author Saumya
 *
 */
public class WSResponse{
	
	private String text;
	private ArrayList<SearchResponse> responses=new ArrayList<>();
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the events
	 */
	public ArrayList<SearchResponse> getResponses() {
		return responses;
	}

	/**
	 * @param events the events to set
	 */
	public void setResponses(ArrayList<SearchResponse> events) {
		this.responses = events;
	}

}
