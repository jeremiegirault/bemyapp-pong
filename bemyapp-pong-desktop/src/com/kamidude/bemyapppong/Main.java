package com.kamidude.bemyapppong;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Bemyapp Pong";
		cfg.useGL20 = true;
		cfg.width = 400;
		cfg.height = 640;
		cfg.resizable = false;
		
		new LwjglApplication(new BeMyAppPong(), cfg);
	}
}
