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

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.testsupport.TestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.haulmont.cuba.testsupport.TestSupport.reserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrmBehaviorTest {

    @RegisterExtension
    public static TestContainer cont = TestContainer.Common.INSTANCE;

    private UUID userId, groupId;

    private static final Logger log = LoggerFactory.getLogger(OrmBehaviorTest.class);

    @AfterEach
    public void tearDown() throws Exception {
        cont.deleteRecord("SEC_USER", userId);
        cont.deleteRecord("SEC_GROUP", groupId);
    }

    /*
     * Test that persist with un-managed attribute works (it didn't work in OpenJPA 2.2+ and worked in OpenJPA pre-2.2)
     */
    @Test
    public void testPersistWithUnManagedAttribute() throws Exception {
        Group group = new Group();
        groupId = group.getId();
        group.setName("Old Name");
        Transaction tx = cont.persistence().createTransaction();
        try {
            cont.persistence().getEntityManager().persist(group);
            tx.commit();
        } finally {
            tx.end();
        }

        // Let's imagine that this entity was loaded with MyBatis
        Group g = new Group();
        g.setId(groupId);
        g.setName("Old Name");

        User user = new User();
        userId = user.getId();
        user.setLogin("typednativesqlquery");
        user.setGroup(g);
        user.setName("Test");

        tx = cont.persistence().createTransaction();
        try {
            cont.persistence().getEntityManager().persist(user);
            tx.commitRetaining();

            user = cont.persistence().getEntityManager().find(User.class, userId,
                    new View(User.class).addProperty("group"));
            tx.commit();
        } finally {
            tx.end();
        }

        user = reserialize(user);
        assertEquals(groupId, user.getGroup().getId());
    }
}