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

import com.haulmont.cuba.gui.meta.StudioFacet;
import com.haulmont.cuba.gui.meta.StudioProperties;
import com.haulmont.cuba.gui.meta.StudioProperty;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Prepares and shows option dialogs.
 */
@StudioFacet(
        caption = "Option Dialog",
        description = "Prepares and shows option dialogs",
        defaultProperty = "message"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", required = true)
        }
)
public interface OptionDialog extends DialogFacet, HasSubParts {

    /**
     * Sets dialog actions.
     * @param actions actions
     */
    void setActions(Collection<DialogAction> actions);

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
         */
        public void setActionHandler(Consumer<DialogActionPerformedEvent> actionHandler) {
            this.actionHandler = actionHandler;
        }
    }

    /**
     * The event that is fired when {@link DialogAction#actionHandler} is triggered.
     */
    class DialogActionPerformedEvent {

        protected OptionDialog dialog;
        protected DialogAction dialogAction;

        public DialogActionPerformedEvent(OptionDialog dialog, DialogAction dialogAction) {
            this.dialog = dialog;
            this.dialogAction = dialogAction;
        }

        public OptionDialog getDialog() {
            return dialog;
        }

        public DialogAction getDialogAction() {
            return dialogAction;
        }
    }
}
