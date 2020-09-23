package ch.unifr.digits.webprotege.attestation.contract;

public class VerifyResult {
    private final boolean valid;
    private final String signer;
    private final String signerName;
    private final int timestamp;

    public VerifyResult(boolean valid, String signer, String signerName, int timestamp) {
        this.valid = valid;
        this.signer = signer;
        this.signerName = signerName;
        this.timestamp = timestamp;
    }

    public boolean isValid() {
        return valid;
    }

    public String getSigner() {
        return signer;
    }

    public String getSignerName() {
        return signerName;
    }

    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "VerifyResult{" +
                "valid=" + valid +
                ", signer='" + signer + '\'' +
                ", signerName='" + signerName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
