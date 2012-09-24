package com.kamidude.bemyapppong;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class BeMyAppPong extends Game {
	private Screen mGameScreen;
	
	@Override
	public void create() {
		mGameScreen = new Pong();
		setScreen(mGameScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		
		mGameScreen.dispose();
	}
}
