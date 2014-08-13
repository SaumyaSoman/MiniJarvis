package edu.winlab.minijarvis.model;


import java.util.ArrayList;


/**
 * Response class 
 * @author Saumya
 *
 */
public class Results{
	
	private String text;
	private ArrayList<SearchResults> responses=new ArrayList<>();
	
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
	public ArrayList<SearchResults> getResponses() {
		return responses;
	}

	/**
	 * @param events the events to set
	 */
	public void setResponses(ArrayList<SearchResults> events) {
		this.responses = events;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WSResponse [text=" + text + ", responses=" + responses + "]";
	}

}
