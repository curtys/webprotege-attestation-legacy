function OntologyContract(web3, jsonInterface, address) {
    this.delegate = new web3.eth.Contract(jsonInterface, address);
    this.verify = function(from, ontologyIri, versionIri, hash) {
        return this.delegate.methods.verify(ontologyIri, versionIri, hash).call({from: from});
    }
    this.attest = function(from, ontologyIri, versionIri, name, hash) {
        return this.delegate.methods.attest(ontologyIri, versionIri, name, hash).send({from: from});
    }
}
