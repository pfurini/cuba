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
import com.haulmont.cuba.gui.GuiDevelopmentException;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.ActionsHolder;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.ScreenFacet;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiControllerUtils;
import com.haulmont.cuba.gui.sys.UiControllerProperty;
import com.haulmont.cuba.gui.sys.UiControllerPropertyInjector;
import com.haulmont.cuba.web.gui.WebAbstractFacet;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Collection;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class WebScreenFacet<T extends Screen> extends WebAbstractFacet
        implements ScreenFacet<T>, BeanLocatorAware {

    protected BeanLocator beanLocator;

    protected String screenId;
    protected Screens.LaunchMode launchMode = OpenMode.NEW_TAB;
    protected Collection<UiControllerProperty> properties;

    protected T screen;

    protected String actionId;
    protected String buttonId;

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
    public String getActionTarget() {
        return actionId;
    }

    @Override
    public void setActionTarget(String actionId) {
        this.actionId = actionId;
    }

    @Override
    public String getButtonTarget() {
        return buttonId;
    }

    @Override
    public void setButtonTarget(String buttonId) {
        this.buttonId = buttonId;
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        subscribe();
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

        if (CollectionUtils.isNotEmpty(properties)) {
            UiControllerPropertyInjector injector =
                    beanLocator.getPrototype(UiControllerPropertyInjector.NAME,
                            screen, owner.getFrameOwner(), properties);
            injector.inject();
        }

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

    protected void subscribe() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Notification is not attached to Frame");
        }

        if (isNotEmpty(actionId) && isNotEmpty(buttonId)) {
            throw new GuiDevelopmentException(
                    "Notification facet should have either action or button target", owner.getId());
        }

        if (isNotEmpty(actionId)) {
            Action action = owner.getAction(actionId);

            if (action == null) {
                String postfixActionId = null;
                int dotIdx = actionId.indexOf('.');
                if (dotIdx > 0) {
                    postfixActionId = actionId.substring(dotIdx + 1);
                }

                for (Component c : owner.getComponents()) {
                    if (c instanceof ActionsHolder) {
                        ActionsHolder actionsHolder = (ActionsHolder) c;
                        action = actionsHolder.getAction(actionId);
                        if (action == null) {
                            action = actionsHolder.getAction(postfixActionId);
                        }
                        if (action != null) {
                            break;
                        }
                    }
                }
            }

            if (!(action instanceof BaseAction)) {
                throw new GuiDevelopmentException(
                        String.format("Unable to find Notification target button with id '%s'", actionId),
                        owner.getId());
            }

            ((BaseAction) action).addActionPerformedListener(e -> show());
        } else if (isNotEmpty(buttonId)) {
            Component component = owner.getComponent(buttonId);
            if (!(component instanceof Button)) {
                throw new GuiDevelopmentException(
                        String.format("Unable to find Notification target button with id '%s'", buttonId),
                        owner.getId());
            }
            ((Button) component).addClickListener(e -> show());
        }
    }
}
