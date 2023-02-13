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
	NeuralNetwork network;

	public HandwrittenDrawer(NeuralNetwork network)
	{
		setSize(600, 600);
		this.network = network;
		data = new double[ComplexDigits.IMAGE_RES * ComplexDigits.IMAGE_RES];

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	private void update()
	{
		Graphics g = getGraphics();
		if (g == null) return;
		g.clearRect(0, 0, getWidth(), getHeight());
		drawGrid(g);


		// draw the data
		for (int i = 0; i < ComplexDigits.IMAGE_RES; i++)
		{
			for (int j = 0; j < ComplexDigits.IMAGE_RES; j++)
			{
				var x = j * (getWidth() / ComplexDigits.IMAGE_RES);
				var y = i * (getHeight() / ComplexDigits.IMAGE_RES);
				var w = getWidth() / ComplexDigits.IMAGE_RES;
				var h = getHeight() / ComplexDigits.IMAGE_RES;
				g.setColor(new Color(0, 0, 0, (int) (data[i * ComplexDigits.IMAGE_RES + j] * 255)));
				g.fillRect(x, y, w, h);
			}
		}

		if (network == null) return;

		// flip the data
		var d = new double[data.length];
		for (int i = 0; i < ComplexDigits.IMAGE_RES; i++)
		{
			for (int j = 0; j < ComplexDigits.IMAGE_RES; j++)
			{
				d[i * ComplexDigits.IMAGE_RES + j] = data[(ComplexDigits.IMAGE_RES - i - 1) * ComplexDigits.IMAGE_RES + j];
			}
		}

		// set title to the network's guess
		double[] guess = network.feed(d);
		int max = 0;
		for (int i = 0; i < guess.length; i++)
		{
			if (guess[i] > guess[max])
			{
				max = i;
			}
		}
		((JFrame) SwingUtilities.getWindowAncestor(this)).setTitle("Network's Guess: " + max);
		System.out.println(max);
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

		double delta = (e.isAltDown() ? -1 : 1) * 0.1;
		// circular brush size that lightens/darkens the pixel in a circle
		for (int i = -bruhSize; i < bruhSize; i++)
		{
			for (int j = -bruhSize; j < bruhSize; j++)
			{
				if (x + i < 0 || x + i >= ComplexDigits.IMAGE_RES || y + j < 0 || y + j >= ComplexDigits.IMAGE_RES)
					continue;
				var dist = Math.sqrt(i * i + j * j);
				if (dist > bruhSize) continue;
				int k = (y + j) * ComplexDigits.IMAGE_RES + x + i;
				data[k] += delta * Math.pow((1 - dist / bruhSize), 2);
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
	public void mouseEntered(MouseEvent e)
	{
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
