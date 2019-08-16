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

package com.haulmont.cuba.web.gui.facets;

import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.components.ContentMode;
import com.haulmont.cuba.gui.components.Dialog;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.gui.theme.ThemeConstantsManager;
import com.haulmont.cuba.gui.xml.FacetProvider;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import com.haulmont.cuba.web.gui.components.WebDialog;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.haulmont.cuba.gui.icons.Icons.ICON_NAME_REGEX;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class DialogFacetProvider implements FacetProvider<Dialog> {

    @Inject
    protected MessageTools messageTools;
    @Inject
    protected Icons icons;
    @Inject
    protected ThemeConstantsManager themeConstantsManager;

    @Override
    public Class<Dialog> getFacetClass() {
        return Dialog.class;
    }

    @Override
    public Dialog create() {
        return new WebDialog();
    }

    @Override
    public String getFacetTag() {
        return "dialog";
    }

    @Override
    public void loadFromXml(Dialog facet, Element element, ComponentLoader.ComponentContext context) {
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

        loadActions(facet, element, context);
    }

    protected void loadId(Dialog facet, Element element) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            facet.setId(id);
        }
    }

    protected void loadCaption(Dialog facet, Element element, ComponentLoader.ComponentContext context) {
        String caption = element.attributeValue("caption");
        if (isNotEmpty(caption)) {
            facet.setCaption(loadResourceString(context.getFrame().getClass(), caption));
        }
    }

    protected void loadMessage(Dialog facet, Element element, ComponentLoader.ComponentContext context) {
        String message = element.attributeValue("message");
        if (isNotEmpty(message)) {
            facet.setMessage(loadResourceString(context.getFrame().getClass(), message));
        }
    }

    protected void loadType(Dialog facet, Element element) {
        String type = element.attributeValue("type");
        if (isNotEmpty(type)) {
            facet.setType(Dialogs.MessageType.valueOf(type));
        }
    }

    protected void loadWidth(Dialog facet, Element element) {
        String width = element.attributeValue("width");
        if (isNotEmpty(width)) {
            facet.setWidth(width);
        }
    }

    protected void loadHeight(Dialog facet, Element element) {
        String height = element.attributeValue("height");
        if (isNotEmpty(height)) {
            facet.setHeight(height);
        }
    }

    protected void loadContentMode(Dialog facet, Element element) {
        String contentMode = element.attributeValue("contentMode");
        if (isNotEmpty(contentMode)) {
            facet.setContentMode(ContentMode.valueOf(contentMode));
        }
    }

    protected void loadMaximized(Dialog facet, Element element) {
        String maximized = element.attributeValue("maximized");
        if (isNotEmpty(maximized)) {
            facet.setMaximized(Boolean.parseBoolean(maximized));
        }
    }

    protected void loadModal(Dialog facet, Element element) {
        String modal = element.attributeValue("modal");
        if (isNotEmpty(modal)) {
            facet.setModal(Boolean.parseBoolean(modal));
        }

    }

    protected void loadStyleName(Dialog facet, Element element) {
        String styleName = element.attributeValue("styleName");
        if (isNotEmpty(styleName)) {
            facet.setStyleName(styleName);
        }
    }

    protected void loadActions(Dialog facet, Element element,
                               ComponentLoader.ComponentContext context) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        List<Dialog.DialogAction> actions = actionsEl.elements("action")
                .stream()
                .map(el -> loadAction(el, context))
                .collect(Collectors.toList());

        facet.setActions(actions);
    }

    protected Dialog.DialogAction loadAction(Element element, ComponentLoader.ComponentContext context) {
        Class<? extends Frame> frameClass = context.getFrame().getClass();

        String id = element.attributeValue("id");
        String caption = loadResourceString(frameClass, element.attributeValue("caption"));
        String description = loadResourceString(frameClass, element.attributeValue("description"));
        String icon = getIconPath(frameClass, element.attributeValue("icon"));
        boolean primary = Boolean.parseBoolean(element.attributeValue("primary"));

        return new Dialog.DialogAction(id, caption, description, icon, primary);
    }

    protected String loadResourceString(Class frameClass, String caption) {
        if (isEmpty(caption)) {
            return caption;
        }

        return messageTools.loadString(frameClass.getPackage().getName(), caption);
    }

    protected String getIconPath(Class<? extends Frame> frameClass, String icon) {
        if (icon == null || icon.isEmpty()) {
            return null;
        }

        String iconPath = null;

        if (ICON_NAME_REGEX.matcher(icon).matches()) {
            iconPath = icons.get(icon);
        }

        if (isEmpty(iconPath)) {
            String themeValue = loadThemeString(icon);
            iconPath = loadResourceString(frameClass, themeValue);
        }

        return iconPath;
    }

    protected String loadThemeString(String value) {
        if (value != null && value.startsWith(ThemeConstants.PREFIX)) {
            value = themeConstantsManager.getConstants()
                    .get(value.substring(ThemeConstants.PREFIX.length()));
        }
        return value;
    }
}
