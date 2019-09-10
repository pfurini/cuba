/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.core.sys.dbupdate;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.haulmont.cuba.core.app.ClusterManagerAPI;
import com.haulmont.cuba.core.app.ServerConfig;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.core.global.Stores;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.DbInitializationException;
import com.haulmont.cuba.core.sys.DbUpdater;
import com.haulmont.cuba.core.sys.events.AppContextInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.haulmont.cuba.core.global.Stores.storeNameToString;

@Component(DbUpdaterManager.NAME)
public class DbUpdaterManager {

    public static final String NAME = "cuba_DbUpdaterManager";

    @Inject
    protected ClusterManagerAPI clusterManager;

    @Inject
    protected ServerConfig serverConfig;

    protected static final Logger log = LoggerFactory.getLogger(DbUpdaterEngine.class);

    @EventListener(AppContextInitializedEvent.class)
    @Order(Events.LOWEST_PLATFORM_PRECEDENCE - 90) // after starting cluster
    protected void applicationInitialized() {
        List<String> stores = Stores.getAll();
        if (clusterManager.isMaster()) {
            for (String storeName : stores) {
                if (supportsAutomaticDatabaseUpdate(storeName)) {
                    updateDatabase(storeName);
                } else {
                    checkDatabase(storeName);
                }
            }
        } else {
            for (String storeName : stores) {
                checkDatabase(storeName);
            }
        }
    }

    protected void updateDatabase(String storeName) {
        DbUpdater dbUpdater = AppBeans.getPrototype(DbUpdater.NAME, storeName);
        try {
            dbUpdater.updateDatabase();
        } catch (DbInitializationException e) {
            throw new RuntimeException(wrapText(
                    "ERROR: Cannot check and update data store [" + storeNameToString(storeName) + "]. See the stacktrace below for details."), e);
        }
    }

    protected void checkDatabase(String storeName) {
        DbUpdater dbUpdater = AppBeans.getPrototype(DbUpdater.NAME, storeName);
        try {
            boolean initialized = dbUpdater.dbInitialized();
            if (!initialized) {
                throw new IllegalStateException(wrapText(
                        "ERROR: Data store [" + storeNameToString(storeName) + "] is not initialized. " +
                                "Set 'cuba.automaticDatabaseUpdate' or '" + getAutomaticUpdatePropertyName(storeName) + "'\n" +
                                "application property to 'true' to initialize and update data store on startup."));
            }
            List<String> scripts = dbUpdater.findUpdateDatabaseScripts();
            if (!scripts.isEmpty()) {
                log.warn(wrapText(
                        "WARNING: The application contains unapplied update scripts for data store [" + storeNameToString(storeName) + "]:\n\n" +
                                Joiner.on('\n').join(scripts) + "\n\n" +
                                "Set 'cuba.automaticDatabaseUpdate' or '" + getAutomaticUpdatePropertyName(storeName) + "'\n" +
                                "application property to 'true' to initialize and update data store on startup."));
            }
        } catch (DbInitializationException e) {
            throw new RuntimeException(wrapText(
                    "ERROR: Cannot check data store [" + storeNameToString(storeName) + "]. See the stacktrace below for details."), e);
        }
    }


    protected boolean supportsAutomaticDatabaseUpdate(String storeName) {
        return serverConfig.getAutomaticDatabaseUpdate() ||
                Boolean.parseBoolean(AppContext.getProperty(getAutomaticUpdatePropertyName(storeName)));
    }

    protected String getAutomaticUpdatePropertyName(String storeName) {
        return String.format("cuba.automaticDatabaseUpdate_%s",
                Stores.isMain(storeName) ? "MAIN" : storeName);
    }

    protected String wrapText(String message) {
        int length = Splitter.on("\n").splitToList(message).stream()
                .mapToInt(String::length)
                .max().orElse(0);
        return "\n" + Strings.repeat("=", length) + '\n'
                + message + '\n'
                + Strings.repeat("=", length);
    }
}