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
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import com.haulmont.cuba.testsupport.TestContainer;
import com.haulmont.cuba.testsupport.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.haulmont.cuba.testsupport.TestSupport.reserialize;
import static org.junit.jupiter.api.Assertions.*;

public class GetReferenceIdTest {

    @RegisterExtension
    public static TestContainer cont = TestContainer.Common.INSTANCE;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = cont.persistence().createTransaction()) {
            user = cont.metadata().create(User.class);
            user.setName("test user");
            user.setLogin("test login");
            user.setGroup(cont.entityManager().find(Group.class, TestSupport.COMPANY_GROUP_ID));
            cont.entityManager().persist(user);

            tx.commit();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        cont.deleteRecord(user);
    }

    @Test
    public void testWithFetchGroup() throws Exception {
        User user = null;

        // not in a view
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.persistence().getEntityManager();

            Query q = em.createQuery("select u from sec$User u where u.id = ?1");
            q.setView(
                    new View(User.class, false)
                            .addProperty("login")
                            .addProperty("userRoles", new View(UserRole.class)
                                    .addProperty("role", new View(Role.class)
                                            .addProperty("name")))
                            .setLoadPartialEntities(true)
            );
            q.setParameter(1, this.user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                user = list.get(0);

                PersistenceTools.RefId refId = cont.persistence().getTools().getReferenceId(user, "group");
                assertFalse(refId.isLoaded());
                try {
                    refId.getValue();
                    fail();
                } catch (IllegalStateException e) {
                    // ok
                }
            }
            tx.commit();
        }
        user = reserialize(user);
        assertNotNull(user);
        assertNotNull(user.getUserRoles());
        user.getUserRoles().size();

        // in a view
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.persistence().getEntityManager();

            Query q = em.createQuery("select u from sec$User u where u.id = ?1");
            q.setView(
                    new View(User.class, false)
                            .addProperty("login")
                            .addProperty("group", new View(Group.class)
                                    .addProperty("name"))
                            .addProperty("userRoles", new View(UserRole.class)
                                    .addProperty("role", new View(Role.class)
                                            .addProperty("name")))
                            .setLoadPartialEntities(true)
            );
            q.setParameter(1, this.user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                user = list.get(0);

                PersistenceTools.RefId refId = cont.persistence().getTools().getReferenceId(user, "group");
                assertTrue(refId.isLoaded());
                assertEquals(TestSupport.COMPANY_GROUP_ID, refId.getValue());
            }
            tx.commit();
        }
        user = reserialize(user);
        assertNotNull(user);
        assertNotNull(user.getUserRoles());
        user.getUserRoles().size();

    }

    @Test
    public void testWithoutFetchGroup() throws Exception {
        User user = null;

        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.persistence().getEntityManager();

            TypedQuery<User> q = em.createQuery("select u from sec$User u where u.id = ?1", User.class);
            q.setParameter(1, this.user.getId());
            List<User> list = q.getResultList();
            if (!list.isEmpty()) {
                user = list.get(0);

                PersistenceTools.RefId refId = cont.persistence().getTools().getReferenceId(user, "group");
                assertTrue(refId.isLoaded());
                assertEquals(TestSupport.COMPANY_GROUP_ID, refId.getValue());
            }
            tx.commit();
        }
        try {
            cont.persistence().getTools().getReferenceId(user, "group");
            fail();
        } catch (IllegalStateException e) {
            // ok
        }
        user = reserialize(user);
        assertNotNull(user);
        try {
            cont.persistence().getTools().getReferenceId(user, "group");
            fail();
        } catch (IllegalStateException e) {
            // ok
        }
    }
}