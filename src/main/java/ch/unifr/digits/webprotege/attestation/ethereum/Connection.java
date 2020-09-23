package ch.unifr.digits.webprotege.attestation.ethereum;

import ch.unifr.digits.gwt.web3.js.Web3;

public class Connection {
    private EthereumProvider provider;
    private Web3 web3;

    public Connection(EthereumProvider provider, Web3 web3) {
        this.provider = provider;
        this.web3 = web3;
    }

    public EthereumProvider getProvider() {
        return provider;
    }

    public Web3 getWeb3() {
        return web3;
    }
}
