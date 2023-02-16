package complexdigits;

import network.NeuralNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

public class HandwrittenDrawer extends Canvas implements MouseMotionListener, MouseListener
{
	public double[] data;
	private final NeuralNetwork network;

	public HandwrittenDrawer(NeuralNetwork network)
	{
		setSize(600, 600);
		this.network = network;
		data = new double[ComplexDigits.IMAGE_RES * ComplexDigits.IMAGE_RES];

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	private void feed()
	{
		double[] guess = network.feed(data);

		int max = 0;
		for (int i = 0; i < guess.length; i++)
		{
			if (guess[i] > guess[max])
			{
				max = i;
			}
		}

//		System.out.println(Arrays.toString(guess));
//		System.out.println("Guess: " + max);
//
		// update title
		((JFrame) SwingUtilities.getWindowAncestor(this)).setTitle("Complex Digits - " + max);
	}

	private void update()
	{
		if (network != null)
		{
			new Thread(this::feed).start();
		}

		Graphics g = getGraphics();
		if (g == null) return;
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		var cellWidth = getWidth() / ComplexDigits.IMAGE_RES;
		var cellHeight = getHeight() / ComplexDigits.IMAGE_RES;

		// draw the data
		for (var i = 0; i < ComplexDigits.IMAGE_RES; i++)
		{
			for (var j = 0; j < ComplexDigits.IMAGE_RES; j++)
			{
				int x = j * (getWidth() / ComplexDigits.IMAGE_RES);
				int y = i * (getHeight() / ComplexDigits.IMAGE_RES);

				int brightness = (int) (data[i * ComplexDigits.IMAGE_RES + j] * 255);
				g.setColor(new Color(brightness, brightness, brightness));
				g.fillRect(x, y, cellWidth, cellHeight);
			}
		}
	}

	@Override
	public void repaint()
	{
		update();
	}

	private void drawGrid(Graphics g)
	{
		g.setColor(Color.GRAY);
		for (int i = 0; i < ComplexDigits.IMAGE_RES; i++)
		{
			var j = i * (getWidth() / ComplexDigits.IMAGE_RES);
			g.drawLine(0, j, getWidth(), j);
			g.drawLine(j, 0, j, getHeight());
		}
	}

	private boolean isDrawing = false;

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.isControlDown())
		{
			Arrays.fill(data, 0);
		}
	}

	int bruhSize = 2;

	public void draw(MouseEvent e)
	{
		var x = e.getX() / (getWidth() / ComplexDigits.IMAGE_RES);
		var y = e.getY() / (getHeight() / ComplexDigits.IMAGE_RES);

		double delta = (e.isAltDown() ? -1 : 1) * 0.2;
		// circular brush size that lightens/darkens the pixel in a circle
		for (int i = -bruhSize; i < bruhSize; i++)
		{
			for (int j = -bruhSize; j < bruhSize; j++)
			{

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

		update();
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		isDrawing = true;
		draw(e);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		isDrawing = false;
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		isDrawing = false;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (isDrawing)
		{
			draw(e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}


	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	public static void main(String[] args)
	{
		var frame = new JFrame("Handwritten Digit Drawer");
		frame.add(new HandwrittenDrawer(null));
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
