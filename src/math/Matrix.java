package math;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class Matrix
{
	public final int rows, columns;

	private final double[][] values;

	public final int BYTES;

	// Construcots

	/**
	 * Creates a new math.Matrix with the given width and height
	 */
	public Matrix(int rows, int column)
	{
		this.rows = rows;
		this.columns = column;

		if (this.rows < 1 || columns < 1)
			throw new UnsupportedOperationException("Invalid math.Matrix Size, Expected " + this.rows + "x" + columns + ", got " + this.rows + "x" + columns);


		values = new double[rows][columns];

		var byteCount = 0;
		byteCount += 2 * Integer.BYTES; // width & height
		byteCount += this.rows * columns * Double.BYTES; // values
		BYTES = byteCount;
	}

	/**
	 * Creates a new math.Matrix a new column matrix with the given values
	 */
	public static Matrix asColumn(double[] array)
	{
		var matrix = new Matrix(1, array.length);
		for (int i = 0; i < array.length; i++)
		{
			matrix.set(0, i, array[i]);
		}
		return matrix;
	}

	/**
	 * Creates a new math.Matrix with random values between min and max
	 */
	public static Matrix random(int width, int height, double min, double max)
	{
		var matrix = new Matrix(width, height);
		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				matrix.set(i, j, Math.random() * (max - min) + min);
			}
		}
		return matrix;
	}

	// arithmetic operations

	public Matrix add(Matrix other)
	{
		if (rows != other.rows || columns != other.columns)
			throw new UnsupportedOperationException("Invalid math.Matrix Size, Expected " + rows + "x" + columns + ", got " + other.rows + "x" + other.columns);

		var result = new Matrix(rows, columns);

		// for each element in the matrix, add the corresponding element in the other matrix
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				result.set(i, j, get(i, j) + other.get(i, j));
		return result;
	}

	public Matrix subtract(Matrix other)
	{
		if (rows != other.rows || columns != other.columns)
			throw new UnsupportedOperationException("Invalid operation between " + rows + "x" + columns + " and " + other.rows + "x" + other.columns + " matrices");
		var result = new Matrix(rows, columns);

		// for each element in the matrix, subtract the corresponding element in the other matrix
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				result.set(i, j, get(i, j) - other.get(i, j));

		return result;
	}

	// math.Matrix multiplication
	public Matrix multiply(Matrix other)
	{
		if (rows != other.columns)
			throw new UnsupportedOperationException("Invalid operation between " + rows + "x" + columns + " and " + other.rows + "x" + other.columns + " matrices");

		var result = new Matrix(other.rows, columns);

		for (int i = 0; i < other.rows; i++)
		{
			for (int j = 0; j < columns; j++)
			{
				double sum = 0;
				for (int k = 0; k < rows; k++)
					sum += get(k, j) * other.get(i, k);

				result.set(i, j, sum);
			}
		}
		return result;
	}


	public Matrix multiply(double scalar)
	{
		return map(x -> x * scalar);
	}

	public Matrix divide(double scalar)
	{
		return multiply(1f / scalar);
	}

	// Dot Product
	public Matrix dot(Matrix other)
	{
		if (rows != other.rows || columns != other.columns)
			throw new UnsupportedOperationException("Invalid operation between " + rows + "x" + columns + " and " + other.rows + "x" + other.columns + " matrices");

		var result = new Matrix(rows, columns);

		// for each element in the matrix, add the corresponding element in the other matrix
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				result.set(i, j, get(i, j) * other.get(i, j));
		return result;
	}

	// Maps all values in the matrix to a new value with map()
	public Matrix map(Function<Double, Double> map)
	{
		var result = new Matrix(rows, columns);

		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				result.set(i, j, map.apply(get(i, j)));
		return result;
	}

	// Rotates the matrix, switching rows & columns
	public Matrix transpose()
	{
		var result = new Matrix(columns, rows);

		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				result.set(j, i, get(i, j));
		return result;
	}


	public void set(int r, int c, double value)
	{
		values[r][c] = value;
	}

	public double get(int r, int c)
	{
		return values[r][c];
	}

	public double[] asColumn()
	{
		var out = new double[columns];
		for (int i = 0; i < columns; i++)
		{
			out[i] = get(0, i);
		}
		return out;
	}

	public double[] asRow()
	{
		var out = new double[rows];
		for (int i = 0; i < rows; i++)
		{
			out[i] = get(i, 0);
		}
		return out;
	}

	public byte[] asBytes()
	{
		ByteBuffer buffer = ByteBuffer.allocate(BYTES);
		return putBytes(buffer);
	}

	public byte[] putBytes(ByteBuffer buffer)
	{
		buffer.putInt(rows);
		buffer.putInt(columns);

		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < columns; j++)
			{
				buffer.putDouble(get(i, j));
			}
		}
		return buffer.array();
	}

	public static Matrix fromBytes(byte[] bytes)
	{
		return fromByteBuffer(ByteBuffer.wrap(bytes));
	}

	public static Matrix fromByteBuffer(ByteBuffer buffer)
	{
		// check if there is enough space for 2 ints
		if (buffer.capacity() - buffer.position() < Integer.BYTES * 2)
			throw new IllegalArgumentException("Malformed bytes for math.Matrix");


		int width = buffer.getInt();
		int height = buffer.getInt();

		if (buffer.capacity() - buffer.position() < Double.BYTES * width * height)
			throw new IllegalArgumentException("Malformed bytes for math.Matrix");

		var mat = new Matrix(width, height);

		for (int i = 0; i < width; i++)
		{
			for (int j = 0; j < height; j++)
			{
				mat.set(i, j, buffer.getDouble());
			}
		}

		return new Matrix(width, height);
	}
}
