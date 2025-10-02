package ers.roadmap;

import ers.roadmap.service.RoadmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoadMapBackendApplication {

    @Autowired
    private static RoadmapService roadmapService;

    public static void main(String[] args) {
        SpringApplication.run(RoadMapBackendApplication.class, args);
        System.out.println("Hello world!");


    }

}
