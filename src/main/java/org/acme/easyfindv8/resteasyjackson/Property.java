package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.StreamSupport;


public class Property {

    public int id;
    public String property_name;
    public String location;
    public String price;
    public String description;
    public String number_of_rooms;
    public String s3Url;
    public int owner_id;
    public boolean available;
    public LocalDateTime timestamp;
    public LocalDateTime updated_timestmp;
    public io.vertx.mutiny.core.buffer.Buffer buffer;

    public Property() {
        //default constructor
    }

    public Property(int id, String property_name, String location, String price, String description, String number_of_rooms, String s3Url, int owner_id, boolean available, LocalDateTime timestamp, LocalDateTime updated_timestmp) {
        this.id = id;
        this.property_name = property_name;
        this.location = location;
        this.price = price;
        this.description = description;
        this.number_of_rooms = number_of_rooms;
        this.s3Url = s3Url;
        this.owner_id = owner_id;
        this.available = available;
        this.timestamp = timestamp;
        this.updated_timestmp = updated_timestmp;
    }

    public Property(Integer id, String propertyname, String location, String price, String description, String numberofrooms, String s3url, Integer ownerid, Boolean available, LocalDateTime timestmp,LocalDateTime updated_timestmp, io.vertx.mutiny.core.buffer.Buffer image_data) {
        this.id = id;
        this.property_name = propertyname;
        this.location = location;
        this.price = price;
        this.description = description;
        this.number_of_rooms = numberofrooms;
        this.s3Url = s3url;
        this.owner_id = ownerid;
        this.available = available;
        this.timestamp = timestmp;
        this.updated_timestmp= updated_timestmp;
        this.buffer =  image_data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProperty_name() {
        return property_name;
    }

    public void setProperty_name(String property_name) {
        this.property_name = property_name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNumber_of_rooms() {
        return number_of_rooms;
    }

    public void setNumber_of_rooms(String number_of_rooms) {
        this.number_of_rooms = number_of_rooms;
    }

    public String getS3Url() {
        return s3Url;
    }

    public void setS3Url(String s3Url) {
        this.s3Url = s3Url;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(int owner_id) {
        this.owner_id = owner_id;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }



    public static  Uni<Long> addAProperty(PgPool client , String userid, Property property){
        LocalDateTime localDateTime = LocalDateTime.now();
        return client.preparedQuery("INSERT INTO property (propertyname,location,price,description,numberofrooms, ownerid, s3url, timestmp) VALUES ($1,$2,$3,$4,$5,$6,$7,$8) RETURNING (id)")
                .execute(Tuple.wrap(new Object[]{property.property_name, property.location, property.price,property.description, property.number_of_rooms, Integer.valueOf(userid), property.s3Url, localDateTime}))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(rowRowIterator -> rowRowIterator.next().getLong("id"));
    }


    public static Function<PgPool, Multi<Property>> findAll = client -> {
        return client.query("SELECT DISTINCT property.*, image_files.image_data FROM property LEFT JOIN image_files ON property.id=image_files.property_id").execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().items(() -> StreamSupport.stream(rows.spliterator(), false)))
                .onItem().transform(Property::from)
                .onFailure().invoke(Throwable::printStackTrace);
    };


    public static BiFunction<PgPool, Integer, Uni<Property>> getPropertyById = (client, id) -> {
        return client.preparedQuery("SELECT DISTINCT property.*, image_files.image_data FROM property LEFT JOIN image_files ON property.id=image_files.property_id WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(rows -> rows.iterator().hasNext() ? from(rows.iterator().next()) : null)
                .onItem().transform(property -> {
                    if (property == null)
                        throw new WebApplicationException(Response.status(404).entity(" Invalid Id").build());
                    return property;
                });
    };



    public static BiFunction<PgPool, Property, Uni<Boolean>> updateProperty = (client, property) -> {
        LocalDateTime updated_at = LocalDateTime.now();
        return client.preparedQuery("UPDATE Property SET propertyname =$1, location =$2, price =$3 ,description =$4 , numberofrooms =$5 ,s3url =$6 ,ownerid =$7 ,available =$8, updated_timestmp=$9 WHERE id =$10")
                .execute(Tuple.wrap(new Object[]{property.property_name, property.location, property.price, property.description, property.number_of_rooms, property.s3Url, property.owner_id, property.available,updated_at,property.id}))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);

    };



    public static BiFunction<PgPool, Long, Uni<Boolean>> deleteProperty = (client, id) -> {

        return client.preparedQuery("DELETE FROM Property WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    };



    public static TriFunction<PgPool, Long, FormData , Uni<Long>> uploadImage = (client, id, formdata)->{
        Buffer buffer = Buffer.buffer(formdata.data.readAllBytes());
        return client.preparedQuery("INSERT INTO image_files (image_data,imagename, property_id) VALUES($1,$2,$3)  RETURNING (image_id)")
                .execute(Tuple.of(buffer,formdata.fileName,id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(rowRowIterator -> rowRowIterator.next().getLong("image_id"));

    };



    public static Property from(Row row) {
        return new Property(
                row.getInteger("id"),
                row.getString("propertyname"),
                row.getString("location"),
                row.getString("price"),
                row.getString("description"),
                row.getString("numberofrooms"),
                row.getString("s3url"),
                row.getInteger("ownerid"),
                row.getBoolean("available"),
                row.getLocalDateTime("timestmp"),
                row.getLocalDateTime("updated_timestmp"),
                row.getBuffer("image_data")
        );
    }


}
