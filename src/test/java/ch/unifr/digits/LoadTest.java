package ch.unifr.digits;

import ch.unifr.digits.webprotege.attestation.server.ChangeTrackingAttestationService;
import ch.unifr.digits.webprotege.attestation.server.FileAttestationService;
import ch.unifr.digits.webprotege.attestation.server.OntologyAttestationService;
import ch.unifr.digits.webprotege.attestation.shared.VerifyResult;
import org.junit.jupiter.api.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.semanticweb.owlapi.owlxml.renderer.OWLXMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoadTest {

    private static final int NUM_RUNS = 0;
    private static final String RESULTS_DIR = "results/";
    private static final Map<String, String> testOntologyFiles = new HashMap<>();
    private static final Map<String, OWLOntology> loadedOntologies = new HashMap<>();
    private static final Measurements measurements = new Measurements();
    private static final List<Runner> serviceRunners = new ArrayList<>();

    static {
        testOntologyFiles.put("foodon", "ontologies/foodon.owl");
        testOntologyFiles.put("doid", "ontologies/doid.owl");
        testOntologyFiles.put("obi", "ontologies/obi.owl");
//        testOntologyFiles.put("ncit", "ontologies/ncit-thesaurus.owl");
        testOntologyFiles.put("dron", "ontologies/dron-full.owl");
//        serviceRunners.add(new Runner.FileAttest(new FileAttestationService(), measurements));
        serviceRunners.add(new Runner.OntologyAttest(new OntologyAttestationService(), measurements));
//        serviceRunners.add(new Runner.ChangeTracking(new ChangeTrackingAttestationService(), measurements));
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        for (Map.Entry<String, String> entry : testOntologyFiles.entrySet()) {
            OWLOntology owlOntology = FileLoader.loadOntologyFromResources(entry.getValue());
            loadedOntologies.put(entry.getKey(), owlOntology);
            int entities = owlOntology.getSignature().size();
            int classes = owlOntology.getClassesInSignature().size();
            System.out.println(entry.getKey() + "[numEntities="+ entities +"][numCLasses=" + classes + "]");
        }
    }

    @Test
    public void batch() throws Exception {

        for (Map.Entry<String, OWLOntology> entry : loadedOntologies.entrySet()) {
            for (Runner runner : serviceRunners) {
                for (int i = 0; i < NUM_RUNS; i++) {
                    runner.attest(entry.getKey(), entry.getValue());
                    runner.verify(entry.getKey(), entry.getValue());
                }
                saveMeasurementsSeries(RESULTS_DIR+runner.name()+"-attest.csv", entry.getKey(),
                        measurements.getSeries(constructSeriesName(entry.getKey(), runner.name(), "attest")));
                saveMeasurementsSeries(RESULTS_DIR+runner.name()+"-hash.csv", entry.getKey(),
                        measurements.getSeries(constructSeriesName(entry.getKey(), runner.name(), "hash")));
                saveMeasurementsSeries(RESULTS_DIR+runner.name()+"-verify.csv", entry.getKey(),
                        measurements.getSeries(constructSeriesName(entry.getKey(), runner.name(), "verify")));
            }
        }

    }

    private static void printSeries(String tag, List<Duration> series) {
        List<Long> collect = series.stream().map(Duration::toMillis).collect(Collectors.toList());
        System.out.println(tag + " : " + collect + " [ms]");
        System.out.println("Mean: " + arimMean(series));
    }

    private static void saveMeasurementsSeries(String fname, String id, List<Duration> series) throws Exception {
        File file = new File(fname);
        String head = null;
        if (!file.exists()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("id").append(",");
            for (int i = 1; i <= NUM_RUNS; i++) {
                buffer.append("run_").append(i).append(",");
            }
            buffer.append("mean");
            head = buffer.toString();
        }
        FileWriter fileWriter = new FileWriter(file, true);
        PrintWriter writer = new PrintWriter(fileWriter);
        if (head != null) writer.println(head);

        String line = id+",";
        line += series.stream().map(Duration::toNanos).map(String::valueOf).collect(Collectors.joining(","));
        line += "," + arimMean(series);
        writer.println(line);
        writer.close();
    }

    private static final String constructSeriesName(String id, String serviceName, String tag) {
        return id + "-" + serviceName + "-" + tag;
    }

    private static final double arimMean(List<Duration> series) {
        List<Long> collect = series.stream().map(Duration::toNanos).collect(Collectors.toList());
        long sum = collect.stream().reduce(Long::sum).orElse(0L);
        double arimMean = sum / (double) Optional.of(collect.size()).filter(i -> i > 0).orElse(1);
        return arimMean;
    }

    private interface Runner {
        void attest(String id, OWLOntology ontology) throws Exception;
        void verify(String id, OWLOntology ontology) throws Exception;
        String name();

        class FileAttest implements Runner {

            private final FileAttestationService service;
            private final Measurements measurements;

            FileAttest(FileAttestationService service, Measurements measurements) {
                this.service = service;
                this.measurements = measurements;
            }

            @Override
            public void attest(String id, OWLOntology ontology) throws Exception {
                String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
                String versionIri = ontology.getOntologyID().getVersionIRI().or(IRI.create("")).toString();
                String document = documentString(ontology);
                int ticket = measurements.begin();
                String hash = service.hashFile(document.getBytes());
                measurements.finish(constructSeriesName(id, name(), "hash"), ticket);
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

            OntologyAttest(OntologyAttestationService service, Measurements measurements) {
                this.service = service;
                this.measurements = measurements;
            }

            @Override
            public void attest(String id, OWLOntology ontology) throws Exception {
                String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
                String versionIri = ontology.getOntologyID().getVersionIRI().or(IRI.create("")).toString();
                int ticket = measurements.begin();
                String ontologyHash = service.ontologyHash(ontology);
                measurements.finish(constructSeriesName(id, name(), "hash"), ticket);
                System.out.println("attest " + name() + " " + iri + " " + versionIri + " " + ontologyHash);

                ticket = measurements.begin();
                service.attest(iri, versionIri,"John doe", String.valueOf(ontologyHash), null);
                measurements.finish(constructSeriesName(id, name(), "attest"), ticket);
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

        class ChangeTracking implements Runner {

            private final ChangeTrackingAttestationService service;
            private final Measurements measurements;

            ChangeTracking(ChangeTrackingAttestationService service, Measurements measurements) {
                this.service = service;
                this.measurements = measurements;
            }

            @Override
            public void attest(String id, OWLOntology ontology) throws Exception {
                String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
                String versionIri = ontology.getOntologyID().getVersionIRI().or(IRI.create("")).toString();
                int ticket = measurements.begin();
                String ontologyHash = service.ontologyHash(ontology);
                List<Integer> classHashes = service.classHashes(ontology);
                measurements.finish(constructSeriesName(id, name(), "hash"), ticket);
                ChangeTrackingAttestationService.EntitySet params =
                        new ChangeTrackingAttestationService.EntitySet(service.toBigInt(classHashes));

                System.out.println("attest " + name() + " " + iri + " " + versionIri + " " + ontologyHash);

                ticket = measurements.begin();
                service.attest(iri, versionIri,"John doe", String.valueOf(ontologyHash), params);
                measurements.finish(constructSeriesName(id, name(), "attest"), ticket);
            }

            @Override
            public void verify(String id, OWLOntology ontology) throws Exception {
                // skip
            }

            @Override
            public String name() {
                return service.getClass().getSimpleName().toLowerCase();
            }
        }
    }
}

