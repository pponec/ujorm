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

package org.ujoframework.orm.renderers;

import java.io.IOException;
import org.ujoframework.orm.SqlRenderer;
import org.ujoframework.orm.UjoSequencer;

/** PostgreSQL (http://www.postgresql.org/) */
public class PostgreSqlRenderer extends SqlRenderer {

    @Override
    public String getJdbcUrl() {
        return "";
    }

    @Override
    public String getJdbcDriver() {
        return "org.postgresql.Driver";
    }

    /** Print SQL 'CREATE SCHEMA' */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA ");
        out.append(schema);
        return out;
    }

    /** Print SQL CREATE SEQUENCE. */
    @Override
    public Appendable printCreateSequence(final UjoSequencer sequence, final Appendable out) throws IOException {

        String seqName
            = sequence.getDatabasSchema()
            + '.'
            + sequence.getSequenceName()
            ;
        out.append("CREATE SEQUENCE ");
        out.append(seqName);
        out.append(" START WITH " + sequence.getInitValue());
        out.append(" INCREMENT BY " + sequence.getInitIncrement());
        out.append(" CACHE " + sequence.getInitDbCache());

        // 
        return out;
    }



    /** Print SQL NEXT SEQUENCE. */
    @Override
    public Appendable printSeqNextValue(final UjoSequencer sequence, final Appendable out) throws IOException {
        String seqName
            = sequence.getDatabasSchema()
            + '.'
            + sequence.getSequenceName()
            ;
        out.append("SELECT CURRVAL('");
        out.append(seqName);
        out.append("'), NEXTVAL('");
        out.append(seqName);
        out.append("')");
        return out;
    }


}
