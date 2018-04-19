/*
 *  Copyright 2018-2018 Pavel Ponec
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
package org.ujorm.orm.ao;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Enum of allowed quoting policy.
 * @author Pavel Ponec
 */
public enum QuoteEnum {

    /** Yes the name will be quoted */
    YES,
    /** No the name will not be quoted */
    NO,
    /** Using of quotation marks depends on the global configuration (default value). */
    BY_CONFIG {
        @Override @Nonnull
        public QuoteEnum changeBy(@Nullable QuoteEnum altenative) {
            return altenative != null ? altenative : this;
        }
    },
    ;

    /** Change default */
    @Nonnull
    public QuoteEnum changeBy(@Nullable QuoteEnum altenative) {
        return this;
    }

}
