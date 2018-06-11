package dev.lb.autorennen;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class Auto {
	
	private double speed, distance, accFor;
	private Color color;
	private String name;
	private int char1, char2, nextChar, engineChar, damageTicks;
	private boolean engine, canTurnOn, finalCutscene, startCutscene, light, flash, dead;
	
	public Auto(Color color, String name, int char1, int char2, int engineChar) {
		this.color = color;
		this.name = name;
		this.char1 = char1;
		this.char2 = char2;
		this.engineChar = engineChar;
		reset();
	}

	public void reset(){
		this.distance = 0;
		this.speed = 0;
		this.damageTicks = 0;
		this.nextChar = char1;
		this.engine = false;
		this.light = false;
		this.flash = false;
		this.dead = false;
		this.finalCutscene = false;
		this.canTurnOn = false;
		this.startCutscene = false;
	}
	
	public boolean handleInput(int input){
		return handleInput(input, false);
	}
	
	public boolean handleInput(int input, boolean force){
		if(engine || force){
			if(input == nextChar){
				//Update speed
				speed += Settings.CAR_TICK_ACC;
				if(speed > (Settings.CAR_MAX_SPEED * (finalCutscene ? 2 : 1))){
					speed = Settings.CAR_MAX_SPEED * (finalCutscene ? 2 : 1); //Double speed during final cutscene
				}
				//Change nextChar
				if(nextChar == char1){
					nextChar = char2;
				}else{
					nextChar = char1;
				}
				return true;
			}else{
				return false;
			}
		}else{
			if(input == engineChar && canTurnOn){
				engine = true;
				light = true;
				return true;
			}else{
				return false;
			}
		}
	}
	
	public void handleTick(){
		//Deceleration
		speed -= Settings.CAR_TICK_DEC;
		if(speed < 0){
			speed = 0;
		}
		distance += speed;
		
		if(finalCutscene){
			if(accFor > 0){
				handleInput(nextChar, true);
				accFor -= speed;
			}
		}
		
		if(startCutscene){
			if(distance < -175){
				handleInput(nextChar, true);
			}else if(speed == 0){ //After standing still, end cutscene
				startCutscene = false;
				distance = 0;
				canTurnOn = true;
				light = false;
			}
		}
		
		//Damage flash
		if(damageTicks > 0){
			if(damageTicks % 5 == 0) flash = !flash;
			damageTicks--;
			if(damageTicks == 0){
				flash = false;
				canTurnOn = true;
			}
		}
	}
	
	public void draw(Graphics2D g, int xStart, int y, int carHeight){
		g.setColor(flash ? Color.WHITE : color);
		//Create the car shape
		int cornerRadius = (int) (carHeight * Settings.CAR_CORNER_CONVERSION);
		int carWidth = (int) (carHeight * Settings.CAR_SIZE_CONVERSION);
		int x = (int) (xStart + distance * Settings.CAR_LENGTH_TO_UNITS * carWidth);
		RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, carWidth, carHeight, cornerRadius, cornerRadius);
		g.fill(shape);
		//clone graphics object to set clip for headlights
		Graphics2D g2 = (Graphics2D) g.create();
		g2.clip(shape);
		g2.setColor(Settings.LIGHT_COLOR);
		g2.fillRoundRect(x + carWidth - cornerRadius/2, y - cornerRadius, cornerRadius * 2, cornerRadius * 2, cornerRadius, cornerRadius);
		g2.fillRoundRect(x + carWidth - cornerRadius/2, y + carHeight - cornerRadius, cornerRadius * 2, cornerRadius * 2, cornerRadius, cornerRadius);
		g2.dispose();
		
		//Light cones
		GeneralPath cone1 = new GeneralPath();
		GeneralPath cone2 = new GeneralPath();
		cone1.moveTo(x + carWidth - cornerRadius/2, y);
		cone1.lineTo(x + carWidth * 3, y - cornerRadius);
		cone1.lineTo(x + carWidth * 3, y + cornerRadius * 2);
		cone1.lineTo(x + carWidth, y + cornerRadius);
		cone1.closePath();
		cone2.moveTo(x + carWidth - cornerRadius/2, y + carHeight); //Start low
		cone2.lineTo(x + carWidth * 3, y + carHeight + cornerRadius);
		cone2.lineTo(x + carWidth * 3, y + carHeight - cornerRadius * 2);
		cone2.lineTo(x + carWidth, y + carHeight - cornerRadius);
		cone2.closePath();
		
		Area coneClip = new Area(cone1);
		coneClip.add(new Area(cone2)); //Include both cones
		coneClip.subtract(new Area(shape)); //Dont draw on the car
		
		//Light cones if engine is on
		if(light){
			Graphics2D g3 = (Graphics2D) g.create();
//			g3.clip(coneClip);
			g3.setPaint(new GradientPaint(x + carWidth, 0, new Color(0xFF, 0xFF, 0x99, 0x33), x + carWidth * 3, 0, new Color(0xFF, 0xFF, 0x99, 0x00))); 
			g3.fill(coneClip);
			g3.dispose();
		}
		
		//The car border
		g.setColor(Color.BLACK);
		g.draw(shape);
		
		g.setColor(Settings.getComplementaryColor(color));
		g.setFont(g.getFont().deriveFont(Settings.getFontSize(carHeight * 0.3F)));
		Settings.drawCenteredString(g, name, shape.getBounds());
	}
	
	public boolean isDone(){
		return dead || finalCutscene;
	}
	
	public double getDistance() {
		return distance;
	}

	public double getSpeed(){
		return speed;
	}
	
	public Color getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public int getFirstChar() {
		return char1;
	}

	public int getSecondChar() {
		return char2;
	}

	public int getNextChar() {
		return nextChar;
	}

	public int getEngineStartChar() {
		return engineChar;
	}

	public boolean isEngineOn() {
		return engine;
	}

	public boolean canBeTurnedOn() {
		return canTurnOn;
	}

	public boolean isLightOn(){
		return light;
	}
	
	public void setDistance(double distance){
		this.distance = distance;
	}
	
	public void setSpeed(double speed){
		this.speed = speed;
	}
	
	public void turnOff(){
		engine = false;
		light = false;
	}
	
	public void turnOffFinal(double distance){
		engine = false;
		canTurnOn = false;
		//Leave light on!!!
		finalCutscene = true;
		accFor = distance;
	}
	
	public void turnOffLose(){
		engine = false;
		canTurnOn = false;
		light = false;
		dead = true;
	}
	
	public void damage(){
		damageTicks = 30;
		canTurnOn = false;
	}
	
	public void playStartCutscene(){
		distance = -398;
		speed = 3;
		light = true;
		startCutscene = true;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFirstChar(int char1) {
		this.char1 = char1;
	}

	public void setSecondChar(int char2) {
		this.char2 = char2;
	}

	public void setEngineStartChar(int engineChar) {
		this.engineChar = engineChar;
	}
	
}
