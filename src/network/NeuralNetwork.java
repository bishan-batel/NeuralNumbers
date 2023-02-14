package network;

import math.ActivationFunction;
import math.Matrix;

public abstract class NeuralNetwork
{
	protected ActivationFunction activationFunction = ActivationFunction.SIGMOID;
	protected double learningRate = 0.1;

	public abstract double[] feed(double... inputRaw);

	public abstract Matrix train(double[] trainingData, double[] expected);

	public final ActivationFunction getActivationFunction()
	{
		return activationFunction;
	}

	public final void setActivationFunction(ActivationFunction activationFunction)
	{
		this.activationFunction = activationFunction;
	}

	public final double getLearningRate()
	{
		return learningRate;
	}

	public final void setLearningRate(double learningRate)
	{
		this.learningRate = learningRate;
	}
}
