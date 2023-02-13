package smalldigits;

import math.ActivationFunction;
import network.DeepNeuralNetwork;
import network.ShallowNeuralNetwork;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SmallDigits
{
	public static void main(String[] args) throws URISyntaxException, FileNotFoundException
	{
		URI uri = Objects.requireNonNull(SmallDigits.class.getResource("training_data")).toURI();
		var scanner = new Scanner(new File(uri));
		var trainingData = new HashMap<Integer, ArrayList<double[]>>();

		while (scanner.hasNextLine())
		{
			var line = scanner.nextLine().replace(" ", "").replace(":", "");
			if (line.startsWith("#")) continue;

			int expected = line.charAt(0) - '0';

			// print out the data as as 3x15 digit

			// convert the data to a float array
			var data = new double[15];
			for (int i = 0; i < 15; i++)
			{
				data[i] = line.charAt(i + 1) == '1' ? 1 : 0;
			}


			// add the data to the training data
			if (!trainingData.containsKey(expected))
				trainingData.put(expected, new ArrayList<>());

			trainingData.get(expected).add(data);
		}


//		var nn = new ShallowNeuralNetwork(15, 32, 10);
		var nn = new DeepNeuralNetwork(15, 10, 16, 12);
		nn.setLearningRate(.1f);
		nn.setActivationFunction(ActivationFunction.SIGMOID);

		for (int i = 0; i < 9999; i++)
		{
			for (var entry : trainingData.entrySet())
			{
				Integer expected = entry.getKey();
				double[] expectedOutput = new double[10];
				expectedOutput[expected] = 1;

				for (double[] data : entry.getValue())
				{
					nn.train(data, expectedOutput);
				}
			}
		}
		System.out.println();


		// test
		var accuracy = 0;
		var count = 0;
		for (var entry : trainingData.entrySet())
		{
			Integer expected = entry.getKey();

			for (double[] data : entry.getValue())
			{
				double[] output = nn.feed(data);
				int maxIndex = 0;
				for (int i = 0; i < output.length; i++)
					if (output[i] > output[maxIndex]) maxIndex = i;

				if (maxIndex == expected) accuracy++;

				count++;
			}
		}

		System.out.println("Accuracy: " + ((float) accuracy / count));

		var drawer = new DigitDrawer(nn);
	}
}
