/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package spec.cuba.core.entity_serialization

import com.haulmont.cuba.core.app.serialization.EntitySerializationAPI
import com.haulmont.cuba.core.app.serialization.EntitySerializationOption
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.TestContainerSpecification
import com.haulmont.cuba.testmodel.entity_serialization.Serialization_Order
import com.haulmont.cuba.testmodel.entity_serialization.Serialization_OrderItem
import groovy.json.JsonSlurper

class EntityJsonSerializationTest extends TestContainerSpecification {

    private EntitySerializationAPI entitySerializationAPI

    void setup() {
        entitySerializationAPI = AppBeans.get(EntitySerializationAPI.class)
    }

     //https://github.com/cuba-platform/cuba/issues/1091
    def "collection property which item contains a reference to the other item of the collection"() {
        def order = cont.metadata().create(Serialization_Order.class)
        order.setNumber('order-1')

        def orderItem1 = cont.metadata().create(Serialization_OrderItem.class)
        orderItem1.name = 'item-1'
        orderItem1.order = order

        def orderItem2 = cont.metadata().create(Serialization_OrderItem.class)
        orderItem2.name = 'item-2'
        orderItem2.order = order
        orderItem2.relatedItem = orderItem1

        order.items = [orderItem1, orderItem2]

        when:

        def json = entitySerializationAPI.toJson(order)

        then: "'relatedItem' property of the 'item-2' shouldn't be an object in a compact entity format"

        def jsonSlurper = new JsonSlurper()
        def parsedObject = jsonSlurper.parseText(json)
        def item1 = parsedObject.items[0]
        item1.name == 'item-1'
        def item2 = parsedObject.items[1]
        item2.name == 'item-2'
        item2.relatedItem.id == item1.id
        item2.relatedItem.name == item1.name //item1 name should exist, it is not a cyclic reference
        item2.relatedItem.order.id == parsedObject.id
        item2.relatedItem.order.name == null //order name should NOT exist, it is a cyclic reference
    }

    def "should serialize non-persistent fields if DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES is NOT provided"() {
        def order = cont.metadata().create(Serialization_Order.class)
        order.setNumber('order-1')
        order.setTransientField("tf")

        when: 'no EntitySerializationOption.DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES is provided'

        def json = entitySerializationAPI.toJson(order)

        then: 'not persistent fields must be absent in JSON'

        def jsonSlurper = new JsonSlurper()
        def parsedObject = jsonSlurper.parseText(json)
        parsedObject.transientField == 'tf'
        parsedObject.valueFromMetaPropertyMethod =='some value'
    }

    def "SHOULD NOT serialize non-persistent fields if DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES IS provided"() {
        def order = cont.metadata().create(Serialization_Order.class)
        order.setNumber('order-1')
        order.setTransientField("tf")

        when: 'EntitySerializationOption.DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES is provided'

        def json = entitySerializationAPI.toJson(order, null, EntitySerializationOption.DO_NOT_SERIALIZE_RO_NON_PERSISTENT_PROPERTIES)

        then: 'persistent fields must be absent in JSON'

        def jsonSlurper = new JsonSlurper()
        def parsedObject = jsonSlurper.parseText(json)
        parsedObject.transientField == 'tf'
        parsedObject.valueFromMetaPropertyMethod == null
    }
}
