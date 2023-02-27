package com.yuriytkach.demo.stream32.app.web;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.yuriytkach.demo.stream32.app.ContractsService;
import com.yuriytkach.demo.stream32.app.model.ContractState;
import com.yuriytkach.demo.stream32.app.model.ContributeBody;
import com.yuriytkach.demo.stream32.app.model.Fund;
import com.yuriytkach.demo.stream32.app.model.SignedTx;
import com.yuriytkach.demo.stream32.app.model.Tx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FundraiserController {

  private final ContractsService contractService;

  @PostMapping("/funds")
  public ResponseEntity<String> createFund(
    @RequestBody final Fund fund
  ) {
    try {
      final var fundraiser = contractService.deployContract(fund);
      return ResponseEntity.ok(fundraiser.getContractAddress());
    } catch (final Exception ex) {
      log.error("Failed to create fund: {}", ex.getMessage(), ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/funds/{contractAddress}/contributions")
  public ResponseEntity<Tx> contribute(
    @PathVariable final String contractAddress,
    @RequestBody final ContributeBody body
  ) {
    try {
      final var tx = contractService.contribute(contractAddress, body);
      return ResponseEntity.ok(tx);
    } catch (final Exception ex) {
      log.error("Failed to create TX: {}", ex.getMessage(), ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/tx")
  public ResponseEntity<String> submitTransaction(
    @RequestBody final SignedTx body
  ) {
    try {
      final var hash = contractService.submitSignedTx(body.signedMessage());
      return ResponseEntity.ok(hash);
    } catch (final IOException ex) {
      log.error("Failed to submit signed TX: {}", ex.getMessage(), ex);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/funds/{contractAddress}")
  public ResponseEntity<ContractState> contractState(
    @PathVariable final String contractAddress
  ) {
    try {
      final ContractState state = contractService.contractState(contractAddress);
      return ResponseEntity.ok(state);
    } catch (final Exception ex) {
      log.error("Failed to get state: {}", ex.getMessage(), ex);
      return ResponseEntity.notFound().build();
    }
  }

}
