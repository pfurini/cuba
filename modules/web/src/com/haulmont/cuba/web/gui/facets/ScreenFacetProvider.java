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

import com.haulmont.cuba.core.global.BeanLocator;
import com.haulmont.cuba.gui.GuiDevelopmentException;
import com.haulmont.cuba.gui.components.ScreenFacet;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.sys.UiControllerProperty;
import com.haulmont.cuba.gui.xml.FacetProvider;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import com.haulmont.cuba.web.gui.components.WebScreenFacet;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class ScreenFacetProvider implements FacetProvider<ScreenFacet> {

    @Inject
    protected BeanLocator beanLocator;

    @Override
    public Class<ScreenFacet> getFacetClass() {
        return ScreenFacet.class;
    }

    @Override
    public ScreenFacet create() {
        WebScreenFacet screenFacet = new WebScreenFacet();
        screenFacet.setBeanLocator(beanLocator);
        return screenFacet;
    }

    @Override
    public String getFacetTag() {
        return "screen";
    }

    @Override
    public void loadFromXml(ScreenFacet facet, Element element,
                            ComponentLoader.ComponentContext context) {
        loadId(facet, element);
        loadScreen(facet, element);
        loadOpenMode(facet, element);
        loadProperties(facet, element, context);
    }

    protected void loadId(ScreenFacet facet, Element element) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            facet.setId(id);
        }
    }

    protected void loadScreen(ScreenFacet facet, Element element) {
        String screen = element.attributeValue("screen");
        if (isNotEmpty(screen)) {
            facet.setScreenId(screen);
        }
    }

    protected void loadOpenMode(ScreenFacet facet, Element element) {
        String openMode = element.attributeValue("openMode");
        if (isNotEmpty(openMode)) {
            facet.setLaunchMode(OpenMode.valueOf(openMode));
        }
    }

    protected void loadProperties(ScreenFacet facet, Element element,
                                  ComponentLoader.ComponentContext context) {
        Element propsEl = element.element("properties");
        if (propsEl == null) {
            return;
        }

        List<Element> propElements = propsEl.elements("property");
        if (propElements.isEmpty()) {
            return;
        }

        List<UiControllerProperty> properties = new ArrayList<>(propElements.size());

        for (Element property : propElements) {
            String name = property.attributeValue("name");
            if (name == null || name.isEmpty()) {
                throw new GuiDevelopmentException("Screen fragment property cannot have empty name", context);
            }

            String value = property.attributeValue("value");
            String ref = property.attributeValue("ref");

            if (StringUtils.isNotEmpty(value) && StringUtils.isNotEmpty(ref)) {
                throw new GuiDevelopmentException("Screen fragment property can have either a value or a reference. Property: " +
                        name, context);
            }

            if (StringUtils.isNotEmpty(value)) {
                properties.add(new UiControllerProperty(name, value, UiControllerProperty.Type.VALUE));
            } else if (StringUtils.isNotEmpty(ref)) {
                properties.add(new UiControllerProperty(name, ref, UiControllerProperty.Type.REFERENCE));
            } else {
                throw new GuiDevelopmentException("No value or reference found for screen fragment property: " + name,
                        context);
            }
        }

        facet.setProperties(properties);
    }
}
