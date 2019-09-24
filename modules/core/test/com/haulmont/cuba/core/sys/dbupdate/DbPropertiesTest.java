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
 */

package com.haulmont.cuba.core.sys.dbupdate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author subbotin
 */
public class DbPropertiesTest {

    @Test
    public void testNullOrEmptyUrl() {
        DbProperties dbProperties = new DbProperties(null);
        Assertions.assertNull(dbProperties.getProperties());

        dbProperties = new DbProperties("jdbc:sqlserver://localhost");
        Assertions.assertNull(dbProperties.getProperties());
    }

    @Test
    public void testWithParamsPostgres() {
        DbProperties dbProperties = new DbProperties("jdbc:sqlserver://localhost;databaseName=refapp_6;currentSchema=Person");
        Map<String, String> properties = dbProperties.getProperties();
        Assertions.assertNotNull(properties);
        Assertions.assertTrue(properties.size() == 2);
        Assertions.assertTrue("Person".equals(properties.get("currentSchema")));

        dbProperties = new DbProperties("jdbc:postgresql://localhost/refapp_6?currentSchema=Person");
        properties = dbProperties.getProperties();
        Assertions.assertNotNull(properties);
        Assertions.assertTrue(properties.size() == 1);
        Assertions.assertTrue("Person".equals(properties.get("currentSchema")));
        Assertions.assertTrue("Person".equals(dbProperties.getCurrentSchemaProperty()));

        dbProperties = new DbProperties("jdbc:postgresql://localhost/refapp_6?currentSchema=\"Person\"");
        properties = dbProperties.getProperties();
        Assertions.assertNotNull(properties);
        Assertions.assertTrue(properties.size() == 1);
        Assertions.assertTrue("\"Person\"".equals(properties.get("currentSchema")));
        Assertions.assertTrue("Person".equals(dbProperties.getCurrentSchemaProperty()));
    }

    @Test
    public void testWithParamsMssql() {
        DbProperties dbProperties = new DbProperties("jdbc:jtds:sqlserver://localhost/refapp_6;currentSchema=Person");
        Map<String, String> properties = dbProperties.getProperties();
        Assertions.assertNotNull(properties);
        Assertions.assertTrue(properties.size() == 1);
        Assertions.assertTrue("Person".equals(properties.get("currentSchema")));
        Assertions.assertTrue("Person".equals(dbProperties.getCurrentSchemaProperty()));

        dbProperties = new DbProperties("jdbc:sqlserver://localhost;databaseName=refapp_6;currentSchema=Person");
        properties = dbProperties.getProperties();
        Assertions.assertNotNull(properties);
        Assertions.assertTrue(properties.size() == 2);
        Assertions.assertTrue("Person".equals(properties.get("currentSchema")));
        Assertions.assertTrue("Person".equals(dbProperties.getCurrentSchemaProperty()));

        dbProperties = new DbProperties("jdbc:sqlserver://;serverName=3ffe:8311:eeee:f70f:0:5eae:10.203.31.9\\\\instance1;integratedSecurity=true;currentSchema=Person");
        properties = dbProperties.getProperties();
        Assertions.assertNotNull(properties);
        Assertions.assertTrue(properties.size() == 3);
        Assertions.assertTrue("Person".equals(properties.get("currentSchema")));
        Assertions.assertTrue("3ffe:8311:eeee:f70f:0:5eae:10.203.31.9\\\\instance1".equals(properties.get("serverName")));
        Assertions.assertTrue("true".equals(properties.get("integratedSecurity")));
        Assertions.assertTrue("Person".equals(dbProperties.getCurrentSchemaProperty()));
    }
}
