import java.util.function.Function;

public class ShallowNeuralNetwork {
	private final int inputSize, outputSize, hiddenSize;

	private final Matrix inputToHiddenWeights, hiddenToOutputWeights;
	private final Matrix hiddenBiases, outputBiases;
	private Function<Float, Float> activationFunction = SIGMOID_ACTIVATION_FUNCTION;

	public static final Function<Float, Float> SIGMOID_ACTIVATION_FUNCTION = (f) -> 1 / (1 + (float) Math.exp(-f));

	public ShallowNeuralNetwork(int inputSize, int outputSize, int hiddenSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.hiddenSize = hiddenSize;

		inputToHiddenWeights = Matrix.random(hiddenSize, inputSize, -1, 1);
		hiddenToOutputWeights = Matrix.random(outputSize, hiddenSize, -1, 1);

		hiddenBiases = Matrix.random(hiddenSize, 1, -1, 1);
		outputBiases = Matrix.random(outputSize, 1, -1, 1);
	}

	public float[] feed(float... inputRaw) {
		if (inputRaw.length != inputSize)
			throw new IllegalArgumentException("Invalid Input Size, Expected " + inputRaw.length + ", got " + inputRaw.length);

		var input = new Matrix(inputSize, 1);
		for (int i = 0; i < inputSize; i++) {
			input.setValue(i, 0, inputRaw[i]);
		}

		var hidden = input.multiply(inputToHiddenWeights).add(hiddenBiases);
		var output = hidden.multiply(hiddenToOutputWeights).add(outputBiases);

		var out = new float[outputSize];
		for (int i = 0; i < outputSize; i++) {
			out[i] = output.getValue(i, 0);
		}
		return out;
	}

	public void train(float[] data, float[] expected) {
		float[] received = feed(data);

		float[] outputCostGradient = new float[outputSize];
	}

	public Function<Float, Float> getActivationFunction() {
		return activationFunction;
	}

	public void setActivationFunction(Function<Float, Float> activationFunction) {
		this.activationFunction = activationFunction;
	}
}
