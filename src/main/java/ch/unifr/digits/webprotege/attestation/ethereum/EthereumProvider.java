package ch.unifr.digits.webprotege.attestation.ethereum;

import ch.unifr.digits.gwt.web3.js.core.Provider;
import elemental2.promise.Promise;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class EthereumProvider extends Provider {
    @JsProperty
    public String selectedAddress;

    @JsMethod(namespace = JsPackage.GLOBAL)
    public static native Promise<EthereumProvider> detectEthereumProvider();
}
