package ch.unifr.digits.webprotege.attestation.server;

import ch.unifr.digits.webprotege.attestation.server.contracts.FileAttestation;
import ch.unifr.digits.webprotege.attestation.server.contracts.OntologyAttestation;
import ch.unifr.digits.webprotege.attestation.shared.VerifyResult;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.HashCode;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static ch.unifr.digits.webprotege.attestation.server.SettingsManager.*;

public class AttestationService {

    private static final String PRIVATE_KEY = "0x2fe929a15797d2170c063b758dee33b55ad7c13313c110b675d3a9709fe83797";

    private static final Credentials CREDENTIALS = Credentials.create(PRIVATE_KEY);
    private static final AtomicReference<Web3j> WEB3_REF = new AtomicReference<>();

    public static VerifyResult verifyFile(String iri, String versionIri, String hash) throws Exception {
        init();
        String id = iri+versionIri;
        FileAttestation contract = FileAttestation.load(ADDRESS_FILE, WEB3_REF.get(), CREDENTIALS,
                DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT);
        RemoteFunctionCall<Tuple4<Boolean, String, String, BigInteger>> remoteFunctionCall =
                contract.verify(id, hash);
        Tuple4<Boolean, String, String, BigInteger> result = remoteFunctionCall.send();
        BigInteger bigInteger = result.component4();
        int time = (bigInteger == null) ? -1 : bigInteger.intValue();
        return new VerifyResult(result.component1(), result.component2(), result.component3(), time);
    }

    public static VerifyResult verifyOntology(String iri, String versionIri, int hash) throws Exception {
        init();
        OntologyAttestation contract = OntologyAttestation.load(ADDRESS_ONTOLOGY, WEB3_REF.get(), CREDENTIALS,
                DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT);
        RemoteFunctionCall<Tuple4<Boolean, String, String, BigInteger>> remoteFunctionCall =
                contract.verify(iri, versionIri, new BigInteger(String.valueOf(hash)));
        Tuple4<Boolean, String, String, BigInteger> result = remoteFunctionCall.send();
        BigInteger bigInteger = result.component4();
        int time = (bigInteger == null) ? -1 : bigInteger.intValue();
        return new VerifyResult(result.component1(), result.component2(), result.component3(), time);
    }

    public static VerifyResult verifyEntity(String iri, String versionIri, int entityHash) throws Exception {
        init();
        OntologyAttestation contract = OntologyAttestation.load(ADDRESS_ONTOLOGY, WEB3_REF.get(), CREDENTIALS,
                DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT);
        RemoteFunctionCall<Tuple4<Boolean, String, String, BigInteger>> remoteFunctionCall =
                contract.verifyEntity(iri, versionIri, new BigInteger(String.valueOf(entityHash)));
        Tuple4<Boolean, String, String, BigInteger> result = remoteFunctionCall.send();
        BigInteger bigInteger = result.component4();
        int time = (bigInteger == null) ? -1 : bigInteger.intValue();
        return new VerifyResult(result.component1(), result.component2(), result.component3(), time);
    }

    public static int ontologyHash(OWLOntology ontology) {
        Set<OWLEntity> signature = ontology.getSignature();
        Set<OWLAxiom> axioms = ontology.getAxioms();
        Set<OWLObject> all = new HashSet<>();
        all.addAll(signature);
        all.addAll(axioms);
        return all.hashCode();
    }

    public static int entityHash(OWLEntity entity) {
        return entity.hashCode();
    }

    private static void init() {
        if (WEB3_REF.compareAndSet(null, null)) {
            Web3j web3 = Web3j.build(new HttpService(PROVIDER_URL));
            boolean result = WEB3_REF.compareAndSet(null, web3);
            if (!result) web3.shutdown();
        }
    }
}
