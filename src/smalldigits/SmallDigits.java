package smalldigits;

import math.ActivationFunction;
import network.DeepNeuralNetwork;
import network.ShallowNeuralNetwork;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SmallDigits {
	public static void main(String[] args) throws URISyntaxException, IOException {
		List<double[][]> trainingData = loadSet(SmallDigits.class.getResource("training_data").toURI());

//		var nn = new ShallowNeuralNetwork(15, 32, 10);
		var nn = new DeepNeuralNetwork(15, 10, 16, 12);

//		nn.setLearningRate(.1);
		nn.setActivationFunction(ActivationFunction.SIGMOID);

		for (int i = 0; i < 9999; i++) {
			int r = (int) (Math.random() * trainingData.size());
			double[][] data = trainingData.get(r);
			nn.train(data[0], data[1]);
		}
		System.out.println();


		// test
//		System.out.println("Accuracy: " + ((float) accuracy / count));

		var drawer = new DigitDrawer(nn);
	}

	private static List<double[][]> loadSet(URI path) throws IOException {
		var data = new ArrayList<double[][]>();

		// for all lines
		for (var line : Files.readAllLines(Paths.get(path))) {
			// skip blanks
			if (line.isBlank()) continue;

			String inputStr = line.substring(0, 15);
			String outputStr = line.substring(16);

			double[] input = new double[15];
			double[] output = new double[10];

			for (int i = 0; i < 15; i++) {
				input[i] = inputStr.charAt(i) - '0';
				System.out.print(input[i] == 1 ? '█' : ' ');
				if (i % 3 == 2) System.out.println();
			}


			int highest = 0;
			for (int i = 0; i < 10; i++) {
				output[i] = outputStr.charAt(i) - '0';

				if (output[i] == 1) highest = i;
			}

			System.out.println("output" + highest);

			data.add(new double[][]{input, output});
		}
		return data;
	}
}
