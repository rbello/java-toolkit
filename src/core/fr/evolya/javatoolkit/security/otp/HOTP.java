package fr.evolya.javatoolkit.security.otp;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import fr.evolya.javatoolkit.security.codec.Base32;

public class HOTP {
	
	private byte[] secretKey;
	private String clearKey;
	private long ttl = 30000;
	
	private static final String HMAC_HASH_FUNCTION = "HmacSHA1";

	public HOTP() {
		this("");
	}
	
	public HOTP(String secretKey) {
		setKey(secretKey);
	}

	public void setKey(String secretKey) {
		Base32 base32 = new Base32();
		this.clearKey = secretKey;
		this.secretKey = base32.encode(secretKey.getBytes());
	}

	public boolean verify(int pin) {
		return pin == calculateCode(secretKey, currentTime(), ttl);
	}
	
	public boolean verify(int pin, long time) {
		return pin == at(time);
	}
	
	public int at(long time) {
		return calculateCode(secretKey, time, ttl);
	}
	
	public int now() {
		return calculateCode(secretKey, new Date().getTime(), ttl );
	}
	
    /**
     * Calculates the verification code of the provided key at the specified
     * instant of time using the algorithm specified in RFC 6238.
     *
     * @param key the secret key in binary format.
     * @param tm  the instant of time.
     * @return the validation code for the provided key at the specified instant
     * of time.
     */
    public static int calculateCode(byte[] key, long tm, long ttl)
    {
    	tm = (long) Math.floor(tm / ttl);
    	
        // Allocating an array of bytes to represent the specified instant
        // of time.
        byte[] data = new byte[8];
        long value = tm;

        // Converting the instant of time from the long representation to a
        // big-endian array of bytes (RFC4226, 5.2. Description).
        for (int i = 8; i-- > 0; value >>>= 8)
        {
            data[i] = (byte) value;
        }

        // Building the secret key specification for the HmacSHA1 algorithm.
        SecretKeySpec signKey = new SecretKeySpec(key, HMAC_HASH_FUNCTION);

        try
        {
            // Getting an HmacSHA1 algorithm implementation from the JCE.
            Mac mac = Mac.getInstance(HMAC_HASH_FUNCTION);

            // Initializing the MAC algorithm.
            mac.init(signKey);

            // Processing the instant of time and getting the encrypted data.
            byte[] hash = mac.doFinal(data);

            // Building the validation code performing dynamic truncation
            // (RFC4226, 5.3. Generating an HOTP value)
            int offset = hash[hash.length - 1] & 0xF;

            // We are using a long because Java hasn't got an unsigned integer type
            // and we need 32 unsigned bits).
            long truncatedHash = 0;

            for (int i = 0; i < 4; ++i)
            {
                truncatedHash <<= 8;

                // Java bytes are signed but we need an unsigned integer:
                // cleaning off all but the LSB.
                truncatedHash |= (hash[offset + i] & 0xFF);
            }

            int codeDigits = 6;
            int keyModulus = (int) Math.pow(10, codeDigits);
            
            // Clean bits higher than the 32nd (inclusive) and calculate the
            // module with the maximum validation code value.
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= keyModulus;

            // Returning the validation code to the caller.
            return (int) truncatedHash;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException ex)
        {
            throw new RuntimeException("The operation cannot be performed now.\n" + ex.getMessage());
        }
    }

	private static long currentTime() {
		return System.currentTimeMillis() / 1000L;
	}

	public String getKey() {
		return this.clearKey;
	}

	public long getTTL() {
		return ttl;
	}

	public void setTTL(long ttl) {
		this.ttl = ttl;
	}
	
}
