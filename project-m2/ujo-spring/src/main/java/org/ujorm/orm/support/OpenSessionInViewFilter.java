/*
 *  Copyright 2020-2022 Tomas Hampl
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
package org.ujorm.orm.support;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author Hampl
 */
public class OpenSessionInViewFilter implements Filter {

    private UjoSessionFactoryFilter ujoSessionFactoryFilter;

    @Override
    public void init(FilterConfig filterConfig) {
        ServletContext sc = filterConfig.getServletContext();
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
        ujoSessionFactoryFilter = (UjoSessionFactoryFilter) wac.getBean("ujoSessionFactory");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            ujoSessionFactoryFilter.openSession();
            chain.doFilter(request, response);
        } finally {
            ujoSessionFactoryFilter.closeSession();
        }
    }

    @Override
    public void destroy() {
        ujoSessionFactoryFilter = null;
    }
}