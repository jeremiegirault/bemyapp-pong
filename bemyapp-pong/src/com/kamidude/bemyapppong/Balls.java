package com.kamidude.bemyapppong;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Disposable;

public class Balls implements Disposable {
	
	private ShaderProgram mShader;
	private Mesh mMesh;
	private Texture mTexture;
	private World mWorld;
	private MouseJoint mMouseJoint;
	private Actor mCurrentActor;
	
	private boolean mJointCreatedForCurrentActor = false;
	
	private ArrayList<Actor> mActors = new ArrayList<Balls.Actor>();
	
	private BallControlListener mListener;
	public static interface BallControlListener {
		public void isPlayerControllingBall(boolean controlling);
	}
	
	public static class Actor {
		private Balls mRef;
		private Matrix4 mTransform;
		private Body mBody;
		
		private Actor(Balls magnet, World world) {
			mRef = magnet;
			
			// prepare matrices
			mTransform = new Matrix4();
			
			// prepare physics			
			BodyDef playerBodyDef = new BodyDef();  
	        playerBodyDef.type = BodyType.DynamicBody;
	        playerBodyDef.position.set(0, 0);  
			
	        mBody = world.createBody(playerBodyDef);
	        CircleShape playerShape = new CircleShape();
	        playerShape.setRadius(0.7f);
	        FixtureDef playerFixture = new FixtureDef();
	        playerFixture.shape = playerShape;  
	        playerFixture.density = 1.0f;  
	        playerFixture.friction = 0.0f;  
	        playerFixture.restitution = 1;  
	        mBody.createFixture(playerFixture);
	        playerShape.dispose();
		}
		
		public Body getBody() {
			return mBody;
		}
		
		public void destroy() {
			mRef.destroyActor(this);
		}
		
		private static final float MIN_SPEED = 3.3f;
		public void update(ILevel level, float delta) {
			float rotSpeed1 = 140;
			float rotSpeed2 = 75;
			float rotSpeed3 = 32;
			
			if(mRef.mJointCreatedForCurrentActor) {
				// if we launched the ball
				
				Vector2 velocity = mBody.getLinearVelocity();
				if(velocity.len() < MIN_SPEED) {
					velocity.set(level.getRandom().nextFloat(), level.getRandom().nextFloat()).nor().mul(MIN_SPEED*20);
					mBody.applyForceToCenter(velocity);
				}
			}
			
			
			Vector2 playerPos = mBody.getPosition();
			mTransform.val[Matrix4.M03] = playerPos.x;
			mTransform.val[Matrix4.M13] = playerPos.y;
			mTransform.rotate(new Vector3(0, 1, 0), rotSpeed1*delta);
			mTransform.rotate(new Vector3(1, 0, 0), rotSpeed2*delta);
			mTransform.rotate(new Vector3(0, 0, 1), rotSpeed3*delta);
		}
	}
	
	public Balls(World world, BallControlListener listener) {
		
		mListener = listener;
		
		// load shader
		mShader = new ShaderProgram(Gdx.files.internal("data/cube.vs"), Gdx.files.internal("data/cube.fs"));
		if(!mShader.isCompiled()) {
			throw new RuntimeException(mShader.getLog());
		}
		
		mTexture = new Texture(Gdx.files.internal("data/metal1024.png"), true);
		mTexture.setFilter(TextureFilter.MipMapLinearNearest, TextureFilter.MipMapLinearNearest);
		
		// load mesh
		mMesh = ObjLoader.loadObj(Gdx.files.internal("data/cube.obj").read(), true, false);
		mWorld = world;
	}
	
	public Actor newActor() {
		if(mMouseJoint != null) {
			mWorld.destroyJoint(mMouseJoint);
			mMouseJoint = null;
		}
		mCurrentActor = new Actor(this, mWorld);
		mActors.add(mCurrentActor);
		mJointCreatedForCurrentActor = false;
		return mCurrentActor;
	}
	
	public void update(ILevel screen, float delta) {
		if(Gdx.input.justTouched()) {
			if(mMouseJoint == null && mCurrentActor != null && !mJointCreatedForCurrentActor) {
				if(mListener != null)
					mListener.isPlayerControllingBall(true);
				
				MouseJointDef def = new MouseJointDef();
				def.maxForce = 1000;
				def.bodyA = screen.getWorldBody();
				def.bodyB = mCurrentActor.mBody;
				Vector2 pos = mCurrentActor.mBody.getPosition();
				def.target.x = pos.x;
				def.target.y = pos.y;
				mMouseJoint = (MouseJoint) mWorld.createJoint(def);
				mJointCreatedForCurrentActor = true;
			}
		} else if(Gdx.input.isTouched()) {
			if(mMouseJoint != null)
				mMouseJoint.setTarget(screen.screenToWorld(Gdx.input.getX(), Gdx.input.getY()));
		} 
		else {
			if(mMouseJoint != null) {
				mWorld.destroyJoint(mMouseJoint);
				mMouseJoint = null;
				
				if(mListener != null)
					mListener.isPlayerControllingBall(false);
			}
		}
		
		for(int i = 0; i < mActors.size(); ++i) {
			Actor actor = mActors.get(i);
			actor.update(screen, delta);
		}
	}
	
	public void render(ILevel level) {
		// render
		mShader.begin();
		
		mShader.setUniformMatrix("u_view", level.getCamera().view);
		mShader.setUniformMatrix("u_proj", level.getCamera().projection);
		mShader.setUniformf("u_lightDir", level.getLightDir());
		mShader.setUniformi("u_tex", 0);
		
		mTexture.bind(0);
		
		
		for(int i = 0; i < mActors.size(); ++i) {
			Actor actor = mActors.get(i);
			mShader.setUniformMatrix("u_normalMat", level.getNormalMatrix(actor.mTransform));
			mShader.setUniformMatrix("u_world", actor.mTransform);
			mMesh.render(mShader, GL20.GL_TRIANGLES);
		}
		
		mShader.end();
	}
	
	public Actor getCurrentActor() {
		return mCurrentActor;
	}
	
	private void destroyActor(Actor actor) {
		// TODO: uncomment to remove error
		//mActors.remove(actor);
		if(mMouseJoint != null && mMouseJoint.getBodyB() == actor.mBody) {
			mWorld.destroyJoint(mMouseJoint);
			mMouseJoint = null;
		}
		// TODO: uncomment to remove error
		//mWorld.destroyBody(actor.mBody);
		
		if(mCurrentActor == actor)
			mCurrentActor = null;
	}

	//
	// Disposable implementation
	//
	
	@Override
	public void dispose() {
		for(int i = 0; i < mActors.size(); ++i) {
			Actor actor = mActors.get(i);
			if(actor.mBody != null) {
				actor.mBody.getWorld().destroyBody(actor.mBody);
			}
		}
		
		if(mMouseJoint != null) {
			mWorld.destroyJoint(mMouseJoint);
			mMouseJoint = null;
		}
		
		mCurrentActor = null;
		mWorld = null;
		
		mMesh.dispose();
		mShader.dispose();
		mActors.clear();
		mTexture.dispose();
	}
}
