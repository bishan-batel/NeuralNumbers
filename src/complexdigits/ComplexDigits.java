package complexdigits;

import math.ActivationFunction;
import network.DeepNeuralNetwork;
import network.NeuralNetwork;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public final class ComplexDigits
{
	public static final int TRAINING_SET_SIZE_COEFF = 69;
	public final static int IMAGE_RES = 28;
	public final static int OUTPUT_SIZE = 10;
	private final static Path FILE = Paths.get("networks/complex_digits.dat");
	private final static double LEARNING_RATE = 0.0001;

	private static DeepNeuralNetwork nn;

	public static void main(String[] args) throws URISyntaxException, IOException
	{
		var kb = new Scanner(System.in);

		System.out.print("Load network from file? (y/n): ");

		if (kb.nextLine().equals("y"))
		{
			System.out.println("Enter file name:");
			nn = DeepNeuralNetwork.readFromFile(FILE);

			// print network information
			System.out.println("Network loaded from file:");
			System.out.println("Input size: " + nn.getInputSize());
			System.out.println("Output size: " + nn.getOutputSize());
			System.out.println("Hidden layers: " + nn.getHiddenSizes().length);
			System.out.println("Hidden layer sizes: " + Arrays.toString(nn.getHiddenSizes()));

			// ask if the user wants to train the network
			System.out.println("Train network? (y/n)");
			if (kb.nextLine().equals("y"))
			{
				ComplexDigits.train();
			}

		} else
		{
			nn = new DeepNeuralNetwork(IMAGE_RES * IMAGE_RES, OUTPUT_SIZE, 64, 32, 24, 16);
			nn.setActivationFunction(ActivationFunction.SIGMOID);
			nn.setLearningRate(LEARNING_RATE);

			ComplexDigits.train();
		}

		ComplexDigits.testNetwork();
		ComplexDigits.startDrawer();

		// save the network to a file
		nn.writeToFile(FILE);
	}

	private static void testNetwork() throws FileNotFoundException, URISyntaxException
	{
		System.out.println("Testing...");

		List<Data> testData = loadData("training/handwritten.csv");

		// get the accuracy for each digit
		int[] accuracy = new int[OUTPUT_SIZE];
		int[] count = new int[OUTPUT_SIZE];
		int total = 0, correct = 0;

		for (Data data : testData)
		{
			count[data.expected]++;

			double[] output = nn.feed(data.input);


			if (NeuralNetwork.largestIndex(output) == data.expected)
			{
				accuracy[data.expected]++;
				correct++;
			}
			total++;
		}

		// print the accuracy for each digit
		for (int i = 0; i < nn.getOutputSize(); i++)
		{
			System.out.print("Accuracy for " + i + ": " + (int) (100 * (float) accuracy[i] / count[i]) + "%");
			// fraction
			System.out.println(" (" + accuracy[i] + "/" + count[i] + ")");
		}

		// print the total accuracy
		System.out.println("Total accuracy: " + (int) (100 * (float) correct / total) + "%");
	}

	public static void train() throws URISyntaxException, FileNotFoundException
	{
		// load training data
		List<Data> trainingData = loadData("training/handwritten.csv");

		int total = TRAINING_SET_SIZE_COEFF * trainingData.size();
		System.out.println("Training for " + total + " samples");

		double error = 1;

		for (int i = 0; i < total; i++)
		{
			// get random data
			Data data = trainingData.get((int) (Math.random() * trainingData.size()));

			// print training message
			System.out.print("\rTraining... " + (int) ((double) i++ / total * 100) + "%" + " ");
//			System.out.print(LOADING_CHARS[i % LOADING_CHARS.length]);
			System.out.print("  Error: " + (int) (error * 100));

			double[] expectedOutput = new double[OUTPUT_SIZE];
			expectedOutput[data.expected] = 1;
			error = nn.train(data.input, expectedOutput).sum();
		}
		System.out.println();

	}

	public static void startDrawer()
	{
		var frame = new JFrame("Handwritten Digit Drawer");
		var hd = new HandwrittenDrawer(nn);
		frame.add(hd);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// pause until the frame is closed
		while (frame.isVisible())
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static List<Data> loadData(String path) throws FileNotFoundException
	{
		var scanner = new Scanner(new File(path));
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


