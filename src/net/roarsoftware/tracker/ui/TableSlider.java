package net.roarsoftware.tracker.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.InputMap;

/**
 * Component that acts as a simple replacement for JSlider. It only knows values between 0 and 1 although you can set
 * a value > 1 (which then will be displayed as "150%" for example.
 * @author Janni Kovacs
 */
public class TableSlider extends JComponent {

	private static final NumberFormat FORMAT = NumberFormat.getPercentInstance();

	private double value = 0.0;
	private String percentageString = "";
	private Color color = new Color(109, 201, 101);

	public TableSlider() {
		setFocusable(true);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocusInWindow(); // request focus when clicked, so left and right keys work
				setValueFor(e.getX());
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				setValueFor(e.getX());
			}
		});
		InputMap map = getInputMap();
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "ValueDec");
		map.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "ValueInc");
		getActionMap().put("ValueDec", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setValue(Math.max(value - 0.01, 0));
			}
		});
		getActionMap().put("ValueInc", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setValue(Math.min(value + 0.01, 1));
			}
		});
		percentageString = FORMAT.format(value);
	}

	private void setValueFor(int x) {
		double value = (double) x / getWidth();
		if(value < 0) value = 0;
		if(value > 1) value = 1;
		setValue(value);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
		this.percentageString = FORMAT.format(value);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int w = getWidth();
		int h = getHeight();
		g.setColor(getBackground());
		g.fillRect(0, 0, w, h);
		g.setColor(color);
		g.fillRect(2, 2, (int) ((w - 4) * Math.min(value, 1)), h - 4);
		FontMetrics fm = g.getFontMetrics();
		int wt = fm.stringWidth(percentageString);
		int ht = fm.getAscent();
		g.setColor(getForeground());
		g.drawString(percentageString, w / 2 - wt / 2, h / 2 + ht / 2);
	}
}
