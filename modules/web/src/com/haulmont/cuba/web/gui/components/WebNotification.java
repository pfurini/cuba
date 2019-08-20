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

import com.haulmont.bali.events.Subscription;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.Notifications.NotificationType;
import com.haulmont.cuba.gui.components.ContentMode;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Notification;
import com.haulmont.cuba.gui.screen.UiControllerUtils;
import com.haulmont.cuba.web.gui.WebAbstractFacet;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WebNotification extends WebAbstractFacet implements Notification {

    protected String caption;
    protected String description;
    protected int delayMs = 3000;
    protected String styleName;
    protected NotificationType type = NotificationType.HUMANIZED;
    protected ContentMode contentMode = ContentMode.TEXT;
    protected Notifications.Position position = Notifications.Position.DEFAULT;

    protected Supplier<String> messageProvider;

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setType(NotificationType type) {
        this.type = type;
    }

    @Override
    public NotificationType getType() {
        return type;
    }

    @Override
    public void setDelay(int delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public int getDelay() {
        return delayMs;
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
    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    @Override
    public String getStyleName() {
        return styleName;
    }

    @Override
    public void setPosition(Notifications.Position position) {
        this.position = position;
    }

    @Override
    public Notifications.Position getPosition() {
        return position;
    }

    @Override
    public Subscription addCloseListener(Consumer<CloseEvent> listener) {
        return getEventHub().subscribe(CloseEvent.class, listener);
    }

    @Override
    public void setMessageProvider(Supplier<String> messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public Supplier<String> getMessageProvider() {
        return messageProvider;
    }

    @Override
    public void show() {
        Frame owner = getOwner();
        if (owner == null) {
            throw new IllegalStateException("Notification is not attached to Frame");
        }

        Notifications notifications = UiControllerUtils.getScreenContext(owner.getFrameOwner())
                .getNotifications();

        String description = this.description;
        if (messageProvider != null) {
            description = messageProvider.get();
        }

        notifications.create(type)
                .withCaption(caption)
                .withDescription(description)
                .withHideDelayMs(delayMs)
                .withContentMode(contentMode)
                .withStyleName(styleName)
                .withPosition(position)
                .withCloseListener(e -> publish(CloseEvent.class, new CloseEvent(this)))
                .show();
    }
}
