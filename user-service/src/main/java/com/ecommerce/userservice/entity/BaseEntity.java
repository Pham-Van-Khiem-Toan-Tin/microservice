package com.ecommerce.userservice.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
public class BaseEntity {
    @Field("created")
    Date createdAt;

}
