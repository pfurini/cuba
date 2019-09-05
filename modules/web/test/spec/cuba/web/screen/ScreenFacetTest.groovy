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

package spec.cuba.web.screen


import com.haulmont.cuba.gui.screen.OpenMode
import spec.cuba.web.UiScreenSpec
import spec.cuba.web.screen.screens.ScreenWithScreenFacet

class ScreenFacetTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.cuba.web.screen.screens'])
    }

    def 'Screen facet opens screen and injects props'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('mainWindow', OpenMode.ROOT)
        mainWindow.show()

        when: 'Screen with ScreenFacet is opened'

        def screenWithScreenFacet = screens.create(ScreenWithScreenFacet)
        def facet = screenWithScreenFacet.testScreenFacet

        then: 'Attribute values are propagated'

        facet.id == 'testScreenFacet'
        facet.screenId == 'test_ScreenToOpenWithFacet'
        facet.launchMode == OpenMode.NEW_TAB

        when: 'Show method is triggered'

        def screen = facet.show()

        then: 'Screen is shown and props are injected'

        screens.openedScreens.activeScreens.contains(screen)

        screen.boolProp
        screen.intProp == 42
        screen.doubleProp == 3.14159d

        screen.labelProp != null
        screen.dcProp != null
    }
}
