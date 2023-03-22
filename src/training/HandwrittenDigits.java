package training;

import drawers.HandwrittenDrawer;
import network.ActivationFunction;
import network.DeepNeuralNetwork;
import network.Malformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.*;


public final class HandwrittenDigits
{
	public static final int TRAINING_SET_SIZE_COEFF = 1_000_000;
	private final static double LEARNING_RATE = 0.00042069;

	public final static int IMAGE_RES = 28;
	public final static int OUTPUT_SIZE = 10;
	public final static Path FILE = Paths.get("networks/complex_digits.dat");
	public final static char[] LOADING_CHARS = {'⡿', '⣟', '⣯', '⣷', '⣾', '⣽', '⣻', '⢿'};

	private static DeepNeuralNetwork network;

	public static void main(String[] args) throws URISyntaxException, IOException
	{
		var kb = new Scanner(System.in);


		System.out.print("Load network from file? (y/n): ");

		if (kb.nextLine().equals("y"))
		{
			System.out.println("Enter file name:");
			network = DeepNeuralNetwork.readFromFile(FILE);

			// print network information
			System.out.println("Network loaded from file:");
			System.out.println("Input size: " + network.getInputSize());
			System.out.println("Output size: " + network.getOutputSize());
			System.out.println("Hidden layers: " + network.getHiddenSizes().length);
			System.out.println("Hidden layer sizes: " + Arrays.toString(network.getHiddenSizes()));
			System.out.println("Hash: " + network.hashCode());

			// ask if the user wants to train the network

			System.out.print("Train network? (y/n): ");
			if (kb.nextLine().equals("y"))
			{
				HandwrittenDigits.train();
			}

		} else
		{
			network = new DeepNeuralNetwork(IMAGE_RES * IMAGE_RES, new int[]{
				40,
				32,
				24,
				16
			}, OUTPUT_SIZE);
			network.setActivationFunction(ActivationFunction.SIGMOID);
			network.setLearningRate(LEARNING_RATE);

			HandwrittenDigits.train();
		}

		HandwrittenDigits.testNetwork();
		HandwrittenDigits.startDrawer();

		// save the network to a file
		network.writeToFile(FILE);
	}

	private static void testNetwork() throws FileNotFoundException, URISyntaxException
	{
		System.out.println("Testing...");

		List<Data> testData = loadData("training/handwritten.csv");

		if (testData != null)
		{

			// get the accuracy for each digit
			int[] accuracy = new int[OUTPUT_SIZE];
			int[] count = new int[OUTPUT_SIZE];
			int total = 0, correct = 0;

			for (Data data : testData)
			{
				count[data.expected]++;

				double[] output = network.feed(data.getMalformed());


				if (DeepNeuralNetwork.largestIndex(output) == data.expected)
				{
					accuracy[data.expected]++;
					correct++;
				}
				total++;
			}

			// print the accuracy for each digit
			for (int i = 0; i < network.getOutputSize(); i++)
			{
				System.out.print("Accuracy for " + i + ": " + (int) (100 * (float) accuracy[i] / count[i]) + "%");
				// fraction
				System.out.println(" (" + accuracy[i] + "/" + count[i] + ")");
			}

			// print the total accuracy
			System.out.println("Total accuracy: " + (int) (100 * (float) correct / total) + "%");

		}
	}

	public static void train() throws FileNotFoundException
	{
		// load training data
		List<Data> trainingData = loadData("training/handwritten.csv");

		if (trainingData == null)
		{
			throw new FileNotFoundException();
		}

		int total = TRAINING_SET_SIZE_COEFF;

		double error = 0f;
		System.out.println("Training for " + total + " samples");

		long averageTime = 0;
		double errorSum = 0f;
		for (int i = 0; i < total || errorSum / i > (12.42069 / 100.); i++)
		{
			// get random data
			Data data = trainingData.get((int) (Math.random() * trainingData.size()));

			// print training message

			double[] expectedOutput = new double[OUTPUT_SIZE];
			expectedOutput[data.expected] = 1;

			long start = System.currentTimeMillis();
			error = network.train(data.getMalformed(), expectedOutput).sum();
			errorSum += error;
			averageTime += System.currentTimeMillis() - start;

			System.out.print("\r" + LOADING_CHARS[i % LOADING_CHARS.length]);
			System.out.print(" Training... " + (int) ((double) i / total * 100) + "%" + " ");
			System.out.printf("%d out of %d",
				i,
				total + Math.max((i - total), 0));
			System.out.print("  Error: " + (int) (error * 100));

			long expectedMillis = (long) ((averageTime / (double) i) * (total - i));

			long expectedSeconds = (expectedMillis / 1000) % 60;
			long expectedMinutes = (expectedMillis / 1000 / 60) % 60;
			long expectedHours = (expectedMillis / 1000 / 60 / 60);

			System.out.printf("\t[%d hours %d minutes %d seconds] Remaining    ", expectedHours, expectedMinutes, expectedSeconds);
			System.out.print("Average Error: " + (int) (100 * errorSum / i) + " ");

			if (i % 100_000 == 0)
			{
				try
				{
					network.writeToFile(FILE);
					System.out.printf("\r[%s] Wrote to file\n",
						DateFormat.getTimeInstance().format(new Date()));
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		System.out.print("\rTraining... 100%  Error: " + (int) (error * 100));
	}

	public static void startDrawer()
	{
		var frame = new HandwrittenDrawer(network);

		// pause until the frame is closed
//		while (frame.isVisible()) {
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}

	public static List<Data> loadData(String path) throws FileNotFoundException
	{
		if (Files.notExists(Path.of(path))) return null;

		var scanner = new Scanner(new File(path));
		var trainingData = new ArrayList<Data>();

		scanner.nextLine();

		System.out.println("Loading data from '" + path + "'. . .");

		int count = 0;

		while (scanner.hasNextLine())
		{
			System.out.print("\r" + LOADING_CHARS[count++ % LOADING_CHARS.length]);
			System.out.print(" " + count + " samples loaded");

			String[] data = scanner.nextLine().split(",");
			int expected = Integer.parseInt(data[0]);

			double[] input = new double[IMAGE_RES * IMAGE_RES];
			for (int i = 0; i < IMAGE_RES * IMAGE_RES; i++)
			{
				input[i] = Double.parseDouble(data[i + 1]) / 255;
			}

			double[] clone = input.clone();
			trainingData.add(new Data(expected, clone));
		}
		System.out.println("\r" + count + " samples loaded");

		return trainingData;
	}

	public static class Data
	{
		int expected;
		private double[] input;

		public Data(int expected, double[] input)
		{
			this.expected = expected;

			this.input = input;
//			network.Malformer.malform(input);
		}

		public double[] getMalformed()
		{
			double[] clone = input.clone();
			Malformer.malform(clone);

			return clone;
		}
	}
}


