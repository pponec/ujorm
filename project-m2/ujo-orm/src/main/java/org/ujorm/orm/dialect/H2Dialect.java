/*
 *  Copyright 2009-2014 Pavel Ponec
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
package org.ujorm.orm.dialect;

import org.ujorm.orm.SqlDialect;

/** H2 (http://www.h2database.com) */
@SuppressWarnings("unchecked")
public class H2Dialect extends SqlDialect {

    /** Returns a default JDBC URL.
     * <br>For a PostgreSQL simulation use: 
     * {@code jdbc:h2:mem:db1;MODE=PostgreSQL}
     */    
    public String getJdbcUrl() {
        return "jdbc:h2:mem:db1";
    }

    /** Returns a JDBC Driver */
    public String getJdbcDriver() {
        return "org.h2.Driver";
    }

    // --- SEQUENCE BEG ---

//    /** Print full sequence name */
//    protected Appendable printSequenceName(final UjoSequencer sequence, final Appendable out) throws IOException {
//        out.append(sequence.getSequenceName());
//        return out;
//    }
//
//    /** Print SQL CREATE SEQUENCE. */
//    public Appendable printSequenceTable(final UjoSequencer sequence, final Appendable out) throws IOException {
//        out.append("CREATE SEQUENCE ");
//        printSequenceName(sequence, out);
//        out.append(" START WITH " + sequence.getIncrement());
//        out.append(" CACHE " + sequence.getInitDbCache());
//        return out;
//    }
//
//    /** Print SQL ALTER SEQUENCE to modify INCREMENT. */
//    public Appendable printSequenceSetValue(final UjoSequencer sequence, final Appendable out) throws IOException {
//        out.append("ALTER SEQUENCE ");
//        printSequenceName(sequence, out);
//        out.append(" INCREMENT BY " + sequence.getIncrement());
//        return out;
//    }
//
//    /** Print the NEXT SQL SEQUENCE. */
//    public Appendable printSequenceCurrentValue(final UjoSequencer sequence, final Appendable out) throws IOException {
//        out.append("SELECT NEXTVAL('");
//        printSequenceName(sequence, out);
//        out.append("')");
//        return out;
//    }
//
//    /** Print SQL NEXT SEQUENCE Update or print none. The method is intended for an emulator of the sequence. */
//    public Appendable printSequenceNextValue(final UjoSequencer sequence, final Appendable out) throws IOException {
//        return out;
//    }

    // --- SEQUENCE END ---

}

