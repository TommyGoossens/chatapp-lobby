package com.tommy.lobby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LobbyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LobbyApplication.class, args);
    }

}
