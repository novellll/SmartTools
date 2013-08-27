package com.smart.utils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class ImageUtil {
	
	private static final Logger log = Logger.getLogger(ImageUtil.class);
	
	/**
	 * 结合图片，数据例
	 * {file: '/BostonCityFlow.jpg', x: '0', y:'0', w:'200', h:'200'}
	 * {file: '/CostaRicanFrog.jpg', x: '205', y:'0', w:'300', h:'300'}
	 * @param fileInfos 源图片及大小信息
	 * @param destFile 结果图片
	 * @param pFinalWidth 结果图片大小
	 * @param pFinalHeight
	 * @throws Exception
	 */
	public static void join(List<Map<String, String>> files, String destFile, int pFinalWidth, int pFinalHeight) throws Exception{
		
        BufferedImage tag = new BufferedImage(pFinalWidth, pFinalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();

        for (Map<String, String> file : files) {
        	
		    // get original image file size
		    BufferedImage bi = ImageIO.read(new File(file.get("file")));
		    
	    	// crop image
	    	ImageFilter cropFilter = new CropImageFilter(0, 0, Integer.parseInt(file.get("w")), Integer.parseInt(file.get("h")));
	    	Image image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(bi.getSource(), cropFilter));

	        // draw photo
	        g.drawImage(image, Integer.parseInt(file.get("x")), Integer.parseInt(file.get("y")), null); 
		}
        g.dispose();
        
        // write to file
        ImageIO.write(tag, "JPEG", new File(destFile));
		log.debug("union photo successfully and file name is " + destFile);
	}
	
	/**
	 * 剪切图片，可以缩放
	 * @param srcFile 源文件
	 * @param destFile 转换后的文件
	 * @param x 剪切时，开始的坐标
	 * @param y
	 * @param pDestWidth 剪切的区域
	 * @param pDestHeight
	 * @param ratio 缩放比例
	 * @param pFinalWidth 最终的大小
	 * @param pFinalHeight
	 * @throws Exception
	 */
	public static void cut(
			String srcFile, String destFile, 
			int x, int y, 
			int pDestWidth, int pDestHeight,
			int ratio, 
			int pFinalWidth, int pFinalHeight) throws Exception{
		
	    File imageFile = new File(srcFile);
	    
	    // get image type (png, jpeg, ...)
	    // String imageType = ImageType.getFormat(imageFile).toString();

	    // get original image file size
	    BufferedImage bi = ImageIO.read(imageFile);
	    int srcWidth = bi.getWidth();
	    int srcHeight = bi.getHeight();

	    // calculate the height 
	    int destHeight = pDestHeight > 0 ? pDestHeight : Math.round(pDestWidth * srcHeight / srcWidth);
	    int finalHeight = pFinalHeight > 0 ? pFinalHeight : Math.round(pFinalWidth * srcHeight / srcWidth);
	    
    	// crop image
    	ImageFilter cropFilter = new CropImageFilter(x, y, pDestWidth, destHeight);
    	Image image = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(bi.getSource(), cropFilter));
        
        // zoom image
        image = image.getScaledInstance(pFinalWidth, Math.round(finalHeight * ratio), Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(pFinalWidth, Math.round(finalHeight * ratio), BufferedImage.TYPE_INT_RGB);
        
        // draw photo
        Graphics g = tag.getGraphics();
        g.drawImage(image, 0, 0, null); 
        g.dispose();
        
        // write to file
        ImageIO.write(tag, "JPEG", new File(destFile));
		log.debug("cut photo successfully and file name is " + destFile);
	}
}
