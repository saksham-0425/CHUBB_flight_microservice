package com.booking.bookingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.booking.bookingservice.config.TestMailConfig;

@SpringBootTest
@ContextConfiguration(classes = {TestMailConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.import-check.enabled=false",
    "eureka.client.enabled=false"
})
class BookingserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
