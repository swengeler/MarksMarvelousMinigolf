package engineTester;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import programStates.State;
import programStates.DesignerState;
import programStates.GameState;
import programStates.MenuState;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import terrains.World;

public class MainGameLoop {
	
	private static State currState;
	private static Loader loader = new Loader();
	private static boolean update = false;
	
	private static int counter;
	
	public static void main(String[] args) {
	
		DisplayManager.createDisplay();
		currState = new MenuState(loader);
		
		DisplayManager.reset();
		
		while(!Display.isCloseRequested()){
			if(!update) {
				currState.checkInputs();
				currState.update();
				GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
				currState.renderScreen();
				counter++;

				DisplayManager.updateDisplay();
			}
		}
		currState.cleanUp();
		DisplayManager.closeDisplay();
		
	}
	
	public static void loadGame(int numberOfPlayers){
		currState.cleanUp();
		DisplayManager.closeDisplay();
		DisplayManager.createDisplay();
		currState = new GameState(loader, numberOfPlayers);
	}
	
	public static void loadGame(World world, int numberOfPlayers){
		currState.cleanUp();
		DisplayManager.closeDisplay();
		DisplayManager.createDisplay();
		currState = new GameState(loader, world, numberOfPlayers);
	}
	
	public static void loadMenu(){
		currState.cleanUp();
		DisplayManager.closeDisplay();
		DisplayManager.createDisplay();
		currState = new MenuState(loader);
	}

	
	public static void loadDesigner(){
		currState.cleanUp();
		DisplayManager.closeDisplay();
		DisplayManager.createDisplay();
		currState = new DesignerState(loader);
	}
	
	public static int getCounter() {
		return counter;
	}

	public static void gameOver() {
		currState.cleanUp();
		DisplayManager.closeDisplay();
		DisplayManager.createDisplay();
		currState = new MenuState(loader);
		
	}

}
