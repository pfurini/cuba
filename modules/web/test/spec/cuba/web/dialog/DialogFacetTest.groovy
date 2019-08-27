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

package spec.cuba.web.dialog

import com.haulmont.cuba.gui.Dialogs
import com.haulmont.cuba.gui.components.ContentMode
import com.haulmont.cuba.gui.screen.OpenMode
import spec.cuba.web.UiScreenSpec
import spec.cuba.web.dialog.screens.ScreenWithDialog

class DialogFacetTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.cuba.web.dialog.screens'])
    }

    def 'MessageDialog attributes are applied'() {
        def screens = vaadinUi.screens

        def mainScreen = screens.create('mainWindow', OpenMode.ROOT)
        mainScreen.show()

        when: 'MessageDialog is configured in XML'

        def screenWithDialog = screens.create(ScreenWithDialog)
        def messageDialog = screenWithDialog.messageDialog

        then: 'Attribute values are propagated to MessageDialog facet'

        messageDialog.id == 'messageDialog'
        messageDialog.caption == 'MessageDialog Facet'
        messageDialog.message == 'MessageDialog Test'
        messageDialog.type == Dialogs.MessageType.WARNING
        messageDialog.contentMode == ContentMode.HTML
        messageDialog.height == 200
        messageDialog.width == 350
        messageDialog.styleName == 'msg-dialog-style'
        messageDialog.modal
        messageDialog.maximized
        messageDialog.closeOnClickOutside

        when: 'MessageDialog is shown'

        messageDialog.show()

        then: 'UI has this dialog window'

        vaadinUi.windows.any { window ->
            window.caption == 'MessageDialog Facet'
        }
    }

    def 'OptionDialog attributes are applied'() {
        def screens = vaadinUi.screens

        def mainScreen = screens.create('mainWindow', OpenMode.ROOT)
        mainScreen.show()

        when: 'OptionDialog is configured in XML'

        def screenWithDialog = screens.create(ScreenWithDialog)
        def optionDialog = screenWithDialog.optionDialog

        then: 'Attribute values are propagated to MessageDialog facet'

        optionDialog.id == 'optionDialog'
        optionDialog.caption == 'OptionDialog Facet'
        optionDialog.message == 'OptionDialog Test'
        optionDialog.type == Dialogs.MessageType.CONFIRMATION
        optionDialog.contentMode == ContentMode.HTML
        optionDialog.height == 200
        optionDialog.width == 350
        optionDialog.styleName == 'opt-dialog-style'
        optionDialog.modal
        optionDialog.maximized

        when: 'OptionDialog is shown'

        optionDialog.show()

        then: 'UI has this dialog window'

        vaadinUi.windows.any { window ->
            window.caption == 'OptionDialog Facet'
        }
    }
}
