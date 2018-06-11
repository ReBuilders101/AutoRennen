package dev.lb.autorennen;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class DrawPanel extends JPanel{
	private static final long serialVersionUID = 5850780939895736909L;
	
	@Override
	protected void paintComponent(Graphics g){
		if(MainFrame.getFrame() != null)
			MainFrame.getFrame().paintCallback((Graphics2D) g, getWidth(), getHeight());
	}
	
}
