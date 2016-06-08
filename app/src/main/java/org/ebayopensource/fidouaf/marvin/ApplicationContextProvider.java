/*
 * Copyright 2015 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.ebayopensource.fidouaf.marvin;

import android.app.Application;
import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

public class ApplicationContextProvider extends Application {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static final String ATTEST_KEY = "UAFAttestKey";
    private static final String X509PRINCIPAL = "CN=Dummy UAF Client";

    /**
     * Keeps a reference of the application context
     */
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
        checkAttestationKey(sContext);
    }

    /***
     * This is one idea on how to best deal with the attestation certificates:
     * Generate self signed certificate on app first run
     * Pros: We can track the device
     * Cons: The certificate will be regenerated if app is reinstall or app data cleaned
     * Cons: The certificate is different for each app instance installed
     *
     * @param context
     */
    private void checkAttestationKey(Context context) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            PrivateKey privateKey = (PrivateKey) ks.getKey(ATTEST_KEY, null);

            if (privateKey == null) {
//                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
//                        KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                        "RSA", "AndroidKeyStore");


                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 10);

                keyPairGenerator.initialize(
                        new KeyPairGeneratorSpec.Builder(context)
                                .setAlias("UAFAttestKey")
                                .setSubject(new X500Principal("CN=Marvin - Android UAF Client"))
                                .setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
                                .setStartDate(start.getTime())
                                .setEndDate(end.getTime())
                                .build());


//                keyPairGenerator.initialize(
//                        new KeyGenParameterSpec.Builder(ATTEST_KEY,
//                                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                                .setDigests(KeyProperties.DIGEST_SHA256)
//                                .setCertificateNotBefore(start.getTime())
//                                .setCertificateNotAfter(end.getTime())
//                                .setCertificateSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
//                                .setCertificateSubject(new X500Principal(X509PRINCIPAL))
//                                .build());

                KeyPair keyPair = keyPairGenerator.generateKeyPair();
            }
        } catch (Exception e) {
            logger.info("e=" + e);
        }
    }

    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {
        return sContext;
    }
}
