package edu.rutgers.winlab.picasadb;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;



public class PicasaFaces {
	private static final String PARAM_PICASA_DB_FOLDER = "C:\\Users\\Saumya\\AppData\\Local\\Google\\Picasa2\\db3\\";
	private static final String PARAM_PICASA_OUTPUT_FOLDER = "E:\\winlab\\training";
	PMPDB db;
	HashMap<String, String> personsId;
	HashMap<String, ArrayList<Face>> personFaces;
	HashMap<Long, Image> images;

	public PicasaFaces(String folder) {
		db = new PMPDB(folder);
		personsId = new HashMap<String, String>();
		personFaces = new HashMap<String, ArrayList<Face>>();
		images = new HashMap<Long, Image>();
	}
	
	public void populate() throws Exception{
		db.populate();
	}
	
	public void populatePersons(){
		ArrayList<String> tokens = db.albumdata.get("token");
		ArrayList<String> name = db.albumdata.get("name");
		int nb = tokens.size();
		personsId.put("0", "nobody");
		
		for (int i=0; i<nb; i++){
			String t = tokens.get(i);
			if(t.startsWith("]facealbum:")){
				personsId.put(t.split(":")[1], name.get(i));
			}
		}
	}
	public static void main(String[] args) throws Exception {
    	String folder=null;
    	String output=null;
    	boolean prefix =false;
    	String convert = null;
        try {
            // parse the command line arguments        
            folder = PARAM_PICASA_DB_FOLDER;
            String path = PARAM_PICASA_OUTPUT_FOLDER;
            System.out.println("Path..."+path);
            	output = EnvironmentVariables.expandEnvVars(path);
                if(!output.endsWith(File.separator)){
                	output += File.separator;
                }
            	if(! new File(output).exists()){
            		new File(output).mkdir();
            	}
            	convert=EnvironmentVariables.expandEnvVars(path);
        }
        catch(Exception exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
        
        PicasaFaces faces = new PicasaFaces(folder);
        faces.populate();
        faces.populatePersons();
        faces.gatherImages();
        faces.processImages(output, prefix, convert);

	}
	
	public void gatherImages(){
		long nb=db.indexes.entries;	
		
		for(int i=0; i<nb; i++){
			
			try{
				
				if(db.indexes.indexes.get(i).compareTo(db.indexes.folderIndex)!=0){ // not a folder
					String path = db.indexes.names.get(new Long(db.indexes.indexes.get(i)).intValue()) + db.indexes.names.get(i);
					int w = Integer.parseInt(db.imagedata.get("width").get(i));
					int h = Integer.parseInt(db.imagedata.get("height").get(i));
					Image img = new Image(path, i, w, h);
					System.out.println(path+"...."+db.imagedata.get("suggestionpersonalbumid"));
					String personName = personsId.get(db.imagedata.get("personalbumid").get(i));
		            if(!db.imagedata.get("facerect").get(i).equals("0000000000000001")){
		            	img.hasFaceData=true;
		            	
		            	Face f = img.addFace(db.imagedata.get("facerect").get(i), personName );
		            	if(!db.imagedata.get("personalbumid").get(i).equals("0")){
		            		if(!personFaces.containsKey(personName)){
		            			personFaces.put(personName, new ArrayList<Face>());
		            		}
		            		
		            		personFaces.get(personName).add(f);
		            	}
		            	
		            	if(!db.imagedata.get("suggestionpersonalbumid").get(i).equals("0")){
		            		personName = personsId.get(db.imagedata.get("suggestionpersonalbumid").get(i));
		            		System.out.println(personName);
		            		if(!personFaces.containsKey(personName)){
		            			personFaces.put(personName, new ArrayList<Face>());
		            		}		            		
		            		f = img.addFace(db.imagedata.get("facerect").get(i), personName );
		            		personFaces.get(personName).add(f);
		            	}
		            }
					images.put((long)i, img);
				}else{ // folder
	            	if(db.indexes.names.get(i).equals("") && db.indexes.originalIndexes.get(i).compareTo(db.indexes.folderIndex)!=0){ // reference
	            		if(i>=db.imagedata.get("personalbumid").size()){
	            			break;
	            		}
	            		String personName = personsId.get(db.imagedata.get("personalbumid").get(i));
	            		Long originalIndex = db.indexes.originalIndexes.get(i);
	            		if(!db.imagedata.get("facerect").get(i).equals("0000000000000001")){
	            			images.get(originalIndex).hasChild=true;
	    	            	Face f = images.get(originalIndex).addFace(db.imagedata.get("facerect").get(i), personName);
	    	            	if(!db.imagedata.get("personalbumid").get(i).equals("0")){
	    	            		if(!personFaces.containsKey(personName)){
	    	            			personFaces.put(personName, new ArrayList<Face>());
	    	            		}
	    	            		
	    	            		personFaces.get(personName).add(f);
	    	            	}
	    	            	if(!db.imagedata.get("suggestionpersonalbumid").get(i).equals("0")){
			            		personName = personsId.get(db.imagedata.get("suggestionpersonalbumid").get(i));
			            		System.out.println(personName);
			            		if(!personFaces.containsKey(personName)){
			            			personFaces.put(personName, new ArrayList<Face>());
			            		}
			            		System.out.println("file.."+db.imagedata.get("name").get(i) );
			            		f = images.get(originalIndex).addFace(db.imagedata.get("facerect").get(i), personName);
			            		personFaces.get(personName).add(f);
			            	}
	    	            }
	            	}// else folder
	            }
			
		}catch(Exception e){
			continue;
		}
		
		}
		
	}
	
	public void processImages(String output, boolean prefix, String convert) throws IOException, InterruptedException{
		StringBuilder csv = new StringBuilder("person;filename;image width;image height;face x;face y;face width;face height\n");
		for(String person:personFaces.keySet()){
			File folderPerson = new File(output+person);
			if(convert!=null && !folderPerson.exists()){
				folderPerson.mkdir();
			}
			
			int i=0;
			for(Face f:personFaces.get(person)){
				String path;
				path=FilenameUtils.separatorsToSystem(f.img.path);
				System.out.println("the path.."+path);
				int x=f.x;
				int y=f.y;
				String separator = File.separator;
				if(separator.equals("\\")){
					separator="\\\\";
				}
				String [] file = path.split(separator);
				String prefixStr = "";
				if(prefix){
					prefixStr = ""+ i +"_";
				}
				String filename = output + person + File.separator + prefixStr+file[file.length-1];
				if(convert!=null && new File(filename).exists()){
					System.out.println("Warning, the filename already exist: "+person + File.separator + prefixStr+file[file.length-1]);
				}
				csv.append(person);
//				csv.append(";");
//				if(prefix){
//					csv.append(i);
//				}else{
//					csv.append("none");
//				}
				csv.append(";");
				csv.append(file[file.length-1]);
				csv.append(";");
//				csv.append(f.img.path);
//				csv.append(";");
//				csv.append(path);
//				csv.append(";");
				csv.append(f.img.w);
				csv.append(";");
				csv.append(f.img.h);
				csv.append(";");
				csv.append(f.x);
				csv.append(";");
				csv.append(f.y);
				csv.append(";");
				csv.append(f.w);
				csv.append(";");
				csv.append(f.h);
				csv.append("\n");
				
				cropImageAndSave(path,filename,f.x,f.y,f.w,f.h);
				i++;
			}
		}
		FileWriter fw = new FileWriter(output+"faces.csv");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(csv.toString());
        bw.close();
	}

	private void cropImageAndSave(String input, String output, int x, int y,
			int width, int height) {
		try {
			BufferedImage originalImage = ImageIO.read(new File(input));
			BufferedImage cropped= originalImage.getSubimage(x, y, width, height);
			File outputfile = new File(output);
			ImageIO.write(cropped, "jpg", outputfile);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
