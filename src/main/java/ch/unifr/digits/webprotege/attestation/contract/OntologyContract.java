package ch.unifr.digits.webprotege.attestation.contract;

import ch.unifr.digits.gwt.web3.js.Web3;
import ch.unifr.digits.gwt.web3.js.core.TransactionReceipt;
import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class OntologyContract {

    public OntologyContract(Web3 web3, Object jsonInterface, String address) {}

    @JsMethod
    public native Promise<VerifyContractReturn> verify(String from, String ontologyIri, String versionIri, String hash);

    @JsMethod
    public native Promise<TransactionReceipt> attest(String from, String ontologyIri, String versionIri, String name, String hash);

}
