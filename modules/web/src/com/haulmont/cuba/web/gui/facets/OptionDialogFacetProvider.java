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
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.OptionDialog;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.theme.ThemeConstants;
import com.haulmont.cuba.gui.theme.ThemeConstantsManager;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import com.haulmont.cuba.web.gui.components.AbstractDialogFacetProvider;
import com.haulmont.cuba.web.gui.components.WebOptionDialog;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.haulmont.cuba.gui.icons.Icons.ICON_NAME_REGEX;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class OptionDialogFacetProvider extends AbstractDialogFacetProvider<OptionDialog> {

    @Inject
    protected MessageTools messageTools;
    @Inject
    protected Icons icons;
    @Inject
    protected ThemeConstantsManager themeConstantsManager;

    @Override
    public Class<OptionDialog> getFacetClass() {
        return OptionDialog.class;
    }

    @Override
    public OptionDialog create() {
        return new WebOptionDialog();
    }

    @Override
    public String getFacetTag() {
        return "optionDialog";
    }

    @Override
    public void loadFromXml(OptionDialog facet, Element element, ComponentLoader.ComponentContext context) {
        super.loadFromXml(facet, element, context);

        loadActions(facet, element, context);
    }

    protected void loadActions(OptionDialog facet, Element element,
                               ComponentLoader.ComponentContext context) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        List<OptionDialog.DialogAction> actions = actionsEl.elements("action")
                .stream()
                .map(el -> loadAction(el, context))
                .collect(Collectors.toList());

        facet.setActions(actions);
    }

    protected OptionDialog.DialogAction loadAction(Element element, ComponentLoader.ComponentContext context) {
        Class<? extends Frame> frameClass = context.getFrame().getClass();

        String id = element.attributeValue("id");
        String caption = loadResourceString(frameClass, element.attributeValue("caption"));
        String description = loadResourceString(frameClass, element.attributeValue("description"));
        String icon = getIconPath(frameClass, element.attributeValue("icon"));
        boolean primary = Boolean.parseBoolean(element.attributeValue("primary"));

        return new OptionDialog.DialogAction(id, caption, description, icon, primary);
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
