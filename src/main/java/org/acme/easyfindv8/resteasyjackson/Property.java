package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
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
    public Long timestamp;

    public Property() {
        //default constructor
    }

    public Property(int id, String property_name, String location, String price, String description, String number_of_rooms, String s3Url, int owner_id, boolean available, Long timestamp) {
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
    }

    public static  Uni<Long> addAProperty(PgPool client , String userid, Property property){
        LocalDateTime localDateTime = LocalDateTime.now();
        return client.preparedQuery("INSERT INTO property (propertyname,location,price,description,numberofrooms, ownerid, s3url, timestmp) VALUES ($1,$2,$3,$4,$5,$6,$7,$8) RETURNING (id)")
                .execute(Tuple.wrap(new Object[]{property.property_name, property.location, property.price,property.description, property.number_of_rooms, Integer.valueOf(userid), property.s3Url, localDateTime}))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(rowRowIterator -> rowRowIterator.next().getLong("id"));
    };

    public static Function<PgPool, Multi<Property>> findAll = client -> {
        return client.query("SELECT * FROM Property").execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().items(() -> StreamSupport.stream(rows.spliterator(), false)))
                .onItem().transform(Property::from)
                .onFailure().invoke(Throwable::printStackTrace);
    };

    public static BiFunction<PgPool, Integer, Uni<Property>> getPropertyById = (client, id) -> {
        return client.preparedQuery("SELECT * FROM Property WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(rows -> rows.iterator().hasNext() ? from(rows.iterator().next()) : null)
                .onItem().transform(property -> {
                    if (property == null)
                        throw new WebApplicationException(Response.status(404).entity(" Invalid Id").build());
                    return property;
                });
    };


    public static BiFunction<PgPool, Property, Uni<Boolean>> updateProperty = (client, property) -> {
        return client.preparedQuery("UPDATE Property SET propertyname =$1, location =$2, price =$3 ,description =$4 , numberofrooms =$5 ,s3url =$6 ,ownerid =$7 ,available =$8 WHERE id =$9")
                .execute(Tuple.wrap(new Object[]{property.property_name, property.location, property.price, property.description, property.number_of_rooms, property.s3Url, property.owner_id, property.available, property.id}))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);

    };

    public static BiFunction<PgPool, Long, Uni<Boolean>> deleteProperty = (client, id) -> {
        return client.preparedQuery("DELETE FROM Property WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
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
                row.getLong("timestmp")
        );
    }


}
