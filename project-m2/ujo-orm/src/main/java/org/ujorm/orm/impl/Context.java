package org.ujorm.orm.impl;

import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.orm.Config;
import org.ujorm.orm.service.CommonService;
public record Context (
        Config config,
        /** Handler commonService */
        DomainHandlerService domainService,
        /** Common commonService */
        CommonService commonService
) {
    private static final CommonService COMMON_SERVICE = new CommonService();

    public static Context ofDefault() {
        return new Context(Config.ofDefault(), DomainHandlerProvider.provider(), COMMON_SERVICE);
    }

    public static Context of(Config config, DomainHandlerService domainService) {
        return new Context(config,domainService, COMMON_SERVICE);
    }
}
