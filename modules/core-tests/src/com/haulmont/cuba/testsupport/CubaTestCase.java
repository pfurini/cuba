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
package com.haulmont.cuba.testsupport;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PasswordEncryption;
import com.haulmont.cuba.core.sys.AppContext;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.*;

/**
 * DEPRECATED. Use TestContainer and JUnit4 test annotations:
 * <pre>
 *    {@literal @}ClassRule
 *     public static TestContainer cont = TestContainer.Common.INSTANCE;
 *
 *    {@literal @}Test
 *     public void testSomething() {
 *     }
 * </pre>
 */
@Deprecated
public abstract class CubaTestCase extends TestCase {

    protected static boolean initialized;

    protected static TestContainer cont;

    static {
        String property = System.getProperty("logback.configurationFile");
        if (StringUtils.isBlank(property)) {
            System.setProperty("logback.configurationFile", "com/haulmont/cuba/testsupport/test-logback.xml");
        }
    }

    protected Persistence persistence;
    protected Metadata metadata;
    protected PasswordEncryption passwordEncryption;

    private class CommonTestContainer extends TestContainer {
        @Override
        public void beforeAll(ExtensionContext context) {
            super.beforeAll(context);
        }

        @Override
        public void afterAll(ExtensionContext context) {
            super.afterAll(context);
        }

        @Override
        public void cleanupContext() {
            super.cleanupContext();
        }

        @Override
        public void setupContext() {
            super.setupContext();
        }

        @Override
        protected void initDataSources() {
            try {
                CubaTestCase.this.initDataSources();
            } catch (Exception e) {
                throw new RuntimeException("Error initializing datasource", e);
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            if (!initialized) {
                System.setProperty("cuba.unitTestMode", "true");
                cont = new CommonTestContainer();
                cont.setAppComponents(getTestAppComponents());
                cont.setAppPropertiesFiles(getTestAppProperties());
                cont.setSpringConfig(getTestSpringConfig());
                cont.beforeAll(null);
                initialized = true;
            }

            cont.setupContext();

            persistence = AppBeans.get(Persistence.class);
            metadata = AppBeans.get(Metadata.class);
            passwordEncryption = AppBeans.get(PasswordEncryption.class);
        } catch (Throwable throwable) {
            if (throwable instanceof Exception)
                throw (Exception) throwable;
            else
                throw new RuntimeException(throwable);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        TestContext.getInstance().unbind(AppContext.getProperty("cuba.dataSourceJndiName"));
        cont.cleanupContext();
        super.tearDown();
    }

    protected void initDataSources() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        TestDataSource ds = new TestDataSource("jdbc:hsqldb:hsql://localhost/cubadb", "sa", "");
        TestContext.getInstance().bind(AppContext.getProperty("cuba.dataSourceJndiName"), ds);
    }

    protected List<String> getTestAppComponents() {
        return Collections.emptyList();
    }

    protected List<String> getTestAppProperties() {
        String[] files = {
                "com/haulmont/cuba/app.properties",
                "com/haulmont/cuba/testsupport/test-app.properties",
        };
        return Arrays.asList(files);
    }

    protected String getTestSpringConfig() {
        return "com/haulmont/cuba/testsupport/test-spring.xml";
    }

    protected void deleteRecord(String table, UUID... ids) {
        cont.deleteRecord(table, (Object[]) ids);
    }

    protected void deleteRecord(String table, String primaryKeyCol, UUID... ids) {
        cont.deleteRecord(table, primaryKeyCol, (Object[]) ids);
    }

    protected EntityManager entityManager() {
        return persistence.getEntityManager();
    }
}
