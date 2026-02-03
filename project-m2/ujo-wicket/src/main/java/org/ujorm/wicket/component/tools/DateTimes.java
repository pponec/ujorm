/*
 *  Copyright 2014-2026 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.wicket.component.tools;

/**
 * DateTimes support
 * @author Pavel Ponec
 */
public abstract class DateTimes {

    /** Default 'date' format key for localizations */
    public static final String LOCALE_DATE_FORMAT_KEY = "locale.date.pattern";
    /** Default 'datetime' format key for localizations */
    public static final String LOCALE_DATETIME_FORMAT_KEY = "locale.datetime.pattern";
    /** Default 'date' format by the 'ISO 8601' */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    /** Default 'datetime' format by the 'ISO 8601' */
    public static final String DEFAULT_DATETIME_PATTERN = DEFAULT_DATE_PATTERN + " HH:mm";

    /** Get a date pattern by the localizationKey. */
    public static String getDefaultPattern(final String localizationKey) {
        return LOCALE_DATE_FORMAT_KEY.equals(localizationKey)
             ? DEFAULT_DATE_PATTERN
             : DEFAULT_DATETIME_PATTERN;
    }
}
