package com.yuriytkach.demo.stream32.app;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import com.yuriytkach.demo.stream32.app.model.ContractState;
import com.yuriytkach.demo.stream32.app.model.ContributeBody;
import com.yuriytkach.demo.stream32.app.model.Fund;
import com.yuriytkach.demo.stream32.app.model.SignedTx;
import com.yuriytkach.demo.stream32.app.model.Tx;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FundraiserFullTest {

  @LocalServerPort
  private int port;

  @BeforeEach
  public void initRestAssured() {
    RestAssured.port = port;
    RestAssured.basePath = "/";
  }

  @Test
  void fullIntegrationTest() {
    final String recipientAddress = "0x5658807ab60fc3c40d8f8bcfcb068cc6e6303db7";

    final var fund = new Fund(1, 1000, 10, recipientAddress);

    final String contractAddress = RestAssured.given()
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .body(fund)
      .when()
      .post("/funds")
      .then()
      .statusCode(HttpStatus.OK.value())
      .extract().body().asString();

    System.out.println("Contract address: " + contractAddress);

    Credentials contributor = Credentials.create("d13185af967be6ba8c8e020db074d96bfb69e3aa11b5f172e78e22b6a5770016");

    final BigDecimal twoEthInwei = Convert.toWei(BigDecimal.valueOf(2), Convert.Unit.ETHER);

    final var contributeBody = new ContributeBody(twoEthInwei.longValue(), contributor.getAddress());

    final Tx tx = RestAssured.given()
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .body(contributeBody)
      .when()
      .post("/funds/{contractAddress}/contributions", contractAddress)
      .then()
      .statusCode(HttpStatus.OK.value())
      .extract().body().as(Tx.class);

    System.out.println("Received TX: " + tx);

    final String signedMessage = signTx(tx, contributor);
    System.out.println("Signed message: " + signedMessage);

    final String hash = RestAssured.given()
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .body(new SignedTx(signedMessage))
      .when()
      .post("/tx")
      .then()
      .statusCode(HttpStatus.OK.value())
      .extract().body().asString();

    System.out.println("TX hash: " + hash);

    final ContractState state = RestAssured.given()
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .when()
      .get("/funds/{contractAddress}", contractAddress)
      .then()
      .statusCode(HttpStatus.OK.value())
      .extract().body().as(ContractState.class);

    System.out.println("Contract state: " + state);
  }

  private String signTx(final Tx tx, final Credentials contributor) {
    final RawTransaction transaction = RawTransaction.createTransaction(
      BigInteger.valueOf(tx.nonce()),
      Numeric.toBigInt(tx.gasPrice()),
      Numeric.toBigInt(tx.gasLimit()),
      tx.to(),
      Numeric.toBigInt(tx.value()),
      tx.data()
    );

    final byte[] signedBytes = TransactionEncoder.signMessage(transaction, tx.chainId(), contributor);
    return Numeric.toHexString(signedBytes);
  }

}
