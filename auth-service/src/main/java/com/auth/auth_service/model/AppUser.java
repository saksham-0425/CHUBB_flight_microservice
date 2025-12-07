package com.auth.auth_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "users")
public class AppUser {

    @Id
    private String id;

    private String name;
    private String email;
    private String password;

    private Set<String> roles = new HashSet<>();
}
