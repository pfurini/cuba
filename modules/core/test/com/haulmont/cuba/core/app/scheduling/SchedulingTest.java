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

package com.haulmont.cuba.core.app.scheduling;

import com.haulmont.cuba.core.entity.ScheduledTask;
import com.haulmont.cuba.core.entity.SchedulingType;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.testsupport.TestContainer;
import junit.framework.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchedulingTest {

    private SimpleDateFormat simpleDateFormat;

    @RegisterExtension
    public static TestContainer cont = TestContainer.Common.INSTANCE;

    public SchedulingTest() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-0"));
    }

    @Test
    public void testCurrentStart() throws Exception {
        Scheduling scheduling = new Scheduling() {
            @Override
            protected TimeZone getCurrentTimeZone() {
                return TimeZone.getTimeZone("GMT-0");
            }
        };

        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setSchedulingType(SchedulingType.CRON);
        scheduledTask.setCron("*/5 * * * * *");

        //scheduler has failed couple of runs and now we should run it
        long currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-11-13 15:29:00").getTime(), date("2013-11-13 15:30:00").getTime(), 10000l);
        assertEquals(date("2013-11-13 15:29:55"), new Date(currentStart));

        //last run was year ago, so now-frame should be considered
        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2012-11-13 15:29:00").getTime(), date("2013-11-13 15:30:00").getTime(), 10000l);
        assertEquals(date("2013-11-13 15:29:55"), new Date(currentStart));

        //last run was very close to now, last start date should be considered
        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-11-13 15:29:59").getTime(), date("2013-11-13 15:30:01").getTime(), 10000l);
        assertEquals(date("2013-11-13 15:30:00"), new Date(currentStart));

        scheduledTask.setCron("0 0 0 * * FRI");

        //task should run in next friday
        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-11-08 01:01:01").getTime(), date("2013-11-13 15:30:00").getTime(), 10000l);
        assertEquals(date("2013-11-15 00:00:00"), new Date(currentStart));

        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-11-08 01:01:01").getTime(), date("2013-11-08 01:01:02").getTime(), 600000l);
        assertEquals(date("2013-11-15 00:00:00"), new Date(currentStart));

        //task is late but matches frame
        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-11-07 23:59:59").getTime(), date("2013-11-08 00:01:00").getTime(), 600000l);
        assertEquals(date("2013-11-8 00:00:00"), new Date(currentStart));

        //task is late and does not match frame
        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-11-07 23:59:59").getTime(), date("2013-11-08 00:11:00").getTime(), 600000l);
        assertEquals(date("2013-11-15 00:00:00"), new Date(currentStart));

        scheduledTask.setCron("0 59 1 * * *");

        //time shift forward
        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-10-26 1:59:59").getTime(), date("2013-10-27 00:00:00").getTime(), 600000l);
        assertEquals(date("2013-10-27 01:59:00"), new Date(currentStart));

        //time shift backward
        currentStart = scheduling.calculateNextCronDate(scheduledTask, date("2013-03-30 1:59:00").getTime(), date("2013-03-31 00:00:00").getTime(), 600000l);
        assertEquals(date("2013-03-31 01:59:00"), new Date(currentStart));
    }

    public Date date(String s) {
        try {
            return simpleDateFormat.parse(s);
        } catch (ParseException e) {
            Assertions.fail();
        }

        return null;
    }

    @Test
    public void testRunning() {
        Scheduling scheduling = new Scheduling() {
            {
                timeSource = new TimeSource() {
                    @Override
                    public Date currentTimestamp() {
                        return new Date();
                    }

                    @Override
                    public long currentTimeMillis() {
                        return System.currentTimeMillis();
                    }

                    @Override
                    public ZonedDateTime now() {
                        return ZonedDateTime.now();
                    }
                };
            }

            @Override
            protected TimeZone getCurrentTimeZone() {
                return TimeZone.getTimeZone("GMT-0");
            }
        };
        ScheduledTask scheduledTask = new ScheduledTask();

        assertFalse(scheduling.setRunning(scheduledTask, true));
        assertTrue(scheduling.setRunning(scheduledTask, true));

    }
}