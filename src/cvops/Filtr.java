package cvops;

import java.util.ArrayList;
import java.util.Collections;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

/*
 *	Ahmed Elgabri 34-16019 T18
 *	Muhammad Khattab 34-14154 T11 
 */

public class Filtr {

	static Mat enhanceImage(Mat image) {
		if (measureNoise(image) != 0) {
			image = fixNoise(image);
		}
		if (measureBlur(image) != 0) {
			image = fixBlur(image);
		}
		if (measureCollapsing(image) < 50.0) {
			image = fixCollapsing(image);
		}
		return image;
	}

	public static float measureNoise(Mat image) {
		return 0;
	}

	// median filter
	public static Mat fixNoise(Mat image) {
		Mat newImage = Mat.zeros(image.size(), image.type());
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];

		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);

		for (int x = 0; x < image.cols(); x++) {
			for (int y = 0; y < image.rows(); y++) {

				ArrayList<Integer> curpxz = new ArrayList<>();
				for (int i = x - 1; i < x + 2; i++) {
					for (int j = y - 1; j < y + 2; j++) {
						if (validIndex(i, j, image.cols(), image.rows())) {
							double pixelValue = imageData[(j * image.cols() + i) * image.channels()];
							pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
							curpxz.add((int) pixelValue);
						}
					}
				}

				Collections.sort(curpxz);
				int curpx = curpxz.get(curpxz.size() / 2);

				for (int ch = 0; ch < image.channels(); ch++) {
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + ch];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					newImageData[(y * image.cols() + x) * image.channels() + ch] = (byte) curpx;
				}
			}
		}

		newImage.put(0, 0, newImageData);
		return newImage;
	}

	public static float measureBlur(Mat image) {
		return 0;
	}

	public static Mat fixBlur(Mat image) {
		Mat smoothImage = meanFilter(image);
		Mat edges = add(image, smoothImage, -1);
		return add(image, edges, 1);
	}

	public static Mat meanFilter(Mat image) {
		Mat newImage = Mat.zeros(image.size(), image.type());
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];

		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);

		for (int x = 0; x < image.cols(); x++) {
			for (int y = 0; y < image.rows(); y++) {

				int curpx = 0;

				for (int i = x - 1; i < x + 2; i++) {
					for (int j = y - 1; j < y + 2; j++) {
						if (validIndex(i, j, image.cols(), image.rows())) {
							double pixelValue = imageData[(j * image.cols() + i) * image.channels()];
							pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
							curpx += pixelValue;
						}
					}
				}

				curpx = (int) (curpx / 9);

				for (int ch = 0; ch < image.channels(); ch++) {
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + ch];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					newImageData[(y * image.cols() + x) * image.channels() + ch] = (byte) curpx;
				}
			}
		}

		newImage.put(0, 0, newImageData);
		return newImage;
	}

	// add = 1, addition
	// add = -1, subtraction
	static Mat add(Mat image1, Mat image2, int add) {
		Mat newImage = Mat.zeros(image1.size(), image1.type());
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];

		byte[] image1Data = new byte[(int) (image1.total() * image1.channels())];
		image1.get(0, 0, image1Data);

		byte[] image2Data = new byte[(int) (image2.total() * image2.channels())];
		image1.get(0, 0, image1Data);

		for (int x = 0; x < image1.cols(); x++) {
			for (int y = 0; y < image1.rows(); y++) {
				for (int ch = 0; ch < image1.channels(); ch++) {

					double pixelValue1 = image1Data[(y * image1.cols() + x) * image1.channels() + ch];
					pixelValue1 = pixelValue1 < 0 ? pixelValue1 + 256 : pixelValue1;

					double pixelValue2 = image2Data[(y * image2.cols() + x) * image2.channels() + ch];
					pixelValue2 = pixelValue2 < 0 ? pixelValue2 + 256 : pixelValue2;

					newImageData[(y * image2.cols() + x) * image2.channels()
							+ ch] = (byte) (pixelValue1 + (add * pixelValue2));
				}
			}
		}

		newImage.put(0, 0, newImageData);
		return newImage;
	}

	static boolean validIndex(int i, int j, int n, int m) {
		return i > -1 && j > -1 && i < n && j < m;
	}

	public static double measureCollapsing(Mat image) {
		int c = 255, d = -1;
		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);

		// min, max intensities
		for (int x = 0; x < image.cols(); x++) {
			for (int y = 0; y < image.rows(); y++) {
				for (int ch = 0; ch < image.channels(); ch++) {
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + ch];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					c = (int) Math.min(c, pixelValue);
					d = (int) Math.max(d, pixelValue);
				}
			}
		}
		return (d - c) * 100.0 / 255;
	}

	public static Mat fixCollapsing(Mat image) {
		Mat newImage = Mat.zeros(image.size(), image.type());
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];

		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);

		int a = 10, b = 240, c = 255, d = -1;

		// min, max intensities
		for (int x = 0; x < image.cols(); x++) {
			for (int y = 0; y < image.rows(); y++) {
				for (int ch = 0; ch < image.channels(); ch++) {
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + ch];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					c = (int) Math.min(c, pixelValue);
					d = (int) Math.max(d, pixelValue);
				}
			}
		}

		for (int x = 0; x < image.cols(); x++) {
			for (int y = 0; y < image.rows(); y++) {
				for (int ch = 0; ch < image.channels(); ch++) {
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + ch];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					newImageData[(y * image.cols() + x) * image.channels()
							+ ch] = (byte) ((pixelValue - c) * ((b - a) / (d - c)) + a);
				}
			}
		}

		newImage.put(0, 0, newImageData);
		return newImage;
	}

	static void showImage(String title, Mat image) {
		HighGui.imshow(title, image);
		HighGui.waitKey();
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

//		String imagePath1 = "Z:\\Fun\\CVops\\src\\1.jpg";
//		Mat image1 = Imgcodecs.imread(imagePath1);
//		showImage("Original Image", image1);
//		image1 = meanFilter(image1);
//		showImage("Mean Filter - Original Image", image1);

		String imagePath2 = "Z:\\Fun\\CVops\\src\\2.jpg";
		Mat image2 = Imgcodecs.imread(imagePath2);
		showImage("Noisy Image", image2);
		image2 = fixNoise(image2);
//		image2 = enhanceImage(image2);
		showImage("Fixed Noise in Image", image2);

//		String imagePath3 = "Z:\\Fun\\CVops\\src\\3.jpg";
//		Mat image3 = Imgcodecs.imread(imagePath3);
//		showImage("Blurry Image", image3);
//		image3 = fixBlur(image3);
//		showImage("Fixed Blur in Image", image3);

//		String imagePath4 = "Z:\\Fun\\CVops\\src\\4.jpg";
//		Mat image4 = Imgcodecs.imread(imagePath4);
//		showImage("Color Collapsing Image", image4);
//		System.out.println("Color Collapsing for image 4: " + measureCollapsing(image4));
//		image4 = fixCollapsing(image4);
//		System.out.println("Color Collapsing after fixing: " + measureCollapsing(image4));
//		image4 = enhanceImage(image4);
//		showImage("Fixed Color Collapsing in Image", image4);

		System.exit(0);
	}
}
