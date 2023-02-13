package network;

import math.Matrix;

import java.nio.ByteBuffer;

public class DeepNeuralNetwork extends NeuralNetwork
{
	private final int inputSize, outputSize;
	private final int[] hiddenSizes;
	private final int BYTES;

	private Matrix[] weights, biases;


	public DeepNeuralNetwork(int inputSize, int outputSize, int... hiddenSizes)
	{
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.hiddenSizes = hiddenSizes;

		weights = new Matrix[hiddenSizes.length + 1];
		biases = new Matrix[hiddenSizes.length + 1];

		weights[0] = Matrix.random(inputSize, hiddenSizes[0], -1, 1);
		biases[0] = Matrix.random(1, hiddenSizes[0], -1, 1);

		for (int i = 1; i < hiddenSizes.length; i++)
		{
			weights[i] = Matrix.random(hiddenSizes[i - 1], hiddenSizes[i], -1, 1);
			biases[i] = Matrix.random(1, hiddenSizes[i], -1, 1);
		}

		weights[weights.length - 1] = Matrix.random(hiddenSizes[hiddenSizes.length - 1], outputSize, -1, 1);
		biases[biases.length - 1] = Matrix.random(1, outputSize, -1, 1);

		var byteCount = 0;
		byteCount += 2 * Integer.BYTES; // inputSize & outputSize
		byteCount += Integer.BYTES; // hiddenSizes.length
		byteCount += hiddenSizes.length * Integer.BYTES; // hiddenSizes

		for (int i = 0; i < weights.length; i++)
		{
			byteCount += weights[i].BYTES;
			byteCount += biases[i].BYTES;
		}

		BYTES = byteCount;
	}

	public double[] feed(double... inputRaw)
	{
		Matrix activation = Matrix.asColumn(inputRaw);

		for (int i = 0; i < weights.length; i++)
		{
			activation = weights[i].multiply(activation).add(biases[i]).map(activationFunction.getFunction());
		}

		return activation.asColumn();
	}

	public void train(double[] trainingData, double[] expected)
	{
		Matrix[] activations = new Matrix[weights.length + 1];
		Matrix[] weightedSums = new Matrix[weights.length + 1];

		activations[0] = Matrix.asColumn(trainingData);
		weightedSums[0] = Matrix.asColumn(trainingData);

		// feed forward
		for (int i = 0; i < weights.length; i++)
		{
			weightedSums[i + 1] = weights[i].multiply(activations[i]).add(biases[i]);
			activations[i + 1] = weightedSums[i + 1].map(activationFunction.getFunction());
		}

		// Backpropagation
		Matrix error = activations[weights.length].subtract(Matrix.asColumn(expected)).multiply(2);

		for (int i = weights.length - 1; i >= 0; i--)
		{
			Matrix delActivationToSum = weightedSums[i + 1].map(activationFunction.getDerivative());
			Matrix delSumToWeight = activations[i].transpose();

			Matrix delBias = delActivationToSum.dot(error);
			Matrix delWeight = delBias.multiply(delSumToWeight);

			biases[i] = biases[i].subtract(delBias.multiply(learningRate));
			weights[i] = weights[i].subtract(delWeight.multiply(learningRate));

			error = weights[i].transpose().multiply(error);
		}
	}

	public int getInputSize()
	{
		return inputSize;
	}

	public int getOutputSize()
	{
		return outputSize;
	}

	public int[] getHiddenSizes()
	{
		return hiddenSizes;
	}

	public void putBytes(ByteBuffer buffer)
	{
		if (buffer.capacity() < BYTES)
		{
			throw new IllegalArgumentException("Buffer capacity is too small");
		}

		buffer.putInt(inputSize);
		buffer.putInt(outputSize);
		buffer.putInt(hiddenSizes.length);
		for (int hiddenSize : hiddenSizes)
		{
			buffer.putInt(hiddenSize);
		}
		for (Matrix weight : weights)
		{
			weight.putBytes(buffer);
		}
		for (Matrix bias : biases)
		{
			bias.putBytes(buffer);
		}
	}

	public byte[] getBytes()
	{
		ByteBuffer buffer = ByteBuffer.allocate(BYTES);
		putBytes(buffer);
		return buffer.array();
	}

	public static DeepNeuralNetwork fromBytes(ByteBuffer buffer)
	{
		int inputSize = buffer.getInt();
		int outputSize = buffer.getInt();
		int[] hiddenSizes = new int[buffer.getInt()];
		for (int i = 0; i < hiddenSizes.length; i++)
		{
			hiddenSizes[i] = buffer.getInt();
		}
		DeepNeuralNetwork network = new DeepNeuralNetwork(inputSize, outputSize, hiddenSizes);
		for (int i = 0; i < network.weights.length; i++)
		{
			network.weights[i] = Matrix.fromByteBuffer(buffer);
		}
		for (int i = 0; i < network.biases.length; i++)
		{
			network.biases[i] = Matrix.fromByteBuffer(buffer);
		}
		return network;
	}
}
