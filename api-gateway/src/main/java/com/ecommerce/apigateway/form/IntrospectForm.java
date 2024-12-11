package com.ecommerce.apigateway.form;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@Builder
public class IntrospectForm {
    private String token;
    private String client_id;
    private String client_secret;
    public MultiValueMap<String, String> toMultiValueMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("token", this.token);
        map.add("client_id", this.client_id);
        map.add("client_secret", this.client_secret);
        return map;
    }
}
