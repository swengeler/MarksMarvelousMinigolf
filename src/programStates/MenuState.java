package programStates;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import entities.camera.Camera;
import guis.GuiButton;
import guis.GuiRenderer;
import particles.ParticleSystem;
import renderEngine.utils.DisplayManager;
import renderEngine.utils.Loader;
import renderEngine.renderers.MasterRenderer;
import water.WaterFrameBuffers;
import water.WaterRenderer;

public class MenuState implements State {
	
	private final float disW = DisplayManager.getWidth();
	private final float disH = DisplayManager.getHeight();
	
	private ArrayList<GuiButton> guis;
	private Camera camera;
	private Loader loader;
	
	private MasterRenderer renderer;
	private WaterRenderer waterRenderer;
	private GuiRenderer guiRenderer;
	
	private ArrayList<ParticleSystem> particles = new ArrayList<ParticleSystem>();
	
	private WaterFrameBuffers fbos;
	
	public MenuState(Loader loader){
		init(loader);
	}
	
	private void loadGuis() {
		guiRenderer = new GuiRenderer(loader);
		guis = new ArrayList<GuiButton>();
		loadMainMenu();
	}

	@Override
	public void renderScreen() {
		guiRenderer.renderButtons(guis);

	}

	@Override
	public void checkInputs() {
		if(Mouse.isButtonDown(0)){
			System.out.println("X=" + Mouse.getX() + ", Y=" + Mouse.getY());
			for(GuiButton button:guis){
				if(button.isInside(new Vector2f(Mouse.getX(), Mouse.getY()))){
					button.click();
				}
			}
		}
	}


	@Override
	public void init(Loader loader) {
		this.loader = loader;
		loadGuis();
	}

	@Override
	public void update() {
		
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}
	
	public GuiButton createButton(String guiTex, Vector2f position,Vector2f scale, String type){
		GuiButton button = new GuiButton(guiTex, position, scale, loader, type, this);
		guis.add(button);
		return button;
	}
	
	public void loadMainMenu(){
		guis.clear();
		createButton("title", new Vector2f(disW/2f,disH*8/10 + 55), new Vector2f(0.8f,0.8f), "static");
		createButton("play_button", new Vector2f(disW/2f + 30,disH*6/10 - 20), new Vector2f(1f,0.66f), "play");
		createButton("designer_button", new Vector2f(disW/2f + 210,disH*5/10 - 100), new Vector2f(1f,0.66f), "designer");
		createButton("options_button", new Vector2f(disW/2f + 90,disH*3/10 - 80), new Vector2f(1f,0.66f), "main_options");
		
	}
	
	public void loadOptions(){
		guis.clear();
		createButton("title", new Vector2f(disW/2f,disH*8/10 + 55), new Vector2f(0.8f,0.8f), "static");
		//createButton("play_button", new Vector2f(disW/2f + 30,disH*6/10 - 20), new Vector2f(1f,0.66f), "play");
		//createButton("designer_button", new Vector2f(disW/2f + 210,disH*5/10 - 100), new Vector2f(1f,0.66f), "designer");
		//createButton("options_button", new Vector2f(disW/2f + 90,disH*3/10 - 80), new Vector2f(1f,0.66f), "main_options");
		
	}

}
