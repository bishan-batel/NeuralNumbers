package network;

import java.util.function.Function;

public final class ActivationFunction {

	public static final ActivationFunction SIGMOID = new ActivationFunction(
			(x) -> 1 / (1 + Math.exp(-x)),
			(x) ->
			{
				var sigmoid = 1 / (1 + Math.exp(-x));
				return sigmoid * (1 - sigmoid);
			}
	);
	private final Function<Double, Double> function, derivative;

	public ActivationFunction(
			Function<Double, Double> function,
			Function<Double, Double> derivative) {
		this.function = function;
		this.derivative = derivative;
	}

	public Function<Double, Double> getFunction() {
		return function;
	}

	public Function<Double, Double> getDerivative() {
		return derivative;
	}

}
