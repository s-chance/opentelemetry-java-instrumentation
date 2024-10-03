/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.spring.webmvc.v6_0.filter;

import static io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint.INDEXED_CHILD;

import io.opentelemetry.instrumentation.testing.junit.http.AbstractHttpServerTest;
import io.opentelemetry.instrumentation.testing.junit.http.ServerEndpoint;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ServletFilterConfig {

  @Bean
  Filter servletFilter() {
    return new Filter() {

      @Override
      public void init(FilterConfig filterConfig) {}

      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        ServerEndpoint endpoint = ServerEndpoint.forPath(req.getServletPath());
        if (endpoint != null && endpoint != ServerEndpoint.NOT_FOUND) {
          AbstractHttpServerTest.controller(
              endpoint,
              () -> {
                resp.setContentType("text/plain");
                if (endpoint == ServerEndpoint.QUERY_PARAM) {
                  resp.setStatus(endpoint.getStatus());
                  resp.getWriter().print(req.getQueryString());
                } else if (endpoint == ServerEndpoint.REDIRECT) {
                  resp.sendRedirect(endpoint.getBody());
                } else if (endpoint == ServerEndpoint.CAPTURE_HEADERS) {
                  resp.setHeader("X-Test-Response", req.getHeader("X-Test-Request"));
                  resp.setStatus(endpoint.getStatus());
                  resp.getWriter().print(endpoint.getBody());
                } else if (endpoint == ServerEndpoint.ERROR) {
                  resp.sendError(endpoint.getStatus(), endpoint.getBody());
                } else if (endpoint == ServerEndpoint.EXCEPTION) {
                  throw new IllegalStateException(endpoint.getBody());
                } else if (endpoint == ServerEndpoint.INDEXED_CHILD) {
                  INDEXED_CHILD.collectSpanAttributes(req::getParameter);
                  resp.getWriter().print(endpoint.getBody());
                } else {
                  resp.setStatus(endpoint.getStatus());
                  resp.getWriter().print(endpoint.getBody());
                }
              });
        } else {
          chain.doFilter(request, response);
        }
      }

      @Override
      public void destroy() {}
    };
  }
}