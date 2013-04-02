package com.jumptap.examples.asyncio.wait;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * Loop a certain number of times performing hashes.
 */
public class HashWait implements ExpensiveWait {
    private int max;

    /**
     * Create a new waiter that will hash until max is reached.
     * 
     * @param max The max amount of operations performed.
     */
    public HashWait(int max) {
        this.max = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String wait(String bogus) {
        HashFunction sha1 = Hashing.sha1();
        Hasher reduction = sha1.newHasher();

        for (int i = 0; i < max; i++) {
            Hasher itemHasher = sha1.newHasher();
            String unhashedString = i + "format" + i;
            byte[] unhashedBytes = unhashedString.getBytes();
            byte[] hashedBytes = itemHasher.putBytes(unhashedBytes).hash().asBytes();
            reduction.putBytes(hashedBytes);
        }

        return reduction.hash().toString();
    }
}
