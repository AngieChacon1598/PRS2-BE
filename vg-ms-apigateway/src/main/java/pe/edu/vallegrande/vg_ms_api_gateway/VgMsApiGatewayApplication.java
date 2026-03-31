package pe.edu.vallegrande.vg_ms_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;

@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class
})
public class VgMsApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(VgMsApiGatewayApplication.class, args);
    }

}
