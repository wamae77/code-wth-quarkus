//package org.acme.easyfindv8.resteasyjackson;
//
//import java.io.File;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import javax.annotation.security.RolesAllowed;
//import javax.inject.Inject;
//import javax.ws.rs.*;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status;
//
//import org.eclipse.microprofile.jwt.Claim;
//import org.jboss.resteasy.annotations.jaxrs.PathParam;
//import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
//
//import io.smallrye.mutiny.Uni;
//import software.amazon.awssdk.core.async.AsyncRequestBody;
//import software.amazon.awssdk.core.async.AsyncResponseTransformer;
//import software.amazon.awssdk.services.s3.S3AsyncClient;
//import software.amazon.awssdk.services.s3.model.*;
//
//@Path("/async-s3")
//public class S3AsyncClientResource extends CommonResource {
//    @Inject
//    S3AsyncClient s3;
//
//    @Inject
//    @Claim(value = "id")
//    String id;
//
//    @POST
//    @Path("upload")
//    @RolesAllowed({Roles.ADMIN,Roles.USER})
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Uni<Response> uploadFile(@MultipartForm FormData formData) {
//
//        if (formData.fileName == null || formData.fileName.isEmpty()) {
//            return Uni.createFrom().item(Response.status(Status.BAD_REQUEST).build());
//        }
//
//        if (formData.mimeType == null || formData.mimeType.isEmpty()) {
//            return Uni.createFrom().item(Response.status(Status.BAD_REQUEST).build());
//        }
//
//        return Uni.createFrom()
//                .completionStage(() -> {
//                    return s3.putObject(buildPutRequest(formData), AsyncRequestBody.fromFile(uploadToTemp(formData.data)));
//                })
//                .onItem().ignore().andSwitchTo(Uni.createFrom().item(Response.created(null).build()))
//                .onFailure().recoverWithItem(th -> {
//                    th.printStackTrace();
//                    return Response.serverError().build();
//                });
//    }
//
//    @GET
//    @Path("download/{objectKey}")
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    @RolesAllowed({Roles.ADMIN,Roles.USER})
//    public Uni<Response> downloadFile(@PathParam("objectKey") String objectKey) throws Exception {
//        File tempFile = tempFilePath();
//
//        return Uni.createFrom()
//                .completionStage(() -> s3.getObject(buildGetRequest(objectKey), AsyncResponseTransformer.toFile(tempFile)))
//                .onItem()
//                .apply(object -> Response.ok(tempFile)
//                        .header("Content-Disposition", "attachment;filename=" + objectKey)
//                        .header("Content-Type", object.contentType()).build());
//    }
//
//    @GET
//    @RolesAllowed({Roles.ADMIN,Roles.USER})
//    public Uni<List<FileObject>> listFiles() {
//        ListObjectsRequest listRequest = ListObjectsRequest.builder()
//                .bucket(bucketName)
//                .build();
//
//        return Uni.createFrom().completionStage(() -> s3.listObjects(listRequest))
//                .onItem().transform(this::toFileItems);
//    }
//
//    @DELETE
//    @RolesAllowed({Roles.ADMIN,Roles.USER})
//    public Uni<Boolean> deleteFile(@QueryParam("objectkey")String objectkey){
//        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
//                .bucket(bucketName)
//                .key(objectkey)
//                .build();
//
//        return Uni.createFrom().completionStage(() ->s3.deleteObject(deleteObjectRequest))
//                .onItem().transform(DeleteObjectResponse::deleteMarker);
//
//
//    }
//
//    private List<FileObject> toFileItems(ListObjectsResponse objects) {
//        return objects.contents().stream()
//                .sorted(Comparator.comparing(S3Object::lastModified).reversed())
//                .map(FileObject::from).collect(Collectors.toList());
//    }
//}
//
