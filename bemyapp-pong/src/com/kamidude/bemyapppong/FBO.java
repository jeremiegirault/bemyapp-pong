package com.kamidude.bemyapppong;

import java.nio.IntBuffer;
import java.util.Deque;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;


/* Usage : 
 * FBO fbo = new FBO();
 * fbo.pushPostEffect(effect1);
 * fbo.pushPostEffect(effect3);
 * fbo.pushPostEffect(effect2);
 * 
 * fbo.bind();
 * -- scene.render();
 * 
 * fbo.render();
 * fbo.unbind();
 */
public class FBO implements Disposable {
	private int mFBO;
	private int mRBO;
	private int mImg;
	
	private int mScreenWidth;
	private int mScreenHeight;
	
	private int mWidth;
	private int mHeight;
	
	private Quad mQuad;
	private Camera mCamera;
	
	private ShaderProgram mFinalPass;
	
	private static final String POSTEFFECT_VS = "posteffect.vs";
	private static final String POSTEFFECT_PS = "posteffect.ps";
	
	private static class PostEffect {
		public String name;
		public ShaderProgram program;
	}
	
	private Deque<PostEffect> mEffects = new LinkedList<FBO.PostEffect>(); 
	
	public FBO() {
		mScreenWidth = Gdx.graphics.getWidth();
		mScreenHeight = Gdx.graphics.getHeight();
		
		mQuad = new Quad(new Vector3(0,0,0), 
				new Vector3(0, mScreenHeight, 0), 
				new Vector3(mScreenWidth, mScreenHeight, 0), 
				new Vector3(mScreenWidth, 0, 0));
		
		mCamera = new OrthographicCamera(mScreenWidth, mScreenHeight);
		
		// load final pass program
		
		mFinalPass = new ShaderProgram(POSTEFFECT_VS, POSTEFFECT_PS);
		
		// create FBO object
		mWidth = Gdx.graphics.getWidth();
		mHeight = Gdx.graphics.getHeight();
		
		// create texture
		IntBuffer buf = IntBuffer.allocate(1);
		
		Gdx.gl20.glGenTextures(1, buf);
		mImg = buf.get();
		
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, mImg);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_GENERATE_MIPMAP, GL20.GL_TRUE);
		// msaa here?
		Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, mWidth, mHeight, 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, null);
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
		buf.clear();
		
		// create FBO
		Gdx.gl20.glGenFramebuffers(1, buf);
		mFBO = buf.get();
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, mFBO);
		Gdx.gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D, mImg, 0);
		
		// create RBO
		buf.clear();
		Gdx.gl20.glGenRenderbuffers(1, buf);
		mRBO = buf.get();
		Gdx.gl20.glBindRenderbuffer(GL20.GL_RENDERBUFFER, mRBO);
		// msaa here?
		Gdx.gl20.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_DEPTH_COMPONENT, mWidth, mHeight);
		
		Gdx.gl20.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
	}
	
	public void begin() {
		// bind
		Gdx.gl20.glViewport(0, 0, mWidth, mHeight);
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, mFBO);
	}
	
	public void end() {
		
		renderPostEffects();
		
		// unbind
		Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, mImg);
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
		
		render();
	}
	
	public void pushPostEffect(String file) {
		ShaderProgram program = null;
		
		try {
			program = new ShaderProgram(POSTEFFECT_VS, file);
			if(!program.isCompiled()) throw new Exception("Error : " + program.getLog());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		PostEffect e = new PostEffect();
		e.name = file;
		e.program = program;
		
		mEffects.push(e);
	}
	
	// helpers
	
	private void renderPostEffects() {
		for(PostEffect effect : mEffects) {
			mQuad.render(effect.program, mCamera);
		}
	}
	
	private void render() {
		mQuad.render(mFinalPass, mCamera);
	}
	
	//
	// Disposable implementation
	//

	@Override
	public void dispose() {
		mFinalPass.dispose();
		for(PostEffect effect : mEffects) {
			effect.program.dispose();
		}
		mQuad.dispose();
	}
}
