package math;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class Matrix
{
	public final int rows, columns;

	private final double[] values;

	public final int BYTES;
	public final int length;

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


//		values = new double[rows][columns];
		values = new double[rows * columns];
		length = values.length;


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
		Matrix matrix = Matrix.column(array.length);
		System.arraycopy(array, 0, matrix.values, 0, array.length);
		return matrix;
	}

	/**
	 * Created a column vector with the given size
	 */
	public static Matrix column(int size)
	{
		return new Matrix(size, 1);
	}

	/**
	 * Creates a new Matrix with random values between min and max
	 */
	public static Matrix random(int width, int height, double min, double max)
	{
		var matrix = new Matrix(width, height);
		for (int i = 0; i < matrix.values.length; i++)
			matrix.values[i] = Math.random() * (max - min) + min;

		return matrix;
	}

	/**
	 * Adds the given value to each element in the matrix, and returns
	 * the result as a new matrix
	 */
	public Matrix add(Matrix other)
	{
		if (rows != other.rows || columns != other.columns)
			throw new UnsupportedOperationException("Invalid math.Matrix Size, Expected " + rows + "x" + columns + ", got " + other.rows + "x" + other.columns);

		var result = new Matrix(rows, columns);

		for (int i = 0; i < values.length; i++)
			result.values[i] = values[i] + other.values[i];

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

	public Matrix multiply(Matrix other)
	{
		if (columns != other.rows)
			throw new UnsupportedOperationException("Invalid operation between " + rows + "x" + columns + " and " + other.rows + "x" + other.columns + " matrices");

		var result = new Matrix(rows, other.columns);

		// for each element in the matrix, multiply the corresponding element in the other matrix
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < other.columns; j++)
				for (int k = 0; k < columns; k++)
					result.set(i, j, result.get(i, j) + get(i, k) * other.get(k, j));

		return result;
	}

	/**
	 * Threaded version of multiply()
	 */
	public Matrix scale(double scalar)
	{
		return map(x -> x * scalar);
	}

	// Dot Product
	public Matrix dot(Matrix other)
	{
		if (rows != other.rows || columns != other.columns)
			throw new UnsupportedOperationException("Invalid operation between " + rows + "x" + columns + " and " + other.rows + "x" + other.columns + " matrices");

		var result = new Matrix(rows, columns);

		// for each element in the matrix, add the corresponding element in the other matrix
		for (int i = 0; i < length; i++)
			result.values[i] = values[i] * other.values[i];
		return result;
	}

	// Maps all values in the matrix to a new value with map()
	public Matrix map(Function<Double, Double> map)
	{
		var result = new Matrix(rows, columns);

		for (int i = 0; i < length; i++)
			result.values[i] = map.apply(values[i]);
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
		values[r * columns + c] = value;
	}

	public double get(int r, int c)
	{
		// turn the row and column into a single index
		return values[r * columns + c];
	}

	public double[] asColumn()
	{
		if (columns != 1)
			throw new UnsupportedOperationException("Invalid operation, Expected " + rows + "x" + columns + " to be a column matrix");

		var result = new double[rows];
		for (int i = 0; i < rows; i++)
		{
			result[i] = get(i, 0);
		}
		return result;
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

		for (double value : values) buffer.putDouble(value);

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

		for (int i = 0; i < mat.values.length; i++)
		{
			mat.values[i] = buffer.getDouble();
		}

		return mat;
	}

	public double sum()
	{
		double sum = 0;
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < columns; j++)
			{
				sum += get(i, j);
			}
		}
		return sum;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < columns; j++)
			{
				builder.append(get(i, j)).append(" ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	public static void main(String[] args)
	{
		// serialize
		var mat = new Matrix(2, 2);
		mat.set(0, 0, 1);
		mat.set(0, 1, 2);
		mat.set(1, 0, 3);
		mat.set(1, 1, 4);
		System.out.println(mat);

		byte[] bytes = mat.asBytes();
		Matrix mat2 = Matrix.fromBytes(bytes);
		System.out.println(mat2);
	}
}
