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

package com.haulmont.cuba.security.app.role.annotation;

import com.haulmont.cuba.security.app.role.AbstractRoleDef;
import com.haulmont.cuba.security.entity.RoleType;
import com.haulmont.cuba.security.role.SecurityStorageMode;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a "Role". Such a role will be available in the user interface
 * if the application is running in the corresponding mode (the app property {@code cuba.rolesStorageMode}
 * equals {@code SOURCE_CODE} or {@code MIXED}).
 *
 * <p>The easiest way to determine the role in the application source code is to extend
 * your class from {@link AbstractRoleDef AbstractRoleDef}
 * and mark it with this annotation. Usage example:
 *
 * <pre>
 *     &#064;Role(name = "My first role", type = RoleType.STANDARD, isDefault = true)
 * public class MyFirstRole extends AbstractRoleDef {
 *
 *     &#064;EntityAccess(target = SomeEntity.class,
 *             deny = {EntityOp.DELETE, EntityOp.UPDATE})
 *     &#064;Override
 *     public EntityAccessPermissions entityAccess() {
 *         return super.entityAccess();
 *     }
 *
 *     &#064;EntityAttributeAccess(target = SomeEntity.class, allow = {"someAttribute"})
 *     &#064;Override
 *     public EntityAttributeAccessPermissions attributeAccess() {
 *         return super.attributeAccess();
 *     }
 *
 *     &#064;SpecificPermission(target = "my.specific.permission", access = Access.ALLOW)
 *     &#064;Override
 *     public SpecificPermissions specificPermissions() {
 *         return super.specificPermissions();
 *     }
 *
 *     &#064;ScreenAccess(deny = {"myapp_SomeEntity.edit"})
 *     &#064;Override
 *     public ScreenPermissions screenAccess() {
 *         return super.screenAccess();
 *     }
 *
 *     &#064;ScreenElementAccess(screen = "myapp_SomeEntity.browse", deny = {"someGroupBox"})
 *     &#064;Override
 *     public ScreenElementsPermissions screenElementsAccess() {
 *         return super.screenElementsAccess();
 *     }
 * }
 * </pre>
 *
 * @see SecurityStorageMode
 * @see EntityAccess
 * @see EntityAttributeAccess
 * @see ScreenAccess
 * @see ScreenElementAccess
 * @see SpecificPermission
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Role {

    /**
     * Role name.
     */
    String name();

    /**
     * Description of the role.
     */
    String description() default "";

    /**
     * Role type. Default value: {@code RoleType.STANDARD}
     */
    RoleType type() default RoleType.STANDARD;

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * Determines if the role is default.
     */
    boolean isDefault() default false;
}
