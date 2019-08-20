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

/**
 * Prepares and shows message dialogs.
 */
@StudioFacet(
        caption = "Message Dialog",
        description = "Prepares and shows message dialogs",
        defaultProperty = "message"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", required = true)
        }
)
public interface MessageDialog extends DialogFacet {

    /**
     * Sets whether the dialog should be closed on click outside.
     *
     * @param closeOnClickOutside close on click outside
     */
    @StudioProperty
    void setCloseOnClickOutside(boolean closeOnClickOutside);

    /**
     * @return whether the dialog should be closed on click outside
     */
    boolean isCloseOnClickOutside();
}
