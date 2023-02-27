package com.yuriytkach.demo.stream32.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import com.yuriytkach.blockchain.contracts.Fundraiser;

@SpringBootTest
class ContractsServiceTest {

  @Autowired
  ContractsService contractService;

  @Autowired
  Web3j web3j;

  @Test
  void shouldDeployContract() throws Exception {
    final Fundraiser fundraiser = contractService.deployContract();

    assertThat(fundraiser).isNotNull();

    System.out.println("Fundraiser address: " + fundraiser.getContractAddress());

    final BigInteger balance = fundraiser.getBalance().send();
    final BigInteger state = fundraiser.state().send();

    System.out.println("Balance: " + balance);
    System.out.println("State: " + state);

    printBalance(fundraiser.getContractAddress());
    final Credentials credentials = Credentials.create("896575db3a0d03b2fd5672dcefa38fe44d82b367522910e721e5917ea0125610");
    printBalance(credentials.getAddress());
    printBalance("0x5658807ab60fc3c40d8f8bcfcb068cc6e6303db7");

    final BigDecimal oneEtherInWei = Convert.toWei(BigDecimal.valueOf(2), Convert.Unit.ETHER);

    System.out.println("Contribute....");
    final TransactionReceipt transactionReceipt = fundraiser.contribute(oneEtherInWei.toBigInteger()).send();
    System.out.println("TX receipt: " + transactionReceipt);

    System.out.println("Balance: " +
      Convert.fromWei(new BigDecimal(fundraiser.getBalance().send()), Convert.Unit.ETHER) + " ETH");
    System.out.println("State: " + fundraiser.state().send());

    System.out.println("Payout....");
    fundraiser.payOut().send();

    printBalance(fundraiser.getContractAddress());
    printBalance(credentials.getAddress());
    printBalance("0x5658807ab60fc3c40d8f8bcfcb068cc6e6303db7");
  }

  private void printBalance(final String address) throws IOException {
    final EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();

    final EthGetBalance balance = web3j.ethGetBalance(
      address,
      new DefaultBlockParameterNumber(ethBlockNumber.getBlockNumber())
    ).send();

    final BigDecimal balanceEth = Convert.fromWei(new BigDecimal(balance.getBalance()), Convert.Unit.ETHER);

    System.out.println("Balance " + address + ": " + balanceEth + " ETH");
  }
}
