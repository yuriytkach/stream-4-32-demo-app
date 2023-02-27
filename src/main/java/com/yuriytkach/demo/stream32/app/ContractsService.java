package com.yuriytkach.demo.stream32.app;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.exceptions.TxHashMismatchException;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;
import org.web3j.utils.TxHashVerifier;

import com.yuriytkach.blockchain.contracts.Fundraiser;
import com.yuriytkach.demo.stream32.app.model.ContractState;
import com.yuriytkach.demo.stream32.app.model.ContributeBody;
import com.yuriytkach.demo.stream32.app.model.Fund;
import com.yuriytkach.demo.stream32.app.model.Tx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractsService {

  private final Web3j web3j;
  private final ContractGasProvider contractGasProvider;

  public Fundraiser deployContract() throws Exception {
    final Credentials credentials = Credentials.create("896575db3a0d03b2fd5672dcefa38fe44d82b367522910e721e5917ea0125610");

    log.info("Deploying contract by: {}", credentials.getAddress());

    return Fundraiser.deploy(
      web3j,
      credentials,
      contractGasProvider,
      BigInteger.ONE,
      BigInteger.valueOf(1000),
      BigInteger.valueOf(10),
      "0x5658807ab60fc3c40d8f8bcfcb068cc6e6303db7",
      credentials.getAddress()
    ).send();
  }

  public Fundraiser deployContract(final Fund fund) throws Exception {
    final Credentials credentials = Credentials.create("896575db3a0d03b2fd5672dcefa38fe44d82b367522910e721e5917ea0125610");

    log.info("Deploying contract by: {}", credentials.getAddress());

    return Fundraiser.deploy(
      web3j,
      credentials,
      contractGasProvider,
      BigInteger.valueOf(fund.goal()),
      BigInteger.valueOf(fund.minimumContribution()),
      BigInteger.valueOf(fund.delayBlocks()),
      fund.recipientAddress(),
      credentials.getAddress()
    ).send();
  }

  public Tx contribute(final String contractAddress, final ContributeBody body) throws IOException {
    final EthGetTransactionCount ethGetTransactionCount = this.web3j
      .ethGetTransactionCount(body.address(), DefaultBlockParameterName.PENDING).send();

    final BigInteger nonce = ethGetTransactionCount.getTransactionCount();
    log.info("Nonce for address {}: {}", body.address(), ethGetTransactionCount.getTransactionCount());

    final Function function = new Function("contribute", List.of(), List.of());
    final String encodedFunc = FunctionEncoder.encode(function);

    final BigInteger chainId = web3j.ethChainId().send().getChainId();
    log.info("Chain id: {}", chainId);

    final var tx = new Tx(
      chainId.longValue(),
      nonce.longValue(),
      Numeric.toHexStringWithPrefix(contractGasProvider.getGasPrice("")),
      Numeric.toHexStringWithPrefix(contractGasProvider.getGasLimit("")),
      contractAddress,
      Numeric.toHexStringWithPrefix(BigInteger.valueOf(body.weiAmount())),
      encodedFunc
    );

    log.info("Created TX: {}", tx);

    return tx;
  }

  public String submitSignedTx(final String signedMessage) throws IOException {
    log.info("Sending tx....");
    final EthSendTransaction ethSendTransaction = this.web3j.ethSendRawTransaction(signedMessage).send();

    log.info("TX status: {}, hasError: {}", ethSendTransaction.getResult(), ethSendTransaction.hasError());

    if (ethSendTransaction != null && !ethSendTransaction.hasError()) {
      String txHashLocal = Hash.sha3(signedMessage);
      String txHashRemote = ethSendTransaction.getTransactionHash();
      if (! new TxHashVerifier().verify(txHashLocal, txHashRemote)) {
        throw new TxHashMismatchException(txHashLocal, txHashRemote);
      }
    }

    log.info("Submitted TX hash: {}", ethSendTransaction.getTransactionHash());

    return ethSendTransaction.getTransactionHash();
  }

  public ContractState contractState(final String contractAddress) throws Exception {
    final Credentials credentials = Credentials.create("896575db3a0d03b2fd5672dcefa38fe44d82b367522910e721e5917ea0125610");

    final Fundraiser fundraiser = Fundraiser.load(
      contractAddress,
      web3j,
      credentials,
      contractGasProvider
    );

    final BigInteger balance = fundraiser.getBalance().send();
    final BigInteger state = fundraiser.state().send();

    return new ContractState(balance.longValue(), state.longValue());
  }
}
