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
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.web.gui.WebAbstractFacet;

/**
 * Base class for web implementations of {@link DialogFacet}.
 *
 * @see MessageDialog
 * @see OptionDialog
 */
public abstract class WebAbstractDialog extends WebAbstractFacet implements DialogFacet {

    protected String caption;
    protected String message;
    protected SizeWithUnit width;
    protected SizeWithUnit height;
    protected boolean maximized;
    protected boolean modal;
    protected String styleName;
    protected ContentMode contentMode = ContentMode.TEXT;
    protected Dialogs.MessageType type = Dialogs.MessageType.CONFIRMATION;

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setType(Dialogs.MessageType type) {
        this.type = type;
    }

    @Override
    public Dialogs.MessageType getType() {
        return type;
    }

    @Override
    public void setContentMode(ContentMode contentMode) {
        this.contentMode = contentMode;
    }

    @Override
    public ContentMode getContentMode() {
        return contentMode;
    }

    @Override
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    @Override
    public boolean isMaximized() {
        return maximized;
    }

    @Override
    public void setModal(boolean modal) {
        this.modal = modal;
    }

    @Override
    public boolean isModal() {
        return modal;
    }

    @Override
    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    @Override
    public String getStyleName() {
        return styleName;
    }

    @Override
    public void setWidth(String width) {
        this.width = SizeWithUnit.parseStringSize(width);
    }

    @Override
    public float getWidth() {
        return width.getSize();
    }

    @Override
    public SizeUnit getWidthSizeUnit() {
        return width.getUnit();
    }

    @Override
    public void setHeight(String height) {
        this.height = SizeWithUnit.parseStringSize(height);
    }

    @Override
    public float getHeight() {
        return height.getSize();
    }

    @Override
    public SizeUnit getHeightSizeUnit() {
        return height.getUnit();
    }
}
