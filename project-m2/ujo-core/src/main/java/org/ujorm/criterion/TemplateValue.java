/*
 *  Copyright 2013-2022 Pavel Ponec
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
package org.ujorm.criterion;

/**
 * Template value
 * @author Pavel Ponec
 */
public final class TemplateValue {

    /** Template by the format {@code "{0} OPERATOR {1}"} */
    private final String template;
    /** Criterion right value */
    private final Object rightVale;

    public TemplateValue(String template, Object rightVale) {
        this.template = template != null ? template : "";
        this.rightVale = rightVale;
    }

    /** Template by the format {@code "{0} OPERATOR {1}"}
     * @return A not nul value;
     */
    public String getTemplate() {
        return template;
    }

    /** Criterion right value */
    public Object getRightVale() {
        return rightVale;
    }

    @Override
    public String toString() {
        return template + " // "+ rightVale;
    }
}
