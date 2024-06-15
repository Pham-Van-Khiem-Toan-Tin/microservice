package com.ecommerce.authservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "user")
@Getter
@Setter
public class UserEntity {
    @Field("id")
    private String id;
    @Field("name")
    private String name;
    @Field("password")
    private String password;
}
