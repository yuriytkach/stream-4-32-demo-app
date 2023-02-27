package com.yuriytkach.demo.stream32.app;

import static org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.utils.Numeric;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MnemonicCredentialsService {

  private final BlockchainProperties blockchainProperties;

  private List<Credentials> credentials;

  public List<Credentials> getCredentials() {
    return List.copyOf(credentials);
  }

  @PostConstruct
  public void loadCredentials() {
    log.info("Mnemonic: {}", blockchainProperties.getMnemonic());

    final byte[] seed = MnemonicUtils.generateSeed(blockchainProperties.getMnemonic(), null);
    final Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);

    // m/44'/60'/0'/0
    final int[] basePath = { 44 | HARDENED_BIT, 60 | HARDENED_BIT, HARDENED_BIT, 0 };
    final Bip32ECKeyPair baseKey = Bip32ECKeyPair.deriveKeyPair(master, basePath);

    final List<Credentials> credentials = IntStream.range(0, blockchainProperties.getCredentialsLength())
      .mapToObj(i -> Credentials.create(Bip32ECKeyPair.deriveKeyPair(baseKey, new int[] { i })))
      .toList();

    printLoadedCredentials(credentials);

    this.credentials = credentials;
  }

  private void printLoadedCredentials(final List<Credentials> credentials) {
    StringBuilder sb = new StringBuilder("\n=====================================================================");
    sb.append("\nAvailable Accounts");
    sb.append("\n==================");
    IntStream.range(0, blockchainProperties.getCredentialsLength())
      .mapToObj(i -> String.format("\n(%d) %s", i, credentials.get(i).getAddress()))
      .forEach(sb::append);

    sb.append("\n\nPrivate Keys");
    sb.append("\n==================");
    IntStream.range(0, blockchainProperties.getCredentialsLength())
      .mapToObj(i -> String.format("\n(%d) %s", i,
        Numeric.toHexStringWithPrefixZeroPadded(credentials.get(i).getEcKeyPair().getPrivateKey(), 64)
      ))
      .forEach(sb::append);
    sb.append("\n=====================================================================");

    sb.append("\n\nPublic Keys");
    sb.append("\n==================");
    IntStream.range(0, blockchainProperties.getCredentialsLength())
      .mapToObj(i -> String.format("\n(%d) %s", i,
        Numeric.toHexStringWithPrefixZeroPadded(credentials.get(i).getEcKeyPair().getPublicKey(), 128)
      ))
      .forEach(sb::append);
    sb.append("\n=====================================================================");

    log.info(sb.toString());
  }

}
