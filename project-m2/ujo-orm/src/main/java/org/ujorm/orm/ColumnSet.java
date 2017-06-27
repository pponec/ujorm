/*
 *  Copyright 2014-2014 Pavel Ponec
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

package org.ujorm.orm;

/**
 * This Interface can be used as a sign for an external column set to be included
 * to a basic database table, so the  {@link ColumnSet} object does not create
 * separated database table.
 * <br>
 * Inner relations to an another implementation of the interface {@link ColumnSet}
 * are allowed.
 *
 * @author Pavel Ponec
 * @deprecated The interface implementations are not finished yet.
 */
public interface ColumnSet extends OrmUjo {

}
