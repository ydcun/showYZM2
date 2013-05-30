
package ocr;

import java.io.File;
import java.io.IOException;

public class Test {

    /** */
    /**
     * @param args
     */
    public static void main(String[] args) {
        String path = "E:/GitHub/showYZM/okimg/jpg/3.jpg";
        try {
        	File[] files = new File("testimg").listFiles();
        	for(File f: files){
        		String valCode = new OCR().recognizeText(f, "jpg");
        		System.out.println(f.getName()+":"+valCode);
        	}
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
