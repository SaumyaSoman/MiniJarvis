package edu.rutgers.winlab;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ResizeImages {
	public static void main(String[] args) {

		String folderName="E:\\Software\\eclipse-jee-juno-SR1-win32-x86_64\\gallery"; 
		File[] files = new File(folderName).listFiles();
		for (File file : files) {
			if (file.isFile()) {
				String imageToMatch=folderName+"/"+file.getName();
				try {
					BufferedImage subImage = ImageIO.read(new File(imageToMatch));		
					BufferedImage thumbnail = new BufferedImage(125, 150, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = thumbnail.createGraphics();
					g.drawImage(subImage, 0, 0,125, 150, null);
					g.dispose();
					String fname=file.getName().replace(".jpg", ".png");
					//fname=fname.replace(".gif", ".png");
					ImageIO.write(thumbnail, "png", new File("E:\\Software\\eclipse-jee-juno-SR1-win32-x86_64\\gallery\\"+fname));
					System.out.println(fname);
				} catch (IOException e) {
					System.out.println("error"+e.toString());
					e.printStackTrace();
				}
			}
		}
	}
}
