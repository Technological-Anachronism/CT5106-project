package bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Run this to start the whole portal. It finds the controller and the in-memory store on its own.
// If you add your own package, keep it under bms or Spring won't see it.
@SpringBootApplication
public class BmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmsApplication.class, args);
    }
}
