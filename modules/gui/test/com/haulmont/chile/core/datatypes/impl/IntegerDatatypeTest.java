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

package com.haulmont.chile.core.datatypes.impl;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerDatatypeTest extends AbstractDatatypeTestCase {

    private Datatype<Integer> intDt;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        intDt = Datatypes.getNN(Integer.class);
    }

    @Test
    public void parseValid() throws ParseException {
        assertEquals((Integer) 10, intDt.parse("10"));
        assertEquals(null, intDt.parse(""));
        assertEquals((Integer) 10000, intDt.parse("10000"));
    }

    @Test
    public void format() throws ParseException {
        assertEquals("10", intDt.format(10));
        assertEquals("", intDt.format(null));
    }

    @Test
    public void parseLocaleRu() throws ParseException {
        assertEquals((Integer) 10, intDt.parse("10", ruLocale));
        assertEquals(null, intDt.parse("", ruLocale));
        assertEquals((Integer) 10000, intDt.parse("10000", ruLocale));
        assertEquals((Integer) 10000, intDt.parse("10 000", ruLocale));
    }

    @Test
    public void parseLocaleEn() throws ParseException {
        assertEquals((Integer) 10, intDt.parse("10", enGbLocale));
        assertEquals(null, intDt.parse("", enGbLocale));
        assertEquals((Integer) 10000, intDt.parse("10000", enGbLocale));
        assertEquals((Integer) 10000, intDt.parse("10,000", enGbLocale));
    }

    @Test
    public void parseLocaleUnknown() throws ParseException {
        assertEquals((Integer) 10, intDt.parse("10", Locale.FRENCH));
        assertEquals(null, intDt.parse("", Locale.FRENCH));
        assertEquals((Integer) 10000, intDt.parse("10000", Locale.FRENCH));
    }

    @Test
    public void formatRu() throws ParseException {
        assertEquals("10", intDt.format(10, ruLocale));
        assertEquals("10 000", intDt.format(10000, ruLocale));
        assertEquals("", intDt.format(null, ruLocale));
    }

    @Test
    public void formatEn() throws ParseException {
        assertEquals("10", intDt.format(10, enGbLocale));
        assertEquals("10,000", intDt.format(10000, enGbLocale));
        assertEquals("", intDt.format(null, enGbLocale));
    }

    @Test
    public void formatUnknown() throws ParseException {
        assertEquals(intDt.format(10), intDt.format(10, Locale.FRENCH));
        assertEquals(intDt.format(10000), intDt.format(10000, Locale.FRENCH));
        assertEquals(intDt.format(null), intDt.format(null, Locale.FRENCH));
    }

    @Test
    public void parseDouble() {
        Assertions.assertThrows(ParseException.class, () -> intDt.parse("12.1"));
    }

    @Test
    public void parseRoundedDouble() {
        Assertions.assertThrows(ParseException.class, () -> intDt.parse("12.0"));
    }

    @Test
    public void parseLowerThanMIN() {
        Assertions.assertThrows(ParseException.class, () -> intDt.parse("-1000000000000"));
    }

    @Test
    public void parseGreaterThanMAX() {
        Assertions.assertThrows(ParseException.class, () -> intDt.parse("1000000000000"));
    }

    @Test
    public void parseLowerThanMINLong() {
        Assertions.assertThrows(ParseException.class, () -> intDt.parse("-1000000000000000000000"));
    }

    @Test
    public void parseGreaterThanMAXLong() {
        Assertions.assertThrows(ParseException.class, () -> intDt.parse("1000000000000000000000"));
    }
}