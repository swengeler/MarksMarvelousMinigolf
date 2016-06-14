package programStates;

import renderEngine.utils.Loader;

public interface State {
	public void init(Loader loader);
	public void update();
	public void renderScreen();
	public void checkInputs();
	public void cleanUp();
}
