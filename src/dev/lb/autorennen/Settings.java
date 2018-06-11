package dev.lb.autorennen;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;

public final class Settings {

	private Settings(){}
	
	/**
	 * The maximum speed ( pixel per keypress) a car can reach
	 */
	public static double CAR_MAX_SPEED = 6D;
	/**
	 * The amount that the car speed increases per keypress if it is below max
	 */
	public static double CAR_TICK_ACC = 0.7D;
	
	/**
	 * The amount that the car speed decreases per tick
	 */
	public static double CAR_TICK_DEC = 0.1D;
	
	public static double CAR_TICK_DEC_OBS = 0.05D;
	
	public static double CAR_SIZE_CONVERSION = 7D/5D;
	
	public static double CAR_CORNER_CONVERSION = 0.4D;

	/**
	 * Multiply the car length/width and the distance in units with this constant to get the distance in pixels
	 */
	public static double CAR_LENGTH_TO_UNITS = 1D / 100D;
	
	public static int TPS = 30;
	
	public static float LINE_WIDTH_CONVERSION = 0.02F;
	
	public static float LINE_DASH_CONVERSION = 0.2F;
	
	public static Color GRASS_COLOR = new Color(0x00, 0x40, 0x00);
	
	public static Color ROAD_COLOR = new Color(0x30, 0x30, 0x30);
	
	public static Color MARK_COLOR = new Color(0xB9, 0xB9, 0xB9);
	
	public static Color LIGHT_COLOR = new Color(0xFF, 0xFF, 0x0F);
	
	public static Color BORDER_COLOR = new Color(0x00, 0x10, 0x00);
	
	public static Color BANNER_BG_COLOR = new Color(0x00, 0x00, 0xB0);
	
	public static Color BANNER_FG_COLOR = getComplementaryColor(BANNER_BG_COLOR);
	
	//From SO
	public static Color getComplementaryColor(Color color) {
	   return new Color(0xFF - color.getRed(), 0xFF - color.getGreen(), 0xFF - color.getBlue(), color.getAlpha());
	}
	
	//From SO
	public static void drawCenteredString(Graphics g, String text, Rectangle rect) {
	    FontMetrics fm = g.getFontMetrics();
	    int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
	    int y = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
	    g.drawString(text, x, y);
	}
	
	public static float getFontSize(float pixelSize){
		return (float) (pixelSize * Toolkit.getDefaultToolkit().getScreenResolution() / 72.0);
	}
	
	public static double getPixels(double carLength, double units){
		return units * carLength * Settings.CAR_LENGTH_TO_UNITS;
	}
	
	public static void main(String[] args){
		MainFrame.init("Autorennen", 1000, 600);
		//MainFrame.getFrame().changeState(new Strasse(2000, new ArrayList<Auto>(){{add(new Auto(Color.RED, "test1", KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S));add(new Auto(Color.BLUE, "test2", KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_K));add(new Auto(Color.LIGHT_GRAY, "test3", KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN));}}));
		MainFrame.getFrame().changeState(new Menu.MainMenu());
		MainFrame.getFrame().start();
	}
	
	
}
