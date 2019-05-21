package cvops;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class High {

	public static Mat contrastHighlights(Mat image) {
		Mat newImage = Mat.zeros(image.size(), image.type());
		double alpha = 3.0;
		int beta = 30;
		double contrastDecay = 2.0 / image.cols();

		byte[] imageData = new byte[(int) (image.total() * image.channels())];
		image.get(0, 0, imageData);
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];

		for (int x = 0; x < image.cols(); x++) {
			for (int y = 0; y < image.rows(); y++) {
				for (int c = 0; c < image.channels(); c++) {
					double pixelValue = imageData[(y * image.cols() + x) * image.channels() + c];
					pixelValue = pixelValue < 0 ? pixelValue + 256 : pixelValue;
					newImageData[(y * image.cols() + x) * image.channels() + c] = saturate(alpha * pixelValue + beta);
				}
			}
			alpha = Math.max(1, alpha - contrastDecay);
		}
		newImage.put(0, 0, newImageData);
		return newImage;
	}

	static Mat fit(Mat image1, Mat image2) {
		byte[] imageData1 = new byte[(int) (image1.total() * image1.channels())];
		byte[] imageData2 = new byte[(int) (image2.total() * image2.channels())];
		image1.get(0, 0, imageData1);
		image2.get(0, 0, imageData2);

		Mat newImage = Mat.zeros(image1.size(), image1.type());
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];

		for (int i = 0; i < imageData1.length; i += image1.channels()) {
			byte c1 = imageData1[i];
			byte c2 = imageData1[i + 1];
			byte c3 = imageData1[i + 2];

			if (c1 + c2 + c3 == 0) {
				for (int c = 0; c < image1.channels(); c++) {
					double pixelValue1 = imageData1[i + c];
					double pixelValue2 = imageData2[i + c];
					pixelValue1 = pixelValue1 < 0 ? pixelValue1 + 256 : pixelValue1;
					pixelValue2 = pixelValue2 < 0 ? pixelValue2 + 256 : pixelValue2;
					newImageData[i + c] = imageData2[i + c];
				}
			} else {
				for (int c = 0; c < image1.channels(); c++) {
					double pixelValue1 = imageData1[i + c];
					double pixelValue2 = imageData2[i + c];
					pixelValue1 = pixelValue1 < 0 ? pixelValue1 + 256 : pixelValue1;
					pixelValue2 = pixelValue2 < 0 ? pixelValue2 + 256 : pixelValue2;
					newImageData[i + c] = imageData1[i + c];
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

	public static Mat blend(Mat image1, Mat image2) {
		byte[] imageData1 = new byte[(int) (image1.total() * image1.channels())];
		byte[] imageData2 = new byte[(int) (image2.total() * image2.channels())];
		image1.get(0, 0, imageData1);
		image2.get(0, 0, imageData2);

		Mat newImage = Mat.zeros(image1.size(), image1.type());
		byte[] newImageData = new byte[(int) (newImage.total() * newImage.channels())];
		double alpha = 0.7;
		for (int x = 0; x < image1.cols(); x++) {
			for (int y = 0; y < image1.rows(); y++) {
				for (int c = 0; c < image1.channels(); c++) {
					double pixelValue1 = imageData1[(y * image1.cols() + x) * image1.channels() + c];
					double pixelValue2 = imageData2[(y * image2.cols() + x) * image2.channels() + c];
					pixelValue1 = pixelValue1 < 0 ? pixelValue1 + 256 : pixelValue1;
					pixelValue2 = pixelValue2 < 0 ? pixelValue2 + 256 : pixelValue2;
					newImageData[(y * image1.cols() + x) * image1.channels() + c] = saturate(
							(1 - alpha) * pixelValue1 + alpha * pixelValue2);
				}
			}
		}
		newImage.put(0, 0, newImageData);
		return newImage;
	}

	static Mat translate(Point[] srcTri, Point[] dstTri, Mat image) {
		Mat warpMat = Imgproc.getAffineTransform(new MatOfPoint2f(srcTri), new MatOfPoint2f(dstTri));

		Mat warpDst = Mat.zeros(image.rows(), image.cols(), image.type());

		Imgproc.warpAffine(image, warpDst, warpMat, warpDst.size());

		return warpDst;
	}

	static void fitImages(Point[] srcTri, Point[] dstTri, Mat imageResized, Mat image) {
		Mat warpMat = Imgproc.getAffineTransform(new MatOfPoint2f(srcTri), new MatOfPoint2f(dstTri));

		Mat warpDst = Mat.zeros(imageResized.rows(), imageResized.cols(), image.type());

		Imgproc.warpAffine(imageResized, warpDst, warpMat, warpDst.size());

		Mat fitted4 = fit(warpDst, image);

		showImage("Fitting", fitted4);
	}

	static void fitImages2(Point[] srcQd, Point[] dstQd, Mat imageResized, Mat image) {
		Mat warpMatQd = Imgproc.getPerspectiveTransform(new MatOfPoint2f(srcQd), new MatOfPoint2f(dstQd));

		Mat warpDstQd = Mat.zeros(imageResized.rows(), imageResized.cols(), image.type());

		Imgproc.warpPerspective(imageResized, warpDstQd, warpMatQd, warpDstQd.size());

		Mat fitted = fit(warpDstQd, image);

		showImage("Fitting", fitted);

	}

	public static byte saturate(double value) {
		int roundedValue = (int) Math.round(value);
		roundedValue = roundedValue > 255 ? 255 : (roundedValue < 0 ? 0 : roundedValue);
		return (byte) roundedValue;
	}

	public static void main(String[] args) {
		// Load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		String imagePath1 = "Z:\\Fun\\CVassi1\\src\\Q1I1.png";
		String imagePath2 = "Z:\\Fun\\CVassi1\\src\\Q1I2.jpg";

		Mat image1 = Imgcodecs.imread(imagePath1);
		Mat image2 = Imgcodecs.imread(imagePath2);

		Mat contrastImage = contrastHighlights(image1);
		showImage("Contrast and Highlight", contrastImage);

		Mat image2Output = Mat.zeros(image2.size(), image2.type());
		Core.flip(image2, image2Output, 1);
		Mat imageResized2 = new Mat();
		Size sz = image1.size();
		Imgproc.resize(image2Output, imageResized2, sz);

		Point[] srcTri2 = new Point[3];
		srcTri2[0] = new Point(0, 0);
		srcTri2[1] = new Point(50, 0);
		srcTri2[2] = new Point(0, 50);

		int translate = 130;

		Point[] dstTri2 = new Point[3];
		dstTri2[0] = new Point(translate, 0);
		dstTri2[1] = new Point(50 + translate, 0);
		dstTri2[2] = new Point(translate, 50);

		Mat translatedBatman = translate(srcTri2, dstTri2, imageResized2);
		Mat finalImage = blend(translatedBatman, contrastImage);
		showImage("Blend", finalImage);

		String imagePath3 = "Z:\\Fun\\CVops\\src\\Q2I1.jpg";
		String imagePath4 = "Z:\\Fun\\CVops\\src\\Q2I2.jpg";
		String imagePath5 = "Z:\\Fun\\CVops\\src\\Q2I3.jpg";
		String imagePath6 = "Z:\\Fun\\CVops\\src\\Q3I1.jpg";
		Mat image3 = Imgcodecs.imread(imagePath3);
		Mat image5 = Imgcodecs.imread(imagePath5);
		Mat image4 = Imgcodecs.imread(imagePath4);
		Mat image6 = Imgcodecs.imread(imagePath6);

		Size imageSize4 = image4.size();
		Mat imageResized3 = new Mat();
		Imgproc.resize(image3, imageResized3, imageSize4);

		Point[] srcTri3 = new Point[3];
		srcTri3[0] = new Point(0, 0);
		srcTri3[1] = new Point(imageResized3.cols() - 1, 0);
		srcTri3[2] = new Point(0, imageResized3.rows() - 1);

		Point[] dstTri3 = new Point[3];
		dstTri3[0] = new Point(1220, 377);
		dstTri3[1] = new Point(1308, 377);
		dstTri3[2] = new Point(1220, 515);

		fitImages(srcTri3, dstTri3, imageResized3, image4);

		Size size5 = image5.size();
		Mat imageResized5 = new Mat();
		Imgproc.resize(image3, imageResized5, size5);

		Point[] srcTri5 = new Point[3];
		srcTri5[0] = new Point(0, 0);
		srcTri5[1] = new Point(imageResized5.cols() - 1, 0);
		srcTri5[2] = new Point(0, imageResized5.rows() - 1);

		Point[] dstTri5 = new Point[3];
		dstTri5[0] = new Point(371, 92);
		dstTri5[1] = new Point(704, 127);
		dstTri5[2] = new Point(327, 525);

		fitImages(srcTri5, dstTri5, imageResized5, image5);

		Size size6 = image6.size();
		Mat imageResized6 = new Mat();
		Imgproc.resize(image3, imageResized6, size6);

		Point[] srcQd = new Point[4];
		srcQd[0] = new Point(0, 0);
		srcQd[1] = new Point(imageResized6.cols() - 1, 0);
		srcQd[2] = new Point(imageResized6.cols() - 1, imageResized6.rows() - 1);
		srcQd[3] = new Point(0, imageResized6.rows() - 1);

		Point[] dstQd = new Point[4];
		dstQd[0] = new Point(163, 36);
		dstQd[1] = new Point(468, 70);
		dstQd[2] = new Point(464, 352);
		dstQd[3] = new Point(159, 389);

		fitImages2(srcQd, dstQd, imageResized6, image6);

		System.exit(0);
	}
}
