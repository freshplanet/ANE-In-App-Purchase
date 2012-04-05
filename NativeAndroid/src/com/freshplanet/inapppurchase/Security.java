// Copyright 2010 Google Inc. All Rights Reserved.

package com.freshplanet.inapppurchase;

import java.security.SecureRandom;
import java.util.HashSet;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class Security {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * This keeps track of the nonces that we generated and sent to the
     * server.  We need to keep track of these until we get back the purchase
     * state and send a confirmation message back to Android Market. If we are
     * killed and lose this list of nonces, it is not fatal. Android Market will
     * send us a new "notify" message and we will re-generate a new nonce.
     * This has to be "static" so that the {@link BillingReceiver} can
     * check if a nonce exists.
     */
    private static HashSet<Long> sKnownNonces = new HashSet<Long>();

    /** Generates a nonce (a random number used once). */
    public static long generateNonce() {
        long nonce = RANDOM.nextLong();
        sKnownNonces.add(nonce);
        return nonce;
    }

    public static void removeNonce(long nonce) {
        sKnownNonces.remove(nonce);
    }

    public static boolean isNonceKnown(long nonce) {
        return sKnownNonces.contains(nonce);
    }

}
