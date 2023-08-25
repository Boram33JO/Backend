package com.sparta.i_mu.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "P.PLE API 명세서",
                description = "P.PLE 프로젝트에 사용되는 API 명세서",
                version = "v1",
                contact = @Contact(name = "p.ple", url="https://frontend-three-dun.vercel.app/", email = "test@test.com")
        ),
        servers = {@Server(url = "/", description = "api.pple.today")}
)

//2안
//@SecuritySchemes({
//        @io.swagger.v3.oas.annotations.security.SecurityScheme(name = "AccessToken",
//                type = SecuritySchemeType.APIKEY,
//                description = "AccessToken",
//                in = SecuritySchemeIn.HEADER,
//                paramName = "AccessToken"),
//})

@Configuration
public class SwaggerConfig {

    private final String BEARER_TOKEN_PREFIX = "Bearer";
    private final String HEADER_ACCESS_TOKEN = "AccessToken";
    private final String HEADER_REFRESH_TOKEN = "RefreshToken";

    @Bean
    // 운영 환경에는 Swagger를 비활성화하기 위해 추가했습니다.
    // access token, refresh token 입력 추가
//    @Profile("!Prod")
    public OpenAPI openAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(HEADER_ACCESS_TOKEN)
                .addList(HEADER_REFRESH_TOKEN);
        Components components = new Components()
                .addSecuritySchemes(HEADER_ACCESS_TOKEN, new SecurityScheme()
                        .name(HEADER_ACCESS_TOKEN)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .scheme(BEARER_TOKEN_PREFIX)
                        .description("Bearer 포함해서 입력하세요."))
                .addSecuritySchemes(HEADER_REFRESH_TOKEN, new SecurityScheme()
                        .name(HEADER_REFRESH_TOKEN)
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .scheme(BEARER_TOKEN_PREFIX)
                        .description("Bearer 포함해서 입력하세요."));
//                        .bearerFormat("JWT"));

        // Swagger UI 접속 후, 딱 한 번만 accessToken을 입력해주면 모든 API에 토큰 인증 작업이 적용됩니다.
        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .path("/user/login", new PathItem()
                        .post(new Operation()
                                .tags(List.of(
                                        "User"
                                ))
                                .summary("로그인")
                                .description("로그인")
                                .requestBody(new RequestBody().
                                        content(new Content().addMediaType("application/json", new MediaType().schema(new ObjectSchema()
                                                        .addProperty("email" ,new StringSchema())
                                                        .addProperty("password", new StringSchema())))).required(true))
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse().description("OK"))
                                )
                        )
                );
    }

}
