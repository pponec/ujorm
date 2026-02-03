/*
 *  Copyright 2020-2022 Pavel Ponec
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

import org.ujorm.orm.annot.Comment;

/**
 * Policy for assigning a comment form {@link Comment} annotation to database.
 * @see Comment
 * @see org.ujorm.orm.dialect.MySqlDialect#printComment(org.ujorm.orm.metaModel.MetaColumn, Appendable) MySqlDialect column implementation
 * @author Pavel Ponec
 */
@Comment
public enum CommentPolicy {

    /** Assign all available comments from annotations to database on a new database object event,
     * exactly on new table, column or index. It is a default value. */
    ON_ANY_CHANGE,
    /** Assign the comment for a new table or new column. */
    FOR_NEW_OBJECT,
    /** No comments are assigned */
    NEVER,
    /** Write all available comments from annotations to database on each metamodel loading event. */
    ALWAYS,

}
