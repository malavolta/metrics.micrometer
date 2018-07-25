package metris.micrometer.metris.example;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@RestController
@RequestMapping(value = "/Person")
class Person{

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	@Autowired
	private MeterRegistry registry;


	@GetMapping("/{id}")
	@Timed(value = "person.add", histogram = true, percentiles = { 0.95, 0.99 }, extraTags = { "version", "v1" })
	public ResponseEntity<?> add() throws InterruptedException{
		ResponseEntity response =new ResponseEntity<>("Ger Person", HttpStatus.OK);
		return response;
	}


	@GetMapping("/save/{id}")
	public ResponseEntity<?> save() throws InterruptedException{
		Timer timer = Timer.builder("my.timer.2")
				.publishPercentiles(0.5, 0.95) // median and 95th percentile
				.publishPercentileHistogram()
				.sla(Duration.ofMillis(100),Duration.ofMillis(500),Duration.ofMillis(1000),Duration.ofMillis(2000),Duration.ofMillis(3000))
				.minimumExpectedValue(Duration.ofMillis(1))
				.maximumExpectedValue(Duration.ofSeconds(10))
		.register(registry);

		long startTime = System.currentTimeMillis();
		int seconds2Sleep = SECURE_RANDOM.nextInt(3000);

		TimeUnit.MILLISECONDS.sleep(seconds2Sleep);

		ResponseEntity response =new ResponseEntity<>("Save Person", HttpStatus.OK);
		Long time1 = (System.currentTimeMillis() - startTime);

		System.out.println("Time: " + time1.intValue());
		registry.timer("my.timer.2").record(time1,TimeUnit.MILLISECONDS);

		return response;
	}
}
