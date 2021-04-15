package ch.unifr.digits;

import ch.unifr.digits.webprotege.attestation.server.FileAttestationService;
import ch.unifr.digits.webprotege.attestation.server.OntologyAttestationService;
import ch.unifr.digits.webprotege.attestation.shared.VerifyResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLWriter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.unifr.digits.FileSupport.saveMeasurementsSeries;

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OntologyLoadTest {

    private static final int NUM_RUNS = 20;
    private static final String RESULTS_DIR = "results/";
    private static final Map<String, String> testOntologyFiles = new HashMap<>();
    private static final Map<String, OWLOntology> loadedOntologies = new HashMap<>();
    private static final Measurements measurements = new Measurements();
    private static final List<Runner> serviceRunners = new ArrayList<>();

    static {
//        testOntologyFiles.put("doid", "ontologies/doid.owl");
//        testOntologyFiles.put("enanomapper", "ontologies/enanomapper.owl");
//        testOntologyFiles.put("foodon", "ontologies/foodon.owl");
//        testOntologyFiles.put("hoom_orphanet", "ontologies/hoom_orphanet.owl");
        testOntologyFiles.put("ncit", "ontologies/ncit-thesaurus.owl");
//        testOntologyFiles.put("obi", "ontologies/obi.owl");
//        testOntologyFiles.put("pato", "ontologies/pato.owl");
//        testOntologyFiles.put("ro", "ontologies/ro.owl");
//        testOntologyFiles.put("uberon", "ontologies/uberon.owl");
//        testOntologyFiles.put("vto", "ontologies/vto.owl");
//        testOntologyFiles.put("ordo", "ontologies/ordo_orphanet.owl");

//        serviceRunners.add(new Runner.FileAttest(new FileAttestationService(), measurements, false));
        serviceRunners.add(new Runner.OntologyAttest(new OntologyAttestationService(), measurements, false));
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        for (Map.Entry<String, String> entry : testOntologyFiles.entrySet()) {
            OWLOntology owlOntology = FileSupport.loadOntologyFromResources(entry.getValue());
            loadedOntologies.put(entry.getKey(), owlOntology);
            int entities = owlOntology.getSignature(Imports.INCLUDED).size();
            int classes = owlOntology.getClassesInSignature(Imports.INCLUDED).size();
            System.out.println(entry.getKey() + "[numEntities="+ entities +"][numClasses=" + classes + "]");
        }
    }

    @Test
    public void batch() throws Exception {

        for (Map.Entry<String, OWLOntology> entry : loadedOntologies.entrySet()) {
            for (Runner runner : serviceRunners) {
                for (int i = 0; i < NUM_RUNS; i++) {
                    runner.attest(entry.getKey(), entry.getValue());
//                    runner.verify(entry.getKey(), entry.getValue());
                }
                saveMeasurementsSeries(RESULTS_DIR+runner.name()+"-attest.csv", entry.getKey(),
                        measurements.getSeries(constructSeriesName(entry.getKey(), runner.name(), "attest"))
                                .stream().map(Duration::toNanos).map(String::valueOf), NUM_RUNS);
                saveMeasurementsSeries(RESULTS_DIR+runner.name()+"-hash.csv", entry.getKey(),
                        measurements.getSeries(constructSeriesName(entry.getKey(), runner.name(), "hash"))
                                .stream().map(Duration::toNanos).map(String::valueOf), NUM_RUNS);
                saveMeasurementsSeries(RESULTS_DIR+runner.name()+"-gas.csv", entry.getKey(),
                        measurements.getManualSeries(constructSeriesName(entry.getKey(), runner.name(), "gas"))
                                .stream().map(String::valueOf), NUM_RUNS);
            }
        }

    }

    private static final String constructSeriesName(String id, String serviceName, String tag) {
        return id + "-" + serviceName + "-" + tag;
    }

    private interface Runner {
        void attest(String id, OWLOntology ontology) throws Exception;
        void verify(String id, OWLOntology ontology) throws Exception;
        String name();

        class FileAttest implements Runner {

            private final FileAttestationService service;
            private final Measurements measurements;
            private final boolean skipBlc;

            FileAttest(FileAttestationService service, Measurements measurements, boolean skipBlc) {
                this.service = service;
                this.measurements = measurements;
                this.skipBlc = skipBlc;
            }

            @Override
            public void attest(String id, OWLOntology ontology) throws Exception {
                String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
                String versionIri = ontology.getOntologyID().getVersionIRI().or(IRI.create("")).toString();
                String document = documentString(ontology);
                int ticket = measurements.begin();
                String hash = service.hashFile(document.getBytes());
                measurements.finish(constructSeriesName(id, name(), "hash"), ticket);
                if (skipBlc) return;
                System.out.println("attest " + name() + " " + iri + " " + versionIri + " " + hash);

                ticket = measurements.begin();
                service.attest(iri, versionIri, "John doe", hash, null);
                measurements.finish(constructSeriesName(id, name(), "attest"), ticket);
            }

            @Override
            public void verify(String id, OWLOntology ontology) throws Exception {
                String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
                String versionIri = ontology.getOntologyID().getVersionIRI().or(IRI.create("")).toString();
                String document = documentString(ontology);
                String hash = service.hashFile(document.getBytes());
                System.out.println("verify " + name() + " " + iri + " " + versionIri + " " + hash);

                int ticket = measurements.begin();
                service.verify(iri, versionIri, hash, null);
                measurements.finish(constructSeriesName(id, name(), "verify"), ticket);
            }

            @Override
            public String name() {
                return service.getClass().getSimpleName().toLowerCase();
            }

            private String documentString(OWLOntology ontology) {
                StringWriter base = new StringWriter();
                OWLXMLWriter writer = new OWLXMLWriter(base, ontology);
                OWLXMLObjectRenderer renderer = new OWLXMLObjectRenderer(writer);
                renderer.visit(ontology);
                return base.toString();
            }
        }

        class OntologyAttest implements Runner {

            private final OntologyAttestationService service;
            private final Measurements measurements;
            private final boolean skipBlc;

            OntologyAttest(OntologyAttestationService service, Measurements measurements, boolean skipBlc) {
                this.service = service;
                this.measurements = measurements;
                this.skipBlc = skipBlc;
            }

            @Override
            public void attest(String id, OWLOntology ontology) throws Exception {
                String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
                String versionIri = ontology.getOntologyID().getVersionIRI().or(IRI.create("")).toString();
                int ticket = measurements.begin();
                String ontologyHash = service.ontologyHash(ontology);
                measurements.finish(constructSeriesName(id, name(), "hash"), ticket);

                if (skipBlc) return;
                System.out.println("attest " + name() + " " + iri + " " + versionIri + " " + ontologyHash);

                ticket = measurements.begin();
                TransactionReceipt receipt = service.attest(iri, versionIri, "John doe", String.valueOf(ontologyHash), null);
                measurements.finish(constructSeriesName(id, name(), "attest"), ticket);
                // gas cost is depends on set of call parameters and state of contract, first call is more expensive
                measurements.manualMeasurement("attest-gas", receipt.getGasUsed().longValue());
                System.out.println(receipt.getGasUsed());
            }

            @Override
            public void verify(String id, OWLOntology ontology) throws Exception {
                String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
                String versionIri = ontology.getOntologyID().getVersionIRI().or(IRI.create("")).toString();
                String ontologyHash = service.ontologyHash(ontology);
                System.out.println("verify " + name() + " " + iri + " " + versionIri + " " + ontologyHash);

                int ticket = measurements.begin();
                VerifyResult verifyResult = service.verify(iri, versionIri, ontologyHash, null);
                measurements.finish(constructSeriesName(id, name(), "verify"), ticket);
                System.out.println(verifyResult.toString());
            }

            @Override
            public String name() {
                return service.getClass().getSimpleName().toLowerCase();
            }
        }
    }
}

