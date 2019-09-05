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

public class WebScreenFacet<T extends Screen> extends WebAbstractFacet
        implements ScreenFacet<T>, BeanLocatorAware {

    protected BeanLocator beanLocator;

    protected String screenId;
    protected Screens.LaunchMode launchMode = OpenMode.NEW_TAB;
    protected Collection<UiControllerProperty> properties;

    protected T screen;

    @Override
    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    @Override
    public String getScreenId() {
        return screenId;
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
    public T create() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Screen facet is not attached to Frame");
        }

        //noinspection unchecked
        screen = (T) UiControllerUtils.getScreenContext(owner.getFrameOwner())
                .getScreens()
                .create(this.screenId, launchMode);

        UiControllerPropertyInjector injector =
                beanLocator.getPrototype(UiControllerPropertyInjector.NAME,
                        screen, owner.getFrameOwner(), properties);
        injector.inject();

        return screen;
    }

    @Override
    public T show() {
        //noinspection unchecked
        return (T) create().show();
    }

    @Override
    public void setBeanLocator(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }
}
