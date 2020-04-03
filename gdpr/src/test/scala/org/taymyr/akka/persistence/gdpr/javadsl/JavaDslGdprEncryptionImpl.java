package org.taymyr.akka.persistence.gdpr.javadsl;

import akka.actor.ExtendedActorSystem;

public class JavaDslGdprEncryptionImpl extends AbstractGdprEncryption {

    private final JavaDslKeyManagement keyManagement;

    public JavaDslGdprEncryptionImpl(ExtendedActorSystem system, String config) {
        super(system);
        keyManagement = new JavaDslKeyManagement();
    }

    @Override
    public KeyManagement keyManagement() {
        return keyManagement;
    }
}
