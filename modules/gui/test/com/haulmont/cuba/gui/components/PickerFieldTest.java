/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.sys.serialization.SerializationSupport;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import mockit.Expectations;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
@Disabled
public class PickerFieldTest extends AbstractComponentTestCase {

    @Override
    protected void initExpectations() {
        super.initExpectations();

        new Expectations() {
            {
                clientConfig.getPickerShortcutModifiers(); result = "CTRL-ALT"; minTimes = 0;
            }
        };
    }

    @Test
    public void testNew() {
        Component component = uiComponents.create(PickerField.NAME);
        assertNotNull(component);
        assertTrue(component instanceof PickerField);
    }

    @Test
    public void testGetSetValue() {
        PickerField component = uiComponents.create(PickerField.class);

        assertNull(component.getValue());

        boolean thrown = false;
        try {
            component.setValue("One");
        } catch (ClassCastException e) {
            thrown = true;
        }
        assertTrue(thrown);

        assertNull(component.getValue());

        User user = new User();
        user.setLogin("admin");

        component.setMetaClass(metadata.getClass(User.class));
        component.setValue(user);

        assertEquals(user, component.getValue());
    }

    @Test
    public void testSetToReadonly() {
        PickerField component = uiComponents.create(PickerField.class);

        component.setEditable(false);
        component.setMetaClass(metadata.getClass(User.class));
        assertFalse(component.isEditable());

        User user = new User();
        user.setLogin("admin");
        component.setValue(user);

        assertEquals(user, component.getValue());
        assertFalse(component.isEditable());
    }

    @Test
    public void testSetToReadonlyFromValueListener() {
        final PickerField component = uiComponents.create(PickerField.class);

        component.setMetaClass(metadata.getClass(User.class));
        assertTrue(component.isEditable());

        component.addValueChangeListener(e -> component.setEditable(false));

        User user = new User();
        user.setLogin("admin");
        component.setValue(user);

        assertEquals(user, component.getValue());
        assertFalse(component.isEditable());
    }

    @Test
    public void testDatasource() {
        PickerField component = uiComponents.create(PickerField.class);

        //noinspection unchecked
        Datasource<User> testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getView(User.class, View.LOCAL))
                .buildDatasource();

        testDs.setItem(new User());
        ((DatasourceImpl) testDs).valid();

        assertNull(component.getValue());
        Group g = new Group();
        testDs.getItem().setGroup(g);

        Group g1 = new Group();
        Group g2 = new Group();

        component.setMetaClass(metadata.getClass(Group.class));

        component.setValue(g2);
        component.setDatasource(testDs, "group");
        assertEquals(g, component.getValue());

        component.setValue(g1);
        assertEquals(g1, testDs.getItem().getGroup());

        testDs.getItem().setGroup(g2);
        assertEquals(g2, component.getValue());
    }

    @Test
    public void testValueChangeListener() {
        PickerField component = uiComponents.create(PickerField.class);

        final AtomicInteger counter = new AtomicInteger(0);

        //noinspection unchecked
        Datasource<User> testDs = new DsBuilder()
                .setId("testDs")
                .setJavaClass(User.class)
                .setView(viewRepository.getView(User.class, View.LOCAL))
                .buildDatasource();

        testDs.setItem(new User());
        ((DatasourceImpl) testDs).valid();

        assertNull(component.getValue());
        final Group g = new Group();
        testDs.getItem().setGroup(g);

        final Group g1 = new Group();
        final Group g2 = new Group();

        Consumer<HasValue.ValueChangeEvent> listener1 = e -> {
            assertNull(e.getPrevValue());
            assertEquals(g2, e.getValue());

            counter.addAndGet(1);
        };
        component.addValueChangeListener(listener1);

        component.setMetaClass(metadata.getClass(Group.class));
        component.setValue(g2);

        component.removeValueChangeListener(listener1);
        assertEquals(1, counter.get());

        Consumer<HasValue.ValueChangeEvent> listener2 = e -> {
            assertEquals(g2, e.getPrevValue());
            assertEquals(g, e.getValue());

            counter.addAndGet(1);
        };
        component.addValueChangeListener(listener2);

        component.setDatasource(testDs, "group");
        assertEquals(g, component.getValue());

        assertEquals(2, counter.get());

        component.removeValueChangeListener(listener2);
        component.setValue(g1);
        assertEquals(g1, testDs.getItem().getGroup());

        assertEquals(2, counter.get());

        Consumer<HasValue.ValueChangeEvent> listener3 = e -> {
            assertEquals(g1, e.getPrevValue());
            assertEquals(g2, e.getValue());

            counter.addAndGet(1);
        };

        component.addValueChangeListener(listener3);
        testDs.getItem().setGroup(g2);
        assertEquals(g2, component.getValue());

        assertEquals(3, counter.get());
        component.removeValueChangeListener(listener3);

        component.setValue(g);
        Group gCopy = (Group) SerializationSupport.deserialize(SerializationSupport.serialize(g));

        Consumer<HasValue.ValueChangeEvent> listener4 = e -> {
            assertEquals(g, e.getPrevValue());
            assertEquals(gCopy, e.getValue());

            counter.addAndGet(1);
        };
        component.addValueChangeListener(listener4);
        component.setValue(gCopy);
        assertEquals(4, counter.get());
        component.removeValueChangeListener(listener4);

        Consumer<HasValue.ValueChangeEvent> listener5 = e -> {
            assertEquals(g, e.getPrevValue());
            assertEquals(gCopy, e.getValue());

            counter.addAndGet(1);
        };
        component.addValueChangeListener(listener5);
        testDs.getItem().setGroup(g);
        assertEquals(5, counter.get());
        component.removeValueChangeListener(listener5);
    }
}