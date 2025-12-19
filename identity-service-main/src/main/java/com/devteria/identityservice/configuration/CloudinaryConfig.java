package com.devteria.identityservice.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dfgwogria",
                "api_key", "715615316369621",
                "api_secret", "XwZD7_v2VXHlBFmwMzWAZMlUHqE",
                "secure", true
        ));
    }
}
