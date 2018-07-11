package com.example.nguyen.fileencryption.model;

import java.util.Arrays;

public class AES {
    /*String cryptKey = "aPb4x9q0H4W8rPs7";
    String data = "hello world      ";
    aes = new AES();
    aes.setKeyOrBytes(cryptKey);
    data = aes.Encrypt(data);
    data = aes.Decrypt(data);*/
    public static String cryptKey = "TruongXuanNguyen";
    public int traceLevel = 0;
    public String traceInfo = "";

    /** AES constants and variables. */
    public static final int
            ROUNDS = 14,        // AES has 10-14 rounds
            BLOCK_SIZE = 16,    // AES uses 128-bit (16 byte) key
            KEY_LENGTH = 32;    // AES uses 128/192/256-bit (16/24/32 byte) key

    // Define key attributes for current AES instance
    /** number of rounds used given AES key set on this instance. */
    int numRounds;
    /** encryption round keys derived from AES key set on this instance. */
    byte[][] Ke;
    /** decryption round keys derived from AES key set on this instance. */
    byte[][] Kd;


    static final byte[] S = {
            99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118,
            -54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64,
            -73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21,
            4, -57, 35, -61, 24, -106, 5, -102, 7, 18, -128, -30, -21, 39, -78, 117,
            9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124,
            83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49,
            -48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, 127, 80, 60, -97, -88,
            81, -93, 64, -113, -110, -99, 56, -11, -68, -74, -38, 33, 16, -1, -13, -46,
            -51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115,
            96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37,
            -32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121,
            -25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8,
            -70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118,
            112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98,
            -31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33,
            -116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22 };

    /** AES decryption S-box.
     *  <p>See FIPS-197 section 5.1.1 or Stallings section 5.2.
     *  Note that hex values have been converted to decimal for easy table
     *  specification in Java.
     */
    static final byte[] Si = {
            82, 9, 106, -43, 48, 54, -91, 56, -65, 64, -93, -98, -127, -13, -41, -5,
            124, -29, 57, -126, -101, 47, -1, -121, 52, -114, 67, 68, -60, -34, -23, -53,
            84, 123, -108, 50, -90, -62, 35, 61, -18, 76, -107, 11, 66, -6, -61, 78,
            8, 46, -95, 102, 40, -39, 36, -78, 118, 91, -94, 73, 109, -117, -47, 37,
            114, -8, -10, 100, -122, 104, -104, 22, -44, -92, 92, -52, 93, 101, -74, -110,
            108, 112, 72, 80, -3, -19, -71, -38, 94, 21, 70, 87, -89, -115, -99, -124,
            -112, -40, -85, 0, -116, -68, -45, 10, -9, -28, 88, 5, -72, -77, 69, 6,
            -48, 44, 30, -113, -54, 63, 15, 2, -63, -81, -67, 3, 1, 19, -118, 107,
            58, -111, 17, 65, 79, 103, -36, -22, -105, -14, -49, -50, -16, -76, -26, 115,
            -106, -84, 116, 34, -25, -83, 53, -123, -30, -7, 55, -24, 28, 117, -33, 110,
            71, -15, 26, 113, 29, 41, -59, -119, 111, -73, 98, 14, -86, 24, -66, 27,
            -4, 86, 62, 75, -58, -46, 121, 32, -102, -37, -64, -2, 120, -51, 90, -12,
            31, -35, -88, 51, -120, 7, -57, 49, -79, 18, 16, 89, 39, -128, -20, 95,
            96, 81, 127, -87, 25, -75, 74, 13, 45, -27, 122, -97, -109, -55, -100, -17,
            -96, -32, 59, 77, -82, 42, -11, -80, -56, -21, -69, 60, -125, 83, -103, 97,
            23, 43, 4, 126, -70, 119, -42, 38, -31, 105, 20, 99, 85, 33, 12, 125 };

    /** AES key schedule round constant table.
     *  <p>See FIPS-197 section 5.1.1 or Stallings section 5.2.
     *  Note that hex values have been converted to decimal for easy table
     *  specification in Java, and that indexes start at 1, hence initial 0 entry.
     */
    static final byte[] rcon = {
            0,
            1, 2, 4, 8, 16, 32,
            64, -128, 27, 54, 108, -40,
            -85, 77, -102, 47, 94, -68,
            99, -58, -105, 53, 106, -44,
            -77, 125, -6, -17, -59, -111 };

    /** Internal AES constants and variables. */
    public static final int
            COL_SIZE = 4,                // depth of each column in AES state variable
            NUM_COLS = BLOCK_SIZE / COL_SIZE,    // number of columns in AES state variable
            ROOT = 0x11B;                // generator polynomial used in GF(2^8)

    /** define ShiftRows transformation as shift amount for each row in state. */
    static final int[] row_shift = {0, 1, 2, 3};

    /* alog table for field GF(2^m) used to speed up multiplications. */
    static final int[] alog = new int[256];
    /* log table for field GF(2^m) used to speed up multiplications. */
    static final int[] log =  new int[256];

    /** static code to initialise the log and alog tables.
     *  Used to implement multiplication in GF(2^8).
     */
    static {
        int i, j;
        // produce log and alog tables, needed for multiplying in the field GF(2^8)
        alog[0] = 1;
        for (i = 1; i < 256; i++) {
            j = (alog[i-1] << 1) ^ alog[i-1];
            if ((j & 0x100) != 0) j ^= ROOT;
            alog[i] = j;
        }
        for (i = 1; i < 255; i++) log[alog[i]] = i;
    }

    /** Construct AES object. */
    public AES() {
    }

    /** return number of rounds for a given AES key size.
     *
     * @param keySize    size of the user key material in bytes.
     * @return        number of rounds for a given AES key size.
     */
    public static int getRounds (int keySize) {
        switch (keySize) {
            case 16:    // 16 byte = 128 bit key
                return 10;
            case 24:    // 24 byte = 192 bit key
                return 12;
            default:    // 32 byte = 256 bit key
                return 14;
        }
    }

    /** multiply two elements of GF(2^8).
     *  <p>Using pre-computed log and alog tables for speed.
     *
     *  @param a 1st value to multiply
     *  @param b 2nd value to multiply
     *  @return product of a * b module its generator polynomial
     */
    static final int mul (int a, int b) {
        return (a != 0 && b != 0) ?
                alog[(log[a & 0xFF] + log[b & 0xFF]) % 255] :
                0;
    }

    /** diagnostic trace of static tables. */
    public static void trace_static() {
        int i,j;
        System.out.print("AES Static Tablesn");
        System.out.print("S[] = n"); for(i=0;i<16;i++) { for(j=0;j<16;j++) System.out.print(Util.toHEX1(S[i*16+j])+", "); System.out.println();}
        System.out.print("Si[] = n"); for(i=0;i<16;i++) { for(j=0;j<16;j++) System.out.print(Util.toHEX1(Si[i*16+j])+", "); System.out.println();}
        System.out.print("rcon[] = n"); for(i=0;i<5;i++) {for(j=0;j<6;j++) System.out.print(Util.toHEX1(rcon[i*6+j])+", "); System.out.println();}
        System.out.print("log[] = n"); for(i=0;i<32;i++) {for(j=0;j<8;j++) System.out.print(Util.toHEX1(log[i*8+j])+", "); System.out.println();}
        System.out.print("alog[] = n"); for(i=0;i<32;i++) {for(j=0;j<8;j++) System.out.print(Util.toHEX1(alog[i*8+j])+", "); System.out.println();}
    }

    //......................................................................
    /**
     * AES encrypt 128-bit plaintext using key previously set.
     *
     * <p>Follows cipher specification given in FIPS-197 section 5.1
     * See pseudo code in Fig 5, and details in this section.
     *
     * @param plain the 128-bit plaintext value to encrypt.
     * @return the encrypted 128-bit ciphertext value.
     */
    public byte[] encrypt(byte[] plain) {
        // define working variables
        byte [] a = new byte[BLOCK_SIZE];    // AES state variable
        byte [] ta = new byte[BLOCK_SIZE];    // AES temp state variable
        byte [] Ker;                // encrypt keys for current round
        int    i, j, k, row, col;

        traceInfo = "";                // reset trace info
        if (traceLevel > 0) traceInfo = "encryptAES(" + Util.toHEX1(plain) + ")";

        // check for bad arguments
        if (plain == null)
            throw new IllegalArgumentException("Empty plaintext");
        if (plain.length != BLOCK_SIZE)
            throw new IllegalArgumentException("Incorrect plaintext length");

        // copy plaintext bytes into state and do initial AddRoundKey(state)
        Ker = Ke[0];
        for (i = 0; i < BLOCK_SIZE; i++)    a[i] = (byte)(plain[i] ^ Ker[i]);
        if (traceLevel > 2)
            traceInfo += "n  R0 (Key = "+Util.toHEX1(Ker)+")ntAK = "+Util.toHEX1(a);
        else if (traceLevel > 1)
            traceInfo += "n  R0 (Key = "+Util.toHEX1(Ker)+")t = "+Util.toHEX1(a);

        // for each round except last, apply round transforms
        for (int r = 1; r < numRounds; r++) {
            Ker = Ke[r];            // get session keys for this round
            if (traceLevel > 1)    traceInfo += "n  R"+r+" (Key = "+Util.toHEX1(Ker)+")t";

            // SubBytes(state) into ta using S-Box S
            for (i = 0; i < BLOCK_SIZE; i++) ta[i] = S[a[i] & 0xFF];
            if (traceLevel > 2)    traceInfo += "ntSB = "+Util.toHEX1(ta);

            // ShiftRows(state) into a
            for (i = 0; i < BLOCK_SIZE; i++) {
                row = i % COL_SIZE;
                k = (i + (row_shift[row] * COL_SIZE)) % BLOCK_SIZE;    // get shifted byte index
                a[i] = ta[k];
            }
            if (traceLevel > 2)    traceInfo += "ntSR = "+Util.toHEX1(a);

            // MixColumns(state) into ta
            //   implemented by expanding matrix mult for each column
            //   see FIPS-197 section 5.1.3
            for (col = 0; col < NUM_COLS; col++) {
                i = col * COL_SIZE;        // start index for this col
                ta[i]   = (byte)(mul(2,a[i]) ^ mul(3,a[i+1]) ^ a[i+2] ^ a[i+3]);
                ta[i+1] = (byte)(a[i] ^ mul(2,a[i+1]) ^ mul(3,a[i+2]) ^ a[i+3]);
                ta[i+2] = (byte)(a[i] ^ a[i+1] ^ mul(2,a[i+2]) ^ mul(3,a[i+3]));
                ta[i+3] = (byte)(mul(3,a[i]) ^ a[i+1] ^ a[i+2] ^ mul(2,a[i+3]));
            }
            if (traceLevel > 2)    traceInfo += "ntMC = "+Util.toHEX1(ta);

            // AddRoundKey(state) into a
            for (i = 0; i < BLOCK_SIZE; i++)    a[i] = (byte)(ta[i] ^ Ker[i]);
            if (traceLevel > 2)    traceInfo += "ntAK";
            if (traceLevel > 1)    traceInfo += " = "+Util.toHEX1(a);
        }

        // last round is special - only has SubBytes, ShiftRows and AddRoundKey
        Ker = Ke[numRounds];            // get session keys for final round
        if (traceLevel > 1)    traceInfo += "n  R"+numRounds+" (Key = "+Util.toHEX1(Ker)+")t";

        // SubBytes(state) into a using S-Box S
        for (i = 0; i < BLOCK_SIZE; i++) a[i] = S[a[i] & 0xFF];
        if (traceLevel > 2)    traceInfo += "ntSB = "+Util.toHEX1(a);

        // ShiftRows(state) into ta
        for (i = 0; i < BLOCK_SIZE; i++) {
            row = i % COL_SIZE;
            k = (i + (row_shift[row] * COL_SIZE)) % BLOCK_SIZE;    // get shifted byte index
            ta[i] = a[k];
        }
        if (traceLevel > 2)    traceInfo += "ntSR = "+Util.toHEX1(a);

        // AddRoundKey(state) into a
        for (i = 0; i < BLOCK_SIZE; i++)    a[i] = (byte)(ta[i] ^ Ker[i]);
        if (traceLevel > 2)    traceInfo += "ntAK";
        if (traceLevel > 1)    traceInfo += " = "+Util.toHEX1(a)+"n";
        if (traceLevel > 0)    traceInfo += " = "+Util.toHEX1(a)+"n";
        return (a);
    }


    //......................................................................
    /**
     * AES decrypt 128-bit ciphertext using key previously set.
     *
     * <p>Follows cipher specification given in FIPS-197 section 5.3
     * See pseudo code in Fig 5, and details in this section.
     *
     * @param cipher the 128-bit ciphertext value to decrypt.
     * @return the decrypted 128-bit plaintext value.
     */
    public byte[] decrypt(byte[] cipher) {
        // define working variables
        byte [] a = new byte[BLOCK_SIZE];    // AES state variable
        byte [] ta = new byte[BLOCK_SIZE];    // AES temp state variable
        byte [] Kdr;                // encrypt keys for current round
        int    i, j, k, row, col;

        traceInfo = "";                // reset trace info
        if (traceLevel > 0) traceInfo = "decryptAES(" + Util.toHEX1(cipher) + ")";

        // check for bad arguments
        if (cipher == null)
            throw new IllegalArgumentException("Empty ciphertext");
        if (cipher.length != BLOCK_SIZE)
            throw new IllegalArgumentException("Incorrect ciphertext length");

        // copy ciphertext bytes into state and do initial AddRoundKey(state)
        Kdr = Kd[0];
        for (i = 0; i < BLOCK_SIZE; i++)    a[i] = (byte)(cipher[i] ^ Kdr[i]);
        if (traceLevel > 2)
            traceInfo += "n  R0 (Key = "+Util.toHEX1(Kdr)+")nt AK = "+Util.toHEX1(a);
        else if (traceLevel > 1)
            traceInfo += "n  R0 (Key = "+Util.toHEX1(Kdr)+")t = "+Util.toHEX1(a);

        // for each round except last, apply round transforms
        for (int r = 1; r < numRounds; r++) {
            Kdr = Kd[r];            // get session keys for this round
            if (traceLevel > 1)    traceInfo += "n  R"+r+" (Key = "+Util.toHEX1(Kdr)+")t";

            // InvShiftRows(state) into ta (nb. same shift as encrypt but subtract)
            for (i = 0; i < BLOCK_SIZE; i++) {
                row = i % COL_SIZE;
                // get shifted byte index
                k = (i + BLOCK_SIZE - (row_shift[row] * COL_SIZE)) % BLOCK_SIZE;
                ta[i] = a[k];
            }
            if (traceLevel > 2)    traceInfo += "ntISR = "+Util.toHEX1(ta);

            // InvSubBytes(state) into a using inverse S-box Si
            for (i = 0; i < BLOCK_SIZE; i++) a[i] = Si[ta[i] & 0xFF];
            if (traceLevel > 2)    traceInfo += "ntISB = "+Util.toHEX1(a);

            // AddRoundKey(state) into ta
            for (i = 0; i < BLOCK_SIZE; i++)    ta[i] = (byte)(a[i] ^ Kdr[i]);
            if (traceLevel > 2)    traceInfo += "nt AK = "+Util.toHEX1(ta);

            // InvMixColumns(state) into a
            //   implemented by expanding matrix mult for each column
            //   see FIPS-197 section 5.3.3
            for (col = 0; col < NUM_COLS; col++) {
                i = col * COL_SIZE;        // start index for this col
                a[i]   = (byte)(mul(0x0e,ta[i]) ^ mul(0x0b,ta[i+1]) ^ mul(0x0d,ta[i+2]) ^ mul(0x09,ta[i+3]));
                a[i+1] = (byte)(mul(0x09,ta[i]) ^ mul(0x0e,ta[i+1]) ^ mul(0x0b,ta[i+2]) ^ mul(0x0d,ta[i+3]));
                a[i+2] = (byte)(mul(0x0d,ta[i]) ^ mul(0x09,ta[i+1]) ^ mul(0x0e,ta[i+2]) ^ mul(0x0b,ta[i+3]));
                a[i+3] = (byte)(mul(0x0b,ta[i]) ^ mul(0x0d,ta[i+1]) ^ mul(0x09,ta[i+2]) ^ mul(0x0e,ta[i+3]));
            }
            if (traceLevel > 2)    traceInfo += "ntIMC";
            if (traceLevel > 1)    traceInfo += " = "+Util.toHEX1(a);
        }

        // last round is special - only has InvShiftRows, InvSubBytes and AddRoundKey
        Kdr = Kd[numRounds];            // get session keys for final round
        if (traceLevel > 1)    traceInfo += "n  R"+numRounds+" (Key = "+Util.toHEX1(Kdr)+")t";

        // InvShiftRows(state) into ta
        for (i = 0; i < BLOCK_SIZE; i++) {
            row = i % COL_SIZE;
            // get shifted byte index
            k = (i + BLOCK_SIZE - (row_shift[row] * COL_SIZE)) % BLOCK_SIZE;
            ta[i] = a[k];
        }
        if (traceLevel > 2)    traceInfo += "ntISR = "+Util.toHEX1(a);

        // InvSubBytes(state) into ta using inverse S-box Si
        for (i = 0; i < BLOCK_SIZE; i++) ta[i] = Si[ta[i] & 0xFF];
        if (traceLevel > 2)    traceInfo += "ntISB = "+Util.toHEX1(a);

        // AddRoundKey(state) into a
        for (i = 0; i < BLOCK_SIZE; i++)    a[i] = (byte)(ta[i] ^ Kdr[i]);
        if (traceLevel > 2)    traceInfo += "nt AK";
        if (traceLevel > 1)    traceInfo += " = "+Util.toHEX1(a)+"n";
        if (traceLevel > 0)    traceInfo += " = "+Util.toHEX1(a)+"n";
        return (a);
    }


    //......................................................................
    /**
     * Expand a user-supplied key material into a session key.
     * <p>See FIPS-197 Section 5.3 Fig 11 for details of the key expansion.
     * <p>Session keys will be saved in Ke and Kd instance variables,
     * along with numRounds being the number of rounds for this sized key.
     *
     * @param key        The 128/192/256-bit AES key to use.
     */
    public void setKey(byte[] key) {
        // assorted internal constants
        final int BC = BLOCK_SIZE / 4;
        final int Klen = key.length;
        final int Nk = Klen / 4;

        int i, j, r;

        traceInfo = "";            // reset trace info
        if (traceLevel > 0) traceInfo = "setKeyOrBytes(" + Util.toHEX1(key) + ")n";

        // check for bad arguments
        if (key == null)
            throw new IllegalArgumentException("Empty key");
        if (!(key.length == 16 || key.length == 24 || key.length == 32))
            throw new IllegalArgumentException("Incorrect key length");

        // set master number of rounds given size of this key
        numRounds = getRounds(Klen);
        final int ROUND_KEY_COUNT = (numRounds + 1) * BC;

        // allocate 4 arrays of bytes to hold the session key values
        // each array holds 1 of the 4 bytes [b0 b1 b2 b3] in each word w
        byte[] w0 = new byte[ROUND_KEY_COUNT];
        byte[] w1 = new byte[ROUND_KEY_COUNT];
        byte[] w2 = new byte[ROUND_KEY_COUNT];
        byte[] w3 = new byte[ROUND_KEY_COUNT];

        // allocate arrays to hold en/decrypt session keys (by byte rather than word)
        Ke = new byte[numRounds + 1][BLOCK_SIZE]; // encryption round keys
        Kd = new byte[numRounds + 1][BLOCK_SIZE]; // decryption round keys

        // copy key into start of session array (by word, each byte in own array)
        for (i=0, j=0; i < Nk; i++) {
            w0[i] = key[j++]; w1[i] = key[j++]; w2[i] = key[j++]; w3[i] = key[j++];
        }

        // implement key expansion algorithm
        byte t0, t1, t2, t3, old0;        // temp byte values for each word
        for (i = Nk; i < ROUND_KEY_COUNT; i++) {
            t0 = w0[i-1]; t1 = w1[i-1]; t2 = w2[i-1]; t3 = w3[i-1];    // temp = w[i-1]
            if (i % Nk == 0) {
                // temp = SubWord(RotWord(temp)) ^ Rcon[i/Nk]
                old0 = t0;            // save old 1st byte value for t3 calc
                t0 = (byte)(S[t1 & 0xFF] ^ rcon[i/Nk]);    // nb. constant XOR 1st byte only
                t1 = S[t2 & 0xFF];
                t2 = S[t3 & 0xFF];    // nb. RotWord done by reordering bytes used
                t3 = S[old0 & 0xFF];
            }
            else if ((Nk > 6) && (i % Nk == 4)) {
                // temp = SubWord(temp)
                t0 = S[t0 & 0xFF]; t1 = S[t1 & 0xFF]; t2 = S[t2 & 0xFF]; t3 = S[t3 & 0xFF];
            }
            // w[i] = w[i-Nk] ^ temp
            w0[i] = (byte)(w0[i-Nk] ^ t0);
            w1[i] = (byte)(w1[i-Nk] ^ t1);
            w2[i] = (byte)(w2[i-Nk] ^ t2);
            w3[i] = (byte)(w3[i-Nk] ^ t3);
        }

        // now copy values into en/decrypt session arrays by round & byte in round
        for (r = 0, i = 0; r < numRounds + 1; r++) {    // for each round
            for (j = 0; j < BC; j++) {        // for each word in round
                Ke[r][4*j] = w0[i];
                Ke[r][4*j+1] = w1[i];
                Ke[r][4*j+2] = w2[i];
                Ke[r][4*j+3] = w3[i];
                Kd[numRounds - r][4*j] = w0[i];
                Kd[numRounds - r][4*j+1] = w1[i];
                Kd[numRounds - r][4*j+2] = w2[i];
                Kd[numRounds - r][4*j+3] = w3[i];
                i++;
            }
        }

        // create trace info if needed
        if (traceLevel > 3) {
            traceInfo += "  Encrypt Round keys:n";
            for(r=0;r<numRounds+1;r++) traceInfo += "  R"+r+"t = "+Util.toHEX1(Ke[r])+"n";
            traceInfo += "  Decrypt Round keys:n";
            for(r=0;r<numRounds+1;r++) traceInfo += "  R"+r+"t = "+Util.toHEX1(Kd[r])+"n";
        }
    }


    /** self-test routine for AES cipher
     *  @param hkey    key to test in hex
     *  @param hplain    plaintext to test in hex
     *  @param hcipher    ciphertext to test in hex
     *  @param lev    trace level to use
     */
    public static void self_test (String hkey, String hplain, String hcipher, int lev) {

        // AES test triple (128-bit key test value from FIPS-197)
        byte [] key    = Util.hex2byte(hkey);
        byte [] plain    = Util.hex2byte(hplain);
        byte [] cipher    = Util.hex2byte(hcipher);
        byte [] result;

        AES testAES = new AES();    // create new AES instance to test triple
        testAES.traceLevel = lev;    // select level of trace info
        testAES.setKey(key);        // set key and display trace info
        System.out.print(testAES.traceInfo);

        result = testAES.encrypt(plain);    // test encryption
        System.out.print(testAES.traceInfo);
        if (Arrays.equals(result, cipher))
            System.out.print("Test OKn");
        else
            System.out.print("Test Failed. Result was "+Util.toHEX(result)+"n");

        result = testAES.decrypt(cipher);    // test decryption
        System.out.print(testAES.traceInfo);
        if (Arrays.equals(result, plain))
            System.out.print("Test OKn");
        else
            System.out.print("Test Failed. Result was "+Util.toHEX(result)+"n");
        System.out.println();
    }




    public static String static_byteArrayToString(byte[] data) {
        String res = "";
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<data.length; i++) {
            int n = (int) data[i];
            if(n<0) n += 256;
            sb.append((char) n);
        }
        res = sb.toString();
        return res;
    }

    public static byte[] static_stringToByteArray(String s){
        byte[] temp = new byte[s.length()];
        for(int i=0;i<s.length();i++){
            temp[i] = (byte) s.charAt(i);
        }
        return temp;
    }

    public static String static_intArrayToString(int[]t){
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<t.length;i++){
            sb.append((char)t[i]);
        }
        return sb.toString();
    }

    public String _cryptAll(String data, int mode)  {
        AES aes = this;
        int rest = data.length()-(data.length() /16)*16;
        if(rest>0&mode==1) {
            for(int i=0; i<16-rest; i++)
                data = data + " ";
        }
        /*if(data.length()/16 > ((int) data.length()/16)) {
            int rest = data.length()-((int) data.length()/16)*16;
            for(int i=0; i<rest; i++)
                data += " ";
        }*/
        int nParts = data.length() /16;
        byte[] res = new byte[data.length()];
        String partStr = "";
        byte[] partByte = new byte[16];
        for(int p=0; p<nParts; p++) {
            partStr = data.substring(p*16, p*16+16);
            partByte = static_stringToByteArray(partStr);
            if(mode==1) partByte = aes.encrypt(partByte);
            if(mode==2) partByte = aes.decrypt(partByte);
            for(int b=0; b<16; b++)
                res[p*16+b] = partByte[b];
        }
        return static_byteArrayToString(res);
    }

    public String Encrypt(String data) {
        return _cryptAll(data, 1);
    }
    public String Decrypt(String data) {
        return _cryptAll(data, 2);
    }

    public void setKey(String key) {
        //System.out.println("CRYPT KEY IS "+key);
        setKey(static_stringToByteArray(key));
    }
}

class Util {

//......................................................................
// utility conversions between byte, short and int arrays

public static byte[] short2byte (short[] sa) {
        int length = sa.length;
        byte[] ba = new byte[length * 2];
        for (int i = 0, j = 0, k; i < length; ) {
        k = sa[i++];
        ba[j++] = (byte)((k >>> 8) & 0xFF);
        ba[j++] = (byte)( k        & 0xFF);
        }
        return (ba);
        }

//......................................................................
public static short[] byte2short (byte[] ba) {
        int length = ba.length;
        short[] sa = new short[length / 2];
        for (int i = 0, j = 0; j < length / 2; ) {
        sa[j++] = (short)(((ba[i++] & 0xFF) <<  8) |
        ((ba[i++] & 0xFF)      ));
        }
        return (sa);
        }

//......................................................................
public static byte[] int2byte (int[] ia) {
        int length = ia.length;
        byte[] ba = new byte[length * 4];
        for (int i = 0, j = 0, k; i < length; ) {
        k = ia[i++];
        ba[j++] = (byte)((k >>>24) & 0xFF);
        ba[j++] = (byte)((k >>>16) & 0xFF);
        ba[j++] = (byte)((k >>> 8) & 0xFF);
        ba[j++] = (byte)( k        & 0xFF);
        }
        return (ba);
        }

//......................................................................
public static int[] byte2int (byte[] ba) {
        int length = ba.length;
        int[] ia = new int[length / 4];
        for (int i = 0, j = 0; j < length / 4; ) {
        ia[j++] = (((ba[i++] & 0xFF) << 24) |
        ((ba[i++] & 0xFF) << 16) |
        ((ba[i++] & 0xFF) <<  8) |
        ((ba[i++] & 0xFF)      ));
        }
        return (ia);
        }

//......................................................................
// utility methods (adapted from cryptix.util.core.Hex class)

/** array mapping hex value (0-15) to corresponding hex digit (0-9a-f). */
public static final char[] HEX_DIGITS = {
        '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
        };

/**
 * utility method to convert a byte array to a hexadecimal string.
 * <p>
 * Each byte of the input array is converted to 2 hex symbols,
 * using the HEX_DIGITS array for the mapping, with spaces after each pair.
 * @param ba array of bytes to be converted into hex
 * @return hex representation of byte array
 */
public static String toHEX (byte[] ba) {
        int length = ba.length;
        char[] buf = new char[length * 3];
        for (int i = 0, j = 0, k; i < length; ) {
        k = ba[i++];
        buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ k        & 0x0F];
        buf[j++] = ' ';
        }
        return new String(buf);
        }

/**
 * utility method to convert a short array to a hexadecimal string.
 * <p>
 * Each word of the input array is converted to 4 hex symbols,
 * using the HEX_DIGITS array for the mapping, with spaces after every 4.
 * @param ia array of shorts to be converted into hex
 * @return hex representation of short array
 */
public static String toHEX (short[] ia) {
        int length = ia.length;
        char[] buf = new char[length * 5];
        for (int i = 0, j = 0, k; i < length; ) {
        k = ia[i++];
        buf[j++] = HEX_DIGITS[(k >>>12) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 8) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ k        & 0x0F];
        buf[j++] = ' ';
        }
        return new String(buf);
        }

/**
 * utility method to convert an int array to a hexadecimal string.
 * <p>
 * Each word of the input array is converted to 8 hex symbols,
 * using the HEX_DIGITS array for the mapping, with spaces after every 4.
 * @param ia array of ints to be converted into hex
 * @return hex representation of int array
 */
public static String toHEX (int[] ia) {
        int length = ia.length;
        char[] buf = new char[length * 10];
        for (int i = 0, j = 0, k; i < length; ) {
        k = ia[i++];
        buf[j++] = HEX_DIGITS[(k >>>28) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>>24) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>>20) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>>16) & 0x0F];
        buf[j++] = ' ';
        buf[j++] = HEX_DIGITS[(k >>>12) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 8) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ k        & 0x0F];
        buf[j++] = ' ';
        }
        return new String(buf);
        }

/**
 * utility method to convert a byte to a hexadecimal string.
 * <p>
 * the byte is converted to 2 hex symbols,
 * using the HEX_DIGITS array for the mapping.
 * @param b byte to be converted into hex
 * @return hex representation of byte
 */
public static String toHEX1 (byte b) {
        char[] buf = new char[2];
        int j = 0;
        buf[j++] = HEX_DIGITS[(b >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ b        & 0x0F];
        return new String(buf);
        }

/**
 * utility method to convert a byte array to a hexadecimal string.
 * <p>
 * Each byte of the input array is converted to 2 hex symbols,
 * using the HEX_DIGITS array for the mapping.
 * @param ba array of bytes to be converted into hex
 * @return hex representation of byte array
 */
public static String toHEX1 (byte[] ba) {
        int length = ba.length;
        char[] buf = new char[length * 2];
        for (int i = 0, j = 0, k; i < length; ) {
        k = ba[i++];
        buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ k        & 0x0F];
        }
        return new String(buf);
        }

/**
 * utility method to convert a short array to a hexadecimal string.
 * <p>
 * Each word of the input array is converted to 4 hex symbols,
 * using the HEX_DIGITS array for the mapping.
 * @param ia array of shorts to be converted into hex
 * @return hex representation of short array
 */
public static String toHEX1 (short[] ia) {
        int length = ia.length;
        char[] buf = new char[length * 4];
        for (int i = 0, j = 0, k; i < length; ) {
        k = ia[i++];
        buf[j++] = HEX_DIGITS[(k >>>12) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 8) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ k        & 0x0F];
        }
        return new String(buf);
        }

/**
 * utility method to convert an int to a hexadecimal string.
 * <p>
 * the int is converted to 8 hex symbols,
 * using the HEX_DIGITS array for the mapping.
 * @param i int to be converted into hex
 * @return hex representation of int
 */
public static String toHEX1 (int i) {
        char[] buf = new char[8];
        int j = 0;
        buf[j++] = HEX_DIGITS[(i >>>28) & 0x0F];
        buf[j++] = HEX_DIGITS[(i >>>24) & 0x0F];
        buf[j++] = HEX_DIGITS[(i >>>20) & 0x0F];
        buf[j++] = HEX_DIGITS[(i >>>16) & 0x0F];
        buf[j++] = HEX_DIGITS[(i >>>12) & 0x0F];
        buf[j++] = HEX_DIGITS[(i >>> 8) & 0x0F];
        buf[j++] = HEX_DIGITS[(i >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ i        & 0x0F];
        return new String(buf);
        }

/**
 * utility method to convert an int array to a hexadecimal string.
 * <p>
 * Each word of the input array is converted to 8 hex symbols,
 * using the HEX_DIGITS array for the mapping.
 * @param ia array of ints to be converted into hex
 * @return hex representation of int array
 */
public static String toHEX1 (int[] ia) {
        int length = ia.length;
        char[] buf = new char[length * 8];
        for (int i = 0, j = 0, k; i < length; ) {
        k = ia[i++];
        buf[j++] = HEX_DIGITS[(k >>>28) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>>24) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>>20) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>>16) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>>12) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 8) & 0x0F];
        buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
        buf[j++] = HEX_DIGITS[ k        & 0x0F];
        }
        return new String(buf);
        }


//......................................................................
/**
 * Returns a byte array from a string of hexadecimal digits.
 *
 * @param hex string of hex characters
 * @return byte array of binary data corresponding to hex string input
 */
public static byte[] hex2byte(String hex) {
        int len = hex.length();
        byte[] buf = new byte[((len + 1) / 2)];

        int i = 0, j = 0;
        if ((len % 2) == 1)
        buf[j++] = (byte) hexDigit(hex.charAt(i++));

        while (i < len) {
        buf[j++] = (byte) ((hexDigit(hex.charAt(i++)) << 4) |
        hexDigit(hex.charAt(i++)));
        }
        return buf;
        }

//......................................................................
/**
 * Returns true if the string consists ONLY of valid hex characters
 *
 * @param hex string of hex characters
 * @return true if a valid hex string
 */
public static boolean isHex(String hex) {
        int len = hex.length();
        int i = 0;
        char ch;

        while (i < len) {
        ch = hex.charAt(i++);
        if (! ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') ||
        (ch >= 'a' && ch <= 'f'))) return false;
        }
        return true;
        }

//......................................................................
/**
 * Returns the number from 0 to 15 corresponding to the hex digit <i>ch</i>.
 *
 * @param ch hex digit character (must be 0-9A-Fa-f)
 * @return   numeric equivalent of hex digit (0-15)
 */
public static int hexDigit(char ch) {
        if (ch >= '0' && ch <= '9')
        return ch - '0';
        if (ch >= 'A' && ch <= 'F')
        return ch - 'A' + 10;
        if (ch >= 'a' && ch <= 'f')
        return ch - 'a' + 10;

        return(0);	// any other char is treated as 0
        }

        }
