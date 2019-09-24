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

import com.haulmont.cuba.core.global.FetchMode;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.entity.Constraint;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.testsupport.TestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.haulmont.cuba.testsupport.TestSupport.reserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EclipseLinkQueriesTest {

    @RegisterExtension
    public static TestContainer cont = TestContainer.Common.INSTANCE;
    private User user1;
    private User user2;
    private Group rootGroup;
    private Group group;

    @BeforeEach
    public void setUp() throws Exception {
        cont.persistence().createTransaction().execute(em ->  {
            rootGroup = em.find(Group.class, UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93"));

            user1 = cont.metadata().create(User.class);
            user1.setName("testUser");
            user1.setLogin("testLogin");
            user1.setGroup(rootGroup);
            em.persist(user1);

            group = cont.metadata().create(Group.class);
            group.setParent(rootGroup);
            group.setName("testGroup" + group.getId());
            em.persist(group);

            user2 = cont.metadata().create(User.class);
            user2.setName("testUser2");
            user2.setLogin("testLogin2");
            user2.setGroup(group);
            user2.setGroup(rootGroup);
            em.persist(user2);

            return null;
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        cont.deleteRecord("SEC_GROUP_HIERARCHY", "GROUP_ID", group.getId());
        cont.deleteRecord(user1, user2, group);
    }

    // cross join, view has ToMany reference
    @Test
    public void testCrossJoinWithToManyView() throws Exception {
        List<Group> result;
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.entityManager();
            View view = new View(Group.class).addProperty("constraints");
            TypedQuery<Group> query = em.createQuery("select g from sec$Group g, sec$User u where u.group = g", Group.class);
            query.setView(view);
            result = query.getResultList();
            tx.commit();
        }
        for (Group group : result) {
            group = reserialize(group);
            group.getConstraints().size();
        }
    }

    // cross join, view with the reference to the parent entity
    @Test
    public void testCrossJoinViewParentReference() throws Exception {
        List<Group> result;
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.entityManager();
            View view = new View(Group.class).addProperty("parent");
            TypedQuery<Group> query = em.createQuery("select g from sec$Group g, sec$User u where u.group = g", Group.class);
            query.setView(view);
            result = query.getResultList();
            tx.commit();
        }
        for (Group g : result) {
            g = reserialize(g);
            if (g.equals(rootGroup))
                assertNull(g.getParent());
            else if (g.equals(group))
                assertEquals(rootGroup, g.getParent());
        }
    }

    // join on, view contains ToMany attribute
    @Test
    public void testJoinOnWithToManyView() throws Exception {
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.entityManager();
            View view = new View(Group.class).addProperty("constraints");
            TypedQuery<Group> query = em.createQuery("select g from sec$Group g join sys$QueryResult qr on qr.entityId = g.id where qr.queryKey = 1", Group.class);
            query.setView(view);
            List<Group> result = query.getResultList();
            tx.commit();
        }
    }

    // join on, view contains parent attribute
    @Test
    public void testJoinOnWithParentReference() throws Exception {
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.entityManager();
            View view = new View(Group.class).addProperty("parent");
            TypedQuery<Group> query = em.createQuery("select g from sec$Group g join sys$QueryResult qr on qr.entityId = g.id where qr.queryKey = 1", Group.class);
            query.setView(view);
            List<Group> result = query.getResultList();
            tx.commit();
        }
    }

    // join on, view contains ToMany attribute, fetch = JOIN
    @Test
    public void testJoinOnWithToManyView2() throws Exception {
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.entityManager();
            View view = new View(Group.class).addProperty("constraints", new View(Constraint.class, View.LOCAL), FetchMode.JOIN);
            TypedQuery<Group> query = em.createQuery("select g from sec$Group g join sys$QueryResult qr on qr.entityId = g.id where qr.queryKey = 1", Group.class);
            query.setView(view);
            List<Group> result = query.getResultList();
            tx.commit();
        }
    }

    @Test
    public void testSeveralEntriesInSelectClause() {
        Object resultList = cont.persistence().createTransaction().execute((em) -> {
            return em.createQuery("select u.group, u.login from sec$User u where u.name like :mask")
                    .setParameter("mask", "%ser")
                    .getResultList();
        });
        List<Object[]> list = (List<Object[]>) resultList;
        Object[] row = list.get(0);

        assertEquals(UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93"), ((Group) row[0]).getId());
        assertEquals("testLogin", row[1]);
    }
}