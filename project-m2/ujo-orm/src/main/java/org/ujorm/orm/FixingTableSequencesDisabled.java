/*
 *  Copyright 2018 Pavel Ponec
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

import java.sql.Connection;
import org.jetbrains.annotations.Nullable;
import org.ujorm.orm.metaModel.MetaDatabase;

/**
 * Class for no sequence corrections
 * @author Pavel Ponec
 */
public final class FixingTableSequencesDisabled extends FixingTableSequences {

    /** Konstruktor */
    public FixingTableSequencesDisabled(@Nullable final MetaDatabase db, @Nullable final Connection conn) throws Exception {
        super(null, conn);
    }

    @Override
    public void run() {
    }

}
