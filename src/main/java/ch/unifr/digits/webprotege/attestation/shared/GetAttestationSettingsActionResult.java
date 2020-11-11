package ch.unifr.digits.webprotege.attestation.shared;

import edu.stanford.bmir.protege.web.shared.dispatch.Result;

public class GetAttestationSettingsActionResult implements Result {

    private String addressFileContract;
    private String addressOntologyContract;

    public GetAttestationSettingsActionResult() {}

    public GetAttestationSettingsActionResult(String addressFileContract, String addressOntologyContract) {
        this.addressFileContract = addressFileContract;
        this.addressOntologyContract = addressOntologyContract;
    }

    public String getAddressFileContract() {
        return addressFileContract;
    }

    public String getAddressOntologyContract() {
        return addressOntologyContract;
    }
}
