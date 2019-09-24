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

package com.haulmont.cuba.entity_cache;

import com.haulmont.cuba.testsupport.TestContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.Arrays;

@RunWith(Suite.class)
@Suite.SuiteClasses({EntityCacheTestClass.class, QueryCacheTestClass.class})
public class EntityCacheTestSuite {

    public static TestContainer cont = new TestContainer()
            .setAppPropertiesFiles(Arrays.asList("com/haulmont/cuba/app.properties", "com/haulmont/cuba/testsupport/test-app.properties", "com/haulmont/cuba/test-app.properties",
                    "com/haulmont/cuba/entity_cache/test-entitycache-app.properties"));

    @BeforeAll
    public static void setUp() throws Exception {
    }

    @AfterAll
    public static void tearDown() throws Exception {
    }
}
