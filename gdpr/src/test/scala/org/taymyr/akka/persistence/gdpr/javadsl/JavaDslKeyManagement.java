package org.taymyr.akka.persistence.gdpr.javadsl;

import akka.Done;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static akka.Done.done;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class JavaDslKeyManagement implements KeyManagement {
    private final Map<String, SecretKey> keyStore = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<Optional<SecretKey>> getKey(String dataSubjectId) {
        return supplyAsync(() -> ofNullable(keyStore.get(dataSubjectId)));
    }

    @Override
    public CompletionStage<SecretKey> getOrCreateKey(String dataSubjectId) {
        return supplyAsync(() -> {
            SecretKey key = keyStore.get(dataSubjectId);
            if (key == null) {
                try {
                    key = KeyGenerator.getInstance("AES").generateKey();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                keyStore.put(dataSubjectId, key);
            }
            return key;
        });
    }

    @Override
    public CompletionStage<Done> shred(String dataSubjectId) {
        return supplyAsync(() -> {
            keyStore.remove(dataSubjectId);
            return done();
        });
    }
}
