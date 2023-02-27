package com.yuriytkach.demo.stream32.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({ BlockchainProperties.class })
public class Web3jConfiguration {

  private final BlockchainProperties blockchainProperties;

  @Bean
  public ContractGasProvider gasProvider() {
    return new DefaultGasProvider();
  }

  @Bean
  public Web3j web3j(final Web3jService web3jService) {
    return Web3j.build(web3jService);
  }

  @Bean
  public Web3jService web3jService() {
    final String clientAddress = blockchainProperties.getHost();

    log.info("Creating Web3j Service for endpoint: " + clientAddress);

    return new HttpService(clientAddress, createOkHttpClient(), false);
  }

  private OkHttpClient createOkHttpClient() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();

    final Logger okHttpLogger = LoggerFactory.getLogger(OkHttpClient.class);
    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(okHttpLogger::debug);
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    builder.addInterceptor(logging);

    return builder.build();
  }

}
