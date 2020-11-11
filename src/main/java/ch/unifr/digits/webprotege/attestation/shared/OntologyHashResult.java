package ch.unifr.digits.webprotege.attestation.shared;

import edu.stanford.bmir.protege.web.shared.dispatch.Result;

import java.util.List;

public class OntologyHashResult implements Result {
    private int hash;
    private List<Integer> classHashes;

    public OntologyHashResult() {}

    public OntologyHashResult(int hash, List<Integer> classHashes) {
        this.hash = hash;
        this.classHashes = classHashes;
    }

    public int getHash() {
        return hash;
    }

    public List<Integer> getClassHashes() {
        return classHashes;
    }
}
