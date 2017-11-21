/*
 * Copyright 2014, Pavel Ponec
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
package org.ujorm.hotels.service.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.hotels.entity.enums.ModuleEnum;
import org.ujorm.hotels.service.impl.AbstractModuleParams;
import org.ujorm.hotels.service.param.annot.PersonalParam;
import org.ujorm.orm.annot.Comment;
/**
 * Common database service implementations
 * @author Ponec
 */
@Service("applRoles")
public class ApplicationRoles<U extends ApplicationRoles> extends AbstractModuleParams {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRoles.class);

    /** Factory */
    private static final KeyFactory<ApplicationRoles> f = newFactory(ApplicationRoles.class);

    @Comment("The Manager role of a user")
    @PersonalParam
    public static final Key<ApplicationRoles, Boolean> MANAGER = f.newKey("Manager", false);
    @Comment("The Tester role of a user")
    @PersonalParam
    public static final Key<ApplicationRoles, Boolean> TESTER = f.newKey("Tester", false);
    @Comment("The Service role of a user")
    @PersonalParam
    public static final Key<ApplicationRoles, Boolean> SERVICE = f.newKey("Service", false);

    static { f.lock(); }

    @Override
    public ModuleEnum getModule() {
        return ModuleEnum.ROLE;
    }

    // --- Generated Getters / Setters powered by: UjoCodeGenerator-1.1.2.nbm ---

    /** The Manager role of a user */
    public Boolean getManager() {
        return MANAGER.of(this);
    }

    /** The Manager role of a user */
    public boolean isManager() {
        final Boolean result = getManager();
        return result != null && result;
    }

    /** The Tester role of a user */
    public Boolean getTester() {
        return TESTER.of(this);
    }

    /** The Tester role of a user */
    public boolean isTester() {
        final Boolean result = getTester();
        return result != null && result;
    }

    /** The Service role of a user */
    public Boolean getService() {
        return SERVICE.of(this);
    }

    /** The Service role of a user */
    public boolean isService() {
        final Boolean result = getService();
        return result != null && result;
    }
    
}
