package com.parking.identity_service.validator;

public class ValidEmail {

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+-]{5,}@[a-zA-Z0-9.-]{3,}\\.[a-zA-Z]{2,}$");
    }

}
