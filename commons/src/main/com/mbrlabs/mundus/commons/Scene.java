/*
 * Copyright (c) 2016. See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mbrlabs.mundus.commons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mbrlabs.mundus.commons.assets.TerrainAsset;
import com.mbrlabs.mundus.commons.env.MundusEnvironment;
import com.mbrlabs.mundus.commons.env.lights.DirectionalLight;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.commons.scene3d.SceneGraph;
import com.mbrlabs.mundus.commons.skybox.Skybox;
import com.mbrlabs.mundus.commons.water.WaterResolution;
import de.damios.guacamole.gdx.graphics.NestableFrameBuffer;

/**
 * @author Marcus Brummer
 * @version 22-12-2015
 */
public class Scene implements Disposable {

    private String name;
    private long id;

    public SceneGraph sceneGraph;
    public MundusEnvironment environment;
    public Skybox skybox;
    public float waterHeight = 0f;
    public WaterResolution waterResolution = WaterResolution.DEFAULT_WATER_RESOLUTION;

    @Deprecated // TODO not here
    public Array<TerrainAsset> terrains;
    @Deprecated // TODO not here
    public GameObject currentSelection;

    public PerspectiveCamera cam;
    public ModelBatch batch;

    private FrameBuffer fboWaterReflection;
    private FrameBuffer fboWaterRefraction;

    protected Vector3 clippingPlaneDisable = new Vector3(0.0f, 0f, 0.0f);
    protected Vector3 clippingPlaneReflection = new Vector3(0.0f, 1f, 0.0f);
    protected Vector3 clippingPlaneRefraction = new Vector3(0.0f, -1f, 0.0f);

    private final float distortionEdgeCorrection = 1f;

    public Scene() {
        environment = new MundusEnvironment();
        currentSelection = null;
        terrains = new Array<TerrainAsset>();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 1, -3);
        cam.lookAt(0, 1, -1);
        cam.near = 0.2f;
        cam.far = 10000;

        DirectionalLight dirLight = new DirectionalLight();
        dirLight.color.set(1, 1, 1, 1);
        dirLight.intensity = 1f;
        dirLight.direction.set(0, -1f, 0);
        dirLight.direction.nor();
        environment.add(dirLight);
        environment.getAmbientLight().intensity = 0.3f;

        sceneGraph = new SceneGraph(this);
    }

    public void render() {
        render(Gdx.graphics.getDeltaTime());
    }

    public void render(float delta) {
        if (fboWaterReflection == null) {
            Vector2 res = waterResolution.getResolutionValues();
            initFrameBuffers((int) res.x, (int) res.y);
        }

        if (sceneGraph.isContainsWater()) {
            captureReflectionFBO(delta);
            captureRefractionFBO(delta);
        }

        // Render objects
        batch.begin(cam);
        sceneGraph.render(delta, clippingPlaneDisable, 0);
        batch.end();

        if (sceneGraph.isContainsWater()) {
            Texture refraction = fboWaterRefraction.getColorBufferTexture();
            Texture reflection = fboWaterReflection.getColorBufferTexture();

            // Render Water
            batch.begin(cam);
            sceneGraph.renderWater(delta, reflection, refraction);
            batch.end();
        }

    }

    private void initFrameBuffers(int width, int height) {
        fboWaterReflection = new NestableFrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
        fboWaterRefraction = new NestableFrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
    }

    private void captureReflectionFBO(float delta) {
        // Calc vertical distance for camera for reflection FBO
        float camReflectionDistance = 2 * (cam.position.y - waterHeight);

        // Save current cam positions
        Vector3 camPos = cam.position.cpy();
        Vector3 camDir = cam.direction.cpy();

        // Position camera for reflection capture
        cam.direction.scl(1, -1, 1).nor();
        cam.position.sub(0, camReflectionDistance, 0);
        cam.update();

        // Render reflections to FBO
        fboWaterReflection.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.begin(cam);
        sceneGraph.render(delta, clippingPlaneReflection, -waterHeight + distortionEdgeCorrection);
        batch.end();
        fboWaterReflection.end();

        // Restore camera positions
        cam.direction.set(camDir);
        cam.position.set(camPos);
        cam.update();
    }

    private void captureRefractionFBO(float delta) {
        // Render refractions to FBO
        fboWaterRefraction.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.begin(cam);
        sceneGraph.render(delta, clippingPlaneRefraction, waterHeight + distortionEdgeCorrection);
        batch.end();
        fboWaterRefraction.end();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setWaterResolution(WaterResolution resolution) {
        this.waterResolution = resolution;
        Vector2 res = waterResolution.getResolutionValues();
        initFrameBuffers((int) res.x, (int) res.y);
    }

    @Override
    public void dispose() {
        if (skybox != null) {
            skybox.dispose();
        }
    }
}
