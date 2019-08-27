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

package spec.cuba.web.notification

import com.haulmont.cuba.gui.Notifications
import com.haulmont.cuba.gui.components.ContentMode
import com.haulmont.cuba.gui.screen.OpenMode
import com.vaadin.server.Extension
import com.vaadin.ui.Notification
import spec.cuba.web.UiScreenSpec
import spec.cuba.web.notification.screens.ScreenWithNotification

class NotificationFacetTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.cuba.web.notification.screens'])
    }

    def 'Notification facet attributes are applied'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('mainWindow', OpenMode.ROOT)
        screens.show(mainWindow)

        def screenWithNotification = screens.create(ScreenWithNotification)

        when: 'Notification is configured in XML'

        screenWithNotification.show()
        def notification = screenWithNotification.testNotification

        then: 'Attribute values are propagated to notification facet'

        notification.id == 'testNotification'
        notification.type == Notifications.NotificationType.HUMANIZED
        notification.caption == 'Notification Facet Test'
        notification.description == 'Description from XML'
        notification.contentMode == ContentMode.HTML
        notification.delay == 3000
        notification.position == Notifications.Position.TOP_CENTER
        notification.styleName == 'notification-facet-style'
    }

    def 'Notification facet install and subscribe handlers'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def screenWithNotification = screens.create(ScreenWithNotification)
        screenWithNotification.show()

        def notification = screenWithNotification.testNotification

        when: 'Notification is shown'

        notification.show()

        then: 'Description provider is triggered'

        screenWithNotification.providerTriggered

        when: 'All notifications are closed'

        closeAllNotifications()

        then: 'CloseEvent is fired'

        screenWithNotification.closeEvtFired
    }

    protected void closeAllNotifications() {
        def notifications = []
        for (Extension ext : vaadinUi.getExtensions()) {
            if (ext instanceof Notification) {
                notifications.push(ext as Notification)
            }
        }
        for (Notification ntf : notifications) {
            ntf.close()
        }
    }
}
