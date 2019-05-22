package com.tommy.lobby.security;

import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tommy.lobby.security.SecurityConstants.SECRET_KEY;

@Component
public class ServiceJWTReader {

    /**
     * This will retrieve the username from the supplied token
     * @param token given from the front end
     * @return username as a String
     */
    public String getUsername(String token){
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * This method will retrieve all the roles the user supplied in the token has
     * Because the token stores the roles as ArrayList<LinkedHashedMap> there should be done some
     * casting to turn this in a List<String>
     * @param token given from the front end
     * @return List<String> with all the roles
     */
    private List<String> getRoles(String token){
        return (List<String>) Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
    }

    /**
     * This will create a UserDetails object from the supplied token
     * @param token given from the front end
     * @return UserDetails object required by Spring Security
     */
    private UserDetails getUserDetails(String token){
        String username = this.getUsername(token);
        List<String> roles = this.getRoles(token);
        return new ServiceUserDetails(username,roles.toArray(new String[0]));
    }

    /**
     * This will create a Authentication object, which spring security uses for Authentication purposes.
     * @param token given from the front end
     * @return Authentication object
     */
    public Authentication getAuthentication(String token){
        UserDetails userDetails = getUserDetails(token);
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }


}
