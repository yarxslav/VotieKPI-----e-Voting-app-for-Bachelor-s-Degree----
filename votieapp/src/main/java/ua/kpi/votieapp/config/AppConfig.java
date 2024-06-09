package ua.kpi.votieapp.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppConfig {

    @Value("${fs.images.path}")
    private String imagesPath;
}
