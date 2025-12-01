package com.config.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.server.bootstrap=false",
        "spring.cloud.config.server.git.uri=file:./dummy",
        "spring.cloud.config.server.native.search-locations=classpath:/dummy",
        "eureka.client.enabled=false"
})
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
