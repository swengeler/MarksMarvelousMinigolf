package guis;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;

import gameEngine.MainGameLoop;
import programStates.MenuState;
import renderEngine.utils.DisplayManager;
import renderEngine.utils.Loader;

public class GuiButton {

	private GuiTexture texture;
	private float width;
	private float height;
	private Vector2f position;
	private String type;
	private MenuState menu;
	
	public GuiButton(String guiTex, Vector2f position, Vector2f scale, Loader loader, String type, MenuState menu) {
		this.type = type;
		this.menu = menu;
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("res/" + guiTex + ".png"));
			width = image.getWidth() * scale.x;
			height = image.getHeight() * scale.y;
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.position = position;
		float SCREEN_WIDTH = DisplayManager.getWidth();
		float SCREEN_HEIGHT = DisplayManager.getHeight();
		float pX = (position.x - SCREEN_WIDTH/2f)/(SCREEN_WIDTH/2f);
		float pY = (position.y - SCREEN_HEIGHT/2f)/(SCREEN_HEIGHT/2f);
		float scaleX = width/SCREEN_WIDTH;
		float scaleY = height/SCREEN_HEIGHT;
		//System.out.println("width=" + width + " height=" + height);
		//System.out.println("px="+pX + ",py=" + pY + ",scalex=" + scaleX + ",scaleY=" + scaleY);
		this.texture = new GuiTexture(loader.loadTexture(guiTex), new Vector2f(pX, pY), new Vector2f(scaleX, scaleY));
	}
	
	public boolean isInside(Vector2f p) {
		if (p.x > position.x - width/2f && p.y > position.y - height/2f && p.x < position.x+width/2f && p.y < position.y+height/2f)
			return true;
		return false;
	}

	public GuiTexture getTexture() {
		return texture;
	}

	public void click() {
		//System.out.println("My size is: width=" + width +", height=" + height);
		if (type.equals("main_menu")) {
			MainGameLoop.loadMenu();
		} else if (type.equals("play")) {
			MainGameLoop.loadGame(2);
		} else if (type.equals("designer")) {
			MainGameLoop.loadDesigner();
		}
	}
	
}
