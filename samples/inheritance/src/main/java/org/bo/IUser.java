/*
 *  Copyright 2009 Paul Ponec
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

package org.bo;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the User object has got an collection of Items.
 * @hidden
 */
public interface IUser {

    public String getLogin();

    public void setLogin(String login);

    public String getName();

    public void setName(String name);

    public String getPassword();

    public void setPassword(String password);



}
