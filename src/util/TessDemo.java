package util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.jdesktop.swingx.util.OS;

import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.TessAPI1.TessBaseAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.vietocr.ImageIOHelper;



public class TessDemo {
	private String tessPath = "d:\\Program Files\\Tesseract-OCR\\";
	private String tempPath="temp/";
	public String getNum(File file) throws Exception {
		String path = file.getParentFile().getPath();
		BufferedImage bi = ImageIO.read(file);
		//		buffImage = buffImage.getSubimage(1, 1, buffImage.getWidth()-2, buffImage.getHeight()-2);
			
				int h=bi.getHeight();//获取图像的高  
		        int w=bi.getWidth();//获取图像的宽  

		        int srgb=bi.getRGB(0, 0);//获取指定坐标的ARGB的像素值  
		        int r,g,b;
		        BufferedImage nbi=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);  
		        for (int y = 0; y < h; y++) {  
		            for (int x = 0; x < w; x++) {  
		            	srgb = bi.getRGB(x, y);
			            nbi.setRGB(x, y, srgb);
		            }  
		        } 
		        ImageIO.write(nbi, "jpg", new File(file.getParentFile(),"temp.jpg"));  

		 
		        File outputFile = new File(file.getParentFile(),"out");
		        StringBuffer strB = new StringBuffer();
		        List<String> cmd = new ArrayList<String>();
		        if (OS.isWindowsXP()) {
		            cmd.add(tessPath + "tesseract");
		        } else if (OS.isLinux()) {
		            cmd.add("tesseract");
		        } else {
		            cmd.add(tessPath + "tesseract");
		        }
		        cmd.add("");
		        cmd.add(outputFile.getName());
		        cmd.add("-l");
		        // cmd.add("chi_sim");
		        cmd.add("newtiff");// 有个verify的包，是从火车订票中提取出来的
		        cmd.add("-psm");
		        cmd.add("7");
		        
		        
		        ProcessBuilder pb = new ProcessBuilder();
		        pb.directory(outputFile.getParentFile());

		        cmd.set(1, "temp.jpg"/* tempImage.getName() */);
		        pb.command(cmd);
		        pb.redirectErrorStream(true);

		        Process process = pb.start();
		        // tesseract.exe 1.jpg 1 -l chi_sim
		        int w1 = process.waitFor();

		        // 删除临时正在工作文件
		        // tempImage.delete();

		        if (w1 == 0) {
		            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
		            		outputFile+".txt"), "UTF-8"));

		            String str;
		            while ((str = in.readLine()) != null) {
		                strB.append(str).append("\r\n");
		            }
		            in.close();
		        } else {
		            String msg;
		            switch (w1) {
		                case 1:
		                    msg = "Errors accessing files.There may be spaces in your image's filename.";
		                    break;
		                case 29:
		                    msg = "Cannot recongnize the image or its selected region.";
		                    break;
		                case 31:
		                    msg = "Unsupported image format.";
		                    break;
		                default:
		                    msg = "Errors occurred.";
		            }
		            // tempImage.delete();
		            throw new RuntimeException(msg);
		        }
		        new File(outputFile.getAbsolutePath() + ".txt").delete();
		        new File(file.getParentFile()+"/temp.jpg").delete();
		        return strB.toString();
		      
		
	}
	public static void main(String[] args) throws Exception {
		File[] files = new File("testimg").listFiles();
		for(File f:files){
			String newString = new TessDemo().getNum(f);
			System.out.println(f.getName()+":"+TessDemo.replaceBlank(newString));
		}
	}
	 public static String replaceBlank(String str) {
	        String a= "";
	        if (str!=null) {
	            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
	            Matcher m = p.matcher(str);
	            a= m.replaceAll("");
	        }
	        return a;
	
	    }
}
