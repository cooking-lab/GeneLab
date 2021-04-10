package coin;

public class TransactionInput {
    public String transactionOutputId; // 이전 Transaction Output Id 참조
    public TransactionOutput UTXO; // Unspent transaction output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
    
    public TransactionInput(String transactionOutputId, TransactionOutput UTXO) {
        this.transactionOutputId = transactionOutputId;
        this.UTXO = UTXO;
    }
}
