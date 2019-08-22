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

package com.haulmont.cuba.core.jmx;

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.sys.connectionpool.ConnectionPoolInfo;
import com.haulmont.cuba.core.sys.connectionpool.ConnectionPoolSpecificFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Component("cuba_ExtendedStatisticCounterMBean")
public class ExtendedStatisticCounter extends StatisticsCounter {
    private Log log = LogFactory.getLog(ExtendedStatisticCounter.class);
    private volatile ConnectionPoolInfo connectionPoolInfo;

    @Inject
    protected GlobalConfig globalConfig;

    @Override
    public int getDbConnectionPoolNumActive() {
        connectionPoolInfo = ConnectionPoolSpecificFactory.getConnectionPoolInfo(globalConfig.getConnectionPoolName());
        try {
            return connectionPoolInfo.getActiveConnectionsCount();
        } catch (Exception e) {
            log.warn(String.format("Can't get number of active connections from the %s!", connectionPoolInfo.getPoolName()));
        }
        return 0;
    }

    @Override
    public int getDbConnectionPoolNumIdle() {
        connectionPoolInfo = ConnectionPoolSpecificFactory.getConnectionPoolInfo(globalConfig.getConnectionPoolName());
        try {
            return connectionPoolInfo.getIdleConnectionsCount();
        } catch (Exception e) {
            log.warn(String.format("Can't get number of idle connections from the %s!", connectionPoolInfo.getPoolName()));
        }
        return 0;
    }

    @Override
    public int getDbConnectionPoolMaxTotal() {
        connectionPoolInfo = ConnectionPoolSpecificFactory.getConnectionPoolInfo(globalConfig.getConnectionPoolName());
        try {
            return connectionPoolInfo.getTotalConnectionsCount();
        } catch (Exception e) {
            log.warn(String.format("Can't get number of total connections from the %s!", connectionPoolInfo.getPoolName()));
        }
        return 0;
    }
}
