// SPDX-License-Identifier: GPL-3.0
pragma solidity >= 0.7.1;

contract OntologyAttestation {

    struct Attestation {
        address signer;
        string name;
        uint timestamp;
        string hash;
    }

    mapping (string => Attestation) internal store;

    function verify(string calldata ontologyIri, string calldata versionIri, string calldata hash) public view 
    returns (bool valid, address signer, string memory signerName, uint timestamp) {
        string memory id = getId(ontologyIri, versionIri);
        Attestation memory attestation = store[id];
        if (attestation.signer != address(0x0)) {
            bool signed = keccak256(bytes(attestation.hash)) == keccak256(bytes(hash));
            if (signed) {
                return (true, attestation.signer, attestation.name, attestation.timestamp);
            }
        }
        return (false, address(0x0), "", 0);
    }

    function attest(string calldata ontologyIri, string calldata versionIri, string calldata name, string calldata hash) public {
        require(bytes(ontologyIri).length > 0, "IRI required");
        require(bytes(name).length > 0, "Name required");
        require(bytes(hash).length > 0, "Ontology hash required");

        string memory id = getId(ontologyIri, versionIri);
        Attestation memory newAttestation = Attestation(msg.sender, name, block.timestamp, hash);
        store[id] = newAttestation;
    }

    function getId(string calldata ontologyIri, string calldata versionIri) internal pure returns(string memory) {
        return string(abi.encodePacked(ontologyIri, versionIri));
    }


}