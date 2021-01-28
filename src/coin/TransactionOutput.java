package coin;

import java.security.PublicKey;

public class TransactionOutput {
    public String id; // pub key + value + transactionId = 트랜잭션 해쉬 키
    public PublicKey reciepient; // output으로 남아있는 코인의 주인 pub key
    public float value; // 보유하고 있는 코인의 총량
    public String parentTransactionId; // 이 트랜잭션을 만든 id == ( input -> output ) Transaction Block을 만든 id

    //Constructor
    // 현재 id, 값, TransactionId 보유
    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
    }

    // output 내 남아있는 coin이력이 나의 것인지 체크
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == reciepient);
    }
}
