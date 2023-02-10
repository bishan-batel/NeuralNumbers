public class Main {
	public static void main(String[] args) {

		var nn = new ShallowNeuralNetwork(15, 10, 16);

		float[] out = nn.feed(1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1);

		for (var i : out) {
			System.out.print(i + ", ");
		}
		System.out.println();
	}
}