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

import com.haulmont.cuba.core.global.Stores;
import com.haulmont.cuba.core.sys.jdbc.ProxyDataSource;
import com.haulmont.cuba.core.sys.persistence.DbmsType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.*;

public class CubaDataSourceFactoryBean extends CubaJndiObjectFactoryBean {
    protected static final String DATASOURCE_PROVIDER_PROPERTY_NAME = "cuba.dataSourceProvider";
    protected static final String HOST = "hostname";
    protected static final String PORT = "port";
    protected static final String DB_NAME = "dbName";
    protected static final String CONNECTION_PARAMS = "connectionParams";
    protected static final String JDBC_URL = "jdbcUrl";
    protected static final String CUBA = "cuba";
    protected static final String MS_SQL_2005 = "2005";
    public static final String POSTGRES_DBMS = "postgres";
    public static final String MSSQL_DBMS = "mssql";
    public static final String ORACLE_DBMS = "oracle";
    public static final String MYSQL_DBMS = "mysql";
    public static final String HSQL_DBMS = "hsql";
    protected String dataSourceProvider;

    private String storeName;
    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public Class<DataSource> getObjectType() {
        return DataSource.class;
    }

    @Override
    public Object getObject() {
        dataSourceProvider = getDataSourceProvider();
        if ("jndi".equals(dataSourceProvider)) {
            return super.getObject();
        } else if (dataSourceProvider == null || "application".equals(dataSourceProvider)) {
            return getApplicationDataSource();
        }
        throw new RuntimeException(String.format("DataSource provider '%s' is unsupported! Available: 'jndi', 'application'", dataSourceProvider));
    }

    protected String getDataSourceProvider() {
        return AppContext.getProperty(getDSProviderPropertyName());
    }

    protected DataSource getApplicationDataSource() {
        if (storeName == null) {
            storeName = Stores.MAIN;
        }
        Properties hikariConfigProperties = getHikariConfigProperties();
        HikariConfig config = new HikariConfig(hikariConfigProperties);

        config.setRegisterMbeans(true);
        config.setPoolName("HikariPool-" + storeName);

        HikariDataSource ds = new HikariDataSource(config);
        return new ProxyDataSource(ds);
    }

    protected Properties getHikariConfigProperties() {
        String filterParam = ".dataSource.";
        if (!Stores.isMain(storeName)) {
            filterParam = ".dataSource_" + storeName + ".";
        }
        String cubaConfigDSPrefix = CUBA + filterParam;

        Map<String, String> cubaDSProperties = getAllDSProperties(cubaConfigDSPrefix);
        Properties hikariConfigProperties = getHikariConfigProperties(cubaDSProperties);

        if (hikariConfigProperties.getProperty(JDBC_URL) == null) {
            hikariConfigProperties.setProperty(JDBC_URL, getJdbcUrlFromParts(cubaConfigDSPrefix));
        }
        return hikariConfigProperties;
    }

    protected Map<String, String> getAllDSProperties(String DSPrefix) {
        Map<String, String> allDSProperties = new HashMap<>();
        String[] propertiesNames = AppContext.getPropertyNames();
        for (String cubaPropertyName : propertiesNames) {
            if (!cubaPropertyName.startsWith(DSPrefix)) {
                continue;
            }
            String value = AppContext.getProperty(cubaPropertyName);
            if (value == null) {
                continue;
            }
            allDSProperties.put(cubaPropertyName.replace(DSPrefix, ""), value);
        }
        return allDSProperties;
    }

    protected Properties getHikariConfigProperties(Map<String, String> properties) {
        Properties hikariConfigProperties = new Properties();
        List<String> cubaDSDefaultParams = new ArrayList<>(Arrays.asList(HOST, PORT, DB_NAME, CONNECTION_PARAMS));
        for (Map.Entry<String, String> property : properties.entrySet()) {
            if (cubaDSDefaultParams.contains(property.getKey())) {
                continue;
            }
            String hikariConfigDSPrefix = "dataSource.";
            if (isHikariConfigField(property.getKey())) {
                hikariConfigDSPrefix = "";
            }
            hikariConfigProperties.put(hikariConfigDSPrefix.concat(property.getKey()), property.getValue());
        }
        return hikariConfigProperties;
    }

    protected String getJdbcUrlFromParts(String dataSourcePrefix) {
        String urlPrefix = getUrlPrefix();
        String host = AppContext.getProperty(dataSourcePrefix + HOST);
        String port = AppContext.getProperty(dataSourcePrefix + PORT);
        String dbName = AppContext.getProperty(dataSourcePrefix + DB_NAME);
        if (host == null || port == null || dbName == null) {
            throw new RuntimeException(String.format("jdbcUrl parameter is not specified! Can't form jdbcUrl from parts: " +
                    "provided hostname: %s, port: %s, dbName: %s.", host, port, dbName));
        }
        String jdbcUrl = urlPrefix + host + ":" + port + "/" + dbName;
        if (AppContext.getProperty(dataSourcePrefix + CONNECTION_PARAMS) != null) {
            jdbcUrl = jdbcUrl + AppContext.getProperty(dataSourcePrefix + CONNECTION_PARAMS);
        }
        return jdbcUrl;
    }

    protected String getUrlPrefix() {
        String dbmsType = DbmsType.getType(storeName);
        if (dbmsType == null) {
            throw new RuntimeException("dbmsType should be specified for each dataSource!");
        }
        switch (dbmsType) {
            case POSTGRES_DBMS:
                return "jdbc:postgresql://";
            case MSSQL_DBMS:
                if (MS_SQL_2005.equals(AppContext.getProperty(DbmsType.getVersion(storeName)))) {
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
                throw new RuntimeException(String.format("dbmsType '%s' is unsupported!", dbmsType));
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
        if (storeName != null) {
            return DATASOURCE_PROVIDER_PROPERTY_NAME + "_" + storeName;
        }
        return DATASOURCE_PROVIDER_PROPERTY_NAME;
    }
}
