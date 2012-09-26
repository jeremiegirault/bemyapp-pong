package com.kamidude.bemyapppong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Disposable;

public class Pads implements Disposable {
	
	private ShaderProgram mShader;
	private Mesh mMesh;
	private World mWorld;
	
	private Body mPadTop;
	private Matrix4 mPadTopTransform;
	private Body mPadBottom;
	private Matrix4 mPadBottomTransform;
	
	private boolean mControlAllowed = true;
	
	public Pads(World world, float topLane, float bottomLane) {
		mWorld = world;
		
		// load shader
		mShader = new ShaderProgram(Gdx.files.internal("data/pad.vs"), Gdx.files.internal("data/pad.fs"));
		if(!mShader.isCompiled()) {
			throw new RuntimeException(mShader.getLog());
		}
		
		// load mesh
		mMesh = ObjLoader.loadObj(Gdx.files.internal("data/pad.obj").read(), false, true);
		
		// prepare physics
		mWorld = world;
		mPadTop = makePadBody(0.0f, topLane);
		mPadBottom = makePadBody(0.0f, bottomLane);
		
		// allocate matrices
		mPadTopTransform = new Matrix4().rotate(0, 1, 0, 180);
		mPadBottomTransform = new Matrix4();
	}
	
	private Body makePadBody(float x, float y) {
		BodyDef padBodyDef = new BodyDef();
		padBodyDef.type = BodyType.StaticBody;
		padBodyDef.position.set(x, y);
        
		Body rv = mWorld.createBody(padBodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(2.0f, 0.5f);
		rv.createFixture(shape, 0.0f);
		shape.dispose();
		return rv;
	}
	
	private void movePad(ILevel level, int x, int y) {
		if(y < Gdx.graphics.getHeight() / 2) {
			// move top pad
			Vector2 topPadPos = level.screenToWorld(x, y);
			
			mPadTop.setTransform(topPadPos.x, mPadTop.getPosition().y, 0);
		} else {
			// move bottom pad
			Vector2 bottomPadPos = level.screenToWorld(x, y);
			
			mPadBottom.setTransform(bottomPadPos.x, mPadBottom.getPosition().y, 0);
		} 
	}
	
	public void update(ILevel level, float delta) {
		
		if(mControlAllowed) {
			if(Gdx.input.isTouched(0)) {
				movePad(level, Gdx.input.getX(0), Gdx.input.getY(0));
			}
			
			if(Gdx.input.isTouched(1)) {
				movePad(level, Gdx.input.getX(1), Gdx.input.getY(1));
			}
		}
		
		Vector2 topPos = mPadTop.getPosition();
		mPadTopTransform.val[Matrix4.M03] = topPos.x;
		mPadTopTransform.val[Matrix4.M13] = topPos.y;
		Vector2 bottomPos = mPadBottom.getPosition();
		mPadBottomTransform.val[Matrix4.M03] = bottomPos.x;
		mPadBottomTransform.val[Matrix4.M13] = bottomPos.y;
	}
	
	public void render(ILevel level) {
		// render
		mShader.begin();
		
		mShader.setUniformMatrix("u_view", level.getCamera().view);
		mShader.setUniformMatrix("u_proj", level.getCamera().projection);
		mShader.setUniformf("u_lightDir", level.getLightDir());
		
		mShader.setUniformMatrix("u_normalMat", level.getNormalMatrix(mPadTopTransform));
		mShader.setUniformMatrix("u_world", mPadTopTransform);
		mMesh.render(mShader, GL20.GL_TRIANGLES);
		
		mShader.setUniformMatrix("u_normalMat", level.getNormalMatrix(mPadBottomTransform));
		mShader.setUniformMatrix("u_world", mPadBottomTransform);
		mMesh.render(mShader, GL20.GL_TRIANGLES);
		
		mShader.end();
	}
	
	public void setControlAllowed(boolean allowed) {
		mControlAllowed = allowed;
	}
	
	//
	// Disposable implementation
	//
	
	@Override
	public void dispose() {
		mMesh.dispose();
		mShader.dispose();
		
		if(mPadTop != null) {
			mWorld.destroyBody(mPadTop);
			mPadTop = null;
		}
		
		if(mPadBottom != null) {
			mWorld.destroyBody(mPadBottom);
			mPadBottom = null;
		}
		
		mWorld = null;
	}
}
