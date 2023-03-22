package network; /**
 * Used to malform training data
 */

import training.HandwrittenDigits;

import java.util.Arrays;

public final class Malformer {
	private Malformer() {
		throw new UnsupportedOperationException();
	}

	public static void randomShift(double[] data) {
		// randomly move the image without losing any non-zero values

		// first, find the bounding box
		int minX = HandwrittenDigits.IMAGE_RES;
		int minY = HandwrittenDigits.IMAGE_RES;

		int maxX = 0;
		int maxY = 0;

		for (int i = 0; i < HandwrittenDigits.IMAGE_RES; i++) {
			for (int j = 0; j < HandwrittenDigits.IMAGE_RES; j++) {
				if (data[i * HandwrittenDigits.IMAGE_RES + j] > 0) {
					minX = Math.min(minX, j);
					minY = Math.min(minY, i);

					maxX = Math.max(maxX, j);
					maxY = Math.max(maxY, i);
				}
			}
		}

		int width = maxX - minX;
		int height = maxY - minY;
		var packed = new double[width * height];

		// copy the image into the packed array
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				packed[i * width + j] = data[(i + minY) * HandwrittenDigits.IMAGE_RES + j + minX];
			}
		}

		// now, randomly shift the image
		int newMinX = (int) (Math.random() * (HandwrittenDigits.IMAGE_RES - width));
		int newMinY = (int) (Math.random() * (HandwrittenDigits.IMAGE_RES - height));

		// clear the old image
		Arrays.fill(data, 0);

		// copy the packed image into the new location
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				data[(i + newMinY) * HandwrittenDigits.IMAGE_RES + j + newMinX] = packed[i * width + j];
			}
		}
	}

	private static final double NOISE_MAGNITUDE = 0.1;

	public static void addNoise(double[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] += (Math.random() - 0.5) * NOISE_MAGNITUDE;

			// clamp
			data[i] = Math.max(0, Math.min(1, data[i]));
		}
	}

	public static void malform(double[] data) {
		randomShift(data);
//		addNoise(data);
	}
}
