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

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.*;

public class CubaDataSourceFactoryBean extends CubaJndiObjectFactoryBean {
    protected final String DATASOURCE_PROVEDER_PROPERTY_NAME = "cuba.dataSourceProvider";
    private String dataSourceName;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    @Override
    public Class<DataSource> getObjectType() {
        return DataSource.class;
    }

    @Override
    public Object getObject() {
        String dataSourceProvider = AppContext.getProperty(getDSProviderPropertyName());
        if ("application".equals(dataSourceProvider)) {
            return getApplicationDataSource();
        } else {
            return super.getObject();
        }
    }

    protected String getDSProviderPropertyName() {
        if (dataSourceName != null) {
            return DATASOURCE_PROVEDER_PROPERTY_NAME + "_" + dataSourceName;
        }
        return DATASOURCE_PROVEDER_PROPERTY_NAME;
    }

    protected DataSource getApplicationDataSource() {
        Properties hikariConfigProperties = getHikariConfigProperties();

        HikariConfig config = new HikariConfig(hikariConfigProperties);

        config.setRegisterMbeans(true);
        config.setPoolName("HikariPool-" + (dataSourceName == null ? "main" : dataSourceName));

        HikariDataSource ds = new HikariDataSource(config);
        return new ProxyDataSource(ds);
    }

    protected Properties getHikariConfigProperties() {
        Properties hikariConfigProperties = new Properties();
        String[] propertiesNames = AppContext.getPropertyNames();
        String filterParam = ".dataSource.";
        if (dataSourceName != null) {
            filterParam = ".dataSource_" + dataSourceName + ".";
        }
        String hikariConfigDSPrefix;
        String cubaConfigDSPrefix = "cuba" + filterParam;
        String hikariPropertyName;

        for (String cubaPropertyName : propertiesNames) {
            if (!cubaPropertyName.contains(filterParam)) {
                continue;
            }
            String value = AppContext.getProperty(cubaPropertyName);
            if (value == null) {
                continue;
            }

            hikariPropertyName = cubaPropertyName.replace(cubaConfigDSPrefix, "");
            hikariConfigDSPrefix = "dataSource.";
            if (isHikariConfigField(hikariPropertyName)) {
                hikariConfigDSPrefix = "";
            }
            hikariConfigProperties.put(cubaPropertyName.replace(cubaConfigDSPrefix, hikariConfigDSPrefix), value);
        }
        return hikariConfigProperties;
    }

    protected boolean isHikariConfigField(String propertyName) {
        Field[] fields = HikariConfig.class.getDeclaredFields();
        for (Field field : fields) {
            if (propertyName.equals(field.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Object lookupWithFallback() throws NamingException {
        Object object = super.lookupWithFallback();
        if (object instanceof DataSource) {
            return new ProxyDataSource((DataSource) object);
        } else {
            return object;
        }
    }
}
