package ch.unifr.digits.webprotege.attestation.web3.core;


import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class CustomChainParams {
    @JsProperty
    public String name;
    @JsProperty
    public int networkId;
    @JsProperty
    public int chainId;
}
