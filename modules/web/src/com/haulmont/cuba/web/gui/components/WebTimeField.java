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
package com.haulmont.cuba.web.gui.components;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.FormatStringsRegistry;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.global.DateTimeTransformations;
import com.haulmont.cuba.core.global.DateTimeTransformations.AmPm;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.RequiredValueMissingException;
import com.haulmont.cuba.gui.components.TimeField;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.cuba.gui.components.ValidationFailedException;
import com.haulmont.cuba.gui.components.data.ConversionException;
import com.haulmont.cuba.gui.components.data.DataAwareComponentsTools;
import com.haulmont.cuba.gui.components.data.ValueSource;
import com.haulmont.cuba.gui.components.data.meta.EntityValueSource;
import com.haulmont.cuba.web.widgets.CubaComboBox;
import com.haulmont.cuba.web.widgets.CubaCssActionsLayout;
import com.haulmont.cuba.web.widgets.CubaTimeField;
import com.vaadin.data.HasValue;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractComponent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;
import static com.haulmont.cuba.web.gui.components.WebWrapperUtils.*;

public class WebTimeField<V>
        extends WebAbstractViewComponent<CubaCssActionsLayout, LocalTime, V>
        implements TimeField<V>, InitializingBean {

    public static final String TIME_FIELD_STYLENAME = "c-timefield-layout";
    public static final String AM_PM_FIELD_STYLE_NAME = "c-timefield-ampm";

    protected static final int VALIDATORS_LIST_INITIAL_CAPACITY = 2;

    @Inject
    protected DateTimeTransformations dateTimeTransformations;

    protected DataAwareComponentsTools dataAwareComponentsTools;

    protected Resolution resolution = Resolution.MIN;
    protected Datatype<V> datatype;

    protected TimeMode timeMode = TimeMode.H_24;
    protected AmPm amPm = AmPm.AM;

    protected CubaTimeField timeField;
    protected CubaComboBox<AmPm> amPmField;

    protected List<Consumer> validators; // lazily initialized list

    protected boolean editable = true;
    protected boolean updatingInstance;

    public WebTimeField() {
        component = createComponent();
        component.setPrimaryStyleName(TIME_FIELD_STYLENAME);

        timeField = createTimeField();
        initTimeField(timeField);

        amPmField = createAmPmField();
        initAmPmField(amPmField);

        setWidthAuto();

        timeField.addValueChangeListener(createTimeValueChangeListener());
        amPmField.addValueChangeListener(createAmPmValueChangeListener());

        initLayout();
    }

    @Inject
    public void setDataAwareComponentsTools(DataAwareComponentsTools dataAwareComponentsTools) {
        this.dataAwareComponentsTools = dataAwareComponentsTools;
    }

    @Override
    public void afterPropertiesSet() {
        UserSessionSource userSessionSource = beanLocator.get(UserSessionSource.NAME);
        FormatStringsRegistry formatStringsRegistry = beanLocator.get(FormatStringsRegistry.NAME);
        String timeFormat = formatStringsRegistry.getFormatStringsNN(userSessionSource.getLocale()).getTimeFormat();
        setFormat(timeFormat);
    }

    @Override
    public void setFormat(String format) {
        timeField.setTimeFormat(format);
    }

    @Override
    public String getFormat() {
        return timeField.getTimeFormat();
    }

    @Override
    public Resolution getResolution() {
        return resolution;
    }

    @Override
    public void setResolution(Resolution resolution) {
        checkNotNullArgument(resolution);

        this.resolution = resolution;
        timeField.setResolution(convertTimeResolution(resolution));
    }

    @Override
    public Datatype<V> getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(Datatype<V> datatype) {
        dataAwareComponentsTools.checkValueSourceDatatypeMismatch(datatype, getValueSource());

        this.datatype = datatype;
    }

    @Override
    public boolean getShowSeconds() {
        return resolution == Resolution.SEC;
    }

    @Override
    public void setShowSeconds(boolean showSeconds) {
        setResolution(Resolution.SEC);
    }

    @Override
    public void focus() {
        timeField.focus();
    }

    @Override
    public int getTabIndex() {
        return timeField.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        timeField.setTabIndex(tabIndex);
    }

    @Override
    public void commit() {
        if (valueBinding != null) {
            valueBinding.write();
        }
    }

    @Override
    public void discard() {
        if (valueBinding != null) {
            valueBinding.discard();
        }
    }

    @Override
    public boolean isBuffered() {
        return valueBinding != null
                && valueBinding.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        if (valueBinding != null) {
            valueBinding.setBuffered(buffered);
        }
    }

    @Override
    public boolean isModified() {
        return valueBinding != null
                && valueBinding.isModified();
    }

    @Override
    public void setTimeMode(TimeMode timeMode) {
        checkNotNullArgument("Time mode cannot be null");

        TimeMode oldMode = this.timeMode;

        this.timeMode = timeMode;

        if (oldMode != timeMode) {
            if (use12hMode()) {
                component.addComponent(amPmField);
            } else {
                component.removeComponent(amPmField);
            }

            if (getValue() != null) {
                setValueToPresentation(convertToPresentation(getValue()));
            }
        }
    }

    @Override
    public TimeMode getTimeMode() {
        return timeMode;
    }

    @Override
    public boolean isRequired() {
        return timeField.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequired(boolean required) {
        // Set requiredIndicatorVisible to a component
        // in order to show required indicator
        component.setRequiredIndicatorVisible(required);
        // Set requiredIndicatorVisible to fields
        // in order to show required message and apply error styles
        timeField.setRequiredIndicatorVisible(required);

        setupComponentErrorProvider(required, timeField);
    }

    @Override
    public String getRequiredMessage() {
        return timeField.getRequiredError();
    }

    @Override
    public void setRequiredMessage(String msg) {
        timeField.setRequiredError(msg);
    }

    @Override
    public void addValidator(Consumer<? super V> validator) {
        if (validators == null) {
            validators = new ArrayList<>(VALIDATORS_LIST_INITIAL_CAPACITY);
        }
        if (!validators.contains(validator)) {
            validators.add(validator);
        }
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        if (validators != null) {
            validators.remove(validator);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Consumer<V>> getValidators() {
        if (validators == null) {
            return Collections.emptyList();
        }
        return (Collection) Collections.unmodifiableCollection(validators);
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    protected LocalTime convertToPresentation(V modelValue) throws ConversionException {
        if (modelValue == null) {
            return null;
        }

        LocalTime time = dateTimeTransformations.transformToLocalTime(modelValue);

        return use12hMode()
                ? dateTimeTransformations.transformTo12hTime(time)
                : time;
    }

    @Override
    public void setValue(V value) {
        this.amPm = dateTimeTransformations.getAmPm(value);

        setValueToPresentation(convertToPresentation(value));

        V oldValue = internalValue;
        this.internalValue = value;

        if (!fieldValueEquals(value, oldValue)) {
            ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, false);
            publish(ValueChangeEvent.class, event);
        }
    }

    @Override
    protected void setValueToPresentation(LocalTime value) {
        updatingInstance = true;
        try {
            if (value == null) {
                timeField.setValue(null);
                amPmField.setValue(AmPm.AM);
            } else {
                timeField.setValue(value);
                amPmField.setValue(amPm);
            }
        } finally {
            updatingInstance = false;
        }
    }

    @Override
    public boolean isValid() {
        try {
            validate();
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    @Override
    public void validate() throws ValidationException {
        if (hasValidationError()) {
            setValidationError(null);
        }

        if (!isVisibleRecursive() || !isEditableWithParent() || !isEnabledRecursive()) {
            return;
        }

        if (isEmpty() && isRequired()) {
            String requiredMessage = getRequiredMessage();
            if (requiredMessage == null) {
                Messages messages = beanLocator.get(Messages.NAME);
                requiredMessage = messages.getMainMessage("validationFail.defaultRequiredMessage");
            }
            throw new RequiredValueMissingException(requiredMessage, this);
        }

        V value = getValue();
        triggerValidators(value);
    }

    protected CubaCssActionsLayout createComponent() {
        return new CubaCssActionsLayout();
    }

    protected CubaTimeField createTimeField() {
        return new CubaTimeField();
    }

    protected void initTimeField(CubaTimeField timeField) {
    }

    protected CubaComboBox<AmPm> createAmPmField() {
        return new CubaComboBox<>();
    }

    protected void initAmPmField(CubaComboBox<AmPm> amPmField) {
        amPmField.addStyleName(AM_PM_FIELD_STYLE_NAME);

        amPmField.setItems(AmPm.values());
        amPmField.setValue(AmPm.AM);

        amPmField.setEmptySelectionAllowed(false);
        amPmField.setTextInputAllowed(false);
        amPmField.setWidthUndefined();
    }

    protected void setupComponentErrorProvider(boolean required, AbstractComponent component) {
        if (required) {
            component.setComponentErrorProvider(this::getErrorMessage);
        } else {
            component.setComponentErrorProvider(null);
        }
    }

    protected ErrorMessage getErrorMessage() {
        return (isEditableWithParent() && isRequired() && isEmpty())
                ? new UserError(getRequiredMessage())
                : null;
    }

    protected void triggerValidators(V value) throws ValidationFailedException {
        if (validators != null) {
            try {
                for (Consumer validator : validators) {
                    validator.accept(value);
                }
            } catch (ValidationException e) {
                setValidationError(e.getDetailsMessage());

                throw new ValidationFailedException(e.getDetailsMessage(), this, e);
            }
        }
    }

    protected HasValue.ValueChangeListener<LocalTime> createTimeValueChangeListener() {
        return event ->
                componentValueChanged(event.isUserOriginated());
    }

    protected HasValue.ValueChangeListener<AmPm> createAmPmValueChangeListener() {
        return e -> {
            amPm = e.getValue();
            if (use12hMode() && getValue() != null) {
                componentValueChanged(e.isUserOriginated());
            }
        };
    }

    protected void componentValueChanged(boolean isUserOriginated) {
        if (isUserOriginated) {
            V value;

            try {
                value = constructModelValue();

                setValueToPresentation(convertToPresentation(value));
            } catch (ConversionException ce) {
                LoggerFactory.getLogger(WebDateField.class)
                        .trace("Unable to convert presentation value to model", ce);

                setValidationError(ce.getLocalizedMessage());
                return;
            }

            V oldValue = internalValue;
            internalValue = value;

            if (!fieldValueEquals(value, oldValue)) {
                ValueChangeEvent<V> event = new ValueChangeEvent<>(this, oldValue, value, isUserOriginated);
                publish(ValueChangeEvent.class, event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected V constructModelValue() {
        LocalTime timeValue = timeField.getValue() != null
                ? timeField.getValue()
                : LocalTime.MIDNIGHT;

        if (use12hMode()) {
            if (timeValue.getHour() <= 12) {
                timeValue = dateTimeTransformations.transformFrom12hTime(timeValue, this.amPm);
            } else {
                this.amPm = dateTimeTransformations.getAmPm(timeValue);
            }
        }

        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource) valueSource)
                    .getMetaPropertyPath().getMetaProperty();
            return (V) convertFromLocalTime(timeValue,
                    metaProperty.getRange().asDatatype().getJavaClass());
        }

        return (V) convertFromLocalTime(timeValue,
                datatype == null ? Date.class : datatype.getJavaClass());
    }

    protected Object convertFromLocalTime(LocalTime localTime, Class javaType) {
        return dateTimeTransformations.transformFromLocalTime(localTime, javaType);
    }

    protected void initLayout() {
        component.addComponent(timeField);
        if (use12hMode()) {
            component.addComponent(amPmField);
        }
    }

    protected boolean use12hMode() {
        return timeMode == TimeMode.H_12;
    }
}