package dev.lb.autorennen;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.Timer;

public class MainFrame extends JFrame implements KeyListener{
	private static final long serialVersionUID = -3179917169647965155L;

	private static MainFrame theOneFrame;
	
	private DrawPanel dp;
	private Timer timer; 
	private GameState currentState;
	
	private MainFrame(String title, int width, int height){
		super("Autorennen");
		dp = new DrawPanel();
		timer = new Timer(1000 / Settings.TPS, this::tick);
		currentState = GameState.getEmptyState();
		add(dp);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(this);
		setPreferredSize(new Dimension(width, height));
		pack();
		setVisible(true);
	}
	
	public void changeState(GameState newState){
		currentState = newState;
	}
	
	public void tick(ActionEvent e){
		//First logic
		currentState.tick();
		//then draw;
		dp.repaint();
	}
	
	public void paintCallback(Graphics2D g, int width, int height){
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		currentState.paint(g, width, height);
	}
	
	public void start(){
		timer.start();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(currentState instanceof KeyListener) ((KeyListener) currentState).keyReleased(e);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(currentState instanceof KeyListener) ((KeyListener) currentState).keyPressed(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if(currentState instanceof KeyListener) ((KeyListener) currentState).keyTyped(e);
	}

	public static MainFrame getFrame(){
		return theOneFrame;
	}
	
	public static void init(String title, int width, int height){
		if(theOneFrame != null){
			theOneFrame.dispose();
		}
		theOneFrame = new MainFrame("Autorennen", width, height);
	}
}
