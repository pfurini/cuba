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

package com.haulmont.cuba.core.jmx;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.PersistenceConfig;
import com.haulmont.cuba.core.app.PersistenceManagerAPI;
import com.haulmont.cuba.core.app.ServerConfig;
import com.haulmont.cuba.core.entity.EntityStatistics;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.DbInitializationException;
import com.haulmont.cuba.core.sys.DbUpdater;
import com.haulmont.cuba.core.sys.persistence.DbmsType;
import com.haulmont.cuba.security.app.Authenticated;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.Table;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Component("cuba_PersistenceManagerMBean")
public class PersistenceManager implements PersistenceManagerMBean {

    protected static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);

    @Inject
    protected PersistenceManagerAPI persistenceManager;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected PersistenceSecurity security;

    protected PersistenceConfig persistenceConfig;

    protected ServerConfig serverConfig;

    @Inject
    public void setConfiguration(Configuration configuration) {
        persistenceConfig = configuration.getConfig(PersistenceConfig.class);
        serverConfig = configuration.getConfig(ServerConfig.class);
    }

    @Override
    public String getDbmsType() {
        return DbmsType.getType();
    }

    @Override
    public String getDbmsVersion() {
        return DbmsType.getVersion();
    }

    @Override
    public int getDefaultLookupScreenThreshold() {
        return persistenceConfig.getDefaultLookupScreenThreshold();
    }

    @Authenticated
    @Override
    public void setDefaultLookupScreenThreshold(int value) {
        persistenceConfig.setDefaultLookupScreenThreshold(value);
    }

    @Override
    public int getDefaultLazyCollectionThreshold() {
        return persistenceConfig.getDefaultLazyCollectionThreshold();
    }

    @Authenticated
    @Override
    public void setDefaultLazyCollectionThreshold(int value) {
        persistenceConfig.setDefaultLazyCollectionThreshold(value);
    }

    @Override
    public int getDefaultFetchUI() {
        return persistenceConfig.getDefaultFetchUI();
    }

    @Authenticated
    @Override
    public void setDefaultFetchUI(int value) {
        persistenceConfig.setDefaultFetchUI(value);
    }

    @Override
    public int getDefaultMaxFetchUI() {
        return persistenceConfig.getDefaultMaxFetchUI();
    }

    @Authenticated
    @Override
    public void setDefaultMaxFetchUI(int value) {
        persistenceConfig.setDefaultMaxFetchUI(value);
    }

    @Override
    public String printViewRepositoryDump() {
        return new ViewRepositoryInfo(metadata).dump();
    }

    @Override
    public String printViewRepositoryDumpHtml() {
        return new ViewRepositoryInfo(metadata).dumpHtml();
    }

    @Authenticated
    @Override
    public String updateDatabase(String token) {
        if (!"update".equals(token))
            return "Pass 'update' in the method parameter if you really want to update database.";
        try {
            AppBeans.getPrototype(DbUpdater.class, Stores.MAIN).updateDatabase();
            return "Updated";
        } catch (Throwable e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Override
    public String findUpdateDatabaseScripts() {
        try {
            List<String> list = AppBeans.getPrototype(DbUpdater.class, Stores.MAIN).findUpdateDatabaseScripts();
            if (!list.isEmpty()) {
                File dbDir = new File(serverConfig.getDbDir());

                String indent = "\t";
                TextStringBuilder sb = new TextStringBuilder();
                sb.append(dbDir.getPath().replace('\\', '/')).append("/").append("\n");
                for (String path : list) {
                    sb.append(indent).append(path).append("\n");
                }

                return sb.toString();
            } else
                return "No updates available";
        } catch (DbInitializationException e) {
            return e.getMessage();
        } catch (Throwable e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Authenticated
    @Override
    public String jpqlLoadList(String queryString) {
        try {
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                Query query = em.createQuery(queryString);
                QueryParser parser = QueryTransformerFactory.createParser(queryString);
                Set<String> paramNames = parser.getParamNames();
                for (String paramName : paramNames) {
                    security.setQueryParam(query, paramName);
                }
                List resultList = query.getResultList();
                tx.commit();

                TextStringBuilder sb = new TextStringBuilder();
                for (Object element : resultList) {
                    if (element instanceof Object[]) {
                        sb.appendWithSeparators((Object[]) element, " | ");
                    } else {
                        sb.append(element);
                    }
                    sb.append("\n");
                }
                return sb.toString();
            } finally {
                tx.end();
            }
        } catch (Throwable e) {
            log.error("jpqlLoadList error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Authenticated
    @Override
    public String jpqlExecuteUpdate(String queryString, boolean softDeletion) {
        try {
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                em.setSoftDeletion(softDeletion);
                Query query = em.createQuery(queryString);
                int count = query.executeUpdate();
                tx.commit();

                return "Done: " + count + " entities affected, softDeletion=" + softDeletion;
            } finally {
                tx.end();
            }
        } catch (Throwable e) {
            log.error("jpqlExecuteUpdate error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Override
    public synchronized String flushStatisticsCache() {
        try {
            persistenceManager.flushStatisticsCache();
            return "Done";
        } catch (Exception e) {
            log.error("flushStatisticsCache error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Authenticated
    @Override
    public String refreshStatistics(String entityName) {
        if (StringUtils.isBlank(entityName))
            return "Pass an entity name (MetaClass name, e.g. sec$User) or 'all' to refresh statistics for all entities.\n" +
                    "Be careful, it can take very long time.";

        try {
            log.info("Refreshing statistics for " + entityName);

            Consumer<MetaClass> refreshStatisticsForEntity = mc -> {
                MetaClass originalMetaClass = metadata.getExtendedEntities().getOriginalOrThisMetaClass(mc);
                Class javaClass = originalMetaClass.getJavaClass();
                Table annotation = (Table) javaClass.getAnnotation(Table.class);
                if (annotation != null) {
                    persistenceManager.refreshStatisticsForEntity(originalMetaClass.getName());
                }
            };

            if ("all".equals(entityName)) {
                for (MetaClass metaClass : metadata.getSession().getClasses()) {
                    refreshStatisticsForEntity.accept(metaClass);
                }
            } else {
                MetaClass metaClass = metadata.getSession().getClass(entityName);
                if (metaClass == null)
                    return "MetaClass not found: " + entityName;
                refreshStatisticsForEntity.accept(metaClass);
            }
            return "Done";
        } catch (Exception e) {
            log.error("refreshStatistics error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Override
    public String showStatistics(String entityName) {
        try {
            Map<String, EntityStatistics> statistics = persistenceManager.getEntityStatistics();
            if (StringUtils.isBlank(entityName)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Displaying statistics for all entities.\n");
                sb.append("To show a particular entity only, pass its name in the method parameter.\n\n");

                for (EntityStatistics stat : statistics.values()) {
                    sb.append(stat).append("\n");
                }

                return sb.toString();
            } else {
                EntityStatistics es = statistics.get(entityName);
                return es == null ? "No such entity" : es.toString();
            }
        } catch (Exception e) {
            log.error("showStatistics error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Authenticated
    @Override
    public synchronized String enterStatistics(String name, Long instanceCount, Integer fetchUI, Integer maxFetchUI,
                                               Integer lazyCollectionThreshold, Integer lookupScreenThreshold) {
        if (StringUtils.isBlank(name))
            return "Entity name is required";
        try {
            EntityStatistics es = persistenceManager.enterStatistics(
                    name, instanceCount, fetchUI, maxFetchUI, lazyCollectionThreshold, lookupScreenThreshold);

            StringBuilder sb = new StringBuilder("Statistics for ").append(name).append(" changed:\n");
            sb.append("instanceCount=").append(es.getInstanceCount()).append("\n");
            sb.append("fetchUI=").append(es.getFetchUI()).append("\n");
            sb.append("maxFetchUI=").append(es.getMaxFetchUI()).append("\n");
            sb.append("lazyCollectionThreshold=").append(es.getLazyCollectionThreshold()).append("\n");
            sb.append("lookupScreenThreshold=").append(es.getLookupScreenThreshold()).append("\n");
            return sb.toString();
        } catch (Exception e) {
            log.error("enterStatistics error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Authenticated
    @Override
    public String deleteStatistics(String name) {
        if (StringUtils.isBlank(name))
            return "Entity name is required";
        try {
            persistenceManager.deleteStatistics(name);
            return "Entity statistics for " + name + " has been deleted";
        } catch (Exception e) {
            log.error("deleteStatistics error", e);
            return ExceptionUtils.getStackTrace(e);
        }
    }
}