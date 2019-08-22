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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.GlobalConfig;

import javax.management.ObjectName;

public abstract class ConnectionPoolInfoImpl implements ConnectionPoolInfo {

    protected GlobalConfig globalConfig;
    protected ObjectName registeredPoolName;

    public ConnectionPoolInfoImpl() {
        Configuration configuration = AppBeans.get(Configuration.NAME);
        globalConfig = configuration.getConfig(GlobalConfig.class);
        this.registeredPoolName = ConnectionPoolUtils.getPoolObjectName(getRegexPattern());
    }

    @Override
    public ObjectName getRegisteredMBeanName() {
        return registeredPoolName;
    }
}
