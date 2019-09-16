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

import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.GuiDevelopmentException;
import com.haulmont.cuba.gui.components.ContentMode;
import com.haulmont.cuba.gui.components.DialogFacet;
import com.haulmont.cuba.gui.xml.FacetProvider;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import org.dom4j.Element;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class AbstractDialogFacetProvider<T extends DialogFacet>
        implements FacetProvider<T> {

    @Override
    public void loadFromXml(T facet, Element element, ComponentLoader.ComponentContext context) {
        loadId(facet, element);
        loadCaption(facet, element, context);
        loadMessage(facet, element, context);
        loadType(facet, element);

        loadWidth(facet, element);
        loadHeight(facet, element);

        loadContentMode(facet, element);
        loadMaximized(facet, element);
        loadModal(facet, element);
        loadStyleName(facet, element);

        loadTarget(facet, element, context);
    }

    protected void loadId(T facet, Element element) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            facet.setId(id);
        }
    }

    protected void loadCaption(T facet, Element element, ComponentLoader.ComponentContext context) {
        String caption = element.attributeValue("caption");
        if (isNotEmpty(caption)) {
            facet.setCaption(loadResourceString(context.getFrame().getClass(), caption));
        }
    }

    protected void loadMessage(T facet, Element element, ComponentLoader.ComponentContext context) {
        String message = element.attributeValue("message");
        if (isNotEmpty(message)) {
            facet.setMessage(loadResourceString(context.getFrame().getClass(), message));
        }
    }

    protected void loadType(T facet, Element element) {
        String type = element.attributeValue("type");
        if (isNotEmpty(type)) {
            facet.setType(Dialogs.MessageType.valueOf(type));
        }
    }

    protected void loadWidth(T facet, Element element) {
        String width = element.attributeValue("width");
        if (isNotEmpty(width)) {
            facet.setWidth(width);
        }
    }

    protected void loadHeight(T facet, Element element) {
        String height = element.attributeValue("height");
        if (isNotEmpty(height)) {
            facet.setHeight(height);
        }
    }

    protected void loadContentMode(T facet, Element element) {
        String contentMode = element.attributeValue("contentMode");
        if (isNotEmpty(contentMode)) {
            facet.setContentMode(ContentMode.valueOf(contentMode));
        }
    }

    protected void loadMaximized(T facet, Element element) {
        String maximized = element.attributeValue("maximized");
        if (isNotEmpty(maximized)) {
            facet.setMaximized(Boolean.parseBoolean(maximized));
        }
    }

    protected void loadModal(T facet, Element element) {
        String modal = element.attributeValue("modal");
        if (isNotEmpty(modal)) {
            facet.setModal(Boolean.parseBoolean(modal));
        }

    }

    protected void loadStyleName(T facet, Element element) {
        String styleName = element.attributeValue("styleName");
        if (isNotEmpty(styleName)) {
            facet.setStyleName(styleName);
        }
    }

    protected void loadTarget(T facet, Element element,
                              ComponentLoader.ComponentContext context) {
        String actionTarget = element.attributeValue("action");
        String buttonTarget = element.attributeValue("button");

        if (isNotEmpty(actionTarget) && isNotEmpty(buttonTarget)) {
            throw new GuiDevelopmentException(
                    "Dialog facet should have either action or button target",
                    context);
        }

        if (isNotEmpty(actionTarget)) {
            facet.setActionTarget(actionTarget);
        } else if (isNotEmpty(buttonTarget)) {
            facet.setButtonTarget(buttonTarget);
        }
    }

    protected abstract String loadResourceString(Class frameClass, String caption);
}
