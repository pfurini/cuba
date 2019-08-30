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

package com.haulmont.cuba.core.sys;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class CubaApplicationDataSourceFactoryBean extends AbstractFactoryBean<Object> {
    @Override
    public Class<HikariDataSource> getObjectType() {
        return HikariDataSource.class;
    }

    @Override
    protected Object createInstance() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/hikari");
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setRegisterMbeans(true);

        return new HikariDataSource(config);
    }
}
