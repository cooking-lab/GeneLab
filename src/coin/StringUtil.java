package coin;

import com.google.gson.GsonBuilder;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class StringUtil {
    //Applies Sha256 to a string and returns the result.
    public static String applySha256(String input){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            //Applies sha256 to our input,
            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Applies ECDSA Signature and returns the result ( as bytes ).
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    //Verifies a String signature
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Short hand helper to turn Object into a json string
    public static String getJson(Object o) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(o);
    }

    // Returns difficulty string target, to compare to hash. eg difficulty of 5 will return "00000"
    public static String getDificultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    public static byte[] getKeyFromString(String string) {
        return Base64.getDecoder().decode(string);
    }
    
    public static PublicKey getPublicKeyFromString(String string) {
    	PublicKey publicKey;
    	try {
    		byte[] pubDecoded = StringUtil.getKeyFromString(string);
        	X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubDecoded);
        	KeyFactory pubFactory = KeyFactory.getInstance("ECDSA", "BC");
        	publicKey = pubFactory.generatePublic(pubSpec);
    	}
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    	return publicKey;
    }
    
    public static PrivateKey getPrivateKeyFromString(String string) {
    	PrivateKey privateKey;
    	try {            
    		byte[] priDecoded = StringUtil.getKeyFromString(string);
            PKCS8EncodedKeySpec priSpec = new PKCS8EncodedKeySpec(priDecoded);
            KeyFactory priFactory = KeyFactory.getInstance("ECDSA", "BC");
            privateKey = priFactory.generatePrivate(priSpec);
    	}
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    	return privateKey;
    }
    

    // 머클트리의 루트를 찾는 함수
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i+=2) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }


}
