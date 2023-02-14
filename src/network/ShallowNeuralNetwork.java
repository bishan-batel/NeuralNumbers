package network;

import math.Matrix;

import java.nio.ByteBuffer;

@Deprecated
public class ShallowNeuralNetwork extends NeuralNetwork {
	private final int inputSize, outputSize, hiddenSize;

	private Matrix inputToHiddenWeights;
	private Matrix hiddenToOutputWeights;
	private Matrix hiddenBiases;
	private Matrix outputBiases;

	public ShallowNeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.hiddenSize = hiddenSize;

		inputToHiddenWeights = Matrix.random(inputSize, hiddenSize, -1, 1);
		hiddenToOutputWeights = Matrix.random(hiddenSize, outputSize, -1, 1);

		hiddenBiases = Matrix.random(1, hiddenSize, -1, 1);
		outputBiases = Matrix.random(1, outputSize, -1, 1);
	}

	public double[] feed(double... inputRaw) {
		if (inputRaw.length != inputSize)
			throw new IllegalArgumentException("Invalid Input Size, Expected " + inputRaw.length + ", got " + inputRaw.length);

		Matrix layer = Matrix.asColumn(inputRaw);

		layer = inputToHiddenWeights.multiply(layer).add(hiddenBiases).map(activationFunction.getFunction());
		layer = hiddenToOutputWeights.multiply(layer).add(outputBiases).map(activationFunction.getFunction());

		// returns the last layer's output
		return layer.asColumn();
	}

	public Matrix train(double[] trainingData, double[] expected) {
		if (trainingData.length != inputSize)
			throw new IllegalArgumentException("Invalid Input Size, Expected " + inputSize + ", got " + trainingData.length);

		if (expected.length != outputSize)
			throw new IllegalArgumentException("Invalid Output Size, Expected " + outputSize + ", got " + expected.length);


		// print input & expected
//		System.out.println("Input: " + Arrays.toString(trainingData));
//		System.out.println("Expected: " + Arrays.toString(expected));
//		System.out.println();

		Matrix input = Matrix.asColumn(trainingData);

		Matrix hidden = inputToHiddenWeights.multiply(input).add(hiddenBiases);
		Matrix hiddenActivations = hidden.map(activationFunction.getFunction());

		Matrix output = hiddenToOutputWeights.multiply(hiddenActivations).add(outputBiases);
		Matrix outputActivations = output.map(activationFunction.getFunction());

		Matrix expectedOutput = Matrix.asColumn(expected);

		Matrix error = expectedOutput.subtract(outputActivations).multiply(2);


		// output layer gradient
		{
			Matrix delActivationToOutput = output.map(activationFunction.getDerivative());
			Matrix delOutputToWeight = hiddenActivations.transpose();

			Matrix biasGradient = delActivationToOutput.dot(error);
			Matrix weightGradient = biasGradient.multiply(delOutputToWeight);


			outputBiases = outputBiases.subtract(biasGradient.multiply(learningRate));
			hiddenToOutputWeights = hiddenToOutputWeights.subtract(weightGradient.multiply(learningRate));
		}

		// hidden layer gradient
		{
			error = hiddenToOutputWeights.transpose().multiply(error);

			Matrix delActivationToHidden = hidden.map(activationFunction.getDerivative());
			Matrix delHiddenToWeight = input.transpose();

			Matrix biasGradient = error.dot(delActivationToHidden);
			Matrix weightGradient = biasGradient.multiply(delHiddenToWeight);

			hiddenBiases = hiddenBiases.subtract(biasGradient.multiply(learningRate));
			inputToHiddenWeights = inputToHiddenWeights.subtract(weightGradient.multiply(learningRate));
		}
		return input;
	}

	public int getInputSize() {
		return inputSize;
	}

	public int getOutputSize() {
		return outputSize;
	}

	public int getHiddenSize() {
		return hiddenSize;
	}

	public byte[] asBytes() {
		var byteLen = 0;

		// sizes for each layer,
		byteLen += 3 * Integer.BYTES;

		// bytes for each weight matrix
		byteLen += inputToHiddenWeights.BYTES;
		byteLen += hiddenToOutputWeights.BYTES;

		// bytes for each bias matrix
		byteLen += hiddenBiases.BYTES;
		byteLen += outputBiases.BYTES;

		ByteBuffer buffer = ByteBuffer.allocate(byteLen);
		buffer.putInt(inputSize);
		buffer.putInt(hiddenSize);
		buffer.putInt(outputSize);

		inputToHiddenWeights.putBytes(buffer);
		hiddenToOutputWeights.putBytes(buffer);

		hiddenBiases.putBytes(buffer);
		outputBiases.putBytes(buffer);

		return buffer.array();
	}

	public static ShallowNeuralNetwork fromBytes(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		int inputSize = buffer.getInt();
		int hiddenSize = buffer.getInt();
		int outputSize = buffer.getInt();

		ShallowNeuralNetwork network = new ShallowNeuralNetwork(inputSize, outputSize, hiddenSize);

		network.inputToHiddenWeights = Matrix.fromByteBuffer(buffer);
		network.hiddenToOutputWeights = Matrix.fromByteBuffer(buffer);

		network.hiddenBiases = Matrix.fromByteBuffer(buffer);
		network.outputBiases = Matrix.fromByteBuffer(buffer);

		return network;
	}
}
