package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class ApplicationUser {
    public Long id;
    public String fname;
    public String lname;
    public String email;
    public String phonenumber;
    public String password;
    public Boolean verified;
    public String user_role;
    public Long timestamp;

    public ApplicationUser() {
        //default constructor
    }

    public ApplicationUser(Long id, String fname, String lname, String email, String phonenumber, String password, Boolean verified, String user_role, Long timestamp) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.phonenumber = phonenumber;
        this.password = password;
        this.verified = verified;
        this.user_role = user_role;
        this.timestamp = timestamp;
    }

    public static Function<String, Boolean> EmailIsValid = email -> {
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        if (email != null) email = email.toLowerCase();
        return pattern.matcher(email).matches();
    };

    public static BiFunction<PgPool, String, Uni<Boolean>> UserExistsByEmail = (client, email) -> {
        return client.preparedQuery("SELECT * FROM users WHERE email = $1").execute(Tuple.of(email))
                .onItem().transform(SqlResult::rowCount)
                .onItem().transform(integer -> integer != 0);
    };


    public Function<PgPool, Uni<Long>> RegisterUser = client -> {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        LocalDateTime localDateTime = LocalDateTime.now();
        return client.preparedQuery("INSERT INTO users (fname, lname, email, phonenumber, password, userrole, timestmp) VALUES ($1, $2, $3, $4, $5, $6, $7)  RETURNING (id)")
                .execute(Tuple.wrap(new Object[]{fname, lname, email, phonenumber, hashedPassword, user_role, localDateTime}))
                .onItem().transform(rows -> rows.iterator().next().getLong("id"));
    };

    public static ApplicationUser loginFunction(PgPool client, String email, String password) {
        return client.preparedQuery("SELECT * FROM users WHERE email = $1").execute(Tuple.of(email))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(rowRowIterator -> rowRowIterator.hasNext() ? from(rowRowIterator.next()) : null)
                .onItem().transform(applicationUser -> applicationUser)
                .stage(applicationUserUni -> {
                    try {
                        ApplicationUser user = applicationUserUni.convert().toCompletableFuture().get();
                        if (user == null)
                            throw new WebApplicationException(Response.status(404).entity(" User does not exist").build());
                        if (!BCrypt.checkpw(password, user.password))
                            throw new WebApplicationException(Response.status(404).entity("invalid email or password").build());
                        return user;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    throw new WebApplicationException(Response.status(404).entity(" User does not exist").build());
                });
    }

    public static BiFunction<PgPool, Long, Uni<ApplicationUser>> getUserById = (client, id) -> {
        return client.preparedQuery("SELECT * FROM users WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(rowRowIterator -> rowRowIterator.hasNext() ? from(rowRowIterator.next()) : null)
                .onItem().transform(applicationUser -> {
                    if (applicationUser == null)
                        throw new WebApplicationException(Response.status(404).entity("invalid id").build());
                    return applicationUser;
                });
    };

    public static Function<PgPool, Multi<ApplicationUser>> findAllUsers = client -> {
        return client.query("SELECT * FROM users").execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().items(() -> StreamSupport.stream(rows.spliterator(), false)))
                .onItem().transform(ApplicationUser::from)
                .onFailure().invoke(Throwable::printStackTrace);
    };

    public static BiFunction<PgPool, ApplicationUser, Uni<Boolean>> updateUserDetails = (client, applicationUser) -> {
        return client.preparedQuery("UPDATE users SET fname =$1, lname =$2, email=$3 ,phonenumber=$4, password=$5,verified=$6 WHERE id =$7")
                .execute(Tuple.wrap(new Object[]{applicationUser.fname,
                        applicationUser.lname,
                        applicationUser.email,
                        applicationUser.phonenumber,
                        applicationUser.password,
                        applicationUser.verified,
                        applicationUser.id})).onItem().transform(rows -> rows.rowCount() == 1);
    };

    public static BiFunction<PgPool, Long, Uni<Boolean>> deleteUser = (client, id) -> {
        return client.preparedQuery("DELETE FROM users WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    };


    public static ApplicationUser from(Row row) {
        return new ApplicationUser(row.getLong("id"),
                row.getString("fname"),
                row.getString("lname"),
                row.getString("email"),
                row.getString("phonenumber"),
                row.getString("password"),
                row.getBoolean("verified"),
                row.getString("userrole"),
                row.getLong("timestmp"));
    }
}
