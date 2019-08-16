/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.core.sys.BeanLocatorAware;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.ScreenFacet;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiControllerUtils;
import com.haulmont.cuba.gui.sys.UiControllerProperty;
import com.haulmont.cuba.gui.sys.UiControllerPropertyInjector;
import com.haulmont.cuba.web.gui.WebAbstractFacet;

import java.util.Collection;

public class WebScreenFacet extends WebAbstractFacet implements ScreenFacet, BeanLocatorAware {

    protected BeanLocator beanLocator;

    protected String screen;
    protected Screens.LaunchMode launchMode = OpenMode.NEW_TAB;
    protected Collection<UiControllerProperty> properties;

    @Override
    public void setScreen(String screen) {
        this.screen = screen;
    }

    @Override
    public String getScreen() {
        return screen;
    }

    @Override
    public void setLaunchMode(Screens.LaunchMode launchMode) {
        this.launchMode = launchMode;
    }

    @Override
    public Screens.LaunchMode getLaunchMode() {
        return launchMode;
    }

    @Override
    public void setProperties(Collection<UiControllerProperty> properties) {
        this.properties = properties;
    }

    @Override
    public Collection<UiControllerProperty> getProperties() {
        return properties;
    }

    @Override
    public void show() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Screen facet is not attached to Frame");
        }

        Screen screen = UiControllerUtils.getScreenContext(owner.getFrameOwner())
                .getScreens()
                .create(this.screen, launchMode);

        UiControllerPropertyInjector injector =
                beanLocator.getPrototype(UiControllerPropertyInjector.NAME,
                        screen, owner.getFrameOwner(), properties);
        injector.inject();

        screen.show();
    }

    @Override
    public void setBeanLocator(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }
}
