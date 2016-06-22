package programStates;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import artificialIntelligence.algorithms.HMPathing;
import artificialIntelligence.algorithms.MonteCarlo;
import entities.camera.Camera;
import entities.camera.Empty;
import entities.obstacles.Entity;
import entities.obstacles.RotatingEntity;
import entities.lights.Light;
import entities.obstacles.Wall;
import entities.playable.Ball;
import entities.playable.RealBall;
import guis.GuiButton;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import physics.engine.PhysicsEngine;
import artificialIntelligence.algorithms.BobTheBot;
import gameEngine.MainGameLoop;
import guis.GuiRenderer;
import models.RawModel;
import models.TexturedModel;
import normalMapping.objConverter.NormalMappedObjLoader;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import physics.noise.NoiseHandler;
import renderEngine.utils.DisplayManager;
import renderEngine.utils.Loader;
import renderEngine.renderers.MasterRenderer;
import terrains.Terrain;
import terrains.World;
import textures.ModelTexture;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class GameState implements State {

	public static Entity wmr;
	
	private static GameState instance;
	
	private Map<String,TexturedModel> tModels = new HashMap<String,TexturedModel>();
	private Map<String,ModelData> mData = new HashMap<String,ModelData>();
	private World world;
	private ArrayList<WaterTile> waterTiles;
	private ArrayList<GuiButton> guis;
	private Camera camera;
	private Loader loader;
	
	private MasterRenderer renderer;
	private WaterRenderer waterRenderer;
	private GuiRenderer guiRenderer;
	
	private PhysicsEngine mainEngine;
	
	private ArrayList<Ball> balls = new ArrayList<Ball>();
	private int currBall;
	
	private ArrayList<ParticleSystem> particles = new ArrayList<ParticleSystem>();
	
	private WaterFrameBuffers fbos;
	
	private boolean water = false;
	private boolean particle = true;
	private boolean shadow = true;
	private boolean normalMap = true;
	private int numberOfPlayers;

	private long lastInput;

	private float timeBallStill;
	
	private BobTheBot bob;

	private boolean testing;
	private int counter;
	
	public GameState(Loader loader, int numberOfPlayers) {
		instance = this;
		this.numberOfPlayers = numberOfPlayers;
		init(loader);
		DisplayManager.reset();
	}
	
	public static GameState getInstance(){
		return instance;
	}
	
	public GameState(Loader loader, World world, int numberOfPlayers) {
		instance = this;
		this.numberOfPlayers = numberOfPlayers;
		buildWithWorld(loader, world);
		DisplayManager.reset();
	}
	
	@Override
	public void init(Loader loader) {
		this.loader = loader;
		loadModels();
		loadGuis();
		createBall(new Vector3f(10f, Ball.RADIUS, 10f), true);
		camera = new Camera(balls.get(0));
		world = new World(camera);
		loadLights();
		renderer = new MasterRenderer(loader, camera);
		mainEngine = new PhysicsEngine(balls, world, null);
		loadParticleSystem();

		//createEntity("box", new Vector3f(230, 0, 20), 0, 0, 0, 10);
		//createEntity("windmill", new Vector3f(150, 0, 250), 0, 0, 0, 6);
		//createEntity("ad_column", new Vector3f(world.getStart().x, 0, world.getStart().z - 50), 0, 180, 0, 3);
		//wmr = createRotatingEntity("windmill_rot", new Vector3f(150, 45.5f, 233.5f), new Vector3f(), 6, new Vector3f());
		//createEntity("ramp", new Vector3f(50, 0, 200), 0, 180, 0, 5);
		createEntity("rampWHole", new Vector3f(100, 0, 150), 0, -135, 0, 5);
		createBoundingWall();

		createTerrain(0, 0, "grass", false);
		/*createWaterTile(Terrain.getSize()/2f, Terrain.getSize()/2f, -8f);
		createEntity("dragon", new Vector3f(100, getWorld().getHeightOfTerrain(100, 60), 60), -10f, 170f, 0f, 3 );
		ParticleSystem system = createParticleSystem("fire", 8, 200, 30, -0.05f, 1.5f, 8.6f, new Vector3f(113,getWorld().getHeightOfTerrain(100, 60) + 21.3f,57));
		system.setLifeError(0.1f);
		system.setScaleError(0.5f);
		system.setSpeedError(0.25f);
		system.randomizeRotation();
		system.setDirection(new Vector3f(1,0,0), 0.1f);*/
		//bob = new BobTheBot(0, balls.get(0), world);
		DisplayManager.reset();
	}
	
	private void buildWithWorld(Loader loader, World world) {
		this.loader = loader;
		loadModels();
		loadGuis();
		this.world = world;
		createTerrain(0, 0, "grass", world.getTerrains().get(0).getHeights());

		List<Wall> list = new ArrayList<>();
		for (Entity e : world.getEntities()) {
			if (e instanceof Wall) {
				list.add((Wall) e);
			}
		}
		for (Wall e : list) {
			world.getEntities().remove(e);
			createWall(e.getP1(), e.getP2());
		}

		createBall(new Vector3f(world.getStart()), true);
		loadLights();
		renderer = new MasterRenderer(loader, camera);
		mainEngine = new PhysicsEngine(balls, world, null);
		loadWater();
		loadParticleSystem();
		setCameraToBall(currBall);
		createEntity("hole", new Vector3f(world.getEnd().x, 0, world.getEnd().z), 0, 0, 0, 1f);
		bob = new BobTheBot(0, balls.get(0), world);
		DisplayManager.reset();
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
	
	@Override
	public void renderScreen() {
		if (shadow){
			renderer.renderShadowMap(world.getEntities(), world.getLights().get(0));
		}
		if (water){
			//Rendering on reflection buffer
			fbos.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - waterTiles.get(0).getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			for (Ball b: balls)
				if (b instanceof RealBall)
					renderer.processEntity((RealBall)b);
			renderer.processWorld(world, new Vector4f(0, 1, 0, - waterTiles.get(0).getHeight()), normalMap);
			if (particle)
				ParticleMaster.renderParticles(camera);
			camera.getPosition().y += distance;
			camera.invertPitch();
			
			//Rendering on refraction buffer
			fbos.bindRefractionFrameBuffer();
			for (Ball b: balls)
				if (b instanceof RealBall)
					renderer.processEntity((RealBall)b);
			renderer.processWorld(world, new Vector4f(0, -1, 0, waterTiles.get(0).getHeight()), normalMap);
			if (particle)
				ParticleMaster.renderParticles(camera);
			fbos.unbindCurrentFrameBuffer();
		}
		
		for (Ball b : balls)
			if (b instanceof RealBall)
				renderer.processEntity((RealBall)b);
		renderer.processWorld(world, new Vector4f(0, -1, 0, 10000), true);
		if (water)
			waterRenderer.render(waterTiles, camera);
		ParticleMaster.renderParticles(camera);
		ParticleMaster.update(camera);
		guiRenderer.renderButtons(guis);
	}
	
	@Override
	public void checkInputs() {
		balls.get(currBall).checkInputs();

		if (Mouse.isButtonDown(0)){
			for (GuiButton button : guis){
				if (button.isInside(new Vector2f(Mouse.getX(), Mouse.getY()))){
					button.click();
				}
			}
		}

		if ((System.currentTimeMillis() - lastInput) > 200 && Keyboard.isKeyDown(Keyboard.KEY_N) && currBall == 0) {
			mainEngine.setNoiseHandler(new NoiseHandler(NoiseHandler.MEDIUM, NoiseHandler.OFF, NoiseHandler.FRICTION, NoiseHandler.RESTITUTION, NoiseHandler.SURFACE_NOISE));
			System.out.println("\nTesting \"Simple\" map without obstacles\nNoise is easy\nAll tests in which the angle does not change have been performed using an angle of 5 degrees\nAll tests in which the maximum velocity does not change have been performed using the velocity 1000");
			counter = 6;
			System.out.println("\nNext play from start with counter " + counter);
			test(counter);
			System.out.println("Now using max-velocity: " + HMPathing.MAX_SHOT_POWER + " and angle: " + HMPathing.DELTA_ANGLE);

			try (FileWriter fw = new FileWriter("boxes_highamount_noisemedium-averages.txt", true); //boxes_highamount_noisemedium
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw))
			{
				out.println("Now using max-velocity: " + HMPathing.MAX_SHOT_POWER + " and angle: " + HMPathing.DELTA_ANGLE);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try (FileWriter fw = new FileWriter("boxes_highamount_noisemedium-counts.txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw))
			{
				out.println("Now using max-velocity: " + HMPathing.MAX_SHOT_POWER + " and angle: " + HMPathing.DELTA_ANGLE);
			} catch (IOException e) {
				e.printStackTrace();
			}

			bob.shoot();
			((RealBall) balls.get(currBall)).setPlayed(true);
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput) > 200 && Keyboard.isKeyDown(Keyboard.KEY_M)) {
			bob.shoot();
			((RealBall) balls.get(currBall)).setPlayed(true);
			lastInput = System.currentTimeMillis();
		}
		if ((System.currentTimeMillis() - lastInput) > 200 && Keyboard.isKeyDown(Keyboard.KEY_O)) {
			System.out.println("Ball's velocity set to something by key press");
			balls.get(currBall).setVelocity(2000, 0, 0);
			balls.get(currBall).setMoving(true);
			lastInput = System.currentTimeMillis();
		}
	}

	private void test(int c) {
		this.testing = true;

		if (c % 48 == 6 && c / 48 < 16) {
			HMPathing.DELTA_ANGLE = 6;
			HMPathing.MAX_SHOT_POWER = 100 + ((int) (c / 48)) * 200;
			((RealBall) balls.get(currBall)).setPlayed(true);
			try (FileWriter fw = new FileWriter("boxes_highamount_noisemedium-averages.txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw))
			{
				out.println();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try (FileWriter fw = new FileWriter("boxes_highamount_noisemedium-counts.txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw))
			{
				out.println();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (c % 48 == 0) {
			HMPathing.DELTA_ANGLE = 48;
			((RealBall) balls.get(currBall)).setPlayed(true);
		} else {
			HMPathing.DELTA_ANGLE = c % 48;
			((RealBall) balls.get(currBall)).setPlayed(true);
		}
	}

	public void setCameraToBall(int index){
		camera = new Camera(balls.get(index));
		renderer.updateCamera(camera);
		world.setCamera(camera);
	}

	@Override
	public void update() {
		if (mainEngine == null) {
			System.out.println("no physics");
		}
		mainEngine.tick();
		camera.move();
		for (ParticleSystem system : particles)
			system.generateParticles();
		if (!balls.get(currBall).isMoving() && ((RealBall)balls.get(currBall)).isPlayed()) {
			timeBallStill += DisplayManager.getFrameTimeSeconds();
			if (timeBallStill >= 1) {
				printScore();
				int bio = checkBallsInHole();
				if (testing) {
					if (bio >= 0) {
						balls.get(bio).setPosition(world.getStart());
						System.out.println("Average of every shot: " + (HMPathing.sum / HMPathing.count) + " over " + HMPathing.count + " (total: " + HMPathing.sum + ")");

						try (FileWriter fw = new FileWriter("boxes_highamount_noisemedium-averages.txt", true);
							 BufferedWriter bw = new BufferedWriter(fw);
							 PrintWriter out = new PrintWriter(bw))
						{
							out.println(HMPathing.sum / HMPathing.count);
						} catch (IOException e) {
							e.printStackTrace();
						}

						try (FileWriter fw = new FileWriter("boxes_highamount_noisemedium-counts.txt", true);
							 BufferedWriter bw = new BufferedWriter(fw);
							 PrintWriter out = new PrintWriter(bw))
						{
							out.println(HMPathing.count);
						} catch (IOException e) {
							e.printStackTrace();
						}

						HMPathing.sum = 0;
						HMPathing.count = 0;
						counter += 6;
						System.out.println("\nNext play from start with counter " + counter);
						test(counter);
						System.out.println("Now using max-velocity: " + HMPathing.MAX_SHOT_POWER + " and angle: " + HMPathing.DELTA_ANGLE);
						bob.shoot();
					} else {
						test(counter);
						bob.shoot();
					}
					return;
				}
				if (bio >= 0){
					System.out.print("Ball " + bio + " in hole!");
					if (numberOfPlayers == 1)
						MainGameLoop.gameOver();
					balls.remove(bio);
					mainEngine.removeBall(bio);
					numberOfPlayers--;
					if (currBall >= bio)
						currBall--;
				}
				swap();
				timeBallStill = 0;
			}
			
		}
	}
	
	private int checkBallsInHole() {
		float hx = world.getEnd().x;
		float hy = -4;
		float hz = world.getEnd().z;
		for (Ball b : balls){
			float bx = b.getPosition().x;
			float by = b.getPosition().y;
			float bz = b.getPosition().z;
			if (Math.abs(bx-hx) < 2 && Math.abs(bz-hz) < 2 && by < 0) {
				return balls.indexOf(b);
			}
		}
		return -1;
	}

	@Override
	public void cleanUp() {
		if (fbos != null)
			fbos.cleanUp();
		if (guiRenderer != null)
			guiRenderer.cleanUp();
		if (waterRenderer != null && waterRenderer.getShader() != null)
			waterRenderer.getShader().cleanUp();
		if (renderer != null)
			renderer.cleanUp();
		if (loader != null)
			loader.cleanUp();
		ParticleMaster.cleanUp();
		if (tModels != null)
			tModels.clear();
		if (mData != null)
			mData.clear();
		System.out.println("CLEANUP IN GAMESTATE CALLED");
	}
	
	public Terrain createTerrain(int gridX, int gridY, String texName, boolean rand) {
		long before = System.currentTimeMillis();
		Terrain t = new Terrain(gridX, gridY, loader, new ModelTexture(loader.loadTexture(texName)), rand);
		world.add(t);
		//System.out.println("Loading terrain: " + (System.currentTimeMillis() - before) + "ms");
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
		long before = System.currentTimeMillis();
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		//System.out.println("Loading particle system: " + (System.currentTimeMillis() - before) + "ms");
	}

	private void loadWater() {
		long before = System.currentTimeMillis();
		fbos = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		//System.out.println("Loading water: " + (System.currentTimeMillis() - before) + "ms");
	}

	private void loadGuis() {
		guiRenderer = new GuiRenderer(loader);
		guis = new ArrayList<>();
		guis.add(new GuiButton("main_menu", new Vector2f(105, 855), new Vector2f(0.2f, 0.2f), loader, "main_menu", world));
		guis.add(new GuiButton("controls", new Vector2f(1505, 700), new Vector2f(0.45f, 0.4f), loader, "overlay", null));
		guis.add(new GuiButton("save", new Vector2f(59, 810), new Vector2f(0.2f, 0.2f), loader, "save", null));
	}
	
	private void loadModels() {
		ModelData ball = OBJFileLoader.loadOBJ("ball_centred_high_scaled2");
		ModelData tree = OBJFileLoader.loadOBJ("tree");
		ModelData box = OBJFileLoader.loadOBJ("box");
		ModelData dragon = OBJFileLoader.loadOBJ("dragon");
		ModelData empty = OBJFileLoader.loadOBJ("empty");
		ModelData disk = OBJFileLoader.loadOBJ("disk");
		ModelData flag = OBJFileLoader.loadOBJ("flag");
	    ModelData hole = OBJFileLoader.loadOBJ("holeObstacle");
        ModelData ramp = OBJFileLoader.loadOBJ("ramp");
        ModelData windmill = OBJFileLoader.loadOBJ("windmill_tower2");
        ModelData windmill_rot = OBJFileLoader.loadOBJ("windmill_wings");
		ModelData ad_column = OBJFileLoader.loadOBJ("ad_column");
		ModelData wall_seg = OBJFileLoader.loadOBJ("wall_seg");
		ModelData rampWHole = OBJFileLoader.loadOBJ("rampWHole");

	    mData.put("ball", ball);
	    mData.put("tree", tree);
	    mData.put("box", box);
	    mData.put("dragon", dragon);
	    mData.put("flag", flag);
	    mData.put("hole", hole);
        mData.put("ramp", ramp);
        mData.put("rampWHole", rampWHole);
        mData.put("windmill", windmill);
        mData.put("windmill_rot", windmill_rot);
		mData.put("ad_column", ad_column);
		mData.put("wall_seg", wall_seg);

		RawModel ballModel = loader.loadToVAO(ball.getVertices(), ball.getTextureCoords(), ball.getNormals(), ball.getIndices());
		RawModel treeModel = loader.loadToVAO(tree.getVertices(), tree.getTextureCoords(), tree.getNormals(), tree.getIndices());
		RawModel boxModel = loader.loadToVAO(box.getVertices(), box.getTextureCoords(), box.getNormals(), box.getIndices());
		RawModel dragonModel = loader.loadToVAO(dragon.getVertices(), dragon.getTextureCoords(), dragon.getNormals(), dragon.getIndices());
		RawModel emptyModel = loader.loadToVAO(empty.getVertices(), empty.getTextureCoords(), empty.getNormals(), empty.getIndices());
		RawModel diskModel = loader.loadToVAO(disk.getVertices(), disk.getTextureCoords(), disk.getNormals(), disk.getIndices());
		RawModel flagModel = loader.loadToVAO(flag.getVertices(), flag.getTextureCoords(), flag.getNormals(), flag.getIndices());
		RawModel holeModel = loader.loadToVAO(hole.getVertices(), hole.getTextureCoords(), hole.getNormals(), hole.getIndices());
        RawModel rampModel = loader.loadToVAO(ramp.getVertices(), ramp.getTextureCoords(), ramp.getNormals(), ramp.getIndices());
        RawModel rampWHoleModel = loader.loadToVAO(rampWHole.getVertices(), rampWHole.getTextureCoords(), rampWHole.getNormals(), rampWHole.getIndices());
        RawModel windmillModel = loader.loadToVAO(windmill.getVertices(), windmill.getTextureCoords(), windmill.getNormals(), windmill.getIndices());
        RawModel windmillRotModel = loader.loadToVAO(windmill_rot.getVertices(), windmill_rot.getTextureCoords(), windmill_rot.getNormals(), windmill_rot.getIndices());
		RawModel columnModel = loader.loadToVAO(ad_column.getVertices(), ad_column.getTextureCoords(), ad_column.getNormals(), ad_column.getIndices());
		RawModel wallSegModel = loader.loadToVAO(wall_seg.getVertices(), wall_seg.getTextureCoords(), wall_seg.getNormals(), wall_seg.getIndices());

		tModels.put("ball", new TexturedModel(ballModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("tree", new TexturedModel(treeModel, new ModelTexture(loader.loadTexture("tree"))));
		tModels.put("box", new TexturedModel(boxModel, new ModelTexture(loader.loadTexture("box"))));
		tModels.put("barrel", new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader), new ModelTexture(loader.loadTexture("barrel"))));
		tModels.put("crate", new TexturedModel(NormalMappedObjLoader.loadOBJ("crate", loader), new ModelTexture(loader.loadTexture("crate"))));
		tModels.put("boulder", new TexturedModel(NormalMappedObjLoader.loadOBJ("boulder", loader), new ModelTexture(loader.loadTexture("boulder"))));
		tModels.put("dragon", new TexturedModel(dragonModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("empty", new TexturedModel(emptyModel, new ModelTexture(loader.loadTexture("empty"))));
		tModels.put("disk", new TexturedModel(diskModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("flag", new TexturedModel(flagModel, new ModelTexture(loader.loadTexture("flag"))));
		tModels.put("hole", new TexturedModel(holeModel, new ModelTexture(loader.loadTexture("white"))));
        tModels.put("ramp", new TexturedModel(rampModel, new ModelTexture(loader.loadTexture("skull"))));
        tModels.put("rampWHole", new TexturedModel(rampWHoleModel, new ModelTexture(loader.loadTexture("white"))));
        tModels.put("windmill", new TexturedModel(windmillModel, new ModelTexture(loader.loadTexture("windmill_tower"))));
        tModels.put("windmill_rot", new TexturedModel(windmillRotModel, new ModelTexture(loader.loadTexture("windmill_wings_alt"))));
		tModels.put("ad_column", new TexturedModel(columnModel, new ModelTexture(loader.loadTexture("ad column default"))));
		tModels.put("wall_seg", new TexturedModel(wallSegModel, new ModelTexture(loader.loadTexture("white"))));

		tModels.get("barrel").getTexture().setShineDamper(10);
		tModels.get("barrel").getTexture().setReflectivity(0.3f);
		tModels.get("barrel").getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
		
		tModels.get("crate").getTexture().setShineDamper(10);
		tModels.get("crate").getTexture().setReflectivity(0.3f);
		tModels.get("crate").getTexture().setNormalMap(loader.loadTexture("crateNormal"));
		
		tModels.get("boulder").getTexture().setShineDamper(10);
		tModels.get("boulder").getTexture().setReflectivity(0.3f);
		tModels.get("boulder").getTexture().setNormalMap(loader.loadTexture("boulderNormal"));
		
		tModels.get("ball").getTexture().setShineDamper(10);
		tModels.get("ball").getTexture().setReflectivity(1);
	}
	
	private void loadLights(){
		long before = System.currentTimeMillis();
		world.getLights().clear();
		List<Light> lights = new ArrayList<Light>();
		lights.add(new Light(new Vector3f(1000000,1500000,-1000000),new Vector3f(1f,1f,1f)));
		world.addLights(lights);
		//System.out.println("Loading lights: " + (System.currentTimeMillis() - before) + "ms");
	}

	public Entity createWall(Vector2f p1, Vector2f p2) {
		//System.out.println("Create wall between " + p1 + " and " + p2);
		Entity e = new Wall(p1, p2, tModels.get("wall_seg"), 0, mData.get("wall_seg"));
		world.add(e);
		return e;
	}
	
	public Entity createEntity(String eName, Vector3f position, float rotX, float rotY, float rotZ, float scale){
		long before = System.currentTimeMillis();
		Entity e = new Entity(tModels.get(eName), 0,mData.get(eName), position, rotX, rotY, rotZ, scale, eName);
		world.add(e);
		//System.out.println("Loading entity: " + (System.currentTimeMillis() - before) + "ms");
		return e;
	}

	public Entity createRotatingEntity(String eName, Vector3f position, Vector3f rotation, float scale, Vector3f rotVel) {
		long before = System.currentTimeMillis();
		Entity e = new RotatingEntity(tModels.get(eName), 0,mData.get(eName), position, rotation, scale, rotVel);
		world.add(e);
		//System.out.println("Loading entity: " + (System.currentTimeMillis() - before) + "ms");
		return e;
	}
	
	public Entity createEntity(String eName, int a,  Vector3f position, float rotX, float rotY, float rotZ, float scale){
		Entity e = new Entity(tModels.get(eName), a, mData.get(eName), position, rotX, rotY, rotZ, scale);
		world.add(e);
		return e;
	}
	
	public Entity createNormalMapEntity(String eName, int a,  Vector3f position, float rotX, float rotY, float rotZ, float scale){
		if(normalMap){
			Entity e = new Entity(tModels.get(eName), a, mData.get(eName), position, rotX, rotY, rotZ, scale);
			e.getModel().getTexture().setNormalMap(loader.loadTexture(eName + "Normal"));
			world.addNE(e);
			return e;
		} 
		//System.out.println("Normal maps disabled");
		return null;
	}
	
	public Ball createBall(Vector3f position, boolean real){
		Ball b;
		if (real){
			b = new RealBall(tModels.get("ball"), position, 0f, 0f, 0f, 1f);
			if (mainEngine != null)
				mainEngine.addBall((RealBall)b);
		} else {
			b = new Empty(tModels.get("ball"), position, 0f, 0f, 0f, 1f);
		}
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
	
	public void swap() {
		if (balls.size() < numberOfPlayers){
			currBall++;
			createBall(new Vector3f(world.getStart()), true);
		} else {
			currBall = (currBall + 1) % numberOfPlayers;
		}
		((RealBall) balls.get(currBall)).setPlayed(false);
		setCameraToBall(currBall);
	}

	public void printScore(){
		((RealBall) balls.get(currBall)).addScore();
		//System.out.println("Score for player " + currBall + " is: " + ((RealBall) balls.get(currBall)).getScore());
	}

	public void removeBall() {
		balls.remove(currBall);
	}

}
