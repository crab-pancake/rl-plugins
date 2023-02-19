package com.scenereloader;

import java.awt.event.KeyEvent;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name="Scene Reloader", description="Reloads the scene with Ctrl-R. By De0")
public class SceneReloader extends Plugin implements KeyListener
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private KeyManager keyManager;
	@Inject
	private ConfigManager cm;

	protected void startUp()
	{
		keyManager.registerKeyListener(this);
		cm.unsetConfiguration("raids", "dummy");
	}

	protected void shutDown()
	{
		keyManager.unregisterKeyListener(this);
	}

	public void keyTyped(KeyEvent e)
	{
	}

	public void keyPressed(KeyEvent e) {
		if (e.isControlDown() && e.isShiftDown() && e.getKeyChar() == '\u0012') {
			clientThread.invoke(() -> {
				if (client.getGameState() == GameState.LOGGED_IN) {
					cm.setConfiguration("raids", "dummy", cm.getConfiguration("raids", "dummy") + "0");
				}
			});
		} else if (e.isControlDown() && e.getKeyChar() == '\u0012') {
			this.clientThread.invoke(() -> {
				if (client.getGameState() == GameState.LOGGED_IN) {
					client.setGameState(GameState.CONNECTION_LOST);
				}
			});
		}
	}

	public void keyReleased(KeyEvent e)
	{
	}
}
