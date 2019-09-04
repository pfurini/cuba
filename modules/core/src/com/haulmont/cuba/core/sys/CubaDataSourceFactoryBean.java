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

import com.haulmont.cuba.core.sys.jdbc.ProxyDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class CubaDataSourceFactoryBean extends CubaJndiDataSourceFactoryBean {
    String dataSourceName;
    String dataSourceProviderPropertyName = "cuba.dataSourceProvider";
    String jdbcUrlPropertyName = "cuba.dataSource.jdbcUrl";
    String usernamePropertyName = "cuba.dataSource.username";
    String passwordPropertyName = "cuba.dataSource.password";

    static Map<String, DataSource> dataSourceMap = new HashMap<>();

    @Override
    public Class<DataSource> getObjectType() {
        return DataSource.class;
    }

    @Override
    public Object getObject() {
        if(!"cuba.dataSourceJndiName".equals(getJndiNameAppProperty())){
            dataSourceName = getJndiNameAppProperty().replace("cuba.dataSourceJndiName_","");
        }
        if (dataSourceName != null) {
            updateDbParamsNames();
        }
        String dataSourceProvider = AppContext.getProperty(dataSourceProviderPropertyName);
        if ("APPLICATION".equals(dataSourceProvider)) {
            return getApplicationDataSource();
        } else {
            return super.getObject();
        }
    }

    protected DataSource getApplicationDataSource() {
        if (dataSourceName == null) {
            dataSourceName = "CubaDS";
        }
        if (dataSourceMap.containsKey(dataSourceName)) {
            return dataSourceMap.get(dataSourceName);
        }

        HikariConfig config = new HikariConfig();
        String jdbcUrl = AppContext.getProperty(jdbcUrlPropertyName);
        String username = AppContext.getProperty(usernamePropertyName);
        String password = AppContext.getProperty(passwordPropertyName);

        if (jdbcUrl == null) {
            throw new RuntimeException("cuba.dataSource_%DS-NAME%.jdbcUrl parameter must be filled in case of APPLICATION connection pool provider");
        }

        HikariDataSource ds;
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setRegisterMbeans(true);
        config.setPoolName("HikariPool-" + dataSourceName);

        ds = new HikariDataSource(config);
        dataSourceMap.put(dataSourceName, ds);

        return new ProxyDataSource(ds);
    }

    protected void updateDbParamsNames() {
        dataSourceProviderPropertyName = dataSourceProviderPropertyName + "_" + dataSourceName;
        jdbcUrlPropertyName = jdbcUrlPropertyName.replace("dataSource", "dataSource_" + dataSourceName);
        usernamePropertyName = usernamePropertyName.replace("dataSource", "dataSource_" + dataSourceName);
        passwordPropertyName = passwordPropertyName.replace("dataSource", "dataSource_" + dataSourceName);
    }
}
