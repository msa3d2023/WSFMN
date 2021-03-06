/*
 * Copyright © 2017 Team 18 (WSFMN), CMPUT301, University of Alberta – All Rights Reserved.
 * You may use, distribute, or modify this code under terms and conditions of the Code of Student Behavior at University of Alberta.
 * You can find a copy of the license in this project. Otherwise please contact nmayne@ualberta.ca.
 *
 *  Team 18 is: Musaed Alsobaie, Siddhant Khanna, Wei Li, Nicholas Mayne, Fredric Mendi.
 *
 *  Code Reuse: https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 */

package com.wsfmn.model;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

/**
 * This code is almost entirely from
 * https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 */
public class IDGenerator {
    /**
     * Generate a random string.
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";

    public static final String other = "!@#$%^&*()-+=";

    public static final String alphanum = upper + lower + digits + other;

    private final Random random;

    private final char[] symbols;

    private final char[] buf;

    public IDGenerator(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        if (random == null) {
            throw new NullPointerException();
        }
        this.random = random;
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an alphanumeric string generator.
     */
    public IDGenerator(int length, Random random) {
        this(length, random, alphanum);
    }

    /**
     * Create an alphanumeric strings from a secure generator.
     */
    public IDGenerator(int length) {
        this(length, new SecureRandom());
    }

    /**
     * Create session identifiers.
     */
    public IDGenerator() {
        this(20);
    }
}



