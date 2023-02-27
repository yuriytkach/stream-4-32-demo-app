package com.yuriytkach.demo.stream32.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app.blockchain")
public class BlockchainProperties {
  private final String host;
  private final String mnemonic;
  private final int credentialsLength;
}
