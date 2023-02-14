package math;

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

	public static final ActivationFunction RELU = new ActivationFunction(
			(x) -> Math.max(0, x),
			(f) -> f > 0 ? 1. : 0.
	);

	public static final ActivationFunction SILU = new ActivationFunction(
			(x) -> x / (1 + Math.exp(-x)),
			(x) ->
			{
				var silu = x / (1 + Math.exp(-x));
				return silu + (1 - silu) * silu;
			}
	);

	public static final ActivationFunction TANH = new ActivationFunction(
			Math::tanh,
			(x) -> 1 - Math.pow(Math.tanh(x), 2)
	);
	private final Function<Double, Double> function;
	private final Function<Double, Double> derivative;

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
