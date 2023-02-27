# Demo Project for Online Stream #4 (#32) - Java microservice that works with smart contract
Demo project for online stream #4 of blockchain course (#32 of total streams) about using web3j library
in Java microservice to work with smart contract.

## Access to Online Stream on YouTube

To get a link to online stream on YouTube please do the following:

- :moneybag: Make any donation to support my volunteering initiative to help Ukrainian Armed Forces by means described on [my website](https://www.yuriytkach.com/volunteer)
- :email: Write me an [email](mailto:me@yuriytkach.com) indicating donation amount and time
- :tv: I will reply with the link to the stream on YouTube.

Thank you in advance for your support! Слава Україні! :ukraine:

## Running the Demo
First, compile and publish to local maven repository smart contract wrappers from project [stream-3-31-demo-app](https://github.com/yuriytkach/stream-3-31-demo-app)

Second, start development blockchain with `truffle develop` from that project.

Then you can run test [FundraiserFullTest](https://github.com/yuriytkach/stream-4-32-demo-app/blob/main/src/test/java/com/yuriytkach/demo/stream32/app/FundraiserFullTest.java) 
that will deploy contract from first address and make contribution from
second address (from the test addresses pool created by ganache)

### Reference Documentation
For further reference, please consider the following sections:

* [Web3j Official Documentation](https://docs.web3j.io/4.8.7/)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.0.3/reference/htmlsingle/#web)
