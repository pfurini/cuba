/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.gui.model;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Sort;
import com.haulmont.cuba.core.global.View;

import java.util.Collection;
import java.util.function.Function;

/**
 *
 */
public interface CollectionLoader<E extends Entity> extends DataLoader {

    CollectionContainer<E> getContainer();

    void setContainer(CollectionContainer<E> container);

    LoadContext<E> createLoadContext();

    /**
     * The position of the first instance to load, numbered from 0.
     * Returns 0 if {@link #setFirstResult(int)} was not called.
     */
    int getFirstResult();

    /**
     * Sets the position of the first instance to load, numbered from 0.
     */
    void setFirstResult(int firstResult);

    /**
     * The maximum number of instances to load.
     * Returns {@code Integer.MAX_VALUE} if {@link #setMaxResults} was not called.
     */
    int getMaxResults();

    /**
     * Sets the maximum number of instances to load.
     */
    void setMaxResults(int maxResults);

    boolean isLoadDynamicAttributes();

    void setLoadDynamicAttributes(boolean loadDynamicAttributes);

    boolean isCacheable();

    void setCacheable(boolean cacheable);

    View getView();

    void setView(View view);

    void setView(String viewName);

    Sort getSort();

    void setSort(Sort sort);

    /**
     * Returns a function which will be used to load data instead of standard implementation.
     */
    Function<LoadContext<E>, Collection<E>> getDelegate();

    /**
     * Sets a function which will be used to load data instead of standard implementation.
     */
    void setLoadDelegate(Function<LoadContext<E>, Collection<E>> delegate);
}
