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

import com.haulmont.cuba.core.entity.BaseEntityInternalAccess;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.testsupport.TestContainer;
import com.haulmont.cuba.testsupport.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.haulmont.cuba.testsupport.TestSupport.reserialize;
import static org.junit.jupiter.api.Assertions.*;

public class EntityStateTest {

    @RegisterExtension
    public static TestContainer cont = TestContainer.Common.INSTANCE;

    private UUID userId;

    @AfterEach
    public void tearDown() throws Exception {
        if (userId != null)
            cont.deleteRecord("SEC_USER", userId);
    }

    @Test
    public void testTransactions() throws Exception {
        User user;
        Group group;

        // create and persist

        Transaction tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();

            user = new User();
            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertFalse(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(em.find(Group.class, UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93")));
            em.persist(user);

            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        // load from DB

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            // find
            user = em.find(User.class, userId);
            assertNotNull(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            group = user.getGroup();
            assertNotNull(group);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            // query
            Query query = em.createQuery("select u from sec$User u where u.id = ?1").setParameter(1, userId);
            user = (User) query.getFirstResult();
            assertNotNull(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            group = user.getGroup();
            assertNotNull(group);

            assertFalse(BaseEntityInternalAccess.isNew(group));
            assertTrue(BaseEntityInternalAccess.isManaged(group));
            assertFalse(BaseEntityInternalAccess.isDetached(group));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        assertFalse(BaseEntityInternalAccess.isNew(group));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        user.setName("changed name");

        // merge changed

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            user = em.merge(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));
    }

    @Test
    public void testSerialization() throws Exception {
        User user;
        Group group;

        // serialize new

        user = new User();
        assertTrue(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertFalse(BaseEntityInternalAccess.isDetached(user));

        user = reserialize(user);

        assertTrue(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertFalse(BaseEntityInternalAccess.isDetached(user));

        // serialize managed

        Transaction tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();

            user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(em.find(Group.class, UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93")));
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            user = em.find(User.class, userId);
            assertNotNull(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            group = user.getGroup();
            assertNotNull(group);

            assertFalse(BaseEntityInternalAccess.isNew(group));
            assertTrue(BaseEntityInternalAccess.isManaged(group));
            assertFalse(BaseEntityInternalAccess.isDetached(group));

            user = reserialize(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertFalse(BaseEntityInternalAccess.isManaged(user));
            assertTrue(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        user.setName("changed name");

        // merge changed and serialize

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            user = em.merge(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        user = reserialize(user);

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));
    }

    @Test
    public void testTransactionRollback_new() throws Exception {
        User user = null;

        // create and persist

        Transaction tx = cont.persistence().createTransaction();
        try {
            user = new User();
            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertFalse(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            userId = user.getId();
            user.setLogin("testLogin");
            cont.persistence().getEntityManager().persist(user);

            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();

            fail(); // due to absence of Group
        } catch (Exception e) {
            // ok
        } finally {
            tx.end();
        }

        assertTrue(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertFalse(BaseEntityInternalAccess.isDetached(user));
    }

    @Test
    public void testTransactionRollback_loaded() {
        User user;

        Transaction tx = cont.persistence().createTransaction();
        try {
            user = cont.persistence().getEntityManager().find(User.class, TestSupport.ADMIN_USER_ID);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));
    }
}
