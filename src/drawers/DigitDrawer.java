package drawers;

import network.DeepNeuralNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DigitDrawer extends JFrame
{
	public static final Color FILL_PIXEL_COLOR = new Color(144, 121, 245);
	public static final Color EMPTY_PIXEL_COLOR = new Color(28, 29, 31);
	public static final Color GRID_COLOR = new Color(126, 171, 127, 100);

	private final DeepNeuralNetwork nn;
	private final Canvas canvas;
	private final JLabel label;
	private final double[] data = new double[15];

	public DigitDrawer(DeepNeuralNetwork nn)
	{
		this.nn = nn;
		setLayout(new FlowLayout());
		getContentPane().setBackground(Color.BLACK);

		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(300, 500));

		label = new JLabel("Draw a digit here:");
		label.setForeground(Color.WHITE);
		// set text alignment to right

		// change text size
		label.setFont(new Font("Agave", Font.PLAIN, 30));
		add(label);

		add(canvas);


		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Digit Drawer");
		setResizable(false);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
		update();
		feed();

		canvas.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				int x = e.getX() / 100;
				int y = e.getY() / 100;

				data[y * 3 + x] = e.isAltDown() ? 0 : 1;

				feed();
				update();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
			}

			@Override
			public void mouseDragged(MouseEvent e)
			{
			}
		});
	}

	public void update()
	{
		Graphics g = canvas.getGraphics();

		g.setColor(Color.GRAY);
		g.drawRect(0, 0, 300, 500);


		for (int i = 0; i < 15; i++)
		{
			int x = i % 3;
			int y = i / 3;

			g.setColor(data[i] == 0 ? EMPTY_PIXEL_COLOR : FILL_PIXEL_COLOR);

			g.fillRect(x * 100, y * 100, 100, 100);
		}

		// draw gridlines 3x15
		g.setColor(GRID_COLOR);

		for (int i = 0; i < 3; i++)
		{
			g.drawLine(i * 100, 0, i * 100, 500);
		}

		for (int i = 0; i < 5; i++)
		{
			g.drawLine(0, i * 100, 300, i * 100);
		}
	}

	private void feed()
	{
		label.setText("NN: ...");
		double[] output = nn.feed(data);

		// get highest output
		int highest = DeepNeuralNetwork.largestIndex(output);

		StringBuilder text = new StringBuilder("<html>Output<br>");

		// append highest guest
		text
			.append("<bold>")
			.append(highest)
			.append(":")
			.append((int) (output[highest] * 100))
			.append("%</bold><br><br>");

		int b = 100;

		for (int i = 0; i < output.length; i++)
		{
			// interpolate between red to green based on output
			int g = (int) (255 * output[i]);
			int r = (int) (255 * (1 - output[i]));

			String hex = String.format("%02x%02x%02x", r, g, b);

			text
				.append("<font color=\"#")
				.append(hex)
				.append("\">")
				.append(i)
				.append(":")
				.append((int) (output[i] * 100))
				.append("%</font><br>");
		}

		text.append("</html>");

		label.setText(text.toString());
	}
}
