package com.kamidude.bemyapppong;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.kamidude.bemyapppong.Balls.BallControlListener;

public final class Pong implements Screen, ILevel {

	private PerspectiveCamera mCamera = new PerspectiveCamera();
	private Matrix4 mInvView;
	private Matrix4 mNormalMatrix;
	private Vector3 mLightDir;
	
	private World mWorld;
	
	private Balls mMagnets;
	private Pads mPads;
	
	private BitmapFont mFont;
	private SpriteBatch mFontSprite;
	
	private Box2DDebugRenderer mDebugRenderer;
	
	private static final float DISTANCE = 25.0f;
	private static final float FAR = 100.0f;
	private static final float NEAR = 1.0f;
	
	private Random mRand = new Random();
	private Body mWorldBody;
	
	private Vector2 mTopLeft;
	private Vector2 mBottomLeft;
	private Vector2 mTopRight;
	private Vector2 mBottomRight;
	
	private int mTopPlayerScore = 0;
	private int mBottomPlayerScore = 0;
	
	private Walls mWalls;
	
	public Pong() {
		super();
		
		mWorld = new World(new Vector2(), true);
		
		BodyDef worldBodyDef = new BodyDef();
        mWorldBody = mWorld.createBody(worldBodyDef);
		
        mWalls = new Walls();
        mMagnets = new Balls(mWorld, new BallControlListener() {
			@Override
			public void isPlayerControllingBall(boolean controlling) {
				if(mPads != null)
					mPads.setControlAllowed(!controlling);
			}
		});
        mMagnets.newActor();
		
        mDebugRenderer = new Box2DDebugRenderer();
        mDebugRenderer.setDrawAABBs(true);
        
        mNormalMatrix = new Matrix4();
        mLightDir = new Vector3();
        mInvView = new Matrix4();
        
        mFontSprite = new SpriteBatch();
        mFont = new BitmapFont();
	}
	
	//
	// Screen Implementation
	//
	
	@Override
	public Vector3 getBallPosition() {
		Vector2 pos = mMagnets.getCurrentActor().getBody().getPosition();
		//Vector2 worldPos = screenToWorld(pos.x, pos.y);
		return new Vector3(pos.x, pos.y, 0.0f);
	}
	
	@Override
	public void render(float delta) {
		mWorld.step(1/60f, 8, 3);
		
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// update camera and stuff
		mLightDir.set(mCamera.direction);
		mInvView.set(mCamera.view);
		mInvView.inv();
		mLightDir.mul(mInvView);
		mLightDir.nor();
		
		// update actors
		mMagnets.update(this, delta);
		Balls.Actor currentMagnet = mMagnets.getCurrentActor();
		if(currentMagnet != null) {
			Vector2 playerPos = currentMagnet.getBody().getPosition();
			boolean destroyMagnet = false;
			if(playerPos.y < mBottomLeft.y) {
				destroyMagnet = true;
				mTopPlayerScore++;
			} else if(playerPos.y > mTopLeft.y) {
				destroyMagnet = true;
				mBottomPlayerScore++;
			}
			
			if(destroyMagnet) {
				currentMagnet.destroy();
				mMagnets.newActor();
			}
			
			
		}
		mPads.update(this, delta);
		
		// render
		mWalls.render(this);
		mMagnets.render(this);
		mPads.render(this);
		
		mFontSprite.begin();
		mFont.setColor(Color.BLUE);
		mFont.draw(mFontSprite, String.valueOf(mTopPlayerScore), 25, Gdx.graphics.getHeight()-5);
		mFont.setColor(Color.RED);
		mFont.draw(mFontSprite, String.valueOf(mBottomPlayerScore), 25, 20);
		mFontSprite.end();
		
		//mDebugRenderer.render(mWorld, mCamera.combined);
	}

	@Override
	public void resize(int width, int height) {
		Gdx.gl20.glViewport(0, 0, width, height);
		mCamera.viewportWidth = width;
		mCamera.viewportHeight = height;
		
		mCamera.fieldOfView = 45.0f;
		mCamera.far = FAR;
		mCamera.near = NEAR;
		mCamera.position.x = 0.0f;
		mCamera.position.y = 0.0f;
		mCamera.position.z = DISTANCE;
		mCamera.lookAt(0, 0, 0);
		mCamera.update(true);
		
		mTopLeft = screenToWorld(0, 0);
		mTopRight = screenToWorld(Gdx.graphics.getWidth(), 0);
		mBottomLeft = screenToWorld(0, Gdx.graphics.getHeight());
		mBottomRight = screenToWorld(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		mWalls.resize(mWorld, mTopLeft, mTopRight, mBottomLeft, mBottomRight);
		
		if(mPads != null)
			mPads.dispose();
		mPads = new Pads(mWorld, mTopLeft.y, mBottomLeft.y);
	}

	@Override
	public void show() {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	//
	// Disposable implementation
	//

	@Override
	public void dispose() {
		mPads.dispose();
		mWalls.dispose();
		mMagnets.dispose();
		mWorld.dispose();
	}
	
	//
	// ILevel implementation
	//

	@Override
	public World getWorld() {
		return mWorld;
	}

	@Override
	public Camera getCamera() {
		return mCamera;
	}

	@Override
	public Vector3 getLightDir() {
		return mLightDir;
	}

	@Override
	public Matrix4 getNormalMatrix(Matrix4 worldTm) {
		
		mNormalMatrix.set(mCamera.view);
		mNormalMatrix.mul(worldTm);
		
		return mNormalMatrix.toNormalMatrix();
	}
	
	public Vector2 screenToWorld(float x, float y) {
		Ray r = mCamera.getPickRay(x, y);
		Vector3 pos = r.getEndPoint(DISTANCE);
		return new Vector2(pos.x, pos.y);
	}
	
	public Random getRandom() {
		return mRand;
	}
	
	public Body getWorldBody() {
		return mWorldBody;
	}
}
