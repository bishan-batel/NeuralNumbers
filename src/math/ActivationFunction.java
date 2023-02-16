package math;

import java.util.function.Function;

public final class ActivationFunction {

	public static final ActivationFunction SIGMOID = new ActivationFunction(
			(x) -> 1 / (1 + Math.exp(-x)),
			(x) ->
			{
				var sigmoid = 1 / (1 + Math.exp(-x));
				return sigmoid * (1 - sigmoid);
			},
			(x) -> {
				if (x <= 0)
					return 0.0;
				else if (x >= 1)
					return 1.0;
				else
					return Math.log(1 / x - 1);
			}
	);
	private final Function<Double, Double> function, derivative, inverse;

	public ActivationFunction(
			Function<Double, Double> function,
			Function<Double, Double> derivative, Function<Double, Double> inverse) {
		this.function = function;
		this.derivative = derivative;
		this.inverse = inverse;
	}

	public Function<Double, Double> getFunction() {
		return function;
	}

	public Function<Double, Double> getDerivative() {
		return derivative;
	}

	public Function<Double, Double> getInverse() {
		return inverse;
	}
}
