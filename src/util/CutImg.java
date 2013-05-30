package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;



public class CutImg {
	public static void main(String[] args) throws Exception {
		new CutImg().cutImg();
	}
	public void cutImg() throws Exception{
		String dirName="testimg";
		//获取目录下所有的文件
		File[] files = new File(dirName).listFiles();
//		File file = files[0];
		int i=0;
		for(File file :files){
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
	        ImageIO.write(nbi, "jpg", new File("okimg/jpg/"+i+".jpg"));  
	        
	        RenderedOp src = JAI.create("fileload","okimg/jpg/"+i+".jpg");
	        OutputStream os = new FileOutputStream("okimg/"+i+".tif");
	        TIFFEncodeParam param = new TIFFEncodeParam();
	        ImageEncoder enc = ImageCodec.createImageEncoder("TIFF", os,param);
	        enc.encode(src);
	        os.close();//关闭流
	        i++;
		}
	}
	/**
	 * 根据rgb的值如（255，255，255）  获取到SRgb的值
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public int getSRgb(int r,int g,int b){
		return -1*(((255-r) << 16 )+((255-g) << 8 )+(255-b)+1);
	}
	/**
	 * 根据SRgb 的值获取到对应的rgb值  
	 * @param SRgb
	 * @return
	 */
	public int[] getRgb(int SRgb){
		int[] rgb= {0,0,0} ;
		rgb[0] = (SRgb & 0xff0000 ) >> 16 ; 
		rgb[1] = (SRgb & 0xff00 ) >> 8 ; 
		rgb[2] = (SRgb & 0xff ); 
		return rgb;
	}

}
