package ers.roadmap;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RoadMapBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoadMapBackendApplication.class, args);

        System.out.println("Hello World!");
    }

}
