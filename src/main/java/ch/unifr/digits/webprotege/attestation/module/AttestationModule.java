package ch.unifr.digits.webprotege.attestation.module;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class AttestationModule implements EntryPoint {

    /**
     * The entry point method, called automatically by loading a module that
     * declares an implementing class as an entry point.
     */
    @Override
    public void onModuleLoad() {
        ScriptLoader loader = GWT.create(ScriptLoader.class);
        loader.load("jszip.min.js");
        loader.load("ontology-contract.js");
    }
}
