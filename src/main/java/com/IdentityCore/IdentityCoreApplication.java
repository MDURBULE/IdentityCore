package com.IdentityCore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.IdentityCore.config.Config;
import com.IdentityCore.config.ConfigProperties;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class IdentityCoreApplication {
	private ConfigProperties cp;

	private IdentityCoreApplication(ConfigProperties cp){
		this.cp = cp;
	}

	@PostConstruct
	public void init(){
		Config.initializeConfig(cp);
	}

	public static void main(String[] args) {
		SpringApplication.run(IdentityCoreApplication.class, args);
	}

}
