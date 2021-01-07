const Web3 = require('web3');
const fs = require('fs');
require('dotenv').config({ path: `.env` });

const buildPath = `${__dirname}/../build/`;
const contracts = ['OntologyAttestation', 'ChangeTracking'];

function load(contractName) {
    const abi = fs.readFileSync(`${buildPath}${contractName}.abi`, 'UTF-8');
    const bin = fs.readFileSync(`${buildPath}${contractName}.bin`, 'UTF-8');
    return { abi: JSON.parse(abi), bin: bin };
}

async function deploy(name, abi, bin) {
    var contract = new web3.eth.Contract(abi);
    return new Promise((resolve, reject) => {
        contract.deploy({ data: bin }).send({
            from: account.address,
            gas: '4700000'
        }).then(newContractInstance => {
            console.log(`Contract ${name} deployed. address: ${newContractInstance.options.address}`);
            resolve(name);
        }).catch(e => {
            console.error(e);
            reject(e);
        });
    });
}

const provider = `${process.env.PROVIDER_HOST}:${process.env.PROVIDER_PORT}`;
console.log(`Using web3 provider: ${provider}`);

const web3 = new Web3(provider);
const account = web3.eth.accounts.privateKeyToAccount(process.env.SERVER_SECRET);
console.log(`Using web3 account: ${account.address}`);

(async function(){
    for (const c of contracts) {
        console.log(`Deploying ${c} ...`);
        const compileOut = load(c);
        await deploy(c, compileOut.abi, compileOut.bin);
    }
})()