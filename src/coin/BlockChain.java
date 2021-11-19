package coin;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class BlockChain {

	// BlockChain
    public static ArrayList<Block> blockchain = new ArrayList<Block>(); 
    
    // 거래 내역을 저장하는 구조
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    
    // Trasnaction을 위한 Player List Upload를 할 필요가 있을 수도 있긴 하다.
    // Chain 정보는 무조건 다 불러와야 하지만 Player는 다 불러올 필요는 없음 ㅇㅇ.
    public static ArrayList<Player> playerList = new ArrayList<Player>();

    public static int difficulty = 5; // 00R000으로 시작하는 hash mining
    public static float minimumTransaction = 0.1f; // 거래 최소량 -> 0.1 이하는 거래불가
    public static Wallet coinpool; // 코인 최초 발행
    public static Player admin; // 관리자
    public static Transaction genesisTransaction; // 최초의 transaction (미리 제작)

    public BlockChain(){
        initialSetting();
    }

    // 필요없을듯 하다
    public static int getChainSize(){
        return blockchain.size();
    }

    // 이것도 딱히
    public static Player getPlayer(){
        return admin;
    }

    // 관리자 입장에서 특정 유저에서 특정 유저로 코인 보낼 수 있도록 (관리용)
    public static Block sendCoin(Player from, Player to, float value){
        System.out.println("\nhash value of previous block : "+ blockchain.get(blockchain.size() - 1)._hash);
        Block block1 = new Block(blockchain.get(blockchain.size() - 1)._hash); // 이전블록에서 잇는 구조
        System.out.println("\n"+ from.nickname+ "'s coin amount : " + from.wallet.getBalance());
        System.out.println("\n"+ to.nickname+ "'s coin amount : " + to.wallet.getBalance());
        System.out.println("\n"+ from.nickname+ " send to "+to.nickname+"'s wallet "+value+"amount coin...");
        // walletA.sendFunds 함수가 transaction자체를 반환해서 한번에 등록하는 구조
        Transaction temp = from.wallet.sendFunds(to.wallet.publicKey, value);
        if(temp != null)
        	block1.addTransaction(temp); // 송금
        else return null;
        System.out.println("Transaction ID : " + temp.transactionId);
        System.out.println("Sender's Public Key (Encoded) : " + temp.senderHash);
        System.out.println("Receiver's Public Key (Encoded) : " + temp.reciepientHash);
        System.out.println("\n"+ from.nickname + "'s coin : " + from.wallet.getBalance());
        System.out.println("\n"+ to.nickname +"'s coin : " + to.wallet.getBalance());
        System.out.println();
        addBlock(block1);      
        
        return block1;
    }

    // 초기 플레이어에게 500코인 셋팅
    public static Block setCoinToPlayer(Player player){
        System.out.println("\nhash value of previous block : "+ blockchain.get(blockchain.size() - 1)._hash);
        Block block1 = new Block(blockchain.get(blockchain.size() - 1)._hash); // 이전블록에서 잇는 구조
        System.out.println("\n's coin amount : " + admin.getBalance());
        System.out.println("\nadmin send coin to Player (500) now...");
        // walletA.sendFunds 함수가 transaction자체를 반환해서 한번에 등록하는 구조
        Transaction temp = admin.wallet.sendFunds(player.wallet.publicKey, 500f);
        block1.addTransaction(temp); // 송금
        System.out.println("\nadmin's coin amount : " + admin.getBalance());
        System.out.println("\n" + player.nickname +"'s coin amount : " + player.getBalance());
        addBlock(block1);
        return block1;
    }
    
    public static void onBC() {    	
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

    }
    
    public static void init() {
//    	coinpool = new Wallet();
//    	String ID = "0";
//    	TransactionOutput to = new TransactionOutput(coinpool.publicKey, 1000000000f, "0");
//    	coinpool.UTXOs.put(ID, to);
//        UTXOs.put("0", coinpool.UTXOs.get(ID));
//        admin = new Player("adminId","adminPw","admin","", true); // 관리자 계정
//        // 초기 트랜잭션 생성 : WalletA에 100 coin 전송
//        // coinpool : 초기에 돈을 생성하는 풀
//        //genesisTransaction = coinpool.sendFunds(admin.wallet.publicKey, 1000000000f);
//        genesisTransaction = new Transaction(coinpool.publicKey, admin.wallet.publicKey, 1000000000f, null);
//        genesisTransaction.generateSignature(coinpool.privateKey); // genesis transaction 셋팅
//        genesisTransaction.transactionId = "0"; // 초기 트랜잭션 id 설정
//        genesisTransaction.outputs.add(new TransactionOutput(
//                genesisTransaction.reciepient,
//                genesisTransaction.value,
//                genesisTransaction.transactionId)); // the Transactions Output 셋팅
//
//        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
//        admin.getBalance();
        
        System.out.println("basic setting fin");
    }

    public static void initialSetting(){              
    	coinpool = new Wallet();
//    	String ID = "0";
//    	TransactionOutput to = new TransactionOutput(coinpool.publicKey, 1000000000f, "0");
//    	coinpool.UTXOs.put(ID, to);
//        UTXOs.put("0", coinpool.UTXOs.get(ID));
        admin = new Player("adminId","adminPw","admin","", true); // 관리자 계정
        // 초기 트랜잭션 생성 : WalletA에 100 coin 전송
        // coinpool : 초기에 돈을 생성하는 풀
        //genesisTransaction = coinpool.sendFunds(admin.wallet.publicKey, 1000000000f);
        genesisTransaction = new Transaction(coinpool.publicKey, admin.wallet.publicKey, 1000000000f, null);
        genesisTransaction.generateSignature(coinpool.privateKey); // genesis transaction 셋팅
        genesisTransaction.transactionId = "0"; // 초기 트랜잭션 id 설정
        genesisTransaction.outputs.add(new TransactionOutput(
                genesisTransaction.reciepient,
                genesisTransaction.value,
                genesisTransaction.transactionId)); // the Transactions Output 셋팅

        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        admin.getBalance();
        System.out.println("Genesis Block generation now...");

        Block genesis = new Block("0"); // 첫블럭의 이전블럭은 존재하지 않기에 0
        genesis.addTransaction(genesisTransaction); // 블럭에 Transaction 추가
        addBlock(genesis); // 체인에 블럭 추가
    }
    
    // 체인에 블럭추가
    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
        System.out.println("\"_hash\" : "+ newBlock._hash);
        System.out.println("\"_previousHash\" : "+ newBlock._previousHash);
        System.out.println("\"_merkleRoot\" : "+ newBlock._merkleRoot);
        System.out.println("\"_timeStamp\" : "+ newBlock._timeStamp);
        System.out.println("\"_nonce\" : "+ newBlock._nonce);
        isChainValid();
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0'); // 00000nnnnnnnnnnnnnnnn의 형태 (difficulty만큼)
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); // a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(blockchain.get(0)._transactions.get(0).outputs.get(0).id, 
        		blockchain.get(0)._transactions.get(0).outputs.get(0));

        // 해쉬를 이용한 전체 블록체인 검증 :
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            // 현재 블럭에 저장된 해쉬와 계산한 해쉬 비교 -> 값 변동성 확인
            // 기존에 저장된 해쉬는 그대로여도 내용이 변했다면 계산된 해쉬의 내용이 변함
            if(!currentBlock._hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return false;
            }
            // 이전 블럭에 저장된 해쉬와 계산한 해쉬 비교 -> 값 변동성 확인
            // 기존에 저장된 해쉬는 그대로여도 내용이 변했다면 계산된 해쉬의 내용이 변함
            if(!previousBlock._hash.equals(currentBlock._previousHash) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            // 앞자리 0 x difficulty 만족하지 않을경우
            if(!currentBlock._hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

//            // 블록 내 트랜잭션 검증 :
//            TransactionOutput tempOutput;
//            for(int t=0; t <currentBlock._transactions.size(); t++) {
//                Transaction currentTransaction = currentBlock._transactions.get(t);
//
//                if(!currentTransaction.verifySignature()) {
//                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
//                    return false;
//                }
//                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
//                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
//                    return false;
//                }
//
//                for(TransactionInput input: currentTransaction.inputs) {                	
//                    tempOutput = tempUTXOs.get(input.transactionOutputId);
//
//                    if(tempOutput == null) {
//                        System.out.println(i +"번째 "+ "#Referenced input on Transaction(" + t + ") is Missing");
//                        return false;
//                    }
//
//                    if(input.UTXO.value != tempOutput.value) {
//                        System.out.println(i +"번째 "+ "#Referenced input Transaction(" + t + ") value is Invalid");
//                        return false;
//                    }
//
//                    tempUTXOs.remove(input.transactionOutputId);
//                }
//
//                for(TransactionOutput output: currentTransaction.outputs) {
//                    tempUTXOs.put(output.id, output);
//                }
//
//                if(!currentTransaction.outputs.get(0).reciepient.equals(currentTransaction.reciepient)) {
//                    System.out.println(i +"번째 "+ "#Transaction(" + t + ") output reciepient is not who it should be");
//                    return false;
//                }
//                
//                if(!currentTransaction.outputs.get(1).reciepient.equals(currentTransaction.sender)) {
//                    System.out.println(i +"번째 "+ "#Transaction(" + t + ") output 'change' is not sender.");
//                    return false;
//                }
//            }
        }
        System.out.println("Blockchain is valid");
        return true;
    }
}
