package io.javalin.example.java.user;

import io.javalin.core.security.BasicAuthCredentials;
import io.javalin.example.java.ErrorResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.plugin.openapi.annotations.*;

// This is a controller, it should contain logic related to client/server IO
public class UserController {

    @OpenApi(
            summary = "Create user",
            operationId = "createUser",
            path = "/users",
            method = HttpMethod.POST,
            tags = {"User"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewUserRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "201"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void create(Context ctx) {
        NewUserRequest user = ctx.bodyAsClass(NewUserRequest.class);
        UserService.save(user.name, user.email);
        ctx.status(201);
    }

    @OpenApi(
            summary = "Get all users",
            operationId = "getAllUsers",
            path = "/users",
            method = HttpMethod.GET,
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = User[].class)})
            }
    )
    public static void getAll(Context ctx) {
        ctx.json(UserService.getAll());
    }

    @OpenApi(
            summary = "Get user by ID",
            operationId = "getUserById",
            path = "/users/:userId",
            method = HttpMethod.GET,
            pathParams = {@OpenApiParam(name = "userId", type = Integer.class, description = "The user ID")},
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "200", content = {@OpenApiContent(from = User.class)}),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            }
    )
    public static void getOne(Context ctx) {
        User user = UserService.findById(validPathParamUserId(ctx));
        if (user == null) {
            throw new NotFoundResponse("User not found");
        } else {
            ctx.json(user);
        }
    }

    @OpenApi(
            summary = "Update user by ID",
            operationId = "updateUserById",
            path = "/users/:userId",
            method = HttpMethod.PATCH,
            pathParams = {@OpenApiParam(name = "userId", type = Integer.class, description = "The user ID")},
            /*
            headers = {
                    @OpenApiParam(name = "X-API-USERNAME", description = "API-USERNAME", required = true),
                    @OpenApiParam(name = "X-API-KEY", description = "API-Key", required = true)
            },
             */
            tags = {"User"},
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = NewUserRequest.class)}),
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            },
            security = {
                @OpenApiSecurity(name = "API_USER"),
                @OpenApiSecurity(name = "API_KEY")
            }
    )
    public static void update(Context ctx) {

        if(!"user".equals(ctx.header("X-API-USERNAME")) || !"f45s4f4sfs5".equals(ctx.header("X-API-KEY"))){
            throw new ForbiddenResponse("Auth failed!");
        }

        User user = UserService.findById(validPathParamUserId(ctx));
        if (user == null) {
            throw new NotFoundResponse("User not found");
        } else {
            NewUserRequest newUser = ctx.bodyAsClass(NewUserRequest.class);
            UserService.update(user.id, newUser.name, newUser.email);
            ctx.status(204);
        }
    }

    @OpenApi(
            summary = "Delete user by ID",
            operationId = "deleteUserById",
            path = "/users/:userId",
            method = HttpMethod.DELETE,
            pathParams = {@OpenApiParam(name = "userId", type = Integer.class, description = "The user ID")},
            tags = {"User"},
            responses = {
                    @OpenApiResponse(status = "204"),
                    @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
                    @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
            },
            security = {
                @OpenApiSecurity(name = "basicAuth")
            }
    )
    public static void delete(Context ctx) {

        if(ctx.sessionAttribute("logged-in-user") == null) {
            if(!ctx.basicAuthCredentialsExist()) {
                throw new ForbiddenResponse("Auth failed!");
            }
            BasicAuthCredentials creds = ctx.basicAuthCredentials();
            if (credentialsAreCorrect(creds)) {
                ctx.sessionAttribute("logged-in-user", creds.getUsername());
            }
            else {
                throw new ForbiddenResponse("Auth failed!");
            }
        }

        User user = UserService.findById(validPathParamUserId(ctx));
        if (user == null) {
            throw new NotFoundResponse("User not found");
        } else {
            UserService.delete(user.id);
            ctx.status(204);
        }
    }

    private static boolean credentialsAreCorrect(BasicAuthCredentials creds) {

        return "user".equals(creds.getUsername()) && "1234".equals(creds.getPassword());
    }

    // Prevent duplicate validation of userId
    private static int validPathParamUserId(Context ctx) {
        return ctx.pathParam("userId", Integer.class).check(id -> id >= 0).get();
    }

}
