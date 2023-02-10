import java.util.function.Function;

public class Matrix {
	public final int width;
	public final int height;
	private final float[][] values;

	public Matrix(int width, int height) {
		this(width, height, new float[width][height]);
	}

	public Matrix(int width, int height, float[][] values) {
		this.width = width;
		this.height = height;
		this.values = values;
	}

	public static Matrix random(int width, int height, float min, float max) {
		var values = new float[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				values[x][y] = (float) (Math.random() * (max - min) + min);
			}
		}
		return new Matrix(width, height, values);
	}

	public Matrix add(Matrix other) {
		if (width != other.width || height != other.height)
			throw new IllegalArgumentException("Invalid Matrix Size, Expected " + width + "x" + height + ", got " + other.width + "x" + other.height);

		var result = new Matrix(width, height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				result.values[x][y] = values[x][y] + other.values[x][y];
			}
		}

		return result;
	}

	public Matrix multiply(Matrix other) {
		if (width != other.height)
			throw new IllegalArgumentException("Invalid Matrix Size, Expected " + width + "x" + height + ", got " + other.width + "x" + other.height);

		var result = new Matrix(other.width, height);

		for (int x = 0; x < other.width; x++) {
			for (int y = 0; y < height; y++) {
				for (int i = 0; i < width; i++) {
					result.values[x][y] += values[i][y] * other.values[x][i];
				}
			}
		}

		return result;
	}

	public Matrix map(Function<Float, Float> mapper) {
		var res = new float[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				res[i][j] = mapper.apply(values[i][j]);
			}
		}

		return new Matrix(width, height, res);
	}

	public float[][] getValues() {
		return values;
	}

	public float getValue(int x, int y) {
		return values[x][y];
	}

	public void setValue(int x, int y, float value) {
		values[x][y] = value;
	}
}
