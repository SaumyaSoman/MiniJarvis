package edu.rutgers.winlab;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to OpenCV " + Core.VERSION);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(new EntityDetect().run("C:\\Users\\Saumya\\Pictures\\banana1.jpg"));
        //WSResponse ws=new WSResponse();
//        ws.setText("banana");
//        ws.setResponses(new ImageSearch().getSearchResults("C:\\Users\\Saumya\\Pictures\\hmm.jpg","banana"));
//        System.out.println(ws);
    }

}
