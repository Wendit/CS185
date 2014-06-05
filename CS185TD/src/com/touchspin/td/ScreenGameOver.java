package com.touchspin.td;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

public class ScreenGameOver extends GameMenu{
	long timeStartGame;
	
	public ScreenGameOver(MainGame MainGame){
		super(MainGame);
		timeStartGame = System.currentTimeMillis();
		
		//reset
		g.i().currentBallType = "Baseball";
		g.i().fire = false;
		g.i().playerHealth = 100;
	}

	void buttons() {
	}

	@Override
	void logo() {
		super.setLogo(Gdx.files.internal("img/menu/LogoGameOver.png"));
	}
	
	public void update() {
		if (TimeUtils.millis()>(timeStartGame+7000)){
			g.i().t.action("menu,Main");
		}
	}

}