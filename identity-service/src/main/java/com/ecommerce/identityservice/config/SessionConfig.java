package com.ecommerce.identityservice.config;


import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(redisNamespace = "spring:session:idp")
@Configuration
public class SessionConfig implements BeanClassLoaderAware {

    private ClassLoader loader;

    /**
     * Note that the bean name for this bean is intentionally
     * {@code springSessionDefaultRedisSerializer}. It must be named this way to override
     * the default {@link RedisSerializer} used by Spring Session.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new Jackson2JsonRedisSerializer<>(objectMapper(), Object.class);
    }

    /**
     * Customized {@link JsonMapper} to add mix-in for class that doesn't have default
     * constructors
     * @return the {@link JsonMapper} to use
     */
    private JsonMapper objectMapper() {
        return JsonMapper.builder().addModules(SecurityJackson2Modules.getModules(this.loader)).build();
    }

    /*
     * @see
     * org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang
     * .ClassLoader)
     */
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.loader = classLoader;
    }
}
