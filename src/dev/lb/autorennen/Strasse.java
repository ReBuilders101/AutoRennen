package dev.lb.autorennen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Strasse implements GameState, KeyListener{

	private int scrollDistance;
	private int goalDistance;
	private int countDown, timer, deadCD;
	private List<Auto> playerList;
	private Menu returnTo;
	private Map<Auto,Number> times;
	private boolean raceOver;
	
	public Strasse(int distance, List<Auto> players, Menu returnto){
		goalDistance = distance;
		scrollDistance = 0;
		countDown = 8 * Settings.TPS;
		playerList = players;
		returnTo = returnto;
		times = new HashMap<>();
		for(Auto player : playerList){
			player.reset();
			player.playStartCutscene();
			times.put(player, Double.NaN);
		}
	}
	
	@Override
	public void tick() {
		
		Auto first = null;
		
		for(Auto player : playerList){
			player.handleTick();
			if(player.getDistance() > goalDistance && player.isEngineOn()){ //Player finishes race
				player.turnOffFinal(400);
				player.damage();
				times.put(player, timer);
			}
			if(first == null){
				first = player;
			}else if(player.getDistance() > first.getDistance()){
				first = player;
			}
			//Player dies when offscreen
			if(player.getDistance() - scrollDistance < -200 && player.canBeTurnedOn()){
				player.turnOffLose();
				player.damage();
			}
			
			//Kill the engine and reset if you start too early
			if(countDown > Settings.TPS && player.getDistance() > 10){
				player.turnOff();
				player.damage();
				player.setDistance(0);
				player.setSpeed(0);
			}
		}
		
		int screenWidthUnits = (int) (700 + playerList.size() * 377.25);
		//If the player is more than half way over the screen, scroll
		if(first.getDistance() - scrollDistance > (screenWidthUnits / 2) && goalDistance - scrollDistance > (screenWidthUnits * 2 / 3)){//except when the goal is on screen enough
			scrollDistance += first.getDistance() - scrollDistance - (screenWidthUnits / 2); 
		}
		
		if(countDown < Settings.TPS){
			timer++; //Increase totel time after countdown
		}
		if(countDown > 0){
			countDown--;
		}
		
		if(!raceOver){
			//If all players are dead
			boolean dead = true;
			for(Auto player : playerList){
				if(!player.isDone()){
					dead = false;
					break;
				}
			}
			if(dead){
				raceOver = true;
			}
		}else{
			deadCD++;
		}
		
		if(deadCD == Settings.TPS * 3){ //End race, show results
			MainFrame.getFrame().changeState(new Menu.ResultMenu(returnTo, times));
		}
	}

	@Override
	public void paint(Graphics2D g, int width, int height) {
		//Do some scaling, height / width = 
		final double ratio = 0.4;
		final double thisRatio = ((double) height / (double) width);
		int scaleWidth, scaleHeight, startX, startY;
		if(ratio < thisRatio){//Wide
			scaleWidth = width;
			scaleHeight = (int) (width *  ratio);
			startX = 0;
			startY = (height - scaleHeight) / 2;
		}else{//Tall
			scaleWidth = (int) (height * (1D / ratio));
			scaleHeight = height;
			startX = (width - scaleWidth) / 2;
			startY = 0;
		}
		
		//Fill bg with border
		g.setColor(Settings.GRASS_COLOR); //That's a dark green
		g.fillRect(0, 0, width, height);
		//then the grass
//		g.setColor(Settings.GRASS_COLOR);
//		g.fillRect(startX, startY, scaleWidth, scaleHeight);
		//The lanes and border
		int lanes = playerList.size() + 2; //All players + the grass border
		int laneHeight = scaleHeight / lanes;
		//The grey lane bg
		g.setColor(Settings.ROAD_COLOR); //Dark gray
		g.fillRect(0, startY + laneHeight, width, laneHeight * playerList.size());
		
		//new graphics object for everything with a changed stroke
		Graphics2D g2 = (Graphics2D) g.create();
		//Start and finish
		int carLength = (int) (laneHeight / 2 * Settings.CAR_SIZE_CONVERSION);
		int scrollPixels = (int) (Settings.getPixels(carLength, scrollDistance));
		int startOffset = (int) (startX + carLength * 2.16) - scrollPixels;
		
//		System.out.println(scaleWidth / (Settings.CAR_LENGTH_TO_UNITS * carLength));
		
		g2.setStroke(new BasicStroke(laneHeight * Settings.LINE_WIDTH_CONVERSION * 5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0)); 
		g2.setColor(Settings.MARK_COLOR);
		g2.drawLine(startOffset, startY + laneHeight, startOffset, startY + laneHeight * (lanes - 1));
		g2.drawLine(startOffset + (int) (Settings.getPixels(carLength, goalDistance)), startY + laneHeight,
				startOffset + (int) (Settings.getPixels(carLength, goalDistance)), startY + laneHeight * (lanes - 1));
		//dashed stroke
		g2.setStroke(new BasicStroke(laneHeight * Settings.LINE_WIDTH_CONVERSION, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{Settings.LINE_DASH_CONVERSION * laneHeight, Settings.LINE_DASH_CONVERSION * laneHeight}, scrollPixels + (Settings.LINE_DASH_CONVERSION * laneHeight * 2) - (startX % (Settings.LINE_DASH_CONVERSION * laneHeight * 2))));
		for(int i = 0; i < playerList.size(); i++){
			//Draw car
			playerList.get(i).draw(g, startX - scrollPixels + carLength, startY + (int) (laneHeight * (i + 1.25)), laneHeight / 2);
			//And lane separator
			g2.drawLine(0, startY + (int) (laneHeight * (i + 1)), width, startY + (int) (laneHeight * (i + 1)));
		}
		
		//Now the border lines
		//reuse the old g2 object
		g2.setStroke(new BasicStroke(laneHeight * Settings.LINE_WIDTH_CONVERSION * 2)); //double width 
		g2.drawLine(0, startY + laneHeight * 1, width, startY + laneHeight * 1);
		g2.drawLine(0, startY + laneHeight * (lanes - 1), width, startY + laneHeight * (lanes - 1));
		g2.dispose();
		
		//Fog
		g.setPaint(new GradientPaint(startX - carLength * 3, 0, new Color(0x66, 0x66, 0x66, 0xFF), startX + carLength * 2, 0, new Color(0x66, 0x66, 0x66, 0x00)));
		g.fillRect(0, 0, startX + carLength * 2, height);
		g.setPaint(new GradientPaint(startX + scaleWidth - carLength * 2, 0, new Color(0x66, 0x66, 0x66, 0x00), startX + scaleWidth + carLength * 3, 0, new Color(0x66, 0x66, 0x66, 0xFF)));
		g.fillRect(startX + scaleWidth - carLength * 2, 0, width - (startX + scaleWidth - carLength * 2), height);
		
		//UI
		if(countDown > 0 && countDown < 4 * Settings.TPS){
			//a centered rectangle, 400 x 50 units
			int countDownNumber = countDown / Settings.TPS;
			double rectPxWidth = Settings.getPixels(carLength, 800);
			double rectPxHeight = Settings.getPixels(carLength, 100);
			double rectPxRound = Settings.getPixels(carLength, 50);
			Shape rect = new RoundRectangle2D.Double((width - rectPxWidth) / 2, (height - rectPxHeight) / 2, rectPxWidth, rectPxHeight, rectPxRound, rectPxRound);
			g.setFont(g.getFont().deriveFont(Settings.getFontSize((float) (rectPxHeight * 0.7))));
			g.setColor(Settings.BANNER_BG_COLOR);
			g.fill(rect);
			g.setColor(Settings.BANNER_FG_COLOR);
			Settings.drawCenteredString(g, countDownNumber == 0 ? "GO!" : countDownNumber + "!", rect.getBounds());
		}
		//Timer in the upper right color
		double timerSeconds = Math.round(timer * 1000 / Settings.TPS) / 1000D;
		String secondsString = String.format("Zeit: %.3f s", timerSeconds);
		
		double rectPxWidth = Settings.getPixels(carLength, 250);
		double rectPxHeight = Settings.getPixels(carLength, 50);
		double rectPxRound = Settings.getPixels(carLength, 10);
		double rectPxOffset = Settings.getPixels(carLength, 25);
		Shape rect = new RoundRectangle2D.Double(startX + scaleWidth - rectPxWidth - rectPxOffset, startY + rectPxOffset,
				rectPxWidth, rectPxHeight, rectPxRound, rectPxRound);
		g.setFont(g.getFont().deriveFont(Settings.getFontSize((float) (rectPxHeight * 0.4))));
		g.setColor(Settings.BANNER_BG_COLOR);
		g.fill(rect);
		g.setColor(Settings.BANNER_FG_COLOR);
		Settings.drawCenteredString(g, secondsString, rect.getBounds());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		playerList.forEach((a) -> a.handleInput(e.getKeyCode()));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
			MainFrame.getFrame().changeState(returnTo);
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {}
}
