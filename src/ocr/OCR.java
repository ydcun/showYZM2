
package ocr;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.util.OS;

public class OCR {
    private final String LANG_OPTION = "-l"; // 英文字母小写l，并非数字1
    private final String OPTION = "-psm"; // 英文字母小写l，并非数字1
    private final String EOL = System.getProperty("line.separator");
    private String tessPath = "d:\\Program Files\\Tesseract-OCR";

    // private String tessPath = new File("tesseract").getAbsolutePath();

    public String recognizeText(File imageFile, String imageFormat) throws Exception {
        File tempImage = imageFile;// ImageIOHelper.createImage(imageFile,imageFormat);

        /************ 降噪处理***begin ***/
        BufferedImage image;
        image = ImageIO.read(imageFile);
        int width = image.getTileWidth();
        int height = image.getTileHeight();

        /*
         * 对Image进行处理。一个二重循环，用image.getRGB方法来取得每个点的颜色， 然后对颜色计数，并放到一个HashMap里去
         */
        Map<Integer, Integer> mapColor = new HashMap<Integer, Integer>();
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int color = image.getRGB(i, j);
                Integer count = mapColor.get(color);
                if (count == null)
                    count = 0;
                count++;
                mapColor.put(color, count);
            }
        /*
         * 已经处理过的HashMap（key是颜色，value该颜色的点的个数）进行排序得到 list。
         * 开心网的验证码是4位，然后每个的颜色不同。那么取list的前5项，必然是背景色和4位验证码的颜色。
         */
        List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(
                mapColor.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Entry<Integer, Integer> arg0,
                    Entry<Integer, Integer> arg1) {
                return arg1.getValue() - arg0.getValue();
            }
        });
        list = list.subList(0, 5);// 实际实验中发现很不理想，同一个字母放大会发现不一样的颜色

        /*
         * 首先取出list的第一项（背景）的颜色：int intBack = list.get(0).getKey();
         * 循环体中，如果是背景色或者验证码的颜色（if
         * (setColor.contains(color))），那么继续循环；否则，该点为噪音，要设置成背景色（image.setRGB(i,
         * j, intBack)）。
         */
        int intBack = list.get(0).getKey();
        Set<Integer> setColor = new HashSet<Integer>();
        for (Map.Entry<Integer, Integer> entry : list) {
            setColor.add(entry.getKey());
        }
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                int color = image.getRGB(i, j);
                if (setColor.contains(color))
                    continue;
                image.setRGB(i, j, intBack);
            }

        File file = new File(imageFile.getParentFile(), "temp.jpg");
        ImageIO.write(image, "JPEG", file);
        /************ 降噪处理***end ***/

        /*** 调用专业接口**begin **/
        BufferedImage image_ex;
        image_ex = ImageIO.read(imageFile);
        ImageFilter filter = new ImageFilter(image_ex);
        ;
        File file_ex = new File(imageFile.getParentFile(), "temp_ex.jpg");
        ImageIO.write(filter.lineGrey(), "JPEG", file_ex);// lineGrey效果已经算不错的了，但是还是识别不出来
        /*** 调用专业接口**end **/

        File outputFile = new File(imageFile.getParentFile(), "output");
        StringBuffer strB = new StringBuffer();
        List<String> cmd = new ArrayList<String>();
        if (OS.isWindowsXP()) {
            cmd.add(tessPath + "\\tesseract");
        } else if (OS.isLinux()) {
            cmd.add("tesseract");
        } else {
            cmd.add(tessPath + "\\tesseract");
        }
        cmd.add("");
        cmd.add(outputFile.getName());
        cmd.add(LANG_OPTION);
        // cmd.add("chi_sim");
        cmd.add("newtiff");// 有个verify的包，是从火车订票中提取出来的
        cmd.add(OPTION);
        cmd.add("7");

        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(imageFile.getParentFile());

        cmd.set(1, "temp_ex.jpg"/* tempImage.getName() */);
        pb.command(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        // tesseract.exe 1.jpg 1 -l chi_sim
        int w = process.waitFor();

        // 删除临时正在工作文件
        // tempImage.delete();

        if (w == 0) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    outputFile.getAbsolutePath() + ".txt"), "UTF-8"));

            String str;
            while ((str = in.readLine()) != null) {
                strB.append(str).append(EOL);
            }
            in.close();
        } else {
            String msg;
            switch (w) {
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
        return strB.toString();
    }
}
