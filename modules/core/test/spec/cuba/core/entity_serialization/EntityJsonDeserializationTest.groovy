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

package spec.cuba.core.entity_serialization

import com.haulmont.cuba.core.app.serialization.EntitySerializationAPI
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.TestContainerSpecification
import com.haulmont.cuba.testmodel.entity_serialization.Serialization_MetaProperty
import com.haulmont.cuba.testmodel.numberformat.TestNumberValuesEntity

class EntityJsonDeserializationTest extends TestContainerSpecification {

    private EntitySerializationAPI entitySerializationAPI

    void setup() {
        entitySerializationAPI = AppBeans.get(EntitySerializationAPI.class)
    }

    def "deserialize fields with the @NumberFormat annotation"() {

        def json = '''
            {
                "percentField": 1.2300,
                "doubleField1": 1.2,
                "doubleField2": null,
                "intField1": 1,
                "longField1": 2
            }
        '''

        when:
        TestNumberValuesEntity entity = entitySerializationAPI.entityFromJson(json, cont.metadata().getClassNN(TestNumberValuesEntity))

        then:
        entity.percentField == 1.23
        entity.doubleField1 == 1.2
        entity.doubleField2 == null
        entity.intField1 == 1
        entity.longField1 == 2
    }

    def "deserialize entity with collection @MetaProperty field"() {

        def json = '''
            {
                data: ["aaa", "bbb", "ccc"]
            }
        '''

        when:
        Serialization_MetaProperty entity = entitySerializationAPI.entityFromJson(json, cont.metadata().getClassNN(Serialization_MetaProperty))

        then:
        entity.data == ['aaa', 'bbb', 'ccc']
    }
}
