package edu.rutgers.winlab.javafaces;

public class MatchResult {
	private String matchFileName;
	private double matchDistance;
	public MatchResult(String matchFileName,double matchDistance){
		this.matchFileName=matchFileName;
		this.matchDistance=matchDistance;
	}
	public String getMatchFileName() {
		return matchFileName;
	}
	public void setMatchFileName(String matchFileName) {
		this.matchFileName = matchFileName;
	}
	public double getMatchDistance() {
		return matchDistance;
	}
	public void setMatchDistance(double matchDistance) {
		this.matchDistance = matchDistance;
	}
	private DetectedFaces face=new DetectedFaces();
	/**
	 * @return the face
	 */
	public DetectedFaces getFace() {
		return face;
	}
	/**
	 * @param face the face to set
	 */
	public void setFace(DetectedFaces face) {
		this.face = face;
	};
	
}