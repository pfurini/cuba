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

package spec.cuba.core.serialization

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.View
import com.haulmont.cuba.core.TestContainerSpecification
import com.haulmont.cuba.core.sys.serialization.KryoSerialization
import com.haulmont.cuba.core.sys.serialization.SerializationException
import com.haulmont.cuba.testmodel.sales.Order
import com.haulmont.cuba.testmodel.sales.OrderLine

import java.util.regex.Matcher
import java.util.regex.Pattern

class KryoSerializationTest extends TestContainerSpecification {

    private Persistence persistence = cont.persistence()
    private Metadata metadata = cont.metadata()
    private DataManager dataManager = AppBeans.get(DataManager)

    private UUID orderId, orderLineId

    void setup() {
        persistence.runInTransaction({ em ->
            Order order = metadata.create(Order)
            order.number = 'orderNumber#1'
            orderId = order.id
            em.persist(order)

            OrderLine orderLine = metadata.create(OrderLine)
            orderLine.product = 'product'
            orderLine.order = order
            orderLineId = orderLine.id
            em.persist(orderLine)
        })
    }

    //https://github.com/cuba-platform/cuba/issues/742
    def "HashSet serialization issue"() {
        setup:
        def view = new View(Order)
                .addProperty("number")
                .addProperty("amount")
                .addProperty("lineSet",
                new View(OrderLine).addProperty("product").addProperty("order",
                        new View(Order).addProperty("number")))
        Order order = dataManager.load(Order).id(orderId).view(view).one()
        KryoSerialization kryoSerialization = new KryoSerialization();
        when:
        Set set = new LinkedHashSet()
        set.add(order.lineSet[0])
        set.add(order)
        Set result = (Set) kryoSerialization.deserialize(kryoSerialization.serialize(set))
        then:
        result[1].lineSet.contains(result[0])
    }


    def "java.Pattern serialization issue"() {
        setup:
        Pattern pattern = Pattern.compile("^[A-Z]{1,2}\\d[A-Z0-9]? ")
        KryoSerialization kryoSerialization = new KryoSerialization()
        pattern = (Pattern) kryoSerialization.deserialize(kryoSerialization.serialize(pattern))
        when:
        Matcher matcher = pattern.matcher("SO51 4DK")
        then:
        matcher.find()
        matcher.group() != null
    }

    //https://github.com/cuba-platform/cuba/issues/1992
    def "default Collection serializer. ArrayList"() {
        setup:
        KryoSerialization kryoSerialization = new KryoSerialization()
        when:
        List<String> cars = new ArrayList<>()
        cars.add('Volvo')
        cars.add('Mercedes')

        then:
        byte[] bytes = kryoSerialization.serialize(cars)
        List<String> cars2 = kryoSerialization.deserialize(bytes) as List<String>
        cars2.size() == 2
        cars2[0] == 'Volvo'
        cars2[1] == 'Mercedes'

    }

    def "default Collection serializer. not Serializable"() {
        setup:
        KryoSerialization kryoSerialization = new KryoSerialization()
        when:
        Multimap<String, String> map = ArrayListMultimap.create()
        map.put('car', 'Volvo')
        map.put('car', 'Mercedes')
        map.put('color', 'White')
        List<String> cars = map.get('car')
        byte[] bytes = kryoSerialization.serialize(cars)
        kryoSerialization.deserialize(bytes) as List<String>

        then:

        SerializationException ex = thrown()
        ex.getCause().class == IllegalArgumentException

    }

}
