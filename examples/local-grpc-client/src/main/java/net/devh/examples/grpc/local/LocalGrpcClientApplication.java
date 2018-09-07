package net.devh.examples.grpc.local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LocalGrpcClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalGrpcClientApplication.class, args);
    }
}
