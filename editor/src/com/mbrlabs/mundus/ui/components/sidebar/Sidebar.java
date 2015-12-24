/*
 * Copyright (c) 2015. See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mbrlabs.mundus.ui.components.sidebar;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneListener;
import org.lwjgl.openal.AL10;

/**
 * @author Marcus Brummer
 * @version 30-11-2015
 */
public class Sidebar extends TabbedPane implements TabbedPaneListener {

    private TerrainTab terrainTab;
    private EntityTab entityTab;
    private ModelTab modelTab;

    private VisTable contentContainer;

    public Sidebar() {
        super();
        terrainTab = new TerrainTab();
        entityTab = new EntityTab();
        modelTab = new ModelTab();
        contentContainer = new VisTable();
        contentContainer.setBackground(VisUI.getSkin().getDrawable("default-pane"));
        contentContainer.align(Align.topLeft);

        add(modelTab);
        add(terrainTab);
        add(entityTab);

        switchTab(modelTab);
        switchedTab(modelTab);

        addListener(this);
    }

    public TerrainTab getTerrainTab() {
        return terrainTab;
    }

    public EntityTab getEntityTab() {
        return entityTab;
    }

    public ModelTab getModelTab() {
        return modelTab;
    }

    public VisTable getContentContainer() {
        return contentContainer;
    }

    @Override
    public void switchedTab(Tab tab) {
        contentContainer.clear();
        contentContainer.add(tab.getContentTable()).fill().expand();
    }

    @Override
    public void removedTab(Tab tab) {
        // we don't remove tabs from the sidebar
    }

    @Override
    public void removedAllTabs() {
        // we don't remove tabs from the sidebar
    }

}
