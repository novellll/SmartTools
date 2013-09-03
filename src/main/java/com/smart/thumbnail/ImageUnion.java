package com.smart.thumbnail;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.lang.Math;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;

/**
 * 图片结合器, 将给定的图片结合成一张<br />
 * <li>宽度固定，高度可调</li> <li>每行最多只允许出现3张图片</li> <li>图片可以缩放，但不允许变形</li> <li>
 * 最终图片需呈现为方形</li>
 * 
 * @author ww
 */
public class ImageUnion {

	private static final Logger log = Logger.getLogger(ImageUnion.class);

	/** 区间分类点 */
	private static final double limitPropA = 0.5;
	private static final double limitPropB = 1.8;
	private static final double limitPropC = 4.0;

	/**
	 * 处理纵向特别长的图片时使用。图片很长时，纵向可以合并3个以上的图片。
	 * 最少横向拼接个数：允许此区间内的图片最少横向拼接个数,必须设定为大于或等于3的整数
	 * 最多横向拼接个数：允许此区间内的图片最多横向拼接个数,必须设定为大于"最少横向拼接个数"的整数
	 */
	private static final int PropA_MIN_UN = 3;
	private static final int PropA_MAX_UN = 7;

	/** 最终图片宽度 */
	private static final int FINAL_WIDTH = 500;

	/** 图片之间的宽度 */
	private static final int INTER_WIDTH = 2;

	/** 是否支持图片的自动旋转 */
	private static boolean AUTO_ROTATE = false;

	/**
	 * 存放待结合图片的目录
	 */
	// private static String srcpath =
	// "C:/Documents and Settings/weiwei/My Documents/tmpPict";
	private static String srcpath = "/Users/Antony/Desktop/pic";

	/**
	 * 结合后的图片
	 */
	// private static String output =
	// "C:/Documents and Settings/weiwei/My Documents/myDb/temp.png";
	private static String output = "/Users/Antony/Desktop/result300.png";

	/**
	 * 程序入口点
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static int operation(String outfile, ArrayList<String> tmpFile)
			throws Exception {
		// 获取图片大小

		List<Quatangle> quatangles = new ArrayList<Quatangle>();
		for (String path : tmpFile) {
			File f = new File(path);
			BufferedImage bimg = ImageIO.read(f);
			Quatangle newQuatangle = new Quatangle();

			newQuatangle.setEdge(bimg.getWidth(), bimg.getHeight());
			newQuatangle.setDirectory(1);
			newQuatangle.setFile(f);

			// 从Exif信息中获取图片方向
			if (AUTO_ROTATE) {
				try {
					Metadata metadata = ImageMetadataReader.readMetadata(f);
					Directory directory = metadata
							.getDirectory(ExifDirectory.class);
					if(directory!=null){
//						System.out.println("directory"+directory.toString()+"directory.getInt(ExifIFD0Directory.TAG_ORIENTATION)"+directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
//						newQuatangle.setDirectory(directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
					}else{
						System.out.println("no directory  bimg.getWidth()"+bimg.getWidth()+"bimg.getHeight()"+bimg.getHeight());
						if(bimg.getWidth() > bimg.getHeight()){
							newQuatangle.setDirectory(6);
						}else{
							newQuatangle.setDirectory(3);
						}
					}

					// 如果是横向图片，则对调宽高
					if (newQuatangle.getDirectory() == 6) {
						newQuatangle.setEdge(bimg.getHeight(), bimg.getWidth());
					}
				} catch (Exception e) {
				}
			}
			System.out.println(f.getName());
			System.out.println(newQuatangle.getDirectory());
			quatangles.add(newQuatangle);
		}

		ImageUnion self = new ImageUnion();

		log.debug("file count : " + tmpFile.size()+" file height"+self.planDesi(quatangles));
		// 合并图片，输出
		self.union(outfile, self.planDesi(quatangles), quatangles);
		return self.planDesi(quatangles);
	}

	public static void main(String[] args) throws Exception {

		// 获取图片一览
		File[] files = new File(srcpath).listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isFile() && !f.isHidden();
			}
		});

		// 获取图片大小
		List<Quatangle> quatangles = new ArrayList<Quatangle>();
		for (File f : files) {

			BufferedImage bimg = ImageIO.read(f);
			Quatangle newQuatangle = new Quatangle();

			newQuatangle.setEdge(bimg.getWidth(), bimg.getHeight());
			newQuatangle.setDirectory(1);
			newQuatangle.setFile(f);

			// 从Exif信息中获取图片方向
			if (AUTO_ROTATE) {
				try {
					Metadata metadata = ImageMetadataReader.readMetadata(f);
					Directory directory = metadata
							.getDirectory(ExifDirectory.class);
					if(directory!=null){
						System.out.println("directory"+directory.toString());
						newQuatangle.setDirectory(directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
					}else{
						System.out.println("no directory");
						if(bimg.getWidth() > bimg.getHeight()){
							newQuatangle.setDirectory(6);
						}else{
							newQuatangle.setDirectory(3);
						}
					}
					// 如果是横向图片，则对调宽高
					if (newQuatangle.getDirectory() == 6) {
						newQuatangle.setEdge(bimg.getHeight(), bimg.getWidth());
					}
				} catch (Exception e) {
				}
			}
			System.out.println(f.getName());
			System.out.println(newQuatangle.getDirectory());
			quatangles.add(newQuatangle);
		}

		ImageUnion self = new ImageUnion();
		log.debug("file count : " + files.length+" file height"+self.planDesi(quatangles));

		// 合并图片，输出

		self.union(output, self.planDesi(quatangles), quatangles);
	}

	/**
	 * 结合图片给定的图片集
	 * 
	 * @param destFile
	 *            结合结果文件
	 * @param height
	 * @param quatangles
	 *            图片的大小及放置的位置
	 * @throws Exception
	 */
	public void union(String destFile, int height, List<Quatangle> quatangles)
			throws Exception {

		BufferedImage thumbnail = new BufferedImage(FINAL_WIDTH, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics graphics = thumbnail.getGraphics();

		for (Quatangle size : quatangles) {

			BufferedImage file = ImageIO.read(size.getFile());

//			// 旋
//			file = (AUTO_ROTATE && size.getDirectory() == 6) ? rotate90ToRight(file)
//					: file;
//			file = (AUTO_ROTATE && size.getDirectory() == 3) ? file
//					: file;

			// 调整大小
			Image workimage;
			System.out.println("size.getDirectory() "+size.getDirectory());
			if(size.getDirectory() ==6 )
				workimage = file.getScaledInstance((int) size.getAedge(),
					(int) size.getBedge(), BufferedImage.SCALE_SMOOTH);
			else{
				workimage = file.getScaledInstance((int) size.getBedge(),
						(int) size.getAedge(), BufferedImage.SCALE_SMOOTH);
			}
			// 在结果画板上绘制调整后的图片
			graphics.drawImage(workimage, (int) size.getX(), (int) size.getY(),
					null);

		}

		// 输出文件
		graphics.dispose();
		ImageIO.write(thumbnail, "PNG", new File(destFile));
	}

	/**
	 * 向右旋转图片90°
	 * 
	 * @param input
	 * @return
	 */
	public BufferedImage rotate90ToRight(BufferedImage input) {
		int width = input.getWidth();
		int height = input.getHeight();

		BufferedImage result = new BufferedImage(height, width, input.getType());
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				// returnImage.setRGB(y, width - x - 1, inputImage.getRGB(x,
				// y));
				result.setRGB(height - y - 1, x, input.getRGB(x, y));
			}
		}
		return result;
	}

	/***
	 * 对输入的图片信息进行分类，计算图片的位置及尺寸
	 * 
	 * @param tmpQuant
	 * @return
	 */
	public int planDesi(List<Quatangle> tmpQuant) {

		// 对图片进行分类
		Vector<Vector<Vector<Integer>>> threeDim = pointMatrix(tmpQuant);

		// 确定每张图片的坐标与显示大小
		return setCoordinate(tmpQuant, threeDim);

	}

	/**
	 * 确定每张图片的坐标与显示大小
	 * 
	 * @param tmpQuant
	 *            输入的图片信息
	 * @param threeDim
	 *            图片分类的结果，最外层代表所有图片，中间代表行，最里层是每一行的图片的标识（即图片顺序）
	 * @return
	 */
	public int setCoordinate(List<Quatangle> tmpQuant,
			Vector<Vector<Vector<Integer>>> threeDim) {

		int sumY = INTER_WIDTH;

		// 最外层Vector，纵向
		while (threeDim.size() > 0) {
			int tmpIndex = (int) (Math.random() * threeDim.size());
			double sumProp = 0.0;
			int sumSubX = INTER_WIDTH;
			int TEMP_WIDTH = FINAL_WIDTH - (threeDim.get(tmpIndex).size() + 1)
					* INTER_WIDTH;

			for (int i = 0; i < threeDim.get(tmpIndex).size(); i++) {
				double tmpProp = 0.0;
				for (int j = 0; j < threeDim.get(tmpIndex).get(i).size(); j++) {
					tmpProp = tmpProp
							+ 1
							/ tmpQuant
									.get(threeDim.get(tmpIndex).get(i).get(j))
									.getProp();
				}
				sumProp = sumProp + 1 / tmpProp;
			}

			// 中间层Vector，横向
			while (threeDim.get(tmpIndex).size() > 0) {

				int subIndex = (int) (Math.random() * threeDim.get(tmpIndex)
						.size());
				int sumSubY = 0;

				// 最里层Vector，横向
				if (threeDim.get(tmpIndex).size() == 1) {
					if (threeDim.get(tmpIndex).get(subIndex).size() == 1) {
						tmpQuant.get(
								threeDim.get(tmpIndex).get(subIndex).get(0))
								.setX(sumSubX);
						tmpQuant.get(
								threeDim.get(tmpIndex).get(subIndex).get(0))
								.setY(sumY);
						tmpQuant.get(
								threeDim.get(tmpIndex).get(subIndex).get(0))
								.setEdge(FINAL_WIDTH - sumSubX - INTER_WIDTH,
										(int) (TEMP_WIDTH / sumProp));
					}
					// 最里层Vector，纵向
					if (threeDim.get(tmpIndex).get(subIndex).size() > 1) {
						while (threeDim.get(tmpIndex).get(subIndex).size() > 0) {
							int ssIndex = (int) (Math.random() * threeDim
									.get(tmpIndex).get(subIndex).size());
							if (threeDim.get(tmpIndex).get(subIndex).size() == 1) {
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setX(sumSubX);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setY(
										sumY + sumSubY);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setEdge(
										FINAL_WIDTH - sumSubX - INTER_WIDTH,
										((int) (TEMP_WIDTH / sumProp))
												- sumSubY);
							} else {
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setX(sumSubX);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setY(
										sumY + sumSubY);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex))
										.setEdge(
												FINAL_WIDTH - sumSubX
														- INTER_WIDTH,
												((int) ((FINAL_WIDTH - sumSubX - INTER_WIDTH) / tmpQuant
														.get(threeDim
																.get(tmpIndex)
																.get(subIndex)
																.get(ssIndex))
														.getProp())));
								sumSubY = sumSubY
										+ ((int) ((FINAL_WIDTH - sumSubX - INTER_WIDTH) / tmpQuant
												.get(threeDim.get(tmpIndex)
														.get(subIndex)
														.get(ssIndex))
												.getProp())) + INTER_WIDTH;
								System.out.println("---352 -- sumSubY:"+sumSubY);
							}
							threeDim.get(tmpIndex).get(subIndex)
									.remove(ssIndex);
						}
					}
				} else {
					if (threeDim.get(tmpIndex).get(subIndex).size() == 1) {
						double subProp = tmpQuant.get(
								threeDim.get(tmpIndex).get(subIndex).get(0))
								.getProp();
						tmpQuant.get(
								threeDim.get(tmpIndex).get(subIndex).get(0))
								.setX(sumSubX);
						tmpQuant.get(
								threeDim.get(tmpIndex).get(subIndex).get(0))
								.setY(sumY);
						tmpQuant.get(
								threeDim.get(tmpIndex).get(subIndex).get(0))
								.setEdge(
										(int) ((subProp / sumProp) * TEMP_WIDTH),
										(int) (TEMP_WIDTH / sumProp));
						sumSubX = sumSubX
								+ ((int) ((subProp / sumProp) * TEMP_WIDTH))
								+ INTER_WIDTH;
					}
					// 最里层Vector，纵向
					if (threeDim.get(tmpIndex).get(subIndex).size() > 1) {
						double subProp = 0.0;
						for (int k = 0; k < threeDim.get(tmpIndex)
								.get(subIndex).size(); k++) {
							subProp = subProp
									+ 1
									/ tmpQuant.get(
											threeDim.get(tmpIndex)
													.get(subIndex).get(k))
											.getProp();
						}
						subProp = 1.0 / subProp;
						while (threeDim.get(tmpIndex).get(subIndex).size() > 0) {
							int ssIndex = (int) (Math.random() * threeDim
									.get(tmpIndex).get(subIndex).size());

							if (threeDim.get(tmpIndex).get(subIndex).size() == 1) {
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setX(sumSubX);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setY(
										sumY + sumSubY);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex))
										.setEdge(
												(int) ((subProp / sumProp) * TEMP_WIDTH),
												((int) (TEMP_WIDTH / sumProp))
														- sumSubY);
							} else {
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setX(sumSubX);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex)).setY(
										sumY + sumSubY);
								tmpQuant.get(
										threeDim.get(tmpIndex).get(subIndex)
												.get(ssIndex))
										.setEdge(
												(int) ((subProp / sumProp) * TEMP_WIDTH),
												(int) (((subProp / sumProp) * TEMP_WIDTH) / tmpQuant
														.get(threeDim
																.get(tmpIndex)
																.get(subIndex)
																.get(ssIndex))
														.getProp()));
								sumSubY = sumSubY
										+ ((int) (((subProp / sumProp) * TEMP_WIDTH) / tmpQuant
												.get(threeDim.get(tmpIndex)
														.get(subIndex)
														.get(ssIndex))
												.getProp())) + INTER_WIDTH;
								System.out.println("-- 435 --sumbSubY:"+sumSubY);
							}

							threeDim.get(tmpIndex).get(subIndex)
									.remove(ssIndex);
						}
						sumSubX = sumSubX
								+ ((int) ((subProp / sumProp) * TEMP_WIDTH))
								+ INTER_WIDTH;
					}
				}
				threeDim.get(tmpIndex).remove(subIndex);

				System.out.println("---448 --- sumSubY:"+sumSubY);
			}
			sumY = sumY + ((int) (TEMP_WIDTH / sumProp)) + INTER_WIDTH;
			System.out.println("--451--((int) (TEMP_WIDTH / sumProp))"+((int) (TEMP_WIDTH / sumProp))+"TEMP_WIDTH:"+TEMP_WIDTH);
			System.out.println("--452-- sumProp:"+sumProp);
			threeDim.remove(tmpIndex);
			
		}
		return sumY;
	}

	/**
	 * 对图片进行分类
	 * 
	 * @param tmpQuant
	 *            图片
	 * @param tmpDim
	 *            分类结果（输出变量）
	 */
	public Vector<Vector<Vector<Integer>>> pointMatrix(List<Quatangle> tmpQuant) {

		// 得到图片分类
		List<List<Integer>> twoIndex = divideSection(tmpQuant);

		Vector<Vector<Vector<Integer>>> tmpDim = new Vector<Vector<Vector<Integer>>>();
		Vector<Vector<Integer>> twoDim = new Vector<Vector<Integer>>();
		Vector<Integer> oneDim = new Vector<Integer>();

		// 处理section1的图片（竖长的图片）
		if (twoIndex.get(0).size() > 0) {

			List<Integer> section1 = twoIndex.get(0);

			// 满足 [公式 x <= max * (x / min)]的
			if (section1.size() <= (PropA_MAX_UN * (section1.size() / PropA_MIN_UN))) {
				int sectionLen = section1.size();

				// 使图片分布到达到最大的均匀
				int rowcount = sectionLen / PropA_MIN_UN;
				if ((sectionLen / PropA_MIN_UN - sectionLen / PropA_MAX_UN) > 1) {
					rowcount = (sectionLen / PropA_MIN_UN + sectionLen
							/ PropA_MAX_UN) / 2;
				}

				// ?
				for (int i = rowcount; i > 0; i--) {
					for (int j = 0; j < sectionLen / i; j++) {
						moveToRow(twoDim, section1);
					}
					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
					sectionLen = sectionLen - sectionLen / i;
				}
			} else {
				int MIDDL_UN = (int) ((PropA_MAX_UN + PropA_MIN_UN) / 2);
				while (section1.size() / MIDDL_UN > 0) {
					for (int i = 0; i < MIDDL_UN; i++) {
						moveToRow(twoDim, section1);
					}
					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}

				if (section1.size() > 0) {
					int tmpR0len = section1.size();
					for (int i = 0; i < tmpR0len; i++) {
						moveToRow(twoDim, section1);
					}

					// 尝试与第3section的结合
					if (twoIndex.get(2).size() > 0) {
						moveToRow(twoDim, twoIndex.get(2));

						// 尝试与第2section的结合
						if (twoIndex.get(2).size() > 0) {
							moveToRow(twoDim, twoIndex.get(2));
						} else if (twoIndex.get(3).size() > 0) {
							moveToRow(twoDim, twoIndex.get(3));
						} else if (twoIndex.get(1).size() > 0) {
							moveToRow(twoDim, twoIndex.get(1));
						}
					}
					// 尝试与第4section的结合
					else if (twoIndex.get(3).size() / 3 > 0) {
						for (int i = 0; i < 3; i++) {
							int tmpInd = (int) (Math.random() * twoIndex.get(3)
									.size());
							oneDim.add(twoIndex.get(3).get(tmpInd));
							twoIndex.get(3).remove(tmpInd);
						}

						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
					}
					// 尝试与第2section的结合
					else if (twoIndex.get(1).size() > 0) {
						moveToRow(twoDim, twoIndex.get(1));
					}

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}
			}
		}

		// 处理第2，第3section的图片。第2第3的图片可以整合
		// deal with (limitPropA,limitPropC) combination
		if ((twoIndex.get(2).size() > 0) && (twoIndex.get(1).size() > 0)) {
			if (((double) twoIndex.get(1).size()) / twoIndex.get(2).size() >= 0.5) {
				// 1:2 deal with (limitPropB,limitPropC)
				while (twoIndex.get(2).size() > 0) {
					if (twoIndex.get(2).size() == 1) {
						int tmpInd = (int) (Math.random() * twoIndex.get(2)
								.size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
						twoIndex.get(2).remove(tmpInd);
					} else {
						for (int i = 0; i < 2; i++) {
							int tmpInd = (int) (Math.random() * twoIndex.get(2)
									.size());
							oneDim.add(twoIndex.get(2).get(tmpInd));
							twoIndex.get(2).remove(tmpInd);
						}
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
					}

					if (twoIndex.get(1).size() == 2) {
						for (int i = 0; i < 2; i++) {
							int tmpInd = (int) (Math.random() * twoIndex.get(1)
									.size());
							oneDim.add(twoIndex.get(1).get(tmpInd));
							twoDim.add(oneDim);
							oneDim = new Vector<Integer>();
							twoIndex.get(1).remove(tmpInd);
						}
					} else {
						int tmpInd = (int) (Math.random() * twoIndex.get(1)
								.size());
						oneDim.add(twoIndex.get(1).get(tmpInd));
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
						twoIndex.get(1).remove(tmpInd);
					}

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}
			} else {
				// 1:2 deal with (limitPropA,limitPropB)
				while (twoIndex.get(1).size() > 0) {
					int tmpInd = (int) (Math.random() * twoIndex.get(1).size());
					oneDim.add(twoIndex.get(1).get(tmpInd));
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();
					twoIndex.get(1).remove(tmpInd);

					if (twoIndex.get(2).size() == 3) {
						tmpInd = (int) (Math.random() * twoIndex.get(2).size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
						twoIndex.get(2).remove(tmpInd);
					} else {
						for (int i = 0; i < 2; i++) {
							tmpInd = (int) (Math.random() * twoIndex.get(2)
									.size());
							oneDim.add(twoIndex.get(2).get(tmpInd));
							twoIndex.get(2).remove(tmpInd);
						}
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
					}

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}
			}
		}

		// 处理section3的图片
		// deal with (limitPropB,limitPropC)
		if (twoIndex.get(2).size() > 0) {
			if (twoIndex.get(2).size() == 1) {
				int tmpInd = (int) (Math.random() * twoIndex.get(2).size());
				oneDim.add(twoIndex.get(2).get(tmpInd));
				twoDim.add(oneDim);
				oneDim = new Vector<Integer>();
				twoIndex.get(2).remove(tmpInd);

				if (twoIndex.get(3).size() > 0) {
					tmpInd = (int) (Math.random() * twoIndex.get(3).size());
					oneDim.add(twoIndex.get(3).get(tmpInd));
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();
					twoIndex.get(3).remove(tmpInd);
				}

				tmpDim.add(twoDim);
				twoDim = new Vector<Vector<Integer>>();
			} else {
				if (twoIndex.get(2).size() % 4 == 1) {
					for (int i = 0; i < 2; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(2)
								.size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoIndex.get(2).remove(tmpInd);
					}
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();

					for (int i = 0; i < 3; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(2)
								.size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoIndex.get(2).remove(tmpInd);
					}
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				} else if (twoIndex.get(2).size() % 4 == 2) {
					for (int i = 0; i < 2; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(2)
								.size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
						twoIndex.get(2).remove(tmpInd);
					}

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				} else if (twoIndex.get(2).size() % 4 == 3) {
					for (int i = 0; i < 3; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(2)
								.size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
						twoIndex.get(2).remove(tmpInd);
					}

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}

				while (twoIndex.get(2).size() > 0) {
					for (int i = 0; i < 2; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(2)
								.size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoIndex.get(2).remove(tmpInd);
					}
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();

					for (int i = 0; i < 2; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(2)
								.size());
						oneDim.add(twoIndex.get(2).get(tmpInd));
						twoIndex.get(2).remove(tmpInd);
					}
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}
			}
		}

		// 处理section2的图片
		// deal with (limitPropA,limitPropB)
		if (twoIndex.get(1).size() > 0) {
			if (twoIndex.get(1).size() == 1) {
				int tmpInd = (int) (Math.random() * twoIndex.get(1).size());
				oneDim.add(twoIndex.get(1).get(tmpInd));
				twoDim.add(oneDim);
				oneDim = new Vector<Integer>();
				twoIndex.get(1).remove(tmpInd);

				if (twoIndex.get(3).size() > 0) {
					tmpInd = (int) (Math.random() * twoIndex.get(3).size());
					oneDim.add(twoIndex.get(3).get(tmpInd));
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();
					twoIndex.get(3).remove(tmpInd);
				}

				tmpDim.add(twoDim);
				twoDim = new Vector<Vector<Integer>>();
			} else {
				if (twoIndex.get(1).size() % 3 == 1) {
					for (int i = 0; i < 2; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(1)
								.size());
						oneDim.add(twoIndex.get(1).get(tmpInd));
						twoIndex.get(1).remove(tmpInd);
					}
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();

					for (int i = 0; i < 2; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(1)
								.size());
						oneDim.add(twoIndex.get(1).get(tmpInd));
						twoIndex.get(1).remove(tmpInd);
					}
					twoDim.add(oneDim);
					oneDim = new Vector<Integer>();

					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				} else if (twoIndex.get(1).size() % 3 == 2) {
					for (int i = 0; i < 2; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(1)
								.size());
						oneDim.add(twoIndex.get(1).get(tmpInd));
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
						twoIndex.get(1).remove(tmpInd);
					}
					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}

				while (twoIndex.get(1).size() > 0) {
					for (int i = 0; i < 3; i++) {
						int tmpInd = (int) (Math.random() * twoIndex.get(1)
								.size());
						oneDim.add(twoIndex.get(1).get(tmpInd));
						twoDim.add(oneDim);
						oneDim = new Vector<Integer>();
						twoIndex.get(1).remove(tmpInd);
					}
					tmpDim.add(twoDim);
					twoDim = new Vector<Vector<Integer>>();
				}
			}
		}

		// 处理section4的图片
		// Deal with (limitPropC,MAX)
		int tmpR3Len = twoIndex.get(3).size();
		for (int i = 0; i < tmpR3Len; i++) {
			int tmpInd = (int) (Math.random() * twoIndex.get(3).size());
			oneDim.add(twoIndex.get(3).get(tmpInd));
			twoDim.add(oneDim);
			oneDim = new Vector<Integer>();
			tmpDim.add(twoDim);
			twoDim = new Vector<Vector<Integer>>();
			twoIndex.get(3).remove(tmpInd);
		}

		return tmpDim;
	}

	private void moveToRow(Vector<Vector<Integer>> row, List<Integer> section) {

		int random = (int) (Math.random() * section.size()); // 抽取图片的随机数

		Vector<Integer> item = new Vector<Integer>();
		item.add(section.get(random));
		row.add(item);

		section.remove(random);
	}

	/**
	 * 根据图片的长宽比，将图片分类到不同区间内，共分4类
	 * 
	 * @param tmpQuant
	 *            原始图片集
	 * @param twoIndex
	 *            分类好的结果集
	 */
	public List<List<Integer>> divideSection(List<Quatangle> tmpQuant) {

		List<List<Integer>> twoIndex = new ArrayList<List<Integer>>();
		List<Integer> section1 = new ArrayList<Integer>();
		List<Integer> section2 = new ArrayList<Integer>();
		List<Integer> section3 = new ArrayList<Integer>();
		List<Integer> section4 = new ArrayList<Integer>();

		for (int i = 0; i < tmpQuant.size(); i++) {
			if (tmpQuant.get(i).getProp() <= limitPropA) {
				section1.add(i);
			}
			if (tmpQuant.get(i).getProp() > limitPropA
					&& tmpQuant.get(i).getProp() <= limitPropB) {
				section2.add(i);
			}
			if (tmpQuant.get(i).getProp() > limitPropB
					&& tmpQuant.get(i).getProp() <= limitPropC) {
				section3.add(i);
			}
			if (tmpQuant.get(i).getProp() > limitPropC) {
				section4.add(i);
			}
		}

		twoIndex.add(section1);
		twoIndex.add(section2);
		twoIndex.add(section3);
		twoIndex.add(section4);

		return twoIndex;
	}

	/**
	 * 坐标类
	 * 
	 * @author ww
	 */
	static class Point {
		private int x;
		private int y;

		public Point() {
			x = 0;
			y = 0;
		}

		public void setX(int tmpx) {
			this.x = tmpx;
		}

		public void setY(int tmpy) {
			this.y = tmpy;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
	}

	/**
	 * 调整后的图片信息
	 * 
	 * @author ww
	 */
	static class Quatangle extends Point {
		private int aEdge;
		private int bEdge;
		private File file;

		/**
		 * 1:无旋转 3:顺时针180° 6:顺时针90° 8:
		 */
		private int directory;

		public Quatangle() {
			super();
			this.aEdge = 0;
			this.bEdge = 0;
		}

		public void setDirectory(int directory) {
			this.directory = directory;
		}

		public int getDirectory() {
			return this.directory;
		}

		public void setEdge(int tmpa, int tmpb) {
			this.aEdge = tmpa;
			this.bEdge = tmpb;
		}

		public int getAedge() {
			return this.aEdge;
		}

		public int getBedge() {
			return this.bEdge;
		}

		public double getProp() {
			System.out.println("aEdge:"+aEdge+"bEdge:"+bEdge);
			return ((double) this.aEdge) / ((double) this.bEdge);
		}

		public void setX(int tmpx) {
			super.setX(tmpx);
		}

		public void setY(int tmpy) {
			super.setY(tmpy);
		}

		public int getX() {
			return super.getX();
		}

		public int getY() {
			return super.getY();
		}

		public void setFile(File f) {
			this.file = f;
		}

		public File getFile() {
			return this.file;
		}
	}
}
