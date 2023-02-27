package com.yuriytkach.demo.stream32.app.model;

public record Fund(
  long goal,
  long minimumContribution,
  long delayBlocks,
  String recipientAddress
) {

}
