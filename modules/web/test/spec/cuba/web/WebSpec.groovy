/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.cuba.web

import com.haulmont.cuba.client.ClientUserSession
import com.haulmont.cuba.client.testsupport.TestUserSessionSource
import com.haulmont.cuba.core.app.PersistenceManagerService
import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.UiComponents
import com.haulmont.cuba.gui.model.DataComponents
import com.haulmont.cuba.gui.theme.ThemeConstants
import com.haulmont.cuba.security.global.UserSession
import com.haulmont.cuba.web.App
import com.haulmont.cuba.web.AppUI
import com.haulmont.cuba.web.Connection
import com.haulmont.cuba.web.DefaultApp
import com.haulmont.cuba.web.container.CubaTestContainer
import com.haulmont.cuba.web.security.ConnectionImpl
import com.haulmont.cuba.web.sys.AppCookies
import com.haulmont.cuba.web.testsupport.TestContainer
import com.haulmont.cuba.web.testsupport.proxy.TestServiceProxy
import com.haulmont.cuba.web.testsupport.ui.TestConnectorTracker
import com.haulmont.cuba.web.testsupport.ui.TestPersistenceManagerService
import com.haulmont.cuba.web.testsupport.ui.TestVaadinRequest
import com.haulmont.cuba.web.testsupport.ui.TestVaadinSession
import com.vaadin.server.VaadinSession
import com.vaadin.server.WebBrowser
import com.vaadin.ui.UI
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField

class WebSpec extends TestContainerSpecification {

    Metadata metadata
    MetadataTools metadataTools
    ViewRepository viewRepository
    EntityStates entityStates
    DataManager dataManager
    DataComponents dataComponents
    UiComponents uiComponents

    TestUserSessionSource sessionSource

    AppUI vaadinUi

    @SuppressWarnings("GroovyAccessibility")
    void setup() {
        metadata = cont.getBean(Metadata)
        metadataTools = cont.getBean(MetadataTools)
        viewRepository = cont.getBean(ViewRepository)
        entityStates = cont.getBean(EntityStates)
        dataManager = cont.getBean(DataManager)
        dataComponents = cont.getBean(DataComponents)
        uiComponents = cont.getBean(UiComponents)

        sessionSource = cont.getBean(UserSessionSource) as TestUserSessionSource

        def serverSession = sessionSource.createTestSession()
        def session = new ClientUserSession(serverSession)
        session.setAuthenticated(false)

        sessionSource.setSession(session)

        // all the rest is required for web components

        TestServiceProxy.mock(PersistenceManagerService, new TestPersistenceManagerService())

        def injectFactory = cont.getApplicationContext().getAutowireCapableBeanFactory()

        def app = new DefaultApp()
        app.themeConstants = new ThemeConstants([:])
        app.cookies = new AppCookies()

        def connection = new ConnectionImpl()
        injectFactory.autowireBean(connection)

        app.connection = connection

        def vaadinSession = new TestVaadinSession(new WebBrowser(), Locale.ENGLISH)

        vaadinSession.setAttribute(App.class, app)
        vaadinSession.setAttribute(App.NAME, app)
        vaadinSession.setAttribute(Connection.class, connection)
        vaadinSession.setAttribute(Connection.NAME, connection)
        vaadinSession.setAttribute(UserSession.class, sessionSource.getSession())

        VaadinSession.setCurrent(vaadinSession)

        injectFactory.autowireBean(app)

        vaadinUi = new AppUI()
        injectFactory.autowireBean(vaadinUi)

        def connectorTracker = new TestConnectorTracker(vaadinUi)
        getDeclaredField(UI.class, "connectorTracker", true)
            .set(vaadinUi, connectorTracker)
        getDeclaredField(UI.class, "session", true)
            .set(vaadinUi, vaadinSession)

        UI.setCurrent(vaadinUi)

        def vaadinRequest = new TestVaadinRequest()
        vaadinUi.getPage().init(vaadinRequest)
        vaadinUi.init(vaadinRequest)
    }

    void cleanup() {
        TestServiceProxy.clear()

        UI.setCurrent(null)

        sessionSource.setSession(null)
    }
}