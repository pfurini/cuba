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
    protected static final String DATASOURCE_PROVIDER_PROPERTY_NAME = "cuba.dataSourceProvider";
    protected static final String DBMS_TYPE = "cuba.dbmsType";
    protected static final String DBMS_VERSION = "cuba.dbmsVersion";
    protected static final String HOST = "dataSource.host";
    protected static final String PORT = "dataSource.port";
    protected static final String DB_NAME = "dataSource.dbName";
    protected static final String CONNECTION_PARAMS = "dataSource.connectionParams";
    protected static final String JDBC_URL = "jdbcUrl";
    protected static final String CUBA = "cuba";
    protected static final String MS_SQL_2005 = "2005";
    public static final String POSTGRES_DBMS = "postgres";
    public static final String MSSQL_DBMS = "mssql";
    public static final String ORACLE_DBMS = "oracle";
    public static final String MYSQL_DBMS = "mysql";
    public static final String HSQL_DBMS = "hsql";

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

    protected DataSource getApplicationDataSource() {
        Properties hikariConfigProperties = getHikariConfigProperties();

        HikariConfig config = new HikariConfig(hikariConfigProperties);

        config.setRegisterMbeans(true);
        config.setPoolName("HikariPool-" + (dataSourceName == null ? "MAIN" : dataSourceName));

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
        String cubaConfigDSPrefix = CUBA + filterParam;
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
        if (hikariConfigProperties.getProperty(JDBC_URL) == null) {
            hikariConfigProperties.setProperty(JDBC_URL, getJdbcUrlFromParts(hikariConfigProperties));
        }
        return hikariConfigProperties;
    }

    protected String getJdbcUrlFromParts(Properties properties) {
        String urlPrefix = getUrlPrefix();
        String jdbcUrl = urlPrefix + properties.getProperty(HOST) + ":" +
                properties.getProperty(PORT) + "/" +
                properties.getProperty(DB_NAME);
        if (properties.get(CONNECTION_PARAMS) != null) {
            jdbcUrl = jdbcUrl + properties.get(CONNECTION_PARAMS);
        }
        return jdbcUrl;
    }

    protected String getUrlPrefix() {
        String dbmsType = AppContext.getProperty(getDbmsTypeProperty());
        if (dbmsType == null) {
            throw new RuntimeException("dbmsType should be specified for each dataSource!");
        }
        switch (dbmsType) {
            case POSTGRES_DBMS:
                return "jdbc:postgresql://";
            case MSSQL_DBMS:
                if (MS_SQL_2005.equals(AppContext.getProperty(getDbmsVersionProperty()))) {
                    return "jdbc:jtds:sqlserver://";
                }
                return "jdbc:sqlserver://";
            case ORACLE_DBMS:
                return "jdbc:oracle:thin:@//";
            case MYSQL_DBMS:
                return "jdbc:mysql://";
            case HSQL_DBMS:
                return "jdbc:hsqldb:hsql://";
            default:
                throw new RuntimeException("dbmsType is unsupported!");
        }
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

    protected String getDSProviderPropertyName() {
        if (dataSourceName != null) {
            return DATASOURCE_PROVIDER_PROPERTY_NAME + "_" + dataSourceName;
        }
        return DATASOURCE_PROVIDER_PROPERTY_NAME;
    }

    protected String getDbmsTypeProperty() {
        if (dataSourceName != null) {
            return DBMS_TYPE + "_" + DBMS_TYPE;
        }
        return DBMS_TYPE;
    }

    protected String getDbmsVersionProperty() {
        if (dataSourceName != null) {
            return DBMS_VERSION + "_" + DBMS_VERSION;
        }
        return DBMS_VERSION;
    }
}
