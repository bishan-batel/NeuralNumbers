package complexdigits;

import math.ActivationFunction;
import math.Matrix;
import network.DeepNeuralNetwork;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;


public class ComplexDigits {
	public final static int IMAGE_RES = 28;

	public static void main(String[] args) throws URISyntaxException, IOException {

		var nn = new DeepNeuralNetwork(IMAGE_RES * IMAGE_RES, 10, 16, 16);
		nn.setActivationFunction(ActivationFunction.SIGMOID);
		nn.setLearningRate(.001f);

		List<Data> trainingData = loadData(Objects.requireNonNull(ComplexDigits.class.getResource("train.csv")).toURI());
		int total = 32 * trainingData.size();
		System.out.println("Training for " + total + " samples");

		double error = 1;

		for (int i = 0; i < total; i++) {
			// get random data
			Data data = trainingData.get((int) (Math.random() * trainingData.size()));

			// print training message
			System.out.print("\rTraining... " + (int) ((double) i++ / total * 100) + "%" + " ");
//			System.out.print(LOADING_CHARS[i % LOADING_CHARS.length]);
			System.out.print("  Error: " + (int) (error * 100));

			double[] expectedOutput = new double[10];
			expectedOutput[data.expected] = 1;
			error = nn.train(data.input, expectedOutput).sum();
		}
		System.out.println();

		System.out.println("Testing...");

		List<Data> testData = loadData(Objects.requireNonNull(ComplexDigits.class.getResource("train.csv")).toURI());

		// get the accuracy for each digit
		int[] accuracy = new int[10];
		int[] count = new int[10];

		for (Data data : testData) {
			count[data.expected]++;

			double[] output = nn.feed(data.input);

			int highest = 0;
			for (int i = 0; i < 10; i++) {
				if (output[i] > output[highest]) highest = i;
			}
			if (highest == data.expected) {
				accuracy[data.expected]++;
			}
		}

		// print the accuracy for each digit
		for (int i = 0; i < 10; i++) {
			System.out.println("Accuracy for " + i + ": " + (int) (100 * (float) accuracy[i] / count[i]) + "%");
		}


		var frame = new JFrame("Handwritten Digit Drawer");
		var hd = new HandwrittenDrawer(nn);
		frame.add(hd);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// get random training data
		Data data = trainingData.get((int) (Math.random() * trainingData.size()));

		hd.data = data.input;

		// save the network to a file
		var file = new File("network.ser");
		var out = new ObjectOutputStream(new java.io.FileOutputStream(file));
		out.write(nn.getBytes());
		out.close();
	}

	public static List<Data> loadData(URI uri) throws FileNotFoundException {
		var scanner = new Scanner(new File(uri));
		var trainingData = new ArrayList<Data>();

		scanner.nextLine();
		while (scanner.hasNextLine()) {
			String[] data = scanner.nextLine().split(",");
			int expected = Integer.parseInt(data[0]);

			double[] input = new double[IMAGE_RES * IMAGE_RES];
			for (int i = 0; i < IMAGE_RES * IMAGE_RES; i++) {
				input[i] = Double.parseDouble(data[i + 1]) / 255;
			}

			trainingData.add(new Data(expected, input));
		}

		return trainingData;
	}

	public static class Data {
		int expected;
		double[] input;

		public Data(int expected, double[] input) {
			this.expected = expected;
			this.input = input;
		}
	}
}


