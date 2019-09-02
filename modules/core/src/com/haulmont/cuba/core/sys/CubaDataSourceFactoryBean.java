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

import org.springframework.beans.factory.config.AbstractFactoryBean;

import javax.sql.DataSource;

public class CubaDataSourceFactoryBean extends AbstractFactoryBean<Object> {
    DataSource jndiDataSource;
    DataSource applicationDataSource;
    String dataSourceProviderPropertyName;
    String jdbcUrlPropertyName;

    public void setJdbcUrlPropertyName(String jdbcUrlPropertyName) {
        this.jdbcUrlPropertyName = jdbcUrlPropertyName;
    }

    public void setDataSourceProviderPropertyName(String dataSourceProviderPropertyName) {
        this.dataSourceProviderPropertyName = dataSourceProviderPropertyName;
    }

    public void setJndiDataSource(DataSource jndiDataSource) {
        this.jndiDataSource = jndiDataSource;
    }

    public void setApplicationDataSource(DataSource applicationDataSource) {
        this.applicationDataSource = applicationDataSource;
    }

    @Override
    public Class<DataSource> getObjectType() {
        return DataSource.class;
    }

    @Override
    protected Object createInstance() throws Exception {
        String dataSourceProvider = AppContext.getProperty(dataSourceProviderPropertyName);
        if ("APPLICATION".equals(dataSourceProvider)) {
            if (AppContext.getProperty(jdbcUrlPropertyName) == null) {
                throw new RuntimeException("cuba.dataSource.jdbcUrl parameter must be filled in case of APPLICATION connection pool provider");
            }
            return applicationDataSource;
        } else {
            return jndiDataSource;
        }
    }
}
