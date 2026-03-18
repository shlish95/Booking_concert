package kr.hhplus.be.server.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingConcertOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Booking Concert Mock API")
                        .description("2단계 학습용 Mock API 문서. 실제 비즈니스 로직 없이 API 계약과 응답 형태를 제공한다.")
                        .version("v0.2")
                        .contact(new Contact().name("Booking Concert")))
                .servers(List.of(new Server().url("/").description("Current server")));
    }
}
