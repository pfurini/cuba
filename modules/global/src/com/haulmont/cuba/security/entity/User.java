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
package com.haulmont.cuba.security.entity;

import com.haulmont.chile.core.annotations.Composition;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.*;
import com.haulmont.cuba.core.global.DeletePolicy;
import com.haulmont.cuba.core.sys.AppContext;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.text.MessageFormat;
import java.util.List;

/**
 * User
 */
@Entity(name = "sec$User")
@Table(name = "SEC_USER")
@Listeners("cuba_UserEntityListener")
@NamePattern("#getCaption|login,name")
@TrackEditScreenHistory
public class User extends StandardEntity {

    private static final long serialVersionUID = 5007187642916030394L;

    @Column(name = "LOGIN", length = 50, nullable = false)
    protected String login;

    @SystemLevel
    @Column(name = "LOGIN_LC", length = 50, nullable = false)
    protected String loginLowerCase;

    @SystemLevel
    @Column(name = "PASSWORD", length = 255)
    protected String password;

    @SystemLevel
    @Column(name = "PASSWORD_ENCRYPTION", length = 50)
    protected String passwordEncryption;

    @Column(name = "NAME", length = 255)
    protected String name;

    @Column(name = "FIRST_NAME", length = 255)
    protected String firstName;

    @Column(name = "LAST_NAME", length = 255)
    protected String lastName;

    @Column(name = "MIDDLE_NAME", length = 255)
    protected String middleName;

    @Column(name = "POSITION_", length = 255)
    protected String position;

    @Column(name = "EMAIL", length = 100)
    protected String email;

    @Column(name = "LANGUAGE_", length = 20)
    protected String language;

    @Column(name = "TIME_ZONE")
    protected String timeZone;

    @Column(name = "TIME_ZONE_AUTO")
    protected Boolean timeZoneAuto;

    @Column(name = "ACTIVE")
    protected Boolean active = true;

    @Column(name = "CHANGE_PASSWORD_AT_LOGON")
    protected Boolean changePasswordAtNextLogon = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GROUP_ID")
    @OnDeleteInverse(DeletePolicy.DENY)
    protected Group group;

    @Column(name = "GROUP_NAMES")
    protected String groupNames;

    @OneToMany(mappedBy = "user")
    @OrderBy("createTs")
    @Composition
    protected List<UserRole> userRoles;

    @OneToMany(mappedBy = "user")
    @OrderBy("createTs")
    @Composition
    protected List<UserSubstitution> substitutions;

    @Column(name = "IP_MASK", length = 200)
    protected String ipMask;

    @Transient
    protected boolean disabledDefaultRoles;

    public boolean isDisabledDefaultRoles() {
        return disabledDefaultRoles;
    }

    public void setDisabledDefaultRoles(boolean disabledDefaultRoles) {
        this.disabledDefaultRoles = disabledDefaultRoles;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLoginLowerCase() {
        return loginLowerCase;
    }

    public void setLoginLowerCase(String loginLowerCase) {
        this.loginLowerCase = loginLowerCase;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordEncryption() {
        return passwordEncryption;
    }

    public void setPasswordEncryption(String passwordEncryption) {
        this.passwordEncryption = passwordEncryption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String groupNames) {
        this.groupNames = groupNames;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getTimeZoneAuto() {
        return timeZoneAuto;
    }

    public void setTimeZoneAuto(Boolean timeZoneAuto) {
        this.timeZoneAuto = timeZoneAuto;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<UserSubstitution> getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(List<UserSubstitution> substitutions) {
        this.substitutions = substitutions;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getIpMask() {
        return ipMask;
    }

    public void setIpMask(String ipMask) {
        this.ipMask = ipMask;
    }

    public String getCaption() {
        String pattern = AppContext.getProperty("cuba.user.namePattern");
        if (StringUtils.isBlank(pattern)) {
            pattern = "{1} [{0}]";
        }
        MessageFormat fmt = new MessageFormat(pattern);
        return StringUtils.trimToEmpty(fmt.format(new Object[]{
                StringUtils.trimToEmpty(login),
                StringUtils.trimToEmpty(name)
        }));
    }

    public Boolean getChangePasswordAtNextLogon() {
        return changePasswordAtNextLogon;
    }

    public void setChangePasswordAtNextLogon(Boolean changePasswordAtNextLogon) {
        this.changePasswordAtNextLogon = changePasswordAtNextLogon;
    }

    @Transient
    @Deprecated
    public String getSalt() {
        return id != null ? id.toString() : "";
    }
}