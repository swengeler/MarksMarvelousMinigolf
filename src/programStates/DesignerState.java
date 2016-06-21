package programStates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entities.obstacles.Wall;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import gameEngine.MainGameLoop;
import entities.playable.Ball;
import entities.camera.Camera;
import entities.camera.CameraDesigner;
import entities.camera.Empty;
import entities.obstacles.Entity;
import entities.lights.Light;
import entities.playable.RealBall;
import fileExplorers.FileFrame;
import fileExplorers.SaveableWorld;
import guis.GuiRenderer;
import guis.GuiButton;
import models.RawModel;
import models.TexturedModel;
import normalMapping.objConverter.NormalMappedObjLoader;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.utils.DisplayManager;
import renderEngine.utils.Loader;
import renderEngine.renderers.MasterRenderer;
import terrains.Terrain;
import terrains.World;
import textures.ModelTexture;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class DesignerState implements State{

	private static DesignerState instance;

	private Map<String, TexturedModel> tModels = new HashMap<>();
	private Map<String, ModelData> mData = new HashMap<>();
	private World world;
	private ArrayList<WaterTile> waterTiles;
	private ArrayList<GuiButton> guis;
	private Loader loader;

	private Camera camera;
	private Vector3f previousPosition = new Vector3f();
	private float previousPitch, previousYaw, previousDistance;
	private boolean birdsEyeView;

	private MasterRenderer renderer;
	private WaterRenderer waterRenderer;
	private GuiRenderer guiRenderer;
	private MousePicker picker;

	private ArrayList<Ball> balls = new ArrayList<>();

	private ArrayList<ParticleSystem> particles = new ArrayList<>();

	private WaterFrameBuffers fbos;

	private ArrayList<Wall> tempWalls = new ArrayList<>();
	private Vector2f firstPoint = new Vector2f(), secondPoint = new Vector2f();
	private boolean firstPointSelected;
	private final float SNAP_RADIUS = 5f;

	private boolean water = false;
	private boolean particle = true;
	private boolean shadow = true;
	private boolean normalMap = true;

	private long lastInput;

	public DesignerState(Loader loader){
		instance = this;
		init(loader);
	}

	public static DesignerState getInstance() {
		return instance;
	}

	@Override
	public void init(Loader loader) {
		this.loader = loader;
		loadModels();
		loadGuis();
		createBall(new Vector3f(Terrain.getSize()/2f, 30, Terrain.getSize()/2f));
		camera = new CameraDesigner(balls.get(0));
		world = new World(camera);
		loadLights();
		renderer = new MasterRenderer(loader, camera);
		loadWater();
		//System.out.println("Before loading particle system for the first time in designerstate");
		loadParticleSystem();
		//System.out.println("After loading particle system for the first time in designerstate");

		createTerrain(0, 0, "grass", false);
		createBoundingWall();
		createWaterTile(Terrain.getSize()/2f, Terrain.getSize()/2f, -8f);
		picker = new MousePicker(camera, renderer.getProjectionMatrix(), world);
		//createEntity("dragon", new Vector3f(100, getWorld().getHeightOfTerrain(100, 60), 60), -10f, 170f, 0f, 3 );
		//ParticleSystem system = createParticleSystem("fire", 8, 200, 30, -0.3f, 1.5f, 8.6f, new Vector3f(113,getWorld().getHeightOfTerrain(100, 60) + 21.3f,57));
		//system.setLifeError(0.1f);
		//system.setScaleError(0.5f);
		//system.setSpeedError(0.25f);
		//system.randomizeRotation();
		//system.setDirection(new Vector3f(1,0,0), 0.1f);

		//createBall(new Vector3f(0,0,20));
		//currBall = 1;
		//setCameraToBall(currBall);
		//createTerrain(0, 0, "grass", true);


	}

	@Override
	public void renderScreen() {
		if(shadow){
			renderer.renderShadowMap(world.getEntities(), world.getLights().get(0));
		}
		if(water){
			//Rendering on reflection buffer
			fbos.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - waterTiles.get(0).getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			for(Ball b : balls)
				if (b instanceof RealBall)
					renderer.processEntity((RealBall)b);
			renderer.processWorld(world, new Vector4f(0, 1, 0, - waterTiles.get(0).getHeight()), normalMap);
			if (particle)
				ParticleMaster.renderParticles(camera);
			camera.getPosition().y += distance;
			camera.invertPitch();

			//Rendering on refraction buffer
			fbos.bindRefractionFrameBuffer();
			for (Ball b : balls)
				if (b instanceof RealBall)
					renderer.processEntity((RealBall)b);
			renderer.processWorld(world, new Vector4f(0, -1, 0, waterTiles.get(0).getHeight()), normalMap);
			if(particle)
				ParticleMaster.renderParticles(camera);
			fbos.unbindCurrentFrameBuffer();
		}

		for (Ball b : balls){
			if (b instanceof RealBall)
				renderer.processEntity((RealBall)b);
		}
		renderer.processWorld(world, new Vector4f(0, -1, 0, 10000), false);
		if(water)
			waterRenderer.render(waterTiles, camera);
		//ParticleMaster.renderParticles(camera);
		//ParticleMaster.update(camera);
		guiRenderer.renderButtons(guis);

	}

	@Override
	public void checkInputs() {
		balls.get(0).checkInputs();

		// commands for the camera
		if ((System.currentTimeMillis() - lastInput > 200) && Keyboard.isKeyDown(Keyboard.KEY_M)) {
			if (!birdsEyeView) {
				previousPosition.set(camera.getPosition());
				previousPitch = camera.getPitch();
				previousYaw = camera.getYaw();
				previousDistance = camera.getDistanceFromBall();
				camera.setBirdsEyeView();
				birdsEyeView = true;
			} else {
				camera.set(previousPosition, previousPitch, previousYaw, previousDistance);
				birdsEyeView = false;
			}
			lastInput = System.currentTimeMillis();
		}

		// gui interactions (only back to main menu button right now)
		if (Mouse.isButtonDown(0)) {
			for (GuiButton button : guis){
				if (button.isInside(new Vector2f(Mouse.getX(), Mouse.getY()))){
					button.click();
				}
			}
		}

		// special undo command
		if ((System.currentTimeMillis() - lastInput > 200) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_Z)) {
			world.removeLastEntity();
			lastInput = System.currentTimeMillis();
		}

		// commands for building the course
		if ((System.currentTimeMillis() - lastInput > 200) && Keyboard.isKeyDown(Keyboard.KEY_B) && loader.getVBOs() <= 350 && picker.getCurrentTerrainPoint() != null) {
			// determine whether a different wall's endpoint is in the vicinity, if so snap to it to connect the walls
			float x = picker.getCurrentTerrainPoint().x;
			float y = picker.getCurrentTerrainPoint().z;
			System.out.println("Picker x = " + x + ", y = " + y);
			for (Wall w : tempWalls) {
				if (Math.pow(w.getP1().x - x, 2) + Math.pow(w.getP1().y - y, 2) < SNAP_RADIUS * SNAP_RADIUS) {
					x = w.getP1().x;
					y = w.getP1().y;
					break;
				} else if (Math.pow(w.getP2().x - x, 2) + Math.pow(w.getP2().y - y, 2) < SNAP_RADIUS * SNAP_RADIUS) {
					x = w.getP2().x;
					y = w.getP2().y;
					break;
				}
			}
			// set a starting or end point based on the current coordinates of the mouse pointer
			if (firstPointSelected) {
				secondPoint.set(x, y);
				createWall(firstPoint, secondPoint);
				firstPointSelected = false;
			} else {
				firstPoint.set(x, y);
				firstPointSelected = true;
			}
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput > 200) && Keyboard.isKeyDown(Keyboard.KEY_Q) && loader.getVBOs() <= 350 && picker.getCurrentTerrainPoint() != null) {
			world.getTerrains().get(0).updateTerrain(loader, ((picker.getCurrentTerrainPoint().x / (Terrain.getSize()/2)) * (world.getTerrains().get(0).getVertexCount()/2)), ((picker.getCurrentTerrainPoint().z / (Terrain.getSize()/2)) * (world.getTerrains().get(0).getVertexCount()/2)));
			for (Entity e:world.getEntities()) {
				float x = e.getPosition().x;
				float z = e.getPosition().z;
				float y = getWorld().getHeightOfTerrain(x, z);
				e.setPosition(new Vector3f(x, y, z));
			}
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput > 200) && Keyboard.isKeyDown(Keyboard.KEY_R) && !world.hasStart() && picker.getCurrentTerrainPoint() != null) {
			createNotCollidingEntity("disk", 0, new Vector3f(picker.getCurrentTerrainPoint().x, getWorld().getHeightOfTerrain(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z), picker.getCurrentTerrainPoint().z), 0f, 0f, 0f, 1);
			world.setStart(new Vector2f(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z));
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput > 200) && Keyboard.isKeyDown(Keyboard.KEY_T) && !world.hasEnd() && picker.getCurrentTerrainPoint() != null) {
			createNotCollidingEntity("flag", 0, new Vector3f(picker.getCurrentTerrainPoint().x, getWorld().getHeightOfTerrain(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z), picker.getCurrentTerrainPoint().z), 0f, 45f, 0f, 5);
			world.setEnd(new Vector2f(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z));
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput > 200) && loader.getVBOs() <= 350 && Keyboard.isKeyDown(Keyboard.KEY_F ) && picker.getCurrentTerrainPoint() != null) {
			createEntity("tree", new Vector3f(picker.getCurrentTerrainPoint().x, getWorld().getHeightOfTerrain(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z), picker.getCurrentTerrainPoint().z), 0f, 0f, 0f, 10);
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput > 200) && loader.getVBOs() <= 350 && Keyboard.isKeyDown(Keyboard.KEY_G) && picker.getCurrentTerrainPoint() != null) {
			createEntity("box", new Vector3f(picker.getCurrentTerrainPoint().x, getWorld().getHeightOfTerrain(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z), picker.getCurrentTerrainPoint().z), 0f, 0f, 0f, 7);
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput > 200) && loader.getVBOs() <= 350 && Keyboard.isKeyDown(Keyboard.KEY_U) && picker.getCurrentTerrainPoint() != null) {
			createEntity("ramp", new Vector3f(picker.getCurrentTerrainPoint().x, getWorld().getHeightOfTerrain(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z), picker.getCurrentTerrainPoint().z), 0f, 180f, 0f, 5);
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput > 200) && loader.getVBOs() <= 350 && Keyboard.isKeyDown(Keyboard.KEY_Z) && !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && picker.getCurrentTerrainPoint() != null) {
			createEntity("rampWHole", new Vector3f(picker.getCurrentTerrainPoint().x, getWorld().getHeightOfTerrain(picker.getCurrentTerrainPoint().x, picker.getCurrentTerrainPoint().z), picker.getCurrentTerrainPoint().z), 0f, 180f, 0f, 5);
			lastInput = System.currentTimeMillis();
		}
		if (world.hasStart() && world.hasEnd() && Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			MainGameLoop.loadGame(world, 2);
		}

		// commands for saving and loading courses
		if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
			saveWorld();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
			loadWorld();
		}
	}

	public void saveWorld() {
		SaveableWorld tmpWorld = new SaveableWorld(world);
		FileFrame tmpFrame = new FileFrame("save", tmpWorld);
		tmpFrame.setVisible();
	}

	public void loadWorld() {
		SaveableWorld tmpWorld = null;
		FileFrame tmpFrame = new FileFrame("load", tmpWorld);
		tmpWorld = tmpFrame.returnWorld();
		if (tmpWorld != null) {
			world = new World(camera);
			world.addEntities(tmpWorld.getEntities());
			world.addLights(tmpWorld.getLights());
			world.addNormE(tmpWorld.getNormEntities());
			world.add(tmpWorld.getTerrains().get(0));
			createTerrain(0, 0, "grass", world.getTerrains().get(0).getHeights());
			world.setStart(tmpWorld.getStart());
			world.setEnd(tmpWorld.getEnd());
			world.setHasStart(tmpWorld.hasStart());
			world.setHasEnd(tmpWorld.hasEnd());
			tmpFrame.setVisible();
			DisplayManager.reset();
		} else System.out.println("Something happened...");
	}

	public void setCameraToBall(int index){
		camera = new Camera(balls.get(index));
		renderer.updateCamera(camera);
		world.setCamera(camera);
	}

	@Override
	public void update() {
		//mainEngine.tick();
		picker.update();
		camera.move();
		for(ParticleSystem system:particles)
			system.generateParticles();
	}

	@Override
	public void cleanUp() {
		fbos.cleanUp();
		guiRenderer.cleanUp();
		waterRenderer.getShader().cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		ParticleMaster.cleanUp();
	}

	public Terrain createTerrain(int gridX, int gridY, String texName, boolean rand){
		Terrain t = new Terrain(gridX, gridY, loader, new ModelTexture(loader.loadTexture(texName)), rand);
		world.add(t);
		return t;
	}

	public Terrain createTerrain(int gridX, int gridY, String texName, String heightMap){
		Terrain t = new Terrain(gridX, gridY, loader, new ModelTexture(loader.loadTexture(texName)), heightMap);
		world.add(t);
		return t;
	}

	public Terrain createTerrain(int gridX, int gridY, String texName, float[][] height){
		long before = System.currentTimeMillis();
		Terrain t = new Terrain(gridX, gridY, loader, new ModelTexture(loader.loadTexture(texName)), height, new Vector2f(world.getEnd().x, world.getEnd().z));
		world.removeTerrain();
		world.add(t);
		//System.out.println("Loading terrain: " + (System.currentTimeMillis() - before) + "ms");
		return t;
	}

	public WaterTile createWaterTile(float tileCenterX, float tileCenterZ, float tileHeight){
		if(waterTiles == null && water){
			waterTiles = new ArrayList<WaterTile>();
			waterTiles.add(new WaterTile(tileCenterX, tileCenterZ, tileHeight));
			return waterTiles.get(0);
		} else {
			//System.out.println("Water disabled or already existent");
			return null;
		}
	}

	private void loadParticleSystem() {
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
	}

	private void loadWater() {
		fbos = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
	}

	private void loadGuis() {
		guiRenderer = new GuiRenderer(loader);
		guis = new ArrayList<>();
		guis.add(new GuiButton("main_menu", new Vector2f(105, 855), new Vector2f(0.2f, 0.2f), loader, "main_menu", null));
		guis.add(new GuiButton("save", new Vector2f(59, 810), new Vector2f(0.2f, 0.2f), loader, "save", null));
		guis.add(new GuiButton("load", new Vector2f(59, 765), new Vector2f(0.2f, 0.2f), loader, "load", null));
		//guis.add(new GuiButton("overlay", new Vector2f(1510, 510), new Vector2f(0.6f, 0.5f), loader, "overlay", null));
	}

	private void loadModels() {
		ModelData human = OBJFileLoader.loadOBJ("person");
		ModelData ball = OBJFileLoader.loadOBJ("ball_centred_high_scaled2");
		ModelData tree = OBJFileLoader.loadOBJ("tree");
		ModelData fern = OBJFileLoader.loadOBJ("fern");
		ModelData grass = OBJFileLoader.loadOBJ("grassModel");
		ModelData pine = OBJFileLoader.loadOBJ("pine");
		ModelData flower = OBJFileLoader.loadOBJ("grassModel");
		ModelData box = OBJFileLoader.loadOBJ("box");
		ModelData dragon = OBJFileLoader.loadOBJ("dragon");
		ModelData empty = OBJFileLoader.loadOBJ("empty");
		ModelData disk = OBJFileLoader.loadOBJ("disk");
		ModelData flag = OBJFileLoader.loadOBJ("flag");
		ModelData wall = OBJFileLoader.loadOBJ("wall");
	    ModelData dragon_low = OBJFileLoader.loadOBJ("dragon_low_test");
	    ModelData hole = OBJFileLoader.loadOBJ("holeObstacle");
		ModelData wall_seg = OBJFileLoader.loadOBJ("wall_seg");
		ModelData ramp = OBJFileLoader.loadOBJ("ramp");
		ModelData rampWHole = OBJFileLoader.loadOBJ("rampWHole");

		mData.put("human", human);
	    mData.put("ball", ball);
	    mData.put("tree", tree);
	    mData.put("fern", fern);
	    mData.put("grass", grass);
	    mData.put("pine", pine);
	    mData.put("flower", flower);
	    mData.put("box", box);
	    mData.put("dragon", dragon);
	    mData.put("wall", wall);
	    mData.put("dragon_low", dragon_low);
	    mData.put("flag", flag);
	    mData.put("hole", hole);
	    mData.put("wall_seg", wall_seg);
		mData.put("ramp", ramp);
		mData.put("rampWHole", rampWHole);

		RawModel humanModel = loader.loadToVAO(human.getVertices(), human.getTextureCoords(), human.getNormals(), human.getIndices());
		RawModel ballModel = loader.loadToVAO(ball.getVertices(), ball.getTextureCoords(), ball.getNormals(), ball.getIndices());
		RawModel treeModel = loader.loadToVAO(tree.getVertices(), tree.getTextureCoords(), tree.getNormals(), tree.getIndices());
		RawModel fernModel = loader.loadToVAO(fern.getVertices(), fern.getTextureCoords(), fern.getNormals(), fern.getIndices());
		RawModel grassModel = loader.loadToVAO(grass.getVertices(), grass.getTextureCoords(), grass.getNormals(), grass.getIndices());
		RawModel pineModel = loader.loadToVAO(pine.getVertices(), pine.getTextureCoords(), pine.getNormals(), pine.getIndices());
		RawModel boxModel = loader.loadToVAO(box.getVertices(), box.getTextureCoords(), box.getNormals(), box.getIndices());
		RawModel flowerModel = loader.loadToVAO(flower.getVertices(), flower.getTextureCoords(), flower.getNormals(), flower.getIndices());
		RawModel dragonModel = loader.loadToVAO(dragon.getVertices(), dragon.getTextureCoords(), dragon.getNormals(), dragon.getIndices());
		RawModel emptyModel = loader.loadToVAO(empty.getVertices(), empty.getTextureCoords(), empty.getNormals(), empty.getIndices());
		RawModel diskModel = loader.loadToVAO(disk.getVertices(), disk.getTextureCoords(), disk.getNormals(), disk.getIndices());
		RawModel flagModel = loader.loadToVAO(flag.getVertices(), flag.getTextureCoords(), flag.getNormals(), flag.getIndices());
		RawModel holeModel = loader.loadToVAO(hole.getVertices(), hole.getTextureCoords(), hole.getNormals(), hole.getIndices());
		RawModel wallModel = loader.loadToVAO(wall.getVertices(), wall.getTextureCoords(), wall.getNormals(), wall.getIndices());
		RawModel dragonLowModel = loader.loadToVAO(dragon_low.getVertices(), dragon_low.getTextureCoords(), dragon_low.getNormals(), dragon_low.getIndices());
		RawModel wallSegModel = loader.loadToVAO(wall_seg.getVertices(), wall_seg.getTextureCoords(), wall_seg.getNormals(), wall_seg.getIndices());
		RawModel rampModel = loader.loadToVAO(ramp.getVertices(), ramp.getTextureCoords(), ramp.getNormals(), ramp.getIndices());
		RawModel rampWHoleModel = loader.loadToVAO(rampWHole.getVertices(), rampWHole.getTextureCoords(), rampWHole.getNormals(), rampWHole.getIndices());


		tModels.put("human", new TexturedModel(humanModel,new ModelTexture(loader.loadTexture("playerTexture"))));
		tModels.put("ball", new TexturedModel(ballModel,new ModelTexture(loader.loadTexture("white"))));
		tModels.put("tree", new TexturedModel(treeModel,new ModelTexture(loader.loadTexture("tree"))));
		tModels.put("fern", new TexturedModel(fernModel,new ModelTexture(loader.loadTexture("fernAtlas"))));
		tModels.put("grass", new TexturedModel(grassModel,new ModelTexture(loader.loadTexture("grassTexture"))));
		tModels.put("pine", new TexturedModel(pineModel,new ModelTexture(loader.loadTexture("pine"))));
		tModels.put("box", new TexturedModel(boxModel,new ModelTexture(loader.loadTexture("box"))));
		tModels.put("flower", new TexturedModel(flowerModel,new ModelTexture(loader.loadTexture("flower"))));
		tModels.put("barrel", new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader), new ModelTexture(loader.loadTexture("barrel"))));
		tModels.put("crate", new TexturedModel(NormalMappedObjLoader.loadOBJ("crate", loader), new ModelTexture(loader.loadTexture("crate"))));
		tModels.put("boulder", new TexturedModel(NormalMappedObjLoader.loadOBJ("boulder", loader), new ModelTexture(loader.loadTexture("boulder"))));
		tModels.put("dragon", new TexturedModel(dragonModel,new ModelTexture(loader.loadTexture("white"))));
		tModels.put("empty", new TexturedModel(emptyModel, new ModelTexture(loader.loadTexture("flower"))));
		tModels.put("disk", new TexturedModel(diskModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("flag", new TexturedModel(flagModel, new ModelTexture(loader.loadTexture("flag"))));
		tModels.put("wall", new TexturedModel(wallModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("hole", new TexturedModel(holeModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("dragon_low", new TexturedModel(dragonLowModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("wall_seg", new TexturedModel(wallSegModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("ramp", new TexturedModel(rampModel, new ModelTexture(loader.loadTexture("skull"))));
		tModels.put("rampWHole", new TexturedModel(rampWHoleModel, new ModelTexture(loader.loadTexture("white"))));


		tModels.get("barrel").getTexture().setShineDamper(10);
		tModels.get("barrel").getTexture().setReflectivity(0.3f);
		tModels.get("barrel").getTexture().setNormalMap(loader.loadTexture("barrelNormal"));

		tModels.get("barrel").getTexture().setShineDamper(10);
		tModels.get("barrel").getTexture().setReflectivity(0.3f);
		tModels.get("barrel").getTexture().setNormalMap(loader.loadTexture("crateNormal"));

		tModels.get("boulder").getTexture().setShineDamper(10);
		tModels.get("boulder").getTexture().setReflectivity(0.3f);
		tModels.get("boulder").getTexture().setNormalMap(loader.loadTexture("boulderNormal"));

		tModels.get("ball").getTexture().setShineDamper(10);
		tModels.get("ball").getTexture().setReflectivity(1);

	}

	private void loadLights(){
		List<Light> lights = new ArrayList<>();
		lights.add(new Light(new Vector3f(1000000, 1500000, -1000000), new Vector3f(1f, 1f, 1f)));
		world.addLights(lights);
	}

	public Entity createWall(Vector2f p1, Vector2f p2) {
		System.out.println("Create wall between " + p1 + " and " + p2);
		Entity e = new Wall(p1, p2, tModels.get("wall_seg"), 0, mData.get("wall_seg"));
		world.add(e);
		tempWalls.add((Wall) e);
		return e;
	}

	public void createBoundingWall() {
		Vector2f p1 = new Vector2f(0, 0), p2 = new Vector2f(0, Terrain.getSize() - 0);
		Vector2f p3 = new Vector2f(0, 3), p4 = new Vector2f(3, 0);
		createWall(p1, p2);
		//createWall(p3, p4);
		p1.set(Terrain.getSize() - 0, Terrain.getSize());
		p2.set(0, Terrain.getSize());
		p3.set(0, Terrain.getSize() - 3);
		p4.set(3, Terrain.getSize());
		createWall(p1, p2);
		//createWall(p3, p4);
		p1.set(Terrain.getSize(), Terrain.getSize() - 0);
		p2.set(Terrain.getSize(), 0);
		p3.set(Terrain.getSize() - 3, Terrain.getSize());
		p4.set(Terrain.getSize(), Terrain.getSize() - 3);
		createWall(p1, p2);
		//createWall(p3, p4);
		p1.set(0, 0);
		p2.set(Terrain.getSize() - 0, 0);
		p3.set(Terrain.getSize(), 3);
		p4.set(Terrain.getSize() - 3, 0);
		createWall(p1, p2);
		//createWall(p3, p4);
	}

	public Entity createEntity(String eName, Vector3f position, float rotX, float rotY, float rotZ, float scale){
		Entity e = new Entity(tModels.get(eName), 0, mData.get(eName), position, rotX, rotY, rotZ, scale, eName);
		world.add(e);
		return e;
	}

	public Entity createEntity(String eName, int a,  Vector3f position, float rotX, float rotY, float rotZ, float scale){
		Entity e = new Entity(tModels.get(eName), a, mData.get(eName), position, rotX, rotY, rotZ, scale);
		world.add(e);
		return e;
	}

	public Entity createNotCollidingEntity(String eName, int a,  Vector3f position, float rotX, float rotY, float rotZ, float scale){
		Entity e = new Entity(tModels.get(eName), a, position, rotX, rotY, rotZ, scale, false);
		world.add(e);
		return e;
	}

	public Entity createNormalMapEntity(String eName, int a,  Vector3f position, float rotX, float rotY, float rotZ, float scale){
		if(normalMap){
			Entity e = new Entity(tModels.get(eName), 0, mData.get(eName),position, rotX, rotY, rotZ, scale);
			e.getModel().getTexture().setNormalMap(loader.loadTexture(eName + "Normal"));
			world.addNE(e);
			return e;
		}
		System.out.println("Normal maps disabled");
		return null;
	}

	public Ball createBall(Vector3f position){
		Empty b = new Empty(tModels.get("empty"), position, 0f, 0f, 0f, 1f);
		balls.add(b);
		return b;
	}

	public ParticleSystem createParticleSystem(String texName, int texRows, float pps, float speed, float gravityComplient, float lifeLength, float scale, Vector3f systemCenter){
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture(texName), texRows);
		ParticleSystem system = new ParticleSystem(particleTexture, pps, speed, gravityComplient, lifeLength, scale, systemCenter);
		particles.add(system);
		return system;
	}

	public World getWorld() {
		return world;
	}

}
