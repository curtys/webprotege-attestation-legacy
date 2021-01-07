package ch.unifr.digits;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

import java.io.InputStream;

public class FileLoader {
    public static OWLOntology loadOntologyFromResources(String filename) throws OWLOntologyCreationException {
        InputStream inputStream = FileLoader.class.getClassLoader().getResourceAsStream(filename);
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology owlOntology = ontologyManager.loadOntologyFromOntologyDocument(inputStream);
        return owlOntology;
    }
}
