package com.sparta.i_mu.filter;

public class AuthenticatedMatchers {

    private AuthenticatedMatchers() {
    }

    public static final String[] swaggerArray = {
            "/api-docs",
            "/swagger-ui-custom.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/api-docs/**",
            "/swagger-ui.html"
    };
}
