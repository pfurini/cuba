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
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.web.gui.WebAbstractFacet;

import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

    protected String actionId;
    protected String buttonId;

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

    protected void subscribe() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Notification is not attached to Frame");
        }

        if (isNotEmpty(actionId) && isNotEmpty(buttonId)) {
            throw new GuiDevelopmentException(
                    "Notification facet should have either action or button target",
                    owner.getId());
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
