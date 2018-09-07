package net.devh.examples.grpc.local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LocalGrpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalGrpcServerApplication.class, args);
    }
}
