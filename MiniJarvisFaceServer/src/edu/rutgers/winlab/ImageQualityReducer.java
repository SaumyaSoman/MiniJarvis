package edu.rutgers.winlab;

import java.io.*;  
import java.util.Iterator;  
import javax.imageio.*;  
import javax.imageio.stream.*;  
import java.awt.image.*;
public class ImageQualityReducer {

	public static void main(String[] args) {

		String folderName="E:\\Software\\eclipse-jee-juno-SR1-win32-x86_64\\demo\\gallery"; 
		File[] files = new File(folderName).listFiles();
		 long sizeThreshold=10000;
		for (File file1 : files) {
			if (file1.isFile()) {
				String imageToMatch=folderName+"/"+file1.getName();
				float quality = 1.0f;  
				  
		        File file = new File(imageToMatch);  
		  
		        long fileSize = file.length();  
		  
		       
				if (fileSize <= sizeThreshold) {  
		            System.out.println("Image file size is under threshold");  
		            return;  
		        }  
		  
		        Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");  
		  
		        ImageWriter writer = (ImageWriter)iter.next();  
		  
		        ImageWriteParam iwp = writer.getDefaultWriteParam();  
		  
		        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);  
		  
		        FileInputStream inputStream;
				try {
					inputStream = new FileInputStream(file);
					 BufferedImage originalImage = ImageIO.read(inputStream);  
				        IIOImage image = new IIOImage(originalImage, null, null);  
				  
				        float percent = 0.1f;   // 10% of 1  
				  
				        while (fileSize > sizeThreshold) {  
				            if (percent >= quality) {  
				                percent = percent * 0.1f;  
				            }  
				  
				            quality -= percent;  
				  
				            File fileOut = new File(imageToMatch);  
				            if (fileOut.exists()) {  
				                fileOut.delete();  
				            }  
				            FileImageOutputStream output = new FileImageOutputStream(fileOut);  
				  
				            writer.setOutput(output);  
				  
				            iwp.setCompressionQuality(quality);  
				  
				            writer.write(null, image, iwp);  
				  
				            File fileOut2 = new File(imageToMatch);  
				            long newFileSize = fileOut2.length();  
				            if (newFileSize == fileSize) {  
				                // cannot reduce more, return  
				                break;  
				            } else {  
				                fileSize = newFileSize;  
				            }  
				            System.out.println("quality = " + quality + ", new file size = " + fileSize);  
				            output.close();  
				        }  
				  
				        writer.dispose();  
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
		  
		       
				System.out.println(imageToMatch);
			}
		}
	}  
}
