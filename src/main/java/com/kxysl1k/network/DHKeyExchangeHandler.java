package com.kxysl1k.network;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

public class DHKeyExchangeHandler {
    private static KeyPair keyPair;
    private static KeyAgreement keyAgree;

    public static void init() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            // Diffie-Hellman (2048-bit)
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(2048);
            DHParameterSpec dhSpec = paramGen.generateParameters().getParameterSpec(DHParameterSpec.class);

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
            keyGen.initialize(dhSpec);
            keyPair = keyGen.generateKeyPair();

            keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(keyPair.getPrivate());

            //publicKey на ссервер через ModDataSender
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KeyAgreement getKeyAgreement() { return keyAgree; }
    public static byte[] getPublicKeyEncoded() { return keyPair.getPublic().getEncoded(); }
}