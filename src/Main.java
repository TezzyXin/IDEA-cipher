import java.math.BigInteger;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        String plainText = "BDAN2021";
        String key = randomKey();
        System.out.println("\nplaintext: " + plainText);
        System.out.println("plaintext: " + toBinary(plainText));
        String[] encryptionSubkeys = createEncryptionSubkeys(toBinary(key));
        System.out.println("\nkey:       " + key);
        System.out.println("key:       " + toBinary(key));
        String cipher = idea_encryption(toBinary(plainText), encryptionSubkeys);
        System.out.println("\ncipher:    " + cipher);
    }

    public static String mul_inv(String x) {
        BigInteger number = new BigInteger(x, 2);
        String modulo = Long.toString((long) (Math.pow(2, 16) + 1));
        BigInteger moduloBI = new BigInteger(modulo);
        BigInteger inverse = number.modInverse(moduloBI);
        return String.format("%16s", Long.toBinaryString(inverse.longValue())).replaceAll(" ", "0");
    }

    public static String add_inv(String x) {
        long n1 = Long.parseLong(x, 2);
        long k = (long) Math.pow(2, 16);
        if (n1 > k)
            n1 = n1 % k;
        n1 = k - n1;

        String ans = Long.toBinaryString(n1);
        if (ans.length() < 16)
            for (int i = ans.length(); i < 16; i++)
                ans = "0" + ans;
        return ans;

    }

    public static String mul_mod(String a, String b) {
        long tempA = Long.parseLong(a, 2);
        long tempB = Long.parseLong(b, 2);
        long output = Math.floorMod((tempA * tempB), (long) (65537));
        return String.format("%16s", Long.toBinaryString(output)).replaceAll(" ", "0");
    }


    public static String add_mod(String a, String b) {
        long tempA = Long.parseLong(a, 2);
        long tempB = Long.parseLong(b, 2);
        long output = Math.floorMod(tempA + tempB, (long) (Math.pow(2, 16)));
        return String.format("%16s", Long.toBinaryString(output)).replaceAll(" ", "0");
    }

    public static String xor_bits(String a, String b) {
        String ans = "";
        int n = a.length();

        for (int i = 0; i < n; i++) {
            if (a.charAt(i) == b.charAt(i))
                ans += "0";
            else
                ans += "1";
        }
        return ans;

    }


    public static String[] createSubBlock(String text) {

        String[] blocks = new String[4];

        blocks[0] = text.substring(0, 16);
        blocks[1] = text.substring(16, 32);
        blocks[2] = text.substring(32, 48);
        blocks[3] = text.substring(48, 64);
        return blocks;
    }

    public static String idea_encryption(String plaintext, String[] subkeys) {
        String[] blocks = createSubBlock(plaintext);

        for (int i = 0; i < 8; i++) {

            String step1 = mul_mod(blocks[0], subkeys[(6 * i)]);
            String step2 = add_mod(blocks[1], subkeys[1 + (6 * i)]);
            String step3 = add_mod(blocks[2], subkeys[2 + (6 * i)]);
            String step4 = mul_mod(blocks[3], subkeys[3 + (6 * i)]);

            String step5 = xor_bits(step1, step3);
            String step6 = xor_bits(step2, step4);

            String step7 = mul_mod(step5, subkeys[4 + (6 * i)]);
            String step8 = add_mod(step6, step7);

            String step9 = mul_mod(step8, subkeys[5 + (6 * i)]);
            String step10 = add_mod(step7, step9);

            String step11 = xor_bits(step1, step9);
            String step12 = xor_bits(step3, step9);
            String step13 = xor_bits(step2, step10);
            String step14 = xor_bits(step4, step10);

            blocks[0] = step11;
            blocks[1] = step12;
            blocks[2] = step13;
            blocks[3] = step14;
        }

        String C1 = mul_mod(blocks[0], subkeys[48]);
        String C2 = add_mod(blocks[1], subkeys[49]);
        String C3 = add_mod(blocks[2], subkeys[50]);
        String C4 = mul_mod(blocks[3], subkeys[51]);


        return C1 + C2 + C3 + C4;
    }


    public static String shiftKey(String key) {
        return key.substring(25) + key.substring(0, 25);
    }

    public static String[] createEncryptionSubkeys(String key) {
        String[] subkeys = new String[52];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                subkeys[j + i * 8] = key.substring(j * 16, (j + 1) * 16);
            }
            key = shiftKey(key);
        }
        subkeys[48] = key.substring(0, 16);
        subkeys[49] = key.substring(16, 32);
        subkeys[50] = key.substring(32, 48);
        subkeys[51] = key.substring(48, 64);
        return subkeys;
    }
    public static String[] createDecryptionSubkeys(String[] subkeys) {
        String[] decryptionSubkeys = new String[52];
        decryptionSubkeys[0] = mul_inv(subkeys[48]);
        decryptionSubkeys[1] = add_inv(subkeys[49]);
        decryptionSubkeys[2] = add_inv(subkeys[50]);
        decryptionSubkeys[3] = mul_inv(subkeys[51]);
        decryptionSubkeys[4] = subkeys[46];
        decryptionSubkeys[5] = subkeys[47];


        for (int i = 1; i < 8; i++) {
            decryptionSubkeys[i * 6] = mul_inv(subkeys[(8 - i) * 6]);
            decryptionSubkeys[(i * 6) + 1] = add_inv(subkeys[((8 - i) * 6) + 2]);
            decryptionSubkeys[(i * 6) + 2] = add_inv(subkeys[((8 - i) * 6) + 1]);
            decryptionSubkeys[(i * 6) + 3] = mul_inv(subkeys[((8 - i) * 6) + 3]);
            decryptionSubkeys[(i * 6) + 4] = subkeys[((8 - i) * 6) - 2];
            decryptionSubkeys[(i * 6) + 5] = subkeys[((8 - i) * 6) - 1];
        }

        decryptionSubkeys[48] = mul_inv(subkeys[0]);
        decryptionSubkeys[49] = add_inv(subkeys[1]);
        decryptionSubkeys[50] = add_inv(subkeys[2]);
        decryptionSubkeys[51] = mul_inv(subkeys[3]);

        return decryptionSubkeys;
    }



    public static String toBinary(String input) {

        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();
        for (char aChar : chars) {
            result.append(
                    String.format("%8s", Integer.toBinaryString(aChar)).replaceAll(" ", "0")
            );
        }
        return result.toString();

    }

    public static String randomKey() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk" + "lmnopqrstuvwxyz!@#$%&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }


}
