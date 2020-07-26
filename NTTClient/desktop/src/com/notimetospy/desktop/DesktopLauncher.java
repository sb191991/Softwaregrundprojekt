package com.notimetospy.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.notimetospy.controller.MessageEmitter;
import com.notimetospy.controller.NoTimeToSpy;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		//config.vSyncEnabled = true;
		//config.fullscreen = true;

		//config.title = "No Time to Spy";
		//LwjglApplicationConfiguration.getDesktopDisplayMode();
		config.width = 1024;
		config.height = 576;

		new LwjglApplication(new NoTimeToSpy(), config);

	}
}
