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
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.components.ContentMode;
import com.haulmont.cuba.gui.components.Notification;
import com.haulmont.cuba.gui.xml.FacetProvider;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import com.haulmont.cuba.web.gui.components.WebNotification;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class NotificationFacetProvider implements FacetProvider<Notification> {

    @Inject
    protected MessageTools messageTools;

    @Override
    public Class<Notification> getFacetClass() {
        return Notification.class;
    }

    @Override
    public Notification create() {
        return new WebNotification();
    }

    @Override
    public String getFacetTag() {
        return "notification";
    }

    @Override
    public void loadFromXml(Notification facet, Element element,
                            ComponentLoader.ComponentContext context) {
        loadId(facet, element);
        loadCaption(facet, element, context);
        loadMessage(facet, element, context);
        loadType(facet, element);
        loadDelay(facet, element);
        loadContentMode(facet, element);
        loadStyleName(facet, element);
        loadPosition(facet, element);
    }

    protected void loadId(Notification facet, Element element) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            facet.setId(id);
        }
    }

    protected void loadCaption(Notification facet, Element element,
                               ComponentLoader.ComponentContext context) {
        String caption = element.attributeValue("caption");
        if (isNotEmpty(caption)) {
            facet.setCaption(loadResourceString(context.getFrame().getClass(), caption));
        }
    }

    protected void loadMessage(Notification facet, Element element,
                               ComponentLoader.ComponentContext context) {
        String message = element.attributeValue("message");
        if (isNotEmpty(message)) {
            facet.setMessage(loadResourceString(context.getFrame().getClass(), message));
        }
    }

    protected void loadType(Notification facet, Element element) {
        String type = element.attributeValue("type");
        if (isNotEmpty(type)) {
            facet.setType(Notifications.NotificationType.valueOf(type));
        }
    }

    protected void loadDelay(Notification facet, Element element) {
        String delay = element.attributeValue("delay");
        if (isNotEmpty(delay)) {
            facet.setDelay(Integer.parseInt(delay));
        }
    }

    protected void loadContentMode(Notification facet, Element element) {
        String contentMode = element.attributeValue("contentMode");
        if (isNotEmpty(contentMode)) {
            facet.setContentMode(ContentMode.valueOf(contentMode));
        }
    }

    protected void loadStyleName(Notification facet, Element element) {
        String styleName = element.attributeValue("styleName");
        if (isNotEmpty(styleName)) {
            facet.setStyleName(styleName);
        }
    }

    protected void loadPosition(Notification facet, Element element) {
        String position = element.attributeValue("position");
        if (isNotEmpty(position)) {
            facet.setPosition(Notifications.Position.valueOf(position));
        }
    }

    protected String loadResourceString(Class frameClass, String caption) {
        if (isEmpty(caption)) {
            return caption;
        }
        return messageTools.loadString(frameClass.getPackage().getName(), caption);
    }
}
