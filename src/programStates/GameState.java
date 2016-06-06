package programStates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import Physics.PhysicsEngine;
import bot.bot;
import engineTester.MainGameLoop;
import entities.Ball;
import entities.Camera;
import entities.Empty;
import entities.Entity;
import entities.Light;
import entities.RealBall;
import entities.VirtualBall;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import terrains.Terrain;
import terrains.World;
import textures.ModelTexture;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class GameState implements State {
	
	private static GameState instance;
	
	private Map<String,TexturedModel> tModels = new HashMap<String,TexturedModel>();
	private Map<String,ModelData> mData = new HashMap<String,ModelData>();
	private World world;
	private ArrayList<WaterTile> waterTiles;
	private ArrayList<GuiTexture> guis;
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
	private boolean multiplayer;
	private int numberOfPlayers;
	
	private boolean gameover=true;

	private float timeBallStill;
	
	private bot bot;
	
	
	public GameState(Loader loader, int numberOfPlayers){
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
		createBall(new Vector3f(100, 20, 100), true);
		camera = new Camera(balls.get(0));
		world = new World(camera);
		balls.get(0).setPosition(world.getStart());
		loadLights();
		renderer = new MasterRenderer(loader, camera);
		mainEngine = new PhysicsEngine(balls, world);
		addRandomWind();
		loadWater();
		loadParticleSystem();

		createTerrain(0, 0, "grass", false);
		createWaterTile(Terrain.getSize()/2f, Terrain.getSize()/2f, -8f);
		createEntity("dragon", new Vector3f(100, getWorld().getHeightOfTerrain(100, 60), 60), -10f, 170f, 0f, 3 );
		ParticleSystem system = createParticleSystem("fire", 8, 200, 30, -0.3f, 1.5f, 8.6f, new Vector3f(113,getWorld().getHeightOfTerrain(100, 60) + 21.3f,57));
		system.setLifeError(0.1f);
		system.setScaleError(0.5f);
		system.setSpeedError(0.25f);
		system.randomizeRotation();
		system.setDirection(new Vector3f(1,0,0), 0.1f);
		bot = new bot(world);
		DisplayManager.reset();
	}
	
	public void buildWithWorld(Loader loader, World world) {
		System.out.println("new game with world");
		this.loader = loader;
		loadModels();
		loadGuis();
		this.world = world;
		createTerrain(0, 0, "grass", world.getTerrains().get(0).getHeights());
		for(Entity e:world.getEntities()) {
			System.out.println(e);
		}
		createBall(new Vector3f(world.getStart().x, world.getStart().y, world.getStart().z), true);
		loadLights();
		renderer = new MasterRenderer(loader, camera);
		System.out.println("newEngine");
		mainEngine = new PhysicsEngine(balls, world);
		loadWater();
		loadParticleSystem();
		setCameraToBall(currBall);
		System.out.println("done game with world");
		createTerrain(0, 1, "grass", false);
		bot = new bot(world);
		DisplayManager.reset();

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
			for(Ball b:balls)
				if(b instanceof RealBall)
				renderer.processEntity((RealBall)b);
			renderer.processWorld(world, new Vector4f(0, 1, 0, - waterTiles.get(0).getHeight()), normalMap);
			if(particle)
				ParticleMaster.renderParticles(camera);
			camera.getPosition().y += distance;
			camera.invertPitch();
			
			//Rendering on refraction buffer
			fbos.bindRefractionFrameBuffer();
			for(Ball b:balls)
				if(b instanceof RealBall)
				renderer.processEntity((RealBall)b);
			renderer.processWorld(world, new Vector4f(0, -1, 0, waterTiles.get(0).getHeight()), normalMap);
			if(particle)
				ParticleMaster.renderParticles(camera);
			fbos.unbindCurrentFrameBuffer();
		}
		
		for(Ball b:balls)
			if(b instanceof RealBall)
			renderer.processEntity((RealBall)b);
		renderer.processWorld(world, new Vector4f(0, -1, 0, 10000), true);
		if(water)
			waterRenderer.render(waterTiles, camera);
		ParticleMaster.renderParticles(camera);
		ParticleMaster.update(camera);
		guiRenderer.render(guis);
	}
	
	@Override
	public void checkInputs() {
		balls.get(currBall).checkInputs();
		if(Keyboard.isKeyDown(Keyboard.KEY_M)&&!((RealBall) balls.get(currBall)).isPlayed()){
			System.out.println("fuck you");
			balls.get(currBall).setVelocity(bot.ab(balls.get(currBall)));
			balls.get(currBall).setMoving(true);
			((RealBall) balls.get(currBall)).setPlayed(true);
			System.out.println("bot velocity= "+ bot.ab(balls.get(currBall)));
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
		for(ParticleSystem system:particles)
			system.generateParticles();
		if(!balls.get(currBall).isMoving() && ((RealBall)balls.get(currBall)).isPlayed()){
			timeBallStill += DisplayManager.getFrameTimeSeconds();
			if(timeBallStill >= 1){
				printScore();
				int bio = checkBallsInHole();
				if(bio >= 0){
					System.out.print("Ball " + bio + " in hole!");
					if(numberOfPlayers == 1)
						MainGameLoop.gameOver();
					balls.remove(bio);
					numberOfPlayers--;
					if(currBall >= bio)
						currBall--;
				}
				swap();
				timeBallStill = 0;
			}
			
		}
	}
	
	private int checkBallsInHole() {
		float hx = world.getEnd().x;
		float hz = world.getEnd().z;
		for(Ball b:balls){
			float bx = b.getPosition().x;
			float bz = b.getPosition().z;
			if(Math.abs(bx-hx) < 4 && Math.abs(bz-hz) < 4){
				return balls.indexOf(b);
			}
		}
		return -1;
	}

	@Override
	public void cleanUp() {
		fbos.cleanUp();
		guiRenderer.cleanUp();
		waterRenderer.getShader().cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		ParticleMaster.cleanUp();
		tModels.clear();
		mData.clear();
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
		Terrain t = new Terrain(gridX, gridY, loader, new ModelTexture(loader.loadTexture(texName)), height, new Vector2f(world.getEnd().x, world.getEnd().z));
		world.removeTerrain();
		world.add(t);
		return t;
	}
	
	public WaterTile createWaterTile(float tileCenterX, float tileCenterZ, float tileHeight){
		if(waterTiles == null && water){
			waterTiles = new ArrayList<WaterTile>();
			waterTiles.add(new WaterTile(tileCenterX, tileCenterZ, tileHeight));
			return waterTiles.get(0);
		} else {
			System.out.println("Water disabled or already existent");
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
		guis = new ArrayList<GuiTexture>();
	}
	
	private void loadModels(){
		ModelData human = OBJFileLoader.loadOBJ("person");
		ModelData ball = OBJFileLoader.loadOBJ("ball_oth_high");
		ModelData tree = OBJFileLoader.loadOBJ("tree");
		ModelData fern = OBJFileLoader.loadOBJ("fern");
		ModelData grass = OBJFileLoader.loadOBJ("grassModel");
		ModelData pine = OBJFileLoader.loadOBJ("pine");
		ModelData flower = OBJFileLoader.loadOBJ("grassModel");
		ModelData box = OBJFileLoader.loadOBJ("box");
		ModelData dragon = OBJFileLoader.loadOBJ("dragon");
		ModelData empty = OBJFileLoader.loadOBJ("empty");
		ModelData disk = OBJFileLoader.loadOBJ("disk");
		ModelData flag = OBJFileLoader.loadOBJ("flag_rounded");
		ModelData wall = OBJFileLoader.loadOBJ("wall3");
	    ModelData dragon_low = OBJFileLoader.loadOBJ("dragon_low_test");
	    ModelData hole = OBJFileLoader.loadOBJ("hole");
		
	    mData.put("human",human);
	    mData.put("ball",ball);
	    mData.put("tree",tree);
	    mData.put("fern",fern);
	    mData.put("grass",grass);
	    mData.put("pine",pine);
	    mData.put("flower",flower);
	    mData.put("box",box);
	    mData.put("dragon",dragon);
	    mData.put("wall",wall);
	    mData.put("dragon_low",dragon_low);
	    mData.put("flag",flag);
	    mData.put("hole",hole);

		
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
		tModels.put("flag", new TexturedModel(flagModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("wall", new TexturedModel(wallModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("hole", new TexturedModel(holeModel, new ModelTexture(loader.loadTexture("white"))));
		tModels.put("dragon_low", new TexturedModel(dragonLowModel, new ModelTexture(loader.loadTexture("white"))));

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
		world.getLights().clear();
		List<Light> lights = new ArrayList<Light>();
		lights.add(new Light(new Vector3f(1000000,1500000,-1000000),new Vector3f(1f,1f,1f)));
		world.addLights(lights);
	}
	
	public Entity createEntity(String eName, Vector3f position, float rotX, float rotY, float rotZ, float scale){
		Entity e = new Entity(tModels.get(eName), 0,mData.get(eName), position, rotX, rotY, rotZ, scale);
		world.add(e);
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
		System.out.println("Normal maps disabled");
		return null;
	}
	
	public Ball createBall(Vector3f position, boolean real){
		Ball b;
		if(real){
			b = new RealBall(tModels.get("ball"), position, 0f, 0f, 0f, 1f);
			if(mainEngine != null)
				mainEngine.addBall((RealBall)b);
		}else {
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
		if(balls.size() < numberOfPlayers){
			currBall++;
			createBall(world.getStart(), true);
			
		} else {
			currBall = (currBall + 1) % numberOfPlayers;
		}
		((RealBall) balls.get(currBall)).setPlayed(false);
		setCameraToBall(currBall);
	}
	
	public void printScore(){
		((RealBall) balls.get(currBall)).addScore();
		System.out.println("Score for player " + currBall + " is: " + ((RealBall) balls.get(currBall)).getScore());
	}

	public void removeBall() {
		balls.remove(currBall);
	}

	public void addRandomWind() {
		Vector3f wind = new Vector3f();
		wind.set((float) (Math.random() * 100 - 50), 0, (float) (Math.random() * 100 - 50));
        System.out.printf("Wind: (%f|%f|%f)\n", wind.x, wind.y, wind.z);
		mainEngine.addGlobalAccel(wind);
	}
}
