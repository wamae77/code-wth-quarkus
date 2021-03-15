package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Path("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationUserResource {

    @Inject
    PgPool client;

    @Inject
    TokenService service;

    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    @Claim(value = "id")
    String id;


    @POST
    @PermitAll
    public Uni<Response> RegisterUserFunction(ApplicationUser applicationUser) {
        if (!ApplicationUser.EmailIsValid.apply(applicationUser.email))
            throw new WebApplicationException(Response.status(404).entity(" Invalid Email").build());
        return ApplicationUser.UserExistsByEmail.apply(client, applicationUser.email)
                .onItem().transform(Boolean::booleanValue)
                .flatMap(exist -> {
                    if (!exist)
                        return ApplicationUser.RegisterUser.apply(client,applicationUser)
                                .onItem().transform(id -> URI.create("/user/" + id))
                                .onItem().transform(uri -> Response.created(uri).build());
                    throw new WebApplicationException(Response.status(404).entity(" User already exist ").build());
                });
    }


    @POST
    @Path("/login")
    @PermitAll
    public Uni<Map<String, Object>> LoginService(ApplicationUser user) {
        ApplicationUser applicationUser = ApplicationUser.loginFunction(client, user.email, user.password);

        String token = service.generateUserToken(applicationUser.id, applicationUser.email, applicationUser.fname);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return Uni.createFrom().item(map);
    }


    @GET
    public Multi<ApplicationUser> allUsers() {
        return ApplicationUser.findAllUsers.apply(client);
    }

    @PUT
    @RolesAllowed({Roles.ADMIN, Roles.USER})
    public Uni<Response> updateUser(ApplicationUser applicationUser) throws IOException {
        return ApplicationUser.updateUserDetails.apply(client, Integer.valueOf(id),applicationUser)
                .onItem().transform(updated -> updated ? Response.Status.OK : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @RolesAllowed({Roles.ADMIN, Roles.USER})
    public Uni<Response> delete() {
        return ApplicationUser.deleteUser.apply(client, Long.valueOf(id))
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

}

