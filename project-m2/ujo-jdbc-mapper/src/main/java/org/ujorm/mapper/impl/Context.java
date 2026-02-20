package org.ujorm.mapper.impl;

import org.ujorm.core.DomainHandlerService;
import org.ujorm.mapper.service.CommonService;
//import org.ujorm.mapper.MapperContext;

/** TODO:pop: create interface from this class */

public record Context (
        Config config,
        /** Handler commonService */
        DomainHandlerService domainService,
        /** Common commonService */
        CommonService commonService
) {








}
