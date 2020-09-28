package ch.unifr.digits.webprotege.attestation;


import ch.unifr.digits.webprotege.attestation.contract.OntologyContract;
import ch.unifr.digits.webprotege.attestation.contract.VerifyContractReturn;
import ch.unifr.digits.webprotege.attestation.contract.VerifyResult;
import ch.unifr.digits.webprotege.attestation.ethereum.Connection;
import ch.unifr.digits.webprotege.attestation.ethereum.EthereumProvider;
import ch.unifr.digits.webprotege.attestation.web3.Web3;
import ch.unifr.digits.webprotege.attestation.web3.core.TransactionReceipt;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.*;
import edu.stanford.bmir.protege.web.shared.download.DownloadFormatExtension;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import elemental2.promise.Promise;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AttestationService {

    public static void signOntology(ProjectId projectId, RevisionNumber revisionNumber, String ontologyIRI,
                                    String versionIRI, String name, String address, Callback<Boolean, Object> callback) {
        DownloadFormatExtension extension = DownloadFormatExtension.owl;
        ProjectDeflateDownloader downloader = new ProjectDeflateDownloader(projectId, revisionNumber, extension);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Promise<String> hashPromise = downloader.download().then(ontology -> {
            byte[] bytes = digest.digest(ontology.getBytes());
            String hash = bytesToHex(bytes);
            GWT.log("[attestation] Ontology hash: " + hash);
            return Promise.resolve(hash);
        });

        Promise<Connection> connectionPromise = connectToChain();
        Promise<Object> interfacePromise = getContractInterface();

        Promise.all(hashPromise, connectionPromise, interfacePromise).then((args) -> {
            String hash = String.valueOf(args[0]);
            Connection connection = (Connection) args[1];
            Object contractInterface = args[2];

            OntologyContract contract = new OntologyContract(connection.getWeb3(), contractInterface, address);
            Promise<TransactionReceipt> attestPromise = contract.attest(connection.getProvider().selectedAddress,
                    ontologyIRI, versionIRI, name, hash);
            attestPromise.then(receipt -> {
                GWT.log("[attestation] transaction result: " + receipt.status);
                callback.onSuccess(receipt.status);
                return null;
            }).catch_(error -> {
                callback.onFailure(error);
                return null;
            });

            return null;
        });

    }

    public static void verifyOntology(ProjectId projectId, RevisionNumber revisionNumber, String ontologyIRI,
                                      String versionIRI, String address, Callback<VerifyResult, Object> callback) {
        DownloadFormatExtension extension = DownloadFormatExtension.owl;
        ProjectDeflateDownloader downloader = new ProjectDeflateDownloader(projectId, revisionNumber, extension);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Promise<String> hashPromise = downloader.download().then(ontology -> {
            byte[] bytes = digest.digest(ontology.getBytes());
            String hash = bytesToHex(bytes);
            GWT.log("[attestation] Ontology hash: " + hash);
            return Promise.resolve(hash);
        });

        Promise<Connection> connectionPromise = connectToChain();
        Promise<Object> interfacePromise = getContractInterface();

        Promise.all(hashPromise, connectionPromise, interfacePromise).then((args) -> {
            String hash = String.valueOf(args[0]);
            Connection connection = (Connection) args[1];
            Object contractInterface = args[2];

            OntologyContract contract = new OntologyContract(connection.getWeb3(), contractInterface, address);
            Promise<VerifyContractReturn> resultPromise = contract.verify(connection.getProvider().selectedAddress,
                    ontologyIRI, versionIRI, hash);
            resultPromise.then(contractReturn -> {
                VerifyResult result = new VerifyResult(contractReturn.valid, contractReturn.signer,
                        contractReturn.signerName, contractReturn.timestamp);
                GWT.log("[attestation] verify result: " + result.toString());
                callback.onSuccess(result);
                return null;
            }).catch_(error -> {
                callback.onFailure(error);
                return null;
            });
            return null;
        });

    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static Promise<Connection> connectToChain() {
        Promise<Connection> promise = new Promise<>((resolve, reject) -> {
            EthereumProvider.detectEthereumProvider().then(p -> {
                Web3 web3 = new Web3(p);

                web3.eth.requestAccounts().then(accounts -> {
                    GWT.log("[attestation] connected to chain.");
                    Connection connection = new Connection(p, web3);
                    resolve.onInvoke(connection);
                    return null;
                }).catch_(error -> {
                    reject.onInvoke(error);
                    return null;
                });
                return null;
            }).catch_(error -> {
                reject.onInvoke(error);
                return null;
            });
        });
        return promise;
    }

    private static Promise<Object> getContractInterface() {
        String url = GWT.getModuleBaseForStaticFiles() + "attestation/OntologyAttestation.abi";
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        requestBuilder.setHeader("Accept", "application/json");
        requestBuilder.setRequestData(null);

        Promise<Object> promise = new Promise<>((resolve, reject) -> {
            requestBuilder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    String json = response.getText();
                    Object object = JsonUtils.unsafeEval(json);
                    GWT.log("[attestation] retrieved contract interface.");
                    resolve.onInvoke(object);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    reject.onInvoke(exception);
                }
            });
            try {
                requestBuilder.send();
            } catch (RequestException e) {
                reject.onInvoke(e);
            }
        });

        return promise;
    }


}
