/*
 * Copyright 2013 No company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.wicket.component.form.fields;

import javax.annotation.Nullable;

/**
 * interpretation of the "quick and dirty" Java converter found in the comments
 * of that link...
 *
 * @see https://github.com/got5/tapestry5-jquery/issues/294
 * @author Geoff
 */
public class DateConverter {

    public String toJQueryUIDateFormat(@Nullable String dateFormat) {

        if (dateFormat == null) {
            return null;
        } else {

            // Year
            if (dateFormat.contains("yyyy")) {
                dateFormat = dateFormat.replaceAll("yyyy", "yy");
            } else {
                dateFormat = dateFormat.replaceAll("yy", "y");
            }

            // Month
            if (dateFormat.contains("MMMM")) {
                dateFormat = dateFormat.replace("MMMM", "MM");
            } else if (dateFormat.contains("MMM")) {
                dateFormat = dateFormat.replace("MMM", "M");
            } else if (dateFormat.contains("MM")) {
                dateFormat = dateFormat.replace("MM", "mm");
            } else if (dateFormat.contains("M")) {
                dateFormat = dateFormat.replace("M", "m");
            }

            // Day
            if (dateFormat.contains("DD")) {
                dateFormat = dateFormat.replace("DD", "oo");
            } else if (dateFormat.contains("D")) {
                dateFormat = dateFormat.replace("D", "o");
            }

            // Day of month
            if (dateFormat.contains("EEEE")) {
                dateFormat = dateFormat.replace("EEEE", "DD");
            } else if (dateFormat.contains("EEE")) {
                dateFormat = dateFormat.replace("EEE", "D");
            }

            // No time:
            // http://momentjs.com/docs/#/parsing/string-format/ :
            // HH - Hours (24 hour time)
            // mm - Minutes
            // ss - Seconds
            if (dateFormat.contains("HH:mm")) {
                dateFormat = dateFormat.replace("HH:mm", "");
            }

            return dateFormat;
        }
    }
}
