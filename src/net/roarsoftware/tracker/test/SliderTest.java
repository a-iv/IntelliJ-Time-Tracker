package net.roarsoftware.tracker.test;

import javax.swing.JFrame;

import net.roarsoftware.tracker.ui.TableSlider;

/**
 * @author Janni Kovacs
 */
public class SliderTest {

	public static void main(String[] args) {
		JFrame f = new JFrame();

		TableSlider s = new TableSlider();
		f.add(s);

		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

}