/*
 *  Copyright 2012 pavel.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.orm.pojo.orm_tutorial.sample;

import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Order;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ujorm.orm.ITypeService;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.Order.State;

/**
 *
 * @author pavel
 */
public class StateConverter_3 implements ITypeService<Order.State, String> {

    public State getValue(MetaColumn mColumn, ResultSet rs, int c) throws SQLException {
        String str = rs.getString(c);

        if (str.startsWith("A")) {
            return State.ACTIVE;
        } else {
            return State.DELETED;
        }
    }

    public State getValue(MetaColumn mColumn, CallableStatement rs, int c) throws SQLException {
        String str = rs.getString(c);

        if (str.startsWith("A")) {
            return State.ACTIVE;
        } else {
            return State.DELETED;
        }
    }

    public void setValue(MetaColumn mColumn, PreparedStatement rs, State value, int c) throws SQLException {
        rs.setString(c, value.name().substring(0, 4));
    }

    public Class<String> getDbTypeClass(MetaColumn column) {
        return String.class;
    }

}
