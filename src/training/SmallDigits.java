package training;

import drawers.DigitDrawer;
import network.ActivationFunction;
import network.DeepNeuralNetwork;
import network.Matrix;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SmallDigits {
	public static final int TRAINING_LIMIT = 10000;
	public static final Path NETWORK_FILE = Paths.get("networks/small_digits" + ".dat");

	public static void main(String[] args) throws IOException {
		var kb = new Scanner(System.in);

		System.out.print("Do you want to load the network from file? (y/n): ");

		DeepNeuralNetwork nn = kb.nextLine().startsWith("y") ? DeepNeuralNetwork.readFromFile(NETWORK_FILE) : createAndTrain();

		System.out.println("Testing...");
		List<double[][]> testData = loadSet("testing/small_digits.csv");

		if (testData != null) {
			int totalCorrect = 0, total = 0;
			var correct = new int[10];
			var totalPerDigit = new int[10];
			var errors = new double[10];
			var totalError = 0.0;

			// for all test data
			for (double[][] data : testData) {
				// get response from network
				double[] result = nn.feed(data[0]);

				// get the expected digit
				int expectedDigit = DeepNeuralNetwork.largestIndex(data[1]);

				// get the guessed digit
				int resultDigit = DeepNeuralNetwork.largestIndex(result);

				// add to total correct for that digit
				totalPerDigit[expectedDigit]++;

				// if correct then add to total correct for that digit & total correct
				if (resultDigit == expectedDigit) {
					correct[expectedDigit]++;
					totalCorrect++;
				}

				total++;

				// calculate error MSE
				errors[expectedDigit] = Matrix.asColumn(data[1]).subtract(Matrix.asColumn(result)).map(n -> n * n).sum();

				// add to total error
				totalError += errors[expectedDigit];
			}

			// print all the data
			for (int i = 0; i < 10; i++) {
				System.out.print("Accuracy for " + i + ": " + (int) (100 * (float) correct[i] / totalPerDigit[i]) + "%");
				System.out.println(" Error: " + (int) (100 * errors[i] / totalPerDigit[i]) + "%");
			}

			System.out.println("Average accuracy: " + (int) (100 * (float) totalCorrect / total) + "%");
			System.out.println("Average error: " + (int) (100 * totalError / total) + "%");
		} else {
			System.out.println("Could not load testing data");
		}


		// interactive mode
		new DigitDrawer(nn);

		System.out.print("Do you want to save the network to file? (y/n): ");

		if (kb.nextLine().startsWith("y"))
			nn.writeToFile(NETWORK_FILE);
	}

	public static DeepNeuralNetwork createAndTrain() throws IOException {
		List<double[][]> trainingData = loadSet("training/small_digits.csv");
		var nn = new DeepNeuralNetwork(15, new int[]{14, 14}, 10);

		nn.setLearningRate(.1);
		nn.setActivationFunction(ActivationFunction.SIGMOID);

		System.out.println("Training...");

		for (int i = 0; i < TRAINING_LIMIT; i++) {
			// pick random training data
			int r = (int) (Math.random() * trainingData.size());
			double[][] data = trainingData.get(r);
			nn.train(data[0], data[1]);

			System.out.print("\rTraining " + i + "/" + TRAINING_LIMIT);
		}
		System.out.println();
		return nn;
	}

	private static List<double[][]> loadSet(String pathRaw) throws IOException {
		Path path = Path.of(pathRaw);

		if (!Files.exists(path)) return null;
		List<String> lines = Files.readAllLines(path);
		var data = new ArrayList<double[][]>();

		// remove CSV header
		lines = lines.subList(1, lines.size());

		for (var line : lines) {
			String[] parts = line.split(",");
			String inputStr = parts[0];
			String outputStr = parts[1];

			double[] input = new double[15];
			double[] output = new double[outputStr.length()];

			for (int i = 0; i < input.length; i++)
				input[i] = inputStr.charAt(i) - '0';

			for (int i = 0; i < output.length; i++)
				output[i] = outputStr.charAt(i) - '0';

			data.add(new double[][]{input, output});
		}

		return data;
	}
}
