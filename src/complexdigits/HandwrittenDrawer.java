package complexdigits;

import network.DeepNeuralNetwork;
import network.NeuralNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Arrays;

public class HandwrittenDrawer extends JFrame implements MouseMotionListener, MouseListener {
	public double[] data;
	private final NeuralNetwork network;
	protected final Canvas canvas;
	protected final JLabel label;

	public HandwrittenDrawer(NeuralNetwork network) {
		this.network = network;
		data = new double[ComplexDigits.IMAGE_RES * ComplexDigits.IMAGE_RES];

		label = new JLabel("0");
		label.setFont(new Font("agave", Font.PLAIN, 40));
		label.setForeground(Color.WHITE);
		label.setHorizontalAlignment(SwingConstants.RIGHT);

		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(600, 600));
		canvas.setSize(600, 600);
		canvas.addMouseMotionListener(this);
		canvas.addMouseListener(this);


		add(label, BorderLayout.WEST);
		add(canvas, BorderLayout.CENTER);

		setSize(700, 600);
		getContentPane().setBackground(Color.BLACK);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void feed() {
		double[] guess = network.feed(data);

		int highest = NeuralNetwork.largestIndex(guess);


		StringBuilder text = new StringBuilder("<html>Output<br>");

		// append highest guest
		text.append("<bold>").append(highest).append(":").append((int) (guess[highest] * 100)).append("%</bold><br><br>");

		int b = 100;

		for (int i = 0; i < guess.length; i++) {
			// interpolate between red to green based on output
			int g = (int) (255 * guess[i]);
			int r = (int) (255 * (1 - guess[i]));

			String hex = String.format("%02x%02x%02x", r, g, b);

			text.append("<font color=\"#").append(hex).append("\">").append(i).append(":").append((int) (guess[i] * 100)).append("%</font><br>");
		}

		text.append("</html>");

		label.setText(text.toString());
		setTitle("Complex Digits - " + highest);
	}

	private void update() {
		if (network != null) {
			new Thread(this::feed).start();
		}

		Graphics g = canvas.getGraphics();
		if (g == null) return;
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		var cellWidth = canvas.getWidth() / ComplexDigits.IMAGE_RES;
		var cellHeight = canvas.getHeight() / ComplexDigits.IMAGE_RES;

		// draw the data
		for (var i = 0; i < ComplexDigits.IMAGE_RES; i++) {
			for (var j = 0; j < ComplexDigits.IMAGE_RES; j++) {
				int x = j * (canvas.getWidth() / ComplexDigits.IMAGE_RES);
				int y = i * (canvas.getHeight() / ComplexDigits.IMAGE_RES);

				int brightness = (int) (data[i * ComplexDigits.IMAGE_RES + j] * 255);
				g.setColor(new Color(brightness, brightness, brightness));
				g.fillRect(x, y, cellWidth, cellHeight);
			}
		}
	}

	@Override
	public void repaint() {
		update();
	}

	private void drawGrid(Graphics g) {
		g.setColor(Color.GRAY);
		for (int i = 0; i < ComplexDigits.IMAGE_RES; i++) {
			var j = i * (canvas.getWidth() / ComplexDigits.IMAGE_RES);
			g.drawLine(0, j, canvas.getWidth(), j);
			g.drawLine(j, 0, j, canvas.getHeight());
		}
	}

	private boolean isDrawing = false;

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.isControlDown()) {
			Arrays.fill(data, 0);
		}
	}

	int bruhSize = 2;

	public void draw(MouseEvent e) {
		var x = e.getX() / (canvas.getWidth() / ComplexDigits.IMAGE_RES);
		var y = e.getY() / (canvas.getHeight() / ComplexDigits.IMAGE_RES);

		double delta = (e.isAltDown() ? -1 : 1) * 0.42;

		// circular brush size that lightens/darkens the pixel in a circle
		for (int i = -bruhSize; i < bruhSize; i++) {
			for (int j = -bruhSize; j < bruhSize; j++) {

				// check if pixel is in bounds of canvas
				if (x + i < 0 || x + i >= ComplexDigits.IMAGE_RES || y + j < 0 || y + j >= ComplexDigits.IMAGE_RES)
					continue;

				// get distance from center of brush
				double dist = Math.sqrt(i * i + j * j);

				// if the distance is greater than the brush size, don't draw
				if (dist > bruhSize) continue;

				// current pixel index
				int k = (y + j) * ComplexDigits.IMAGE_RES + x + i;

				// draw the pixel
				data[k] += delta * Math.pow((1 - dist / bruhSize), 2);

				// constrain to 0 to 1
				data[k] = Math.max(0, Math.min(1, data[k]));
			}
		}

		if (e.isShiftDown()) {
			Malformer.malform(data);
		}

		update();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		isDrawing = true;
		draw(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		isDrawing = false;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		isDrawing = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (isDrawing) {
			draw(e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}


	@Override
	public void mouseMoved(MouseEvent e) {
	}

	public static void main(String[] args) throws IOException {
		var hd = new HandwrittenDrawer(null);
		DeepNeuralNetwork nn = DeepNeuralNetwork.readFromFile(ComplexDigits.FILE);

		var input = new double[10];
		Arrays.fill(input, 0.0001);
		input[8] = 0.9999;
		double[] out = nn.inverse(input);

		System.out.println(Arrays.toString(out));
		hd.data = out;
		hd.update();
	}
}
