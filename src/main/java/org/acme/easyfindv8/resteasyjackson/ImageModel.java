package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.function.BiFunction;
import java.util.stream.StreamSupport;

public class ImageModel {
    public int id;
    public int property_id;
    public String imageName;
    public io.vertx.mutiny.core.buffer.Buffer buffer;

    public ImageModel() {
        // no args
    }

    public ImageModel(Integer image_id, Integer property_id, String imageName, Buffer image_data) {
        this.id = image_id;
        this.property_id = property_id;
        this.imageName = imageName;
        this.buffer = image_data;
    }

    public static BiFunction<PgPool,Integer ,Multi<ImageModel>> getImageByPropertyId = (client, id) ->{
        return client.preparedQuery("SELECT * FROM image_files WHERE property_id = $1")
                .execute(Tuple.of(id))
                .onItem().transformToMulti(rows -> Multi.createFrom().items(() -> StreamSupport.stream(rows.spliterator(), false)))
                .onItem().transform(ImageModel::fromImageTable)
                .onFailure().invoke(Throwable::printStackTrace);
    };

    public static ImageModel fromImageTable(Row row) {
        return new ImageModel(
                row.getInteger("image_id"),
                row.getInteger("property_id"),
                row.getString("imageName"),
                row.getBuffer("image_data")
        );
    }
}