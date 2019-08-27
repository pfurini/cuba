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

package spec.cuba.web.notification.screens;


import com.haulmont.cuba.gui.components.Notification;
import com.haulmont.cuba.gui.screen.*;

import javax.inject.Inject;

@UiController
@UiDescriptor("screen-with-notification.xml")
public class ScreenWithNotification extends Screen {

    @Inject
    public Notification testNotification;

    public boolean providerTriggered = false;
    public boolean closeEvtFired = false;

    @Install(subject = "descriptionProvider", to = "testNotification")
    public String getNotificationDescription() {
        providerTriggered = true;
        return "Description from provider";
    }

    @Subscribe("testNotification")
    public void onNotificationClosed(Notification.CloseEvent e) {
        closeEvtFired = true;
    }
}
