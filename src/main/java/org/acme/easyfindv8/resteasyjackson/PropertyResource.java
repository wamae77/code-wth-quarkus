package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.jwt.Claim;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("property")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PropertyResource {

    @Inject
    PgPool client;

    @Inject
    @Claim(value = "id")
    String id;


    @POST
    @RolesAllowed({Roles.ADMIN,Roles.USER})
    public Uni<Long> newProperty( Property property) {
        return Property.addAProperty(client,id,property);
    }

    @GET
    @Path("/all")
    @PermitAll
    public Multi<Property> findAllProperties() {
        return Property.findAll.apply(client);
    }


    @GET
    @RolesAllowed({Roles.USER, Roles.ADMIN})
    public Uni<Property> findPropertyById(@QueryParam("id") Integer id) {
        return Property.getPropertyById.apply(client, id);
    }

    @PUT
    @RolesAllowed({Roles.ADMIN,Roles.USER})
    public Uni<Response> updateProperty(Property property) {
        return Property.updateProperty.apply(client, property)
                .onItem().transform(updated -> updated ? Response.Status.OK : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @RolesAllowed({Roles.ADMIN,Roles.USER})
    public Uni<Response> delete(@QueryParam("id") Long id) {
        return Property.deleteProperty.apply(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Long> addImage(@MultipartForm FormData formData, @QueryParam("id")Long idp) throws IOException {
        return Property.uploadImage.apply(client,idp,formData);
    }
}

