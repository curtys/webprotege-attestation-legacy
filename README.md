## Requirements
* solc 7.1.0
* web3j-cli
* node and npm

## Installation
The `scripts` folder contains utilities for compiling and deploying the contracts.
Compile solidity contracts and generate interfaces and wrappers:
```
sh ./compile-contracts.sh
```

Deploy contracts:
```
npm install
npm run deploy
```