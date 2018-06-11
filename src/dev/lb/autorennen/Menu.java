package dev.lb.autorennen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Menu implements GameState, KeyListener{

	protected Menu parent;
	protected String[] menuItems;
	protected int selectedIndex;
	private String id;
	
	public Menu(String id, Menu parent, String...items){
		this.menuItems = items;
		this.parent = parent;
		this.id = id;
		this.selectedIndex = 0;
	}
	
	@Override
	public void tick() {}
	
	public abstract void handleSelection(int index);
	public abstract void handleReturn(String idFrom, Object data);
	public abstract Object getReturnData();
	
	@Override
	public void paint(Graphics2D g, int width, int height) {
		// This is basically the same as in Straße
		int rows = menuItems.length + 2;
		int rowHeight = height / rows;
		
		g.setColor(Settings.ROAD_COLOR);
		g.fillRect(0, 0, width, height);
		
		//The menu entries should be half as wide as the screen
		for(int i = 0; i < menuItems.length; i++){
			g.setColor(i == selectedIndex ? Settings.BANNER_FG_COLOR : Settings.BANNER_BG_COLOR);
			g.fillRoundRect(width / 6, (int) (rowHeight * (i + 1.25)), width * 2 / 3, rowHeight / 2, 10, 10);
			g.setFont(g.getFont().deriveFont(Settings.getFontSize(rowHeight / 3)));
			g.setColor(i == selectedIndex ? Settings.BANNER_BG_COLOR : Settings.BANNER_FG_COLOR);
			Settings.drawCenteredString(g, menuItems[i], new Rectangle(width / 6, (int) (rowHeight * (i + 1.25)), width * 2 / 3, rowHeight / 2));
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//Menu controls: WASD or Arrows, Enter or Space
		if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP){
			selectedIndex--;
			if(selectedIndex < 0) selectedIndex = menuItems.length - 1;
		}else if(e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN){
			selectedIndex++;
			if(selectedIndex >= menuItems.length) selectedIndex = 0;
		}else if(e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE){
			handleSelection(selectedIndex);
		}else if(e.getKeyCode()== KeyEvent.VK_ESCAPE){
			if(parent != null && MainFrame.getFrame() != null){
				parent.handleReturn(this.id, getReturnData());
				MainFrame.getFrame().changeState(parent);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
	
	public static class MainMenu extends Menu{

		private List<Auto> players;
		
		public MainMenu() {
			super("main", null, "Spielen", "Optionen", "Beenden");
		}

		@Override
		public void handleSelection(int index) {
			if(index == 0){
				if(players == null){
					MainFrame.getFrame().changeState(new OptionsMenu(MainMenu.this, true));
				}else{
					MainFrame.getFrame().changeState(new Strasse(Settings.MAP_LENGTH, players, this));
				}
			}else if(index == 1){
				//Submenu
				MainFrame.getFrame().changeState(new OptionsMenu(MainMenu.this, false));
			}else if(index == 2){
				MainFrame.getFrame().dispose();
				System.exit(0);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleReturn(String idFrom, Object data) {
			if(idFrom.equals("options") && data instanceof List){
				try{
					players = (List<Auto>) data;
				}catch(ClassCastException e){
					System.err.println("ClassCastException: data from options dialog is not List<Auto>");
				}
			}
		}

		@Override
		public Object getReturnData() {
			return null;
		}
		
	}
	
	public static class OptionsMenu extends Menu{

		private List<Auto> players;
		private int playerNumber;
		private int customizeNumber;
		private boolean playAfter;
		
		private static final int MAX_PLAYERS = 5;
		
		public OptionsMenu(Menu parent, boolean playAfter) {
			super("options", parent, "< Spieler: 2 >", "< Spieler einstellen: 1 >");
			players = new ArrayList<>();
			players.add(new Auto(Color.RED, "Spieler 1", KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_S));
			players.add(new Auto(Color.BLUE, "Spieler 2", KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_K));
			players.add(new Auto(Color.GREEN, "Spieler 3", KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN));
			players.add(new Auto(Color.ORANGE, "Spieler 4", KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD5));
			players.add(new Auto(Color.CYAN, "Spieler 5", KeyEvent.VK_F, KeyEvent.VK_H, KeyEvent.VK_G));
			playerNumber = 2;
			customizeNumber = 1;
			this.playAfter = playAfter;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleSelection(int index) {
			if(index == 0){
				parent.handleReturn(super.id, (List<Auto>) getReturnData());
				if(playAfter){
					MainFrame.getFrame().changeState(new Strasse(Settings.MAP_LENGTH, (List<Auto>) getReturnData(), parent));
				}else{
					MainFrame.getFrame().changeState(parent);
				}
			}else if(index == 1){
				//Customize menu
				MainFrame.getFrame().changeState(new Menu.CustomizeMenu(OptionsMenu.this, players.get(customizeNumber - 1)));
			}
		}

		@Override
		public void handleReturn(String idFrom, Object data) {}

		@Override
		public Object getReturnData() {
			return players.subList(0, playerNumber);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);
			if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A){
				if(selectedIndex == 0){
					if(playerNumber > 1){
						playerNumber--;
						menuItems[0] = "< Spieler: " + playerNumber + " >";
						if(customizeNumber > playerNumber){
							customizeNumber = playerNumber;
							menuItems[1] = "< Spieler einstellen: " + customizeNumber + " >";
						}
					}
				}else{
					if(customizeNumber > 1){
						customizeNumber--;
						menuItems[1] = "< Spieler einstellen: " + customizeNumber + " >";
					}
				}
			}else if(e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D){
				if(super.selectedIndex == 0){
					if(playerNumber < MAX_PLAYERS){
						playerNumber++;
						menuItems[0] = "< Spieler: " + playerNumber + " >";
					}
				}else{
					if(customizeNumber < playerNumber){
						customizeNumber++;
						menuItems[1] = "< Spieler einstellen: " + customizeNumber + " >";
					}
				}
			}
		}
	}
	
	public static class CustomizeMenu extends Menu{

		private Auto player;
		private boolean awaitKey, awaitText;
		
		public CustomizeMenu(Menu parent, Auto player) {
			super("custom", parent, "Name: '" + player.getName() + "'",
					"Erste Taste: " + KeyEvent.getKeyText(player.getFirstChar()),
					"Zweite Taste: " + KeyEvent.getKeyText(player.getSecondChar()),
					"Starttaste: " + KeyEvent.getKeyText(player.getEngineStartChar()));
			this.player = player;
		}

		@Override
		public void handleSelection(int index) {
			if(index == 0){ //Name
				awaitText = true;
				menuItems[0] = "";
			}else{
				awaitKey = true;
				menuItems[index] = "...";
			}
		}

		@Override
		public void handleReturn(String idFrom, Object data) {}

		@Override
		public Object getReturnData() {
			return player;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if(awaitKey){
				if(selectedIndex == 1){
					player.setFirstChar(e.getKeyCode());
					menuItems[1] = "Erste Taste: " + KeyEvent.getKeyText(e.getKeyCode());
				}else if(selectedIndex == 2){
					player.setSecondChar(e.getKeyCode());
					menuItems[2] = "Zweite Taste: " + KeyEvent.getKeyText(e.getKeyCode());
				}else if(selectedIndex == 3){
					player.setEngineStartChar(e.getKeyCode());
					menuItems[3] = "Starttaste: " + KeyEvent.getKeyText(e.getKeyCode());
				}
				awaitKey = false;
			}else if(awaitText){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					awaitText = false;
					player.setName(super.menuItems[0]);
					super.menuItems[0] = "Name: '" + player.getName() + "'";
				}
			}else{
				super.keyPressed(e);
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			if(awaitText){
				super.menuItems[0] += String.valueOf(e.getKeyChar());
			}
		}
		
	}
	
	public static class ResultMenu extends Menu{

		public ResultMenu(Menu parent, Map<Auto,Number> results) {
			super("results", parent, "");
			menuItems = new String[results.size() + 1];
			menuItems[0] = "Zurück";
			results = Settings.sortByValue(results, Settings.numberComparator());
			int i = 1;
			for(Auto key : results.keySet()){
				if(Double.isNaN(results.get(key).doubleValue())){
					menuItems[i] = String.format("%d. %s: Ausgeschieden", i, key.getName());
				}else{
					menuItems[i] = String.format("%d. %s: %.3f s", i, key.getName(), results.get(key).doubleValue() / Settings.TPS);
				}
				i++;
			}
		}

		@Override
		public void handleSelection(int index) {
			if(index == 0){
				MainFrame.getFrame().changeState(parent);
			}
		}

		@Override
		public void handleReturn(String idFrom, Object data) {}

		@Override
		public Object getReturnData() {
			return null;
		}
		
	}

}
