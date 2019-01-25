/*
 * Copyright 2013 Pavel Ponec
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
package org.ujorm.wicket;

/**
 * Common GUI Actions
 * @author Pavel Ponec
 */
public interface CommonActions {

    /** Default value is the same like the field */
    public static final String CREATE = "CREATE";
    /** Default value is the same like the field */
    public static final String READ = "DISPLAY";
    /** Default value is the same like the field */
    public static final String UPDATE = "UPDATE";
    /** Default value is the same like the field */
    public static final String DELETE = "DELETE";
    /** Default value is the same like the field */
    public static final String CLONE = "CLONE";
    /** Default value is the same like the field */
    public static final String FILTER = "FILTER";
    /** Default value is the same like the field */
    public static final String EXIT = "EXIT";
    /** Default value is the same like the field */
    public static final String ERROR = "ERROR";
    /** Default value is the same like the field */
    public static final String LOGIN = "LOGIN";
    /** Default value is the same like the field */
    public static final String LOGIN_CHANGED = "LOGIN_CHANGED";
    /** Default value is the same like the field */
    public static final String SELECT = "SELECT";
    /** Default value is the same like the field */
    public static final String CHANGED = "CHANGED";
    /** Default value is an empty string */
    public static final String UNDEFINED = "";

}
