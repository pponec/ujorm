package org.ujorm.orm.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.core.KeyInfo;
import org.ujorm.orm.demo.Employee;
import org.ujorm.orm.impl.Context;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

class CommonServiceTest {

    CommonService instance = new CommonService();

    /** Test finding changes between two domain objects */
    @Test
    void findChanges() {
        var handler = DomainHandlerProvider.getHandler(Employee.class);
        var domain1 = Employee.of(10L, "Test", true);
        var domain2 = Employee.of(11L, "Demo", false);
        var diff = CommonService.findChanges(domain1, domain2, handler).getActive();
        Assertions.assertEquals(3, diff.length);

        domain2 = Employee.of(10L, "Test", true);
        diff = CommonService.findChanges(domain1, domain2, handler).getActive();
        Assertions.assertEquals(0, diff.length);
    }

    /** Test finding primary key directly */
    @Test
    void findPrimaryKey() {
        var keyInfoMock = Mockito.mock(KeyInfo.class);
        when(keyInfoMock.primaryKey()).thenReturn(true);
        var keyMock = Mockito.mock(Key.class);
        when(keyMock.info()).thenReturn(keyInfoMock);

        var handlerMock = Mockito.mock(DomainHandler.class);
        when(handlerMock.getKeyList()).thenReturn(List.of(keyMock));

        var ctxMock = Mockito.mock(Context.class, RETURNS_DEEP_STUBS);
        when(ctxMock.domainService().getHandler(any())).thenReturn(handlerMock);
    }

    /** Test fallback to the first property */
    @Test
    void findPrimaryKey_fallbackToFirstProperty() {
        var keyInfoMock = Mockito.mock(KeyInfo.class);
        when(keyInfoMock.primaryKey()).thenReturn(false);

        var keyMock = Mockito.mock(Key.class);
        when(keyMock.info()).thenReturn(keyInfoMock);

        var handlerMock = Mockito.mock(DomainHandler.class);
        when(handlerMock.getKeyList()).thenReturn(List.of(keyMock));

        var ctxMock = Mockito.mock(Context.class, RETURNS_DEEP_STUBS);
        when(ctxMock.domainService().getHandler(any())).thenReturn(handlerMock);
        when(ctxMock.config().acceptDefaultPk()).thenReturn(true);
    }

    /** Test exception when no primary key is found */
    @Test
    void findPrimaryKey_notFoundThrowsException() {
        var keyInfoMock = Mockito.mock(KeyInfo.class);
        when(keyInfoMock.primaryKey()).thenReturn(false);
        var keyMock = Mockito.mock(Key.class);
        when(keyMock.info()).thenReturn(keyInfoMock);

        var handlerMock = Mockito.mock(DomainHandler.class);
        when(handlerMock.getKeyList()).thenReturn(List.of(keyMock));

        var ctxMock = Mockito.mock(Context.class, RETURNS_DEEP_STUBS);
        when(ctxMock.domainService().getHandler(any())).thenReturn(handlerMock);
        when(ctxMock.config().acceptDefaultPk()).thenReturn(false);
    }

}