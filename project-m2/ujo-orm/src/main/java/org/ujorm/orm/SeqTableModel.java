/*
 *  Copyright 2011-2022 Pavel Ponec
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
 * Class to describe a sequence table model for an internal sequence database support
 * @author Pavel Ponec
 */
final public class SeqTableModel {

    private final String tableName;
    private final String id;
    private final String sequence;
    private final String cache;
    private final String maxValue;
    private final String comment;

    /** Default Constructor */
    public SeqTableModel() {
        this( "ujorm_pk_support", "id", "seq", "cache", "max_value"
            , "Auxiliary table of the Ujorm framework to hold sequence values");
    }

    public SeqTableModel
            ( String seqTableName
            , String idColumn
            , String sequenceColumn
            , String cacheColumn
            , String maxValueColumn
            , String comment
        ){
        this.tableName = seqTableName;
        this.id = idColumn;
        this.sequence = sequenceColumn;
        this.cache = cacheColumn;
        this.maxValue = maxValueColumn;
        this.comment = comment;
    }

    /** The table key for a common sequence emulator.
     * <br>The SQL script for migration to the Ujorm 0.93:
     * <pre>ALTER TABLE ormujo_pk_support RENAME TO ujorm_pk_support;</pre>.
     * Default value is 'ujorm_pk_support';
     */
    public String getTableName() {
        return tableName;
    }

    /** Get comment of the database table */
    public String getTableComment() {
        return comment;
    }

    /** ID column name, default value is 'id'. */
    public String getId() {
        return id;
    }

    /** Sequence column name, default value is 'seq'. */
    public String getSequence() {
        return sequence;
    }

    /** Cache Column Name, default value is 'cache'. */
    public String getCache() {
        return cache;
    }

    /** MaxValue column name, default value is 'max_value'. */
    public String getMaxValue() {
        return maxValue;
    }
}