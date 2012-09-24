package com.kamidude.bemyapppong;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;

public class Quad implements Disposable {
	
	private Mesh mMesh;
	private Body mBody;
	
	public Quad(Vector3 firstPoint, Vector3 secondPoint, Vector3 thirdPoint, Vector3 fourthPoint) {
		mMesh = new Mesh(true, 6, 0, VertexAttribute.Position(), VertexAttribute.Normal());
		
		Vector3 normalFace1 = new Vector3(thirdPoint).sub(firstPoint);
		normalFace1.crs(new Vector3(secondPoint).sub(firstPoint)).nor();
		Vector3 normalFace2 = new Vector3(thirdPoint).sub(secondPoint);
		normalFace2.crs(new Vector3(fourthPoint).sub(secondPoint)).nor();
		
		mMesh.setVertices(new float[] {
			firstPoint.x, firstPoint.y, firstPoint.z, normalFace1.x, normalFace1.y, normalFace1.z,
			secondPoint.x, secondPoint.y, secondPoint.z, normalFace1.x, normalFace1.y, normalFace1.z, 
			thirdPoint.x, thirdPoint.y, thirdPoint.z, normalFace1.x, normalFace1.y, normalFace1.z,
			secondPoint.x, secondPoint.y, secondPoint.z, normalFace2.x, normalFace2.y, normalFace2.z,
			thirdPoint.x, thirdPoint.y, thirdPoint.z, normalFace2.x, normalFace2.y, normalFace2.z,
			fourthPoint.x, fourthPoint.y, fourthPoint.z, normalFace2.x, normalFace2.y, normalFace2.z
		});
	}
	
	public void render(ShaderProgram shader, Camera camera) {
		mMesh.render(shader, GL20.GL_TRIANGLES);
	}

	@Override
	public void dispose() {
		mMesh.dispose();
	}
}
