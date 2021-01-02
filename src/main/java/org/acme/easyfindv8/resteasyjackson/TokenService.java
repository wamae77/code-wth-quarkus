package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logmanager.Logger;
import org.jose4j.jwt.JwtClaims;

import javax.enterprise.context.RequestScoped;
import java.util.Arrays;
import java.util.UUID;

@RequestScoped
public class TokenService {

    public final static Logger LOGGER = Logger.getLogger(TokenService.class.getSimpleName());

    public String generateUserToken(Long id, String email, String username) {
        return generateToken(username, id, email, Roles.USER);
    }

    public String generateServiceToken(String serviceId, Long id, String serviceName) {
        return generateToken(serviceId, id, serviceName, Roles.SERVICE);
    }

    public String generateToken(String subject, Long id, String name, String... roles) {
        try {
            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setIssuer("easyfind@gmail.com"); // change to your company
            jwtClaims.setJwtId(UUID.randomUUID().toString());
            jwtClaims.setSubject(subject);
            jwtClaims.setClaim(Claims.upn.name(), subject);
            jwtClaims.setClaim(Claims.preferred_username.name(), name); //add more
            jwtClaims.setClaim("id", id);
            jwtClaims.setClaim(Claims.groups.name(), Arrays.asList(roles));
            jwtClaims.setAudience("using-jwt");
            jwtClaims.setExpirationTimeMinutesInTheFuture(60); // TODO specify how long do you need
            String token = TokenUtils.generateTokenString(jwtClaims);
            LOGGER.info("TOKEN generated: " + token);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}