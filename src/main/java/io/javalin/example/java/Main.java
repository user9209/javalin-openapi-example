package io.javalin.example.java;

import io.javalin.Javalin;
import io.javalin.example.java.user.UserController;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    public static void main(String[] args) {
        Javalin.create(config -> {
            config.registerPlugin(getConfiguredOpenApiPlugin());
            config.defaultContentType = "application/json";
        }).routes(() -> {
            path("users", () -> {
                get(UserController::getAll);
                post(UserController::create);
                path(":userId", () -> {
                    get(UserController::getOne);
                    patch(UserController::update);
                    delete(UserController::delete);
                });
            });
        }).start(7002);

        System.out.println("Check out ReDoc docs at http://localhost:7002/redoc");
        System.out.println("Check out Swagger UI docs at http://localhost:7002/swagger-ui");
    }

    private static OpenApiPlugin getConfiguredOpenApiPlugin() {
        Components components = new Components().addSecuritySchemes(
                // "bearerAuth",
                // "basicAuth",
                // "JSESSIONID",
                "API_KEY",

                // https://swagger.io/docs/specification/authentication/bearer-authentication/
                //new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")

                // https://swagger.io/docs/specification/authentication/cookie-authentication/
                // new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("JSESSIONID")

                // https://swagger.io/docs/specification/authentication/api-keys/
                new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-API-KEY")
        ).addSecuritySchemes(
                "API_USER",
                // https://swagger.io/docs/specification/authentication/basic-authentication/
                new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-API-USERNAME")
        ).addSecuritySchemes(
                "basicAuth",
                // https://swagger.io/docs/specification/authentication/basic-authentication/
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")
        );
        Info info = new Info().version("1.0").title("User API").description("Demo API with 5 operations");


        OpenApiOptions options =
                new OpenApiOptions(() -> new OpenAPI()
                        .components(components)
                        .info(info)
                )
                .activateAnnotationScanningFor("io.javalin.example.java")
                .path("/swagger-docs") // endpoint for OpenAPI json
                .swagger(new SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
                .reDoc(new ReDocOptions("/redoc")) // endpoint for redoc
                .defaultDocumentation(doc -> {
                    doc.json("500", ErrorResponse.class);
                    doc.json("503", ErrorResponse.class);
                });
        return new OpenApiPlugin(options);
    }

}
