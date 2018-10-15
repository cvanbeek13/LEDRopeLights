package com.CoryVanBeek.RopeLights.thing.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Properties;

/**
 * This is a helper class to facilitate reading of the configurations and
 * certificate from the resource files.
 */
public class LightsUtil {
    private static final Logger logger = LoggerFactory.getLogger(LightsUtil.class);
    private static final String PropertyFile = "rope-lights.properties";

    public static class KeyStorePasswordPair {
        public KeyStore keyStore;
        public String keyPassword;

        public KeyStorePasswordPair(KeyStore keyStore, String keyPassword) {
            this.keyStore = keyStore;
            this.keyPassword = keyPassword;
        }
    }

    public static String getConfig(String name) {
        Properties prop = new Properties();
        URL resource = LightsUtil.class.getResource(PropertyFile);
        if (resource == null) {
            return null;
        }
        try (InputStream stream = resource.openStream()) {
            prop.load(stream);
        } catch (IOException e) {
            return null;
        }
        String value = prop.getProperty(name);
        if (value == null || value.trim().length() == 0) {
            return null;
        } else {
            return value;
        }
    }

    public static KeyStorePasswordPair getKeyStorePasswordPair(final String certificateFile, final String privateKeyFile) {
        return getKeyStorePasswordPair(certificateFile, privateKeyFile, null);
    }

    public static KeyStorePasswordPair getKeyStorePasswordPair(final String certificateFile, final String privateKeyFile,
                                                               String keyAlgorithm) {
        if (certificateFile == null || privateKeyFile == null) {
            logger.warn("Certificate or private key file missing");
            return null;
        }
        logger.trace("Cert file:" +certificateFile + " Private key: "+ privateKeyFile);

        PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile, keyAlgorithm);
        if(privateKey == null)
            privateKey = loadPrivateKeyFromResource(privateKeyFile,  keyAlgorithm);

        List<Certificate> certChain = loadCertificatesFromFile(certificateFile);
        if(certChain == null)
            certChain = loadCertificatesFromResource(certificateFile);

        if (certChain == null || privateKey == null) return null;

        return getKeyStorePasswordPair(certChain, privateKey);
    }

    public static KeyStorePasswordPair getKeyStorePasswordPair(final List<Certificate> certificates, final PrivateKey privateKey) {
        KeyStore keyStore;
        String keyPassword;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            // randomly generated key password for the key in the KeyStore
            keyPassword = new BigInteger(128, new SecureRandom()).toString(32);

            Certificate[] certChain = new Certificate[certificates.size()];
            certChain = certificates.toArray(certChain);
            keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certChain);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            logger.warn("Failed to create key store");
            return null;
        }

        return new KeyStorePasswordPair(keyStore, keyPassword);
    }

    private static List<Certificate> loadCertificatesFromFile(final String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            logger.debug("Certificate file: " + filename + " is not found as a file.");
            return null;
        }

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (List<Certificate>) certFactory.generateCertificates(stream);
        } catch (IOException | CertificateException e) {
            logger.warn("Failed to load certificate file " + filename);
        }
        return null;
    }

    private static List<Certificate> loadCertificatesFromResource(final String resourceName) {
        URL resource = LightsUtil.class.getResource(resourceName);
        if (resource == null) {
            logger.debug("Certificate file: " + resourceName + " is not found in resources.");
            return null;
        }

        try (BufferedInputStream stream = new BufferedInputStream(resource.openStream())) {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (List<Certificate>) certFactory.generateCertificates(stream);
        } catch (IOException | CertificateException e) {
            logger.warn("Failed to load certificate file from resource" + resourceName);
        }
        return null;
    }

    private static PrivateKey loadPrivateKeyFromFile(final String filename, final String algorithm) {
        PrivateKey privateKey = null;

        File file = new File(filename);
        if (!file.exists()) {
            logger.debug("Private key file not found: " + filename);
            return null;
        }
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
            privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
        } catch (IOException | GeneralSecurityException e) {
            logger.warn("Failed to load private key from file " + filename);
        }

        return privateKey;
    }

    private static PrivateKey loadPrivateKeyFromResource(final String filename, final String algorithm) {
        PrivateKey privateKey = null;

        URL resource = LightsUtil.class.getResource(filename);
        if (resource == null) {
            logger.debug("Private key file not found in resources: " + filename);
            return null;
        }
        try (DataInputStream stream = new DataInputStream(resource.openStream())) {
            privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
        } catch (IOException | GeneralSecurityException e) {
            logger.warn("Failed to load private key from resource file " + filename);
        }

        return privateKey;
    }
}
