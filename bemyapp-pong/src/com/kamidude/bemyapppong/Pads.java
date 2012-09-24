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
		padBodyDef.type = BodyType.KinematicBody;
		padBodyDef.position.set(x, y);
        
		Body rv = mWorld.createBody(padBodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(4.5f, 0.5f);
		rv.createFixture(shape, 0.0f);
		shape.dispose();
		return rv;
	}
	
	public void update(ILevel level, float delta) {
		Vector2 topPos = mPadTop.getPosition();
		mPadTopTransform.val[Matrix4.M03] = topPos.x;
		mPadTopTransform.val[Matrix4.M13] = topPos.y;
		//mPadBottomTransform.rotate(1, 1, 1, 0.2f);
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
