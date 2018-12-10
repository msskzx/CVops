package cvops;

/*
 *	Ahmed Elgabri 34-16019 T18
 *	Muhammad Khattab 34-14154 T11 
 */

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

public class Disparitz {

	static void showImage(String title, Mat image) {
		HighGui.imshow(title, image);
		HighGui.waitKey();
	}

	public static Mat disparitz(Mat image) {
		Mat newImage = Mat.zeros(image.size(), image.type());
		byte[] outputImage = new byte[(int) (newImage.total() * newImage.channels())];

		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);

		for (int x = 0; x < image.cols() / 2; x++) {
			for (int y = 0; y < image.rows(); y++) {
				for (int ch = 0; ch < image.channels(); ch++) {
					int ssd = 10000;

					// min SSD between the 76 candidate
					for (int z = 0; z <= 75; z++) {
						int candidateX = x - z + (image.cols() / 2);
						int candidateY = y;
						int curssd = 1000;

						// 7x7 window to calculate the current ssd
						for (int r = -3; r <= 3; r++) {
							for (int t = -3; t <= 3; t++) {
								// if valid index compute the current ssd
								if (validIndex(x + r, y + t, image.cols() / 2, image.rows())
										&& validIndex2(candidateX + r, candidateY + t, image.cols(), image.rows())) {

									double pixelValue1 = imageData[((y + t) * image.cols() + (x + r)) * image.channels()
											+ ch];
									pixelValue1 = pixelValue1 < 0 ? pixelValue1 + 256 : pixelValue1;

									double pixelValue2 = imageData[((candidateY + t) * image.cols() + (candidateX + r))
											* image.channels() + ch];
									pixelValue2 = pixelValue2 < 0 ? pixelValue2 + 256 : pixelValue2;

									curssd += ((pixelValue1 - pixelValue2) * (pixelValue1 - pixelValue2));

								}
							}
						}
						ssd = Math.min(ssd, curssd);
					}
					// normalize
					ssd /= 255;
					outputImage[(y * image.cols() + x) * image.channels() + ch] = (byte) ssd;
				}
			}
		}

		newImage.put(0, 0, outputImage);
		return newImage;
	}

	static boolean validIndex(int i, int j, int n, int m) {
		return i > -1 && j > -1 && i < n && j < m;
	}

	static boolean validIndex2(int i, int j, int n, int m) {
		return i > n / 2 && j > -1 && i < n && j < m;
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		String imagePath = "Z:\\Fun\\CVops\\src\\As3.jpg";
		Mat image = Imgcodecs.imread(imagePath);
//		showImage("Original Image", image);
		showImage("Output Image", disparitz(image));
		System.exit(0);

	}
}
