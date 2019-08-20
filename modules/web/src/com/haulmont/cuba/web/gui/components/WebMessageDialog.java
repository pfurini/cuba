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
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.MessageDialog;
import com.haulmont.cuba.gui.screen.UiControllerUtils;

public class WebMessageDialog extends WebAbstractDialog implements MessageDialog {

    protected boolean closeOnClickOutside;

    @Override
    public void setCloseOnClickOutside(boolean closeOnClickOutside) {
        this.closeOnClickOutside = closeOnClickOutside;
    }

    @Override
    public boolean isCloseOnClickOutside() {
        return closeOnClickOutside;
    }

    @Override
    public void show() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("MessageDialog is not attached to Frame");
        }

        Dialogs.MessageDialogBuilder builder = UiControllerUtils.getScreenContext(owner.getFrameOwner())
                .getDialogs()
                .createMessageDialog(type);

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
                .withCloseOnClickOutside(closeOnClickOutside)
                .show();
    }
}
