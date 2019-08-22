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

package com.haulmont.cuba.core.sys.connectionpool;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.jmx.ExtendedStatisticCounter;
import com.haulmont.cuba.core.sys.AppContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Inject;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.regex.Pattern;

public class ConnectionPoolSpecificFactory {
    private static Log log = LogFactory.getLog(ExtendedStatisticCounter.class);

    @Inject
    protected static GlobalConfig globalConfig;

    public static ConnectionPoolInfo getConnectionPoolInfo() {
        ObjectName registeredPoolName;
        ConnectionPoolInfo connectionPoolInfo;

        String poolName = globalConfig.getConnectionPoolName();
        switch (poolName) {
            case "COMMONS":
                registeredPoolName = getPoolObjectName(getCommonsRegexPattern());
                connectionPoolInfo = new CommonsConnectionPoolInfo(registeredPoolName);
                break;
            case "TOMCAT":
                registeredPoolName = getPoolObjectName(getTomcatRegexPattern());
                connectionPoolInfo = new TomcatConnectionPoolInfo(registeredPoolName);
                break;
            case "HIKARI":
                registeredPoolName = getPoolObjectName(getHikariRegexPatter());
                connectionPoolInfo = new HikariConnectionPoolInfo(registeredPoolName);
                break;
            default:
                log.warn(String.format("Connection Pool %s is unsupported!", poolName));
                return new EmptyConnectionPoolInfo();
        }

        if (registeredPoolName == null){
            log.warn(String.format("No one connection pool was found for %s type!", poolName));
            return new EmptyConnectionPoolInfo();
        }
        return connectionPoolInfo;
    }

    protected static ObjectName getPoolObjectName(Pattern regexPattern) {
        Set<ObjectName> names = ManagementFactory.getPlatformMBeanServer().queryNames(null, null);
        for (ObjectName name : names) {
            if (regexPattern.matcher(name.toString()).matches()) {
                return name;
            }
        }
        return null;
    }

    protected static Pattern getCommonsRegexPattern() {
        String usualDsRegexp = String.format(
                "Catalina:type=DataSource,host=[\\w\\d]+,context=/%s,class=javax.sql.DataSource,name=\"%s\"",
                globalConfig.getWebContextName(),
                getMainDatasourceName()
        );
        return Pattern.compile(usualDsRegexp);
    }

    protected static Pattern getTomcatRegexPattern() {
        String tomcatDsRegexp = String.format(
                "^tomcat\\.jdbc:name=\"%s\",context=/%s,engine=Catalina,type=ConnectionPool,host=[\\w\\d]+,class=[\\w\\d\\.]+$",
                globalConfig.getWebContextName(),
                getMainDatasourceName()
        );
        return Pattern.compile(tomcatDsRegexp);
    }

    protected static Pattern getHikariRegexPatter() {
        return Pattern.compile("^tcom\\.zaxxer\\.hikari:type=Pool \\(.*\\)$");
    }

    protected static String getMainDatasourceName() {
        String jndiName = AppContext.getProperty("cuba.dataSourceJndiName");
        Preconditions.checkNotNull(jndiName);

        // mentioned as constant in tomcat docs
        String tomcatPrefix = "java:comp/env/";
        if (jndiName.startsWith(tomcatPrefix)) {
            jndiName = jndiName.substring(tomcatPrefix.length());
        }
        return jndiName;
    }
}
