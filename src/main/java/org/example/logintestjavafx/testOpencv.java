package org.example.logintestjavafx;
import org.opencv.core.Core;

public class testOpencv {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println(Core.VERSION);
    }
}
