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
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.OptionDialog;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.screen.UiControllerUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class WebOptionDialog extends WebAbstractDialog implements OptionDialog {

    protected Collection<DialogAction> actions;

    @Override
    public void setActions(Collection<DialogAction> actions) {
        this.actions = actions;
    }

    @Override
    public void show() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("OptionDialog is not attached to Frame");
        }

        Dialogs.OptionDialogBuilder builder = UiControllerUtils.getScreenContext(owner.getFrameOwner())
                .getDialogs()
                .createOptionDialog(type);

        if (width != null) {
            builder.withWidth(width.stringValue());
        }
        if (height != null) {
            builder.withHeight(height.stringValue());
        }

        builder.withCaption(caption)
                .withMessage(message)
                .withContentMode(contentMode)
                .withMaximized(maximized)
                .withModal(modal)
                .withStyleName(styleName)
                .withActions(createActions())
                .show();
    }

    protected Action[] createActions() {
        if (actions == null) {
            return new Action[]{};
        }
        return actions.stream()
                    .map(this::createAction)
                    .collect(Collectors.toList())
                    .toArray(new Action[]{});
    }

    protected BaseAction createAction(DialogAction action) {
        return new BaseAction(action.getId())
                .withCaption(action.getCaption())
                .withDescription(action.getDescription())
                .withIcon(action.getIcon())
                .withPrimary(action.isPrimary())
                .withHandler(evt -> {
                    if (action.getActionHandler() != null) {
                        action.getActionHandler().accept(
                                new DialogActionPerformedEvent(this, action));
                    }
                });
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        if (actions == null) {
            return null;
        }
        return actions.stream()
                .filter(action -> Objects.equals(action.getId(), name))
                .findFirst()
                .orElse(null);
    }
}
