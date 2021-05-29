package coin;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    // String과 output -> 코인 보유자 & 보유량 저장하는 구조
    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    
    public Wallet(PublicKey pub, PrivateKey pri) {
    	this.publicKey = pub;
    	this.privateKey = pri;
    }
    
    public Wallet() {
    	generateKeyPair();
    } // 지갑 생성시 자동으로 KeyPair 생성
    
    public String getStringFromPublicKey() {
        String pub = StringUtil.getStringFromKey(publicKey);
    	return pub;
    }
    
    public String getStringFromPrivateKey() {
        String pri = StringUtil.getStringFromKey(privateKey);
    	return pri;
    }
    
    public void setPublicKeyFromString(String pub) {
    	try {
    		byte[] pubDecoded = StringUtil.getKeyFromString(pub);
        	X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubDecoded);
        	KeyFactory pubFactory = KeyFactory.getInstance("ECDSA");
        	System.out.println("publicKey Decoding");
        	this.publicKey = pubFactory.generatePublic(pubSpec);
    	}
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setPrivateKeyFromString(String pri) {
    	try {            
    		byte[] priDecoded = StringUtil.getKeyFromString(pri);
            PKCS8EncodedKeySpec priSpec = new PKCS8EncodedKeySpec(priDecoded);
            KeyFactory priFactory = KeyFactory.getInstance("ECDSA");
            System.out.println("privateKey Decoding");
            this.privateKey = priFactory.generatePrivate(priSpec);
    	}
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // key generator
    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("sect163k1");
//            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // generator 초기화 및 KeyPair 생성
            keyGen.initialize(ecSpec);
//            keyGen.initialize(ecSpec, random); //256
            KeyPair keyPair = keyGen.generateKeyPair();
            // KeyPair로부터 PublicKey, PrivateKey 생성
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            System.out.println("Key Generation Fin!");
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 지갑에 보유한 코인 총량 반환
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: BlockChain.UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(getStringFromPublicKey())) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id, UTXO); //add it to our list of unspent transactions.
                total += UTXO.value ;
            }
        }
        return total;
    }

    // 코인 전송
    public Transaction sendFunds(PublicKey _recipient, float value) {
        if(getBalance() < value) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;

        // Transaction 전체 탐색 -> Transaction Output의 미거래 잔량 도합 체크 = 본인이 보유하고 있는 코인 전체
        // hashMap.entrySet() -> hashmap 내 포함되어있는 모든 key value 쌍을 출력
        // ex) map.put(A,1) -> [A=1]의 형태
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
            TransactionOutput UTXO = item.getValue(); // ->UTXO에서 output 객체 반환
            total += UTXO.value; // transaction output 객체 내 들어있는 Coin 확인
            inputs.add(new TransactionInput(UTXO.id)); // Transaction Input 에 새로운 input으로 생성
            if(total > value) break; 
        }

        // 위에서 생성된 inputs들을 새 Transaction으로 생성
        // 현재 지갑 주인 -> _recipient로 전송
        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){
            // 코인을 보냈으니까 UTXOs에서 비우기
            // UTXOs 는 "거래되지 않은 Coin List를 저장하고 있는 것!"
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction; // 트랜잭션 반환
    }
}