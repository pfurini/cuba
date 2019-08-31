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
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class CubaApplicationDataSourceFactoryBean extends AbstractFactoryBean<Object> {
    String jdbcUrlPropertyName;
    String usernamePropertyName;
    String passwordPropertyName;

    public void setJdbcUrlPropertyName(String jdbcUrlPropertyName) {
        this.jdbcUrlPropertyName = jdbcUrlPropertyName;
    }

    public void setUsernamePropertyName(String usernamePropertyName) {
        this.usernamePropertyName = usernamePropertyName;
    }

    public void setPasswordPropertyName(String passwordPropertyName) {
        this.passwordPropertyName = passwordPropertyName;
    }

    @Override
    public Class<HikariDataSource> getObjectType() {
        return HikariDataSource.class;
    }

    @Override
    protected Object createInstance() throws Exception {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = AppContext.getProperty(jdbcUrlPropertyName);
        String username = AppContext.getProperty(usernamePropertyName);
        String password = AppContext.getProperty(passwordPropertyName);

        if (jdbcUrl != null) {
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setRegisterMbeans(true);

            HikariDataSource ds = new HikariDataSource(config);
            return new ProxyDataSource(ds);
        }
        return null;
    }
}
