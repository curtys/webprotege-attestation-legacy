## Requirements
* solc 7.1.0
* web3j-cli
* node and npm
* ganache-cli
* docker-compose

## Installation
The `scripts` folder contains utilities for compiling and deploying the contracts.
Compile solidity contracts and generate interfaces and wrappers:
```
sh ./compile-contracts.sh
```
Start a Ganache test blockchain
```
docker-compose up -d
```

Deploy contracts:
```
npm install
npm run deploy
```

## Running the tests
The tests require the ontologies to be placed in `src/test/resources/ontologies`. 
The ontologies need to be downloaded manually. Download links can be found in the file `test-ontologies.txt`.
Configuration of the test blockchain provider are located in `src/test/resources/configuration`. The tests use the JUnit 
framework and can thus be run manually by an appropriate runner, e.g., through Intellij.
