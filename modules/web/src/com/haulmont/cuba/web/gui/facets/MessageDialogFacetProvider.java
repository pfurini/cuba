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
import com.haulmont.cuba.gui.components.MessageDialog;
import com.haulmont.cuba.gui.xml.layout.ComponentLoader;
import com.haulmont.cuba.web.gui.components.AbstractDialogFacetProvider;
import com.haulmont.cuba.web.gui.components.WebMessageDialog;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class MessageDialogFacetProvider extends AbstractDialogFacetProvider<MessageDialog> {

    @Inject
    protected MessageTools messageTools;

    @Override
    public Class<MessageDialog> getFacetClass() {
        return MessageDialog.class;
    }

    @Override
    public MessageDialog create() {
        return new WebMessageDialog();
    }

    @Override
    public String getFacetTag() {
        return "messageDialog";
    }

    @Override
    public void loadFromXml(MessageDialog facet, Element element, ComponentLoader.ComponentContext context) {
        super.loadFromXml(facet, element, context);

        loadCloseOnClickOutside(facet, element);
    }

    protected void loadCloseOnClickOutside(MessageDialog facet, Element element) {
        String closeOnClickOutside = element.attributeValue("closeOnClickOutside");
        if (isNotEmpty(closeOnClickOutside)) {
            facet.setCloseOnClickOutside(Boolean.parseBoolean(closeOnClickOutside));
        }
    }

    protected String loadResourceString(Class frameClass, String caption) {
        if (isEmpty(caption)) {
            return caption;
        }
        return messageTools.loadString(frameClass.getPackage().getName(), caption);
    }
}
