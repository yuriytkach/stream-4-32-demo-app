package com.yuriytkach.demo.stream32.app.model;

public record Tx(
  long chainId,
  long nonce,
  String gasPrice,
  String gasLimit,
  String to,
  String value,
  String data
) {}
