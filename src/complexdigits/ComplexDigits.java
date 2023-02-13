package complexdigits;

import math.ActivationFunction;
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

public class ComplexDigits
{
	public final static int IMAGE_RES = 28;

	public static void main(String[] args) throws URISyntaxException, IOException
	{
		List<Data> trainingData = loadData(Objects.requireNonNull(ComplexDigits.class.getResource("train.csv")).toURI());

		var nn = new DeepNeuralNetwork(IMAGE_RES * IMAGE_RES, 10, 16, 16);
		nn.setActivationFunction(ActivationFunction.SIGMOID);
		nn.setLearningRate(.4f);

		int l = 0;
		int total = 5 * trainingData.size();

		for (int i = 0; i < 1; i++)
			for (Data data : trainingData)
			{
				System.out.print("\r" + (int) ((double) l++ / total * 100) + "%" + " ");

				double[] expectedOutput = new double[10];
				expectedOutput[data.expected] = 1;
				nn.train(data.input, expectedOutput);
			}
		System.out.println();

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

	public static List<Data> loadData(URI uri) throws FileNotFoundException
	{
		var scanner = new Scanner(new File(uri));
		var trainingData = new ArrayList<Data>();

		scanner.nextLine();
		while (scanner.hasNextLine())
		{
			String[] data = scanner.nextLine().split(",");
			int expected = Integer.parseInt(data[0]);

			double[] input = new double[IMAGE_RES * IMAGE_RES];
			for (int i = 0; i < IMAGE_RES * IMAGE_RES; i++)
			{
				input[i] = Double.parseDouble(data[i + 1]) / 255;
			}

			trainingData.add(new Data(expected, input));
		}

		return trainingData;
	}

	public static class Data
	{
		int expected;
		double[] input;

		public Data(int expected, double[] input)
		{
			this.expected = expected;
			this.input = input;
		}
	}
}


