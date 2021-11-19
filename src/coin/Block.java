package coin;

import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String _hash;
    public String _previousHash;
    public String _merkleRoot;
    public ArrayList<Transaction> _transactions = new ArrayList<Transaction>(); // 1 Block 내 여러 트랜잭션 가능
    public long _timeStamp; // 1970/01/01 이후 milliseconds (비트코인 구조 참조)
    public int _nonce; // 해쉬값 증가용

    // 생성자
    public Block(String previousHash) {
        this._previousHash = previousHash; // 이전 블럭의 해쉬값
        this._timeStamp = new Date().getTime(); // 블럭이 생성된 시간
        this._hash = calculateHash(); // 해쉬 계산
    }
    
    // for load (트랜잭션은 따로 add)
    public Block(
    		String hash, 
    		String previousHash, 
    		String merkleRoot,
    		ArrayList<Transaction> transactions,
    		long timeStamp,
    		int nonce
    		) {
    	this._hash = hash;
        this._previousHash = previousHash; 
        this._merkleRoot = merkleRoot;
        this._transactions = transactions;
        this._timeStamp = timeStamp;
        this._nonce = nonce;
    }

    // 블럭 내 요소들을 바탕으로 해쉬값 계산
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                _previousHash +
                        Long.toString(_timeStamp) +
                        Integer.toString(_nonce) +
                        _merkleRoot
        );
        return calculatedhash;
    }

    // difficulty만큼 앞자리를 0으로 배치 ->
    // (hash 자릿수) ^ (최대 자릿수 - dificulty) 보다 작은 숫자 발생할 때 까지
    // nonce++ 진행
    // 조건 달생시 mine success
    public void mineBlock(int difficulty) {
        _merkleRoot = StringUtil.getMerkleRoot(_transactions);
        String target = StringUtil.getDificultyString(difficulty);
        while(!_hash.substring(0, difficulty).equals(target)) {
            _nonce++;
            _hash = calculateHash();
        }
        System.out.println("_merkleRoot : " + _merkleRoot);
        System.out.println("Block Mined!!! : " + _hash);
    }

    // 블럭에 트랜잭션 추가
    public boolean addTransaction(Transaction transaction) {
        // Genesis 블록이 아닌 애들중에 트랜잭션이 유효하지 않으면 트랜잭션 추가 x
        // Genesis Block이면 그냥 추가
        if(transaction == null) return false;
        if((!"0".equals(_previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        _transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }
}
