package com.touchspin.td;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/* ======================================================================================
 * File:			Hero.java
 * Authors:			Brian Adams - b.adams5736@edmail.edcc.edu
 * 					Russell Brendel - russell.brendel.2925@edmail.edcc.edu
 * 					Damian Forrester - dforrester777@gmail.com
 * 					Wendi Tang - w.tang2404@myedmail.edcc.edu
 * 
 * Organization:	Edmonds Community College
 * Term:			Spring 2014
 * Class:			CS 185 - Game Project Developement
 * Instructor:		Tim Hunt - thunt@edcc.edu
 * 
 * Project:			Ollie
 * --------------------------------------------------------------------------------------
 * 
 * This class holds information for player.
 * 
 * ======================================================================================
 */
public class Hero extends GameThing {
	public OrthographicCamera camera;
	public MoverInput heroMover = new MoverInput();

	private Map<String, TextureRegion> ballTypeMap = new HashMap<String, TextureRegion>();
	private Animation fireAnimation;
	private Animation smokeAnimation;
	private TextureRegion currentFireFrame;
	private TextureRegion currentSmokeFrame;
	private float scaleFactor;

	private int frameCount = 0;
	private Sprite heroSprite;
	private Sprite fireEffect;
	private Sprite smokeEffect;
	public boolean flammable;
	private float ballHeight;
	private float ballWidth;
	private float countTime;
	public float mass;
	public float radius;
	private float ventRatio;
	private Color baseColor = new Color();

	// private float distancePerFrameX;
	// private float distancePerFrameY;
	// private int gravity = -10;

	/**
	 * The constructor
	 * 
	 * @param camera
	 *            - the camera used in the game screen
	 * @param tiledMapWrapper
	 *            - the wrapper class of the tiledMap
	 */
	public Hero(OrthographicCamera camera, TiledMapWrapper tiledMapWrapper) {
		this.tiledMapWrapper = tiledMapWrapper;
		this.camera = camera;

		heroSprite = new Sprite();

		loadBallType();
		changeBall(g.i().currentBallType);

		heroSprite.setBounds(0, 32, ballWidth * camera.zoom, ballHeight
				* camera.zoom);
		heroSprite.setOrigin(heroSprite.getWidth() / 2,
				heroSprite.getHeight() / 2);

		setHeight(heroSprite.getHeight() * camera.zoom);
		setWidth(heroSprite.getWidth() * camera.zoom);
		setX(10);
		setY(100);

		scaleFactor = 1f;

		// read in file animation
		loadFireAnimation();
		stateTime = 0f;
		currentFireFrame = fireAnimation.getKeyFrame(stateTime, true);

		loadSmokeAnimation();
		currentSmokeFrame = smokeAnimation.getKeyFrame(stateTime, true);

		fireEffect = new Sprite(currentFireFrame);
		fireEffect.setBounds(0, 32, ballWidth * camera.zoom, ballHeight
				* fireEffect.getHeight() / fireEffect.getWidth() * camera.zoom);
		fireEffect.setOrigin(heroSprite.getWidth() / 2,
				heroSprite.getHeight() / 2);

		smokeEffect = new Sprite(currentSmokeFrame);
		smokeEffect.setBounds(0, 32, ballWidth * camera.zoom, ballHeight
				* smokeEffect.getHeight() / smokeEffect.getWidth()
				* camera.zoom);
		smokeEffect.setOrigin(heroSprite.getWidth() / 2,
				heroSprite.getHeight() / 2);

		for (int i = 0; i < g.i().mapObjects.size(); i++) {
			if (g.i().mapObjects.get(i).getName().equalsIgnoreCase("enter1")) {
				setPosition(g.i().mapObjects.get(i).getX(), g.i().mapObjects
						.get(i).getY());
			}
		}

	}

	/**
	 * Draw the player
	 */
	@Override
	public void draw(Batch batch, float alpha) {
		batch.setProjectionMatrix(camera.combined);
		if (frameCount == 60) {
			heroSprite.setColor(Color.RED);
			Gdx.input.vibrate(1000);
		} else if (frameCount == 1) {
			heroSprite.setColor(baseColor);
		}
		heroSprite.draw(batch);
		if (g.i().fire) {
			drawEffect(batch);
		}
	}

	/**
	 * Draw effect
	 * 
	 * @param batch
	 */
	private void drawEffect(Batch batch) {
		// batch.draw(currentFrame,getX(),getY(),32f,currentFrame.getRegionHeight()*32/currentFrame.getRegionWidth());
		smokeEffect.draw(batch);
		fireEffect.draw(batch);
	}

	/**
	 * Update method
	 */
	@Override
	public void act(float delta) {
		heroMover.move(this);

		// Attack
		if (frameCount > 1)
			frameCount--;
		// position
		setSpritesPosition();

		// Rotation
		heroSprite.rotate(360 * (heroMover.previousX - getX())
				/ ((float) Math.PI * heroSprite.getRegionHeight()));
		setRotationAndScale();

		// animation
		stateTime += Gdx.graphics.getDeltaTime();

		currentFireFrame = fireAnimation.getKeyFrame(stateTime, true);
		fireEffect.setRegion(currentFireFrame);

		currentSmokeFrame = smokeAnimation.getKeyFrame(stateTime, true);
		smokeEffect.setRegion(currentSmokeFrame);

		if (g.i().fire) {
			countTime += Gdx.graphics.getDeltaTime();
			if (countTime > 2) {
				g.i().playerHealth -= 5;
				getHurt();
				countTime = 0;
			}
		}

		if (g.i().playerHealth < 0) {
			g.i().playerHealth = 0;
			g.i().t.action("menu,gameOver");
		}

	}

	/**
	 * tint player read for 60 frames if the player get hurt
	 */
	public void getHurt() {
		frameCount = 60;
	}

	/**
	 * Change the ball type to the given type
	 * 
	 * @param type
	 *            - the ball type to change to
	 */
	public void changeBall(String type) {

		switch (type) {
		case Balls.BallPingPong:
			g.i().playerFriction = 0.008f;
			flammable = true;
			ballWidth = 5.8f;
			ballHeight = 5.8f;
			ventRatio = 1f;
			radius = 2.9f;
			break;
		case Balls.BallBowling:
			g.i().playerFriction = 0.03f;
			g.i().fire = false;
			ballWidth = 30f;
			ballHeight = 30f;
			ventRatio = 0f;
			radius = 15f;
			flammable = false;
			g.i().fire = false;
			break;
		case Balls.BallBasket:
			g.i().playerFriction = 0.011f;
			ballWidth = 30f;
			ballHeight = 30f;
			radius = 15f;
			ventRatio = 0.3f;
			flammable = true;
			break;
		case Balls.BallBase:
			g.i().playerFriction = 0.008f;
			ballWidth = 22.16f;
			ballHeight = 22.16f;
			radius = 11.08f;
			ventRatio = 0.5f;
			flammable = true;
			break;
		case Balls.BallTennis:
			g.i().playerFriction = 0.008f;
			ballWidth = 22.16f;
			ballHeight = 22.16f;
			radius = 11.08f;
			ventRatio = 0.5f;
			flammable = true;
			break;
		case Balls.BallBalloon:
			g.i().playerFriction = 0.015f;
			ballWidth = 30f;
			ballHeight = 30f;
			radius = 15f;
			ventRatio = 1f;
			flammable = true;
		}
		if (!g.i().fire) {
			g.i().sound.fire(false);
		}
		heroSprite.setRegion(ballTypeMap.get(type));
		g.i().currentBallType = type;
		g.i().sound.setBounce();
		baseColor = Color.WHITE;
		heroSprite.setColor(Color.WHITE);
		if (type.equalsIgnoreCase("Balloon")) {
			randomTint();
		}
		setSpriteBounds();
		calcualteDyInWater();
	}

	/**
	 * Ignited the ball or put out the fire
	 * 
	 * @param fireOn
	 *            - ignite the player if it is true put out the fire if it is
	 *            false
	 */
	public void igniteBall(boolean fireOn) {
		g.i().fire = fireOn;
		if (!g.i().fire) {
			g.i().sound.fire(false);
		}
	}

	/**
	 * Change the ball's speed along x axis
	 * 
	 * @param speed
	 *            - the base speed
	 */
	public void changeBallX(float speed) {
		if (ventRatio != 0)
			heroMover.speedXPerSecond = speed * ventRatio;
	}

	/**
	 * Change the ball's speed along y axis
	 * 
	 * @param speed
	 *            - the base speed
	 */
	public void changeBallY(float speed) {
		if (ventRatio != 0)
			heroMover.speedYPerSecond = speed * ventRatio;
	}

	/**
	 * Get the current speed of the ball along y axis
	 * 
	 * @return the current speed along y axis
	 */
	public float getYSpeed() {
		return heroMover.speedYPerSecond;
	}

	/**
	 * Get the current speed of the ball along x axis
	 * 
	 * @return the current speed along x axis
	 */
	public float getXSpeed() {
		return heroMover.speedXPerSecond;
	}

	@Override
	public void setPosition(float x, float y) {
		setX(x);
		setY(y);

		camera.position.x = getX() + getWidth() / 2;
		camera.position.y = getY() + getHeight() / 2;
		if (camera.position.x - camera.viewportWidth / 2 < 0)
			camera.position.x = camera.viewportWidth / 2;
		else if (camera.position.x + camera.viewportWidth / 2 > tiledMapWrapper
				.getPixelWidth())
			camera.position.x = tiledMapWrapper.getPixelWidth()
					- camera.viewportWidth / 2;
		if (camera.position.x - camera.viewportWidth / 2 < 0)
			camera.position.x = camera.viewportWidth / 2;
		else if (camera.position.y + camera.viewportHeight > tiledMapWrapper
				.getPixelHeight())
			camera.position.y = tiledMapWrapper.getPixelHeight()
					- camera.viewportHeight / 2;
	}

	/**
	 * return the center of the ball
	 * 
	 * @return the center of the ball
	 */
	public Vector2 getCenter() {
		Vector2 center = new Vector2(getX() + radius, getY() + radius);
		return center;

	}

	// --------------Private helper
	// method------------------------------------------
	/**
	 * Set all sprites position based on actor's position
	 */
	private void setSpritesPosition() {
		heroSprite.setX(getX());
		heroSprite.setY(getY());
		fireEffect.setX(getX());
		fireEffect.setY(getY());
		smokeEffect.setX(getX());
		smokeEffect.setY(getY());
	}

	/**
	 * load ball texture regions based on ball type
	 */
	private void loadBallType() {
		Texture appearance = new Texture("img/spritesheet/BallSquish.png");
		TextureRegion[][] tmp = TextureRegion.split(appearance,
				appearance.getWidth() / 6, appearance.getHeight() / 12);

		ballTypeMap.put(Balls.BallBowling, tmp[0][0]);
		ballTypeMap.put(Balls.BallBasket, tmp[1][0]);
		ballTypeMap.put(Balls.BallPingPong, tmp[2][0]);
		ballTypeMap.put(Balls.BallBase, tmp[4][0]);
		ballTypeMap.put(Balls.BallBeach, tmp[5][0]);
		ballTypeMap.put(Balls.BallMarble, tmp[6][0]);
		ballTypeMap.put(Balls.BallSoccer, tmp[7][0]);
		ballTypeMap.put(Balls.BallPool, tmp[8][0]);
		ballTypeMap.put(Balls.BallTennis, tmp[9][0]);
		ballTypeMap.put(Balls.BallGolf, tmp[10][0]);
		ballTypeMap.put(Balls.BallBalloon, tmp[11][0]);

	}

	/**
	 * Load in the fire animation
	 */
	private void loadFireAnimation() {
		Texture fire = new Texture(
				Gdx.files.internal("img/spritesheet/Fireball.png"));
		TextureRegion[][] tmp = TextureRegion.split(fire, fire.getWidth() / 12,
				fire.getHeight() / 4);
		TextureRegion[] fireFrames = new TextureRegion[12 * 4];
		int index = 0;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 12; j++) {
				fireFrames[index++] = tmp[i][j];
			}
		}
		fireAnimation = new Animation(0.025f, fireFrames);
	}

	/**
	 * Load in the smoke animation
	 */
	private void loadSmokeAnimation() {
		Texture fire = new Texture(
				Gdx.files.internal("img/spritesheet/Smoke.png"));
		TextureRegion[][] tmp = TextureRegion.split(fire, fire.getWidth() / 15,
				fire.getHeight() / 6);
		TextureRegion[] smokeFrames = new TextureRegion[15 * 6 - 4];
		int index = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 15; j++) {
				smokeFrames[index++] = tmp[i][j];
				if (index == smokeFrames.length)
					break;
			}
		}
		smokeAnimation = new Animation(0.025f, smokeFrames);
	}

	/**
	 * Set the rotation and scale of each ball properly
	 */
	private void setRotationAndScale() {

		if (heroMover.speedXPerSecond == 0) {
			if (heroMover.speedYPerSecond > 0)
				fireEffect.setRotation((float) (-180));
			else if (heroMover.speedYPerSecond < 0)
				fireEffect.setRotation(0);
		}

		else if (heroMover.speedYPerSecond == 0) {

			if (g.i().gameMode == 'R') {
				if (heroMover.speedXPerSecond > 0)
					fireEffect.setRotation((float) (40 * Math
							.atan(heroMover.speedXPerSecond / 200)));
				else if (heroMover.speedXPerSecond < 0)
					fireEffect.setRotation((float) (40 * Math
							.atan(heroMover.speedXPerSecond / 200)));
			} else {
				if (heroMover.speedXPerSecond > 0)
					fireEffect.setRotation((float) (90));
				else if (heroMover.speedXPerSecond < 0)
					fireEffect.setRotation((float) (-90));
			}
		}

		else if (heroMover.speedXPerSecond * heroMover.speedYPerSecond > 0) {
			if (heroMover.speedXPerSecond > 0)
				fireEffect.setRotation((float) (90 + Math
						.atan(heroMover.speedYPerSecond
								/ heroMover.speedXPerSecond)
						/ Math.PI * 180));
			else
				fireEffect.setRotation((float) (-(90 - Math
						.atan(heroMover.speedYPerSecond
								/ heroMover.speedXPerSecond)
						/ Math.PI * 180)));

		} else if (heroMover.speedXPerSecond * heroMover.speedYPerSecond < 0) {

			if (heroMover.speedXPerSecond > 0)
				fireEffect.setRotation((float) (90 + Math
						.atan(heroMover.speedYPerSecond
								/ heroMover.speedXPerSecond)
						/ Math.PI * 180));
			else
				fireEffect.setRotation((float) (-90 + Math
						.atan(heroMover.speedYPerSecond
								/ heroMover.speedXPerSecond)
						/ Math.PI * 180));
		}

		smokeEffect.setRotation(fireEffect.getRotation());

		scaleFactor = Math.max((float) Math.hypot(heroMover.speedXPerSecond,
				heroMover.speedYPerSecond) / 300, 1f);
		fireEffect.setScale(1f, scaleFactor);
		smokeEffect.setScale(1f, scaleFactor);
	}

	/**
	 * Set the bounds of the ball sprite
	 */
	private void setSpriteBounds() {
		if (heroSprite != null) {
			heroSprite.setBounds(0, 32, ballWidth * camera.zoom, ballHeight
					* camera.zoom);
			heroSprite.setOrigin(heroSprite.getWidth() / 2,
					heroSprite.getHeight() / 2);
		}
		setHeight(heroSprite.getHeight() * camera.zoom);
		setWidth(heroSprite.getWidth() * camera.zoom);
		if (fireEffect != null) {
			fireEffect.setBounds(0, 32, ballWidth * camera.zoom, ballHeight
					* fireEffect.getHeight() / fireEffect.getWidth()
					* camera.zoom);
			fireEffect.setOrigin(heroSprite.getWidth() / 2,
					heroSprite.getHeight() / 2);
		}
		if (smokeEffect != null) {
			smokeEffect.setBounds(0, 32, ballWidth * camera.zoom, ballHeight
					* smokeEffect.getHeight() / smokeEffect.getWidth()
					* camera.zoom);
			smokeEffect.setOrigin(heroSprite.getWidth() / 2,
					heroSprite.getHeight() / 2);
		}
	}

	/**
	 * Calculate the ball's acceleration along y axis in water
	 */
	private void calcualteDyInWater() {
		float radius = 0;
		float floatforce = 0;
		int inWaterdyFactor = 1;
		switch (g.i().currentBallType) {
		case Balls.BallPingPong:
			mass = 0.0027f;
			radius = 0.0200f;
			break;
		case Balls.BallBowling:
			mass = 7.3000f;
			radius = 0.0900f;
			break;
		case Balls.BallBasket:
			mass = 2.6200f;
			radius = 0.1210f;
			break;
		case Balls.BallBase:
			mass = 0.1450f;
			radius = 0.0382f;
			break;
		case Balls.BallTennis:
			mass = 0.0600f;
			radius = 0.0335f;
			break;
		case Balls.BallBalloon:
			mass = 0.0020f;
			radius = 0.1210f;
			break;
		}
		floatforce = (float) (1000 * 9.8f * 4 / 3 * Math.PI * Math.pow(radius,
				3));
		float force = (floatforce + (-9.8f * mass)) * inWaterdyFactor;
		float dy = force / mass;
		g.i().playerdyInWater = dy;
	}

	/**
	 * Randomly tint the player.
	 */
	private void randomTint() {

		baseColor = new Color(g.i().rnd.nextFloat(), g.i().rnd.nextFloat(),
				g.i().rnd.nextFloat(), 1);
		heroSprite.setColor(baseColor);

	}

}
