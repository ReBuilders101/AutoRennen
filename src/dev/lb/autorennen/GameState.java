package dev.lb.autorennen;

import java.awt.Graphics2D;

public interface GameState {
	public void tick();
	public void paint(Graphics2D g, int width, int height);
	
	public static GameState getEmptyState(){
		return new GameState() {
			@Override
			public void tick() {}
			@Override
			public void paint(Graphics2D g, int width, int height) {}
		};
	}
}
