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

import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.meta.PropertyType;
import com.haulmont.cuba.gui.meta.StudioFacet;
import com.haulmont.cuba.gui.meta.StudioProperties;
import com.haulmont.cuba.gui.meta.StudioProperty;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Prepares and shows dialogs.
 */
@StudioFacet(
        caption = "Dialog",
        description = "Prepares and shows dialogs",
        defaultProperty = "message"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", required = true)
        }
)
public interface Dialog extends Facet, HasSubParts {

    /**
     * Sets dialog caption.
     * @param caption caption
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setCaption(String caption);

    /**
     * @return dialog caption
     */
    String getCaption();

    /**
     * Sets dialog message.
     * @param message message
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setMessage(String message);

    /**
     * @return dialog message
     */
    String getMessage();

    /**
     * Sets dialog type.
     * @param type type
     */
    @StudioProperty
    void setType(Dialogs.MessageType type);

    /**
     * @return dialog type
     */
    Dialogs.MessageType getType();

    /**
     * Sets dialog message content mode.
     * @param contentMode content mode
     */
    @StudioProperty
    void setContentMode(ContentMode contentMode);

    /**
     * @return dialog message content mode
     */
    ContentMode getContentMode();

    /**
     * Sets whether dialog should be maximized.
     * @param maximized maximized
     */
    @StudioProperty
    void setMaximized(boolean maximized);

    /**
     * @return whether dialog should be maximized
     */
    boolean isMaximized();

    /**
     * Sets whether dialog should be modal
     * @param modal modal
     */
    @StudioProperty
    void setModal(boolean modal);

    /**
     * @return whether dialog should be modal
     */
    boolean isModal();

    /**
     * Sets dialog style name.
     * @param styleName style name
     */
    @StudioProperty
    void setStyleName(String styleName);

    /**
     * @return dialog style name
     */
    String getStyleName();

    /**
     * Sets dialog width.
     * @param width width
     */
    @StudioProperty
    void setWidth(String width);

    /**
     * @return dialog width
     */
    float getWidth();

    /**
     * @return dialog width size unit
     */
    SizeUnit getWidthSizeUnit();

    /**
     * Sets dialog height.
     * @param height height
     */
    @StudioProperty
    void setHeight(String height);

    /**
     * @return dialog height
     */
    float getHeight();

    /**
     * @return dialog height size unit
     */
    SizeUnit getHeightSizeUnit();

    /**
     * Sets dialog actions.
     * @param actions actions
     */
    void setActions(Collection<DialogAction> actions);

    /**
     * Shows dialog.
     */
    void show();

    /**
     * Immutable POJO that stores dialog action settings.
     */
    class DialogAction {

        protected final String id;
        protected final String caption;
        protected final String description;
        protected final String icon;
        protected final boolean primary;

        protected Consumer<DialogActionPerformedEvent> actionHandler;

        public DialogAction(String id, String caption, String description, String icon, boolean primary) {
            this.id = id;
            this.caption = caption;
            this.description = description;
            this.icon = icon;
            this.primary = primary;
        }

        public String getId() {
            return id;
        }

        public String getCaption() {
            return caption;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }

        public boolean isPrimary() {
            return primary;
        }

        public Consumer<DialogActionPerformedEvent> getActionHandler() {
            return actionHandler;
        }

        /**
         * INTERNAL.
         * <p>
         *
         * Intended to set handlers via {@code @Install} annotation.
         * @param actionHandler action handler
         *
         * @noinspection unused
         */
        public void setActionHandler(Consumer<DialogActionPerformedEvent> actionHandler) {
            this.actionHandler = actionHandler;
        }
    }

    /**
     * The event that is fired when {@link DialogAction#actionHandler} is triggered.
     */
    class DialogActionPerformedEvent {

        protected Dialog dialog;
        protected DialogAction dialogAction;

        public DialogActionPerformedEvent(Dialog dialog, DialogAction dialogAction) {
            this.dialog = dialog;
            this.dialogAction = dialogAction;
        }

        public Dialog getDialog() {
            return dialog;
        }

        public DialogAction getDialogAction() {
            return dialogAction;
        }
    }
}
