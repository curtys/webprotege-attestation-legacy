solc --evm-version istanbul --abi --bin --optimize --overwrite -o ./../build/ ../src/main/solidity/*
for f in ../build/*.abi; do 
    cp -- "$f" "../src/main/java/ch/unifr/digits/webprotege/attestation/public/attestation/interfaces/$(basename -- "$f" .abi).json"
    epirus generate solidity generate -a "$f" -o ../build/wrapper/ -p ch.unifr.digits.webprotege.attestation.server.contracts
done
find ../build/wrapper -type f | grep -i java$ | xargs -i cp {} ../src/main/java/ch/unifr/digits/webprotege/attestation/server/contracts