/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.cuba.core.data_manager

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.FluentLoaderTestAccess
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.TemporalValue
import com.haulmont.cuba.core.global.View
import com.haulmont.cuba.core.global.ViewRepository
import com.haulmont.cuba.core.TestContainerSpecification
import com.haulmont.cuba.testmodel.sales.Customer

import javax.persistence.TemporalType

class FluentLoaderTest extends TestContainerSpecification {

    private DataManager dataManager
    private ViewRepository viewRepository
    private View baseView
    private customer, customer2
    private UUID customerId, customer2Id

    void setup() {
        dataManager = AppBeans.get(DataManager)
        viewRepository = AppBeans.get(ViewRepository)
        baseView = viewRepository.getView(Customer, '_base')

        customer = cont.metadata().create(Customer)
        customer.name = 'Smith'
        customerId = customer.id

        customer2 = cont.metadata().create(Customer)
        customer2.name = 'Johns'
        customer2Id = customer2.id

        dataManager.commit(customer, customer2)
    }

    void cleanup() {
        cont.deleteRecord(customer, customer2)
    }

    def "usage examples"() {

        expect:

        // load all

        dataManager.load(Customer).list() instanceof List
        dataManager.load(Customer).one() instanceof Customer
        dataManager.load(Customer).optional() instanceof Optional

        dataManager.load(Customer).view('_base').list() instanceof List
        dataManager.load(Customer).view(baseView).list() instanceof List

        // load by id

        dataManager.load(Customer).id(customerId).one() == customer
        dataManager.load(Customer).id(customerId).optional() == Optional.of(customer)

        dataManager.load(Customer).id(customerId).view('_base').one() == customer
        dataManager.load(Customer).id(customerId).view(baseView).one() == customer

        // load by query

        dataManager.load(Customer).query('select c from test$Customer c').list() instanceof List
        dataManager.load(Customer).query('select c from test$Customer c').one() instanceof Customer
        dataManager.load(Customer).query('select c from test$Customer c').optional() instanceof Optional

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .list() instanceof List

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .one() == customer

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .cacheable(true)
                .list() instanceof List

        dataManager.load(Customer).query('select c from test$Customer c where c.name = :n')
                .parameter('n', 'Smith')
                .firstResult(10)
                .maxResults(100)
                .list() instanceof List

        // load by collection of ids

        dataManager.load(Customer).ids(customerId, customer2Id).list()
                .size() == 2

        dataManager.load(Customer).ids([customerId, customer2Id]).list()
                .size() == 2
    }

    def "test LoadContext when loading all"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.metaClass == 'test$Customer'
        loadContext.query.queryString == 'select e from test$Customer e'
        loadContext.softDeletion
        !loadContext.query.cacheable

        when:

        loader = dataManager.load(Customer).softDeletion(false)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).view('_base')
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.view == baseView

        when:

        loader = dataManager.load(Customer).view(baseView)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.view == baseView
    }


    def "test LoadContext when loading by id"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer).id(customer.id)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.id == customerId
        loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).id(customer.id).softDeletion(false)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).id(customer.id).view('_base')
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.view == baseView

        when:

        loader = dataManager.load(Customer).id(customer.id).view(baseView)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.view == baseView
    }

    def "test LoadContext when loading by query"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c')
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.queryString == 'select c from test$Customer c'
        loadContext.id == null
        loadContext.softDeletion
        !loadContext.query.cacheable

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c where name = :n')
            .parameter('n', 'Smith')
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.parameters['n'] == 'Smith'
        loadContext.query.noConversionParams == null

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c where name = :n')
            .parameter('n', 'Smith', false)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.parameters['n'] == 'Smith'
        loadContext.query.noConversionParams[0] == 'n'

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c where createTs >= :c')
            .parameter('c', new Date(), TemporalType.DATE)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.parameters['c'] instanceof TemporalValue

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c').cacheable(true)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.cacheable

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c').softDeletion(false)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).query('select c from test$Customer c')
            .firstResult(10)
            .maxResults(100)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.query.firstResult == 10
        loadContext.query.maxResults == 100
    }

    def "test LoadContext when loading by collection of ids"() {
        def loader
        LoadContext loadContext

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.id == null
        loadContext.ids == [customerId, customer2Id]
        loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id).softDeletion(false)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        !loadContext.softDeletion

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id).view('_base')
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.view == baseView

        when:

        loader = dataManager.load(Customer).ids(customer.id, customer2.id).view(baseView)
        loadContext = FluentLoaderTestAccess.createLoadContext(loader)

        then:

        loadContext.view == baseView
    }

}
