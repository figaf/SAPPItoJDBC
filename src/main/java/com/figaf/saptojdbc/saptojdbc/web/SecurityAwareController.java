package com.figaf.saptojdbc.saptojdbc.web;

import com.figaf.saptojdbc.saptojdbc.dto.Credentials;
import com.figaf.saptojdbc.saptojdbc.errors.InvalidFormatForUsernamePasswordToken;
import com.figaf.saptojdbc.saptojdbc.errors.MissingAuthInfoException;

import java.util.Base64;
import java.util.List;

public interface SecurityAwareController {

    default Credentials processAuthHeader(List<String> allAuthHeaders) {
        if (null == allAuthHeaders ||   allAuthHeaders.size() != 1) {
            throw new MissingAuthInfoException();
        } else {
            String trimmedToken = trimAuthScheme(allAuthHeaders.get(0));
            String usernamePasswordToken = new String(Base64.getDecoder().decode(trimmedToken));
            return parseUsernamePasswordToken(usernamePasswordToken);
        }
    }

    default Credentials parseUsernamePasswordToken(String usernamePasswordToken) {

        int delimiter = usernamePasswordToken.indexOf(":");
        if (-1 == delimiter) {
            throw new InvalidFormatForUsernamePasswordToken();
        } else {
            String username = usernamePasswordToken.substring(0, delimiter);
            String password = usernamePasswordToken.substring(delimiter + 1);
            return new Credentials(username, password);
        }
    }

    default String trimAuthScheme(String usernamePasswordToken) {
        if(!usernamePasswordToken.startsWith("Basic ")) {
            throw new InvalidFormatForUsernamePasswordToken();
        } else {
            return usernamePasswordToken.substring("Basic ".length());
        }
    }
}
