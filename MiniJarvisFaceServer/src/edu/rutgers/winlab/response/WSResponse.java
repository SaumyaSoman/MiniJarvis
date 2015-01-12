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
	private String result="";
	private ArrayList<Annotations> annotations=new ArrayList<Annotations>();
	
	

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return the annotations
	 */
	public ArrayList<Annotations> getAnnotations() {
		return annotations;
	}

	/**
	 * @param annotations the annotations to set
	 */
	public void setAnnotations(ArrayList<Annotations> annotations) {
		this.annotations = annotations;
	}
}
	


