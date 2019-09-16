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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.meta.StudioFacet;
import com.haulmont.cuba.gui.meta.StudioProperties;
import com.haulmont.cuba.gui.meta.StudioProperty;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.sys.UiControllerProperty;

import java.util.Collection;

/**
 * Prepares and shows screens.
 */
@StudioFacet(
        caption = "Screen",
        description = "Prepares and shows screens",
        defaultProperty = "screen"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", required = true)
        }
)
public interface ScreenFacet<T extends Screen> extends Facet {

    /**
     * Sets the id of screen to be opened.
     * @param screenId screen id
     */
    void setScreenId(String screenId);

    /**
     * @return screen id
     */
    String getScreenId();

    /**
     * Sets how the screen should be opened
     * @param launchMode launch mode
     */
    void setLaunchMode(Screens.LaunchMode launchMode);

    /**
     * @return how the screen should be opened
     */
    Screens.LaunchMode getLaunchMode();

    /**
     * Sets properties that will be injected into opened screen via public setters.
     * @param properties screen properties
     */
    void setProperties(Collection<UiControllerProperty> properties);

    /**
     * @return properties that will be injected into opened screen via public setters.
     */
    Collection<UiControllerProperty> getProperties();

    /**
     * @return id of action that triggers screen
     */
    String getActionTarget();

    /**
     * Sets that screen should be shown when action with id {@code actionId} is performed.
     *
     * @param actionId action id
     */
    void setActionTarget(String actionId);

    /**
     * @return id of button that triggers screen
     */
    String getButtonTarget();

    /**
     * Sets that screen should be shown when button with id {@code actionId} is clicked.
     *
     * @param buttonId button id
     */
    void setButtonTarget(String buttonId);

    /**
     * @return new screen instance
     */
    T create();

    /**
     * Shows and returns screen.
     */
    T show();
}
