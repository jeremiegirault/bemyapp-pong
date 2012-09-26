package com.kamidude.bemyapppong;

import java.util.Random;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public interface ILevel {
	public Vector2 screenToWorld(float x, float y);
	public World getWorld();
	public Body getWorldBody();
	public Random getRandom();
	
	public Camera getCamera();
	public Vector3 getLightDir();
	public Matrix4 getNormalMatrix(Matrix4 worldTransform);
	
	public Vector3 getBallPosition();
}
