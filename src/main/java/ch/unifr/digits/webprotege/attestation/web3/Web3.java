package ch.unifr.digits.webprotege.attestation.web3;

import ch.unifr.digits.gwt.web3.js.eth.Eth;
import ch.unifr.digits.webprotege.attestation.web3.core.Provider;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Web3 {

    @JsProperty
    public Eth eth;
    @JsProperty
    public Provider currentProvider;

    public Web3() {}
    public Web3(String url) {}
    public Web3(Provider provider) {}

    @JsMethod
    public native boolean setProvider(String url);
}
