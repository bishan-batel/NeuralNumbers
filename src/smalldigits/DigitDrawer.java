package smalldigits;

import network.NeuralNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DigitDrawer extends JFrame {
	private NeuralNetwork nn;
	private Canvas canvas;
	private JLabel label;
	private double[] data = {1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1};

	public DigitDrawer(NeuralNetwork nn) {
		this.nn = nn;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Digit Drawer");
		setLayout(new FlowLayout());

		canvas = new Canvas();
		canvas.setSize(300, 500);

		label = new JLabel("Draw a digit here:");
		// set text alignment to right
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		// change text size
		label.setFont(new Font("Arial", Font.PLAIN, 20));
		add(label);


		add(canvas);
		setResizable(false);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);

		update();
		feed();

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX() / 100;
				int y = e.getY() / 100;

				data[y * 3 + x] = e.isAltDown() ? 0 : 1;

				feed();
				update();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
	}

	public void update() {
		var g = canvas.getGraphics();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 300, 500);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, 300, 500);

		for (int i = 0; i < 15; i++) {
			int x = i % 3;
			int y = i / 3;

			if (data[i] == 1)
				g.fillRect(x * 100, y * 100, 100, 100);
		}

		// draw gridlines 3x15
		g.setColor(Color.GRAY);

		for (int i = 0; i < 3; i++) {
			g.drawLine(i * 100, 0, i * 100, 500);
		}

		for (int i = 0; i < 5; i++) {
			g.drawLine(0, i * 100, 300, i * 100);
		}
	}

	private void feed() {
		label.setText("NN: ...");
		double[] output = nn.feed(data);

		// get highest output
		int highest = 0;
		for (int i = 0; i < output.length; i++) {
			if (output[i] > output[highest])
				highest = i;
		}

		StringBuilder txt = new StringBuilder();

		// print out the output
		for (int i = 0; i < output.length; i++) {
			txt.append(i).append(": ").append(Math.round(output[i] * 100)).append(
					"%<br>");
		}

		label.setText("<html>NN: " + highest + "<br>" + txt);
	}
}
