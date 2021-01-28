package coin;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {

    public static ArrayList<Block> blockchain = new ArrayList<Block>(); // Block들을 저장하는 구조
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); // 거래 내역을 저장하는 구조

    public static int difficulty = 5; // 00000으로 시작하는 hash mining
    public static float minimumTransaction = 0.1f; // 거래 최소량 -> 0.1 이하는 거래불가
    public static Wallet coinpool; // 코인 최초 발행
    public static Player admin; // 관리자
    public static Transaction genesisTransaction; // 최초의 transaction (미리 제작)

    // 쓰지 않는다
    public BlockChain(){
        init();
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
    public static void sendCoin(Player from, Player to, float value){
        System.out.println("\n이전 블럭의 해쉬값 : "+ blockchain.get(blockchain.size() - 1).hash);
        Block block1 = new Block(blockchain.get(blockchain.size() - 1).hash); // 이전블록에서 잇는 구조
        System.out.println("\n"+ from.name+ "의 코인 총량 : " + from.wallet.getBalance());
        System.out.println("\n"+ from.name+ "의 지갑에서 "+to.name+" 지갑으로 "+value+"의 코인 전송 시도 중...");
        // walletA.sendFunds 함수가 transaction자체를 반환해서 한번에 등록하는 구조
        // block1.addTransaction(from.wallet.sendFunds(to.wallet.publicKey, value)); // 송금
        System.out.println("\n"+ from.name + "의 코인 총량 : " + from.wallet.getBalance());
        System.out.println("\n"+ to.name +"의 코인 총량 : " + to.wallet.getBalance());
        addBlock(block1);
        isChainValid();
    }

    // 초기 플레이어에게 500코인 셋팅
    public static void setCoinToPlayer(Player player){
        System.out.println("\n이전 블럭의 해쉬값 : "+ blockchain.get(blockchain.size() - 1).hash);
        Block block1 = new Block(blockchain.get(blockchain.size() - 1).hash); // 이전블록에서 잇는 구조
        System.out.println("\n관리자 지갑의 코인 총량 : " + admin.getBalance());
        System.out.println("\n관리자 지갑에서 플레이어 지갑으로 (500)의 코인 전송 시도 중...");
        // walletA.sendFunds 함수가 transaction자체를 반환해서 한번에 등록하는 구조
        block1.addTransaction(admin.wallet.sendFunds(player.wallet.publicKey, 500f)); // 송금
        System.out.println("\n관리자의 코인 총량 : " + admin.getBalance());
        System.out.println("\n" + player.name +"의 코인 총량 : " + player.getBalance());
        addBlock(block1);
        isChainValid();
    }

    public static void init(){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
        coinpool = new Wallet();
        admin = new Player("adminId","adminPw","admin",true); // 관리자 계정

        // 초기 트랜잭션 생성 : WalletA에 100 coin 전송
        // coinpool : 초기에 돈을 생성하는 풀
        genesisTransaction = new Transaction(coinpool.publicKey, admin.wallet.publicKey, 10000f, null);
        genesisTransaction.generateSignature(coinpool.privateKey); // genesis transaction 셋팅
        genesisTransaction.transactionId = "0"; // 초기 트랜잭션 id 설정
        genesisTransaction.outputs.add(new TransactionOutput(
                genesisTransaction.reciepient,
                genesisTransaction.value,
                genesisTransaction.transactionId)); // the Transactions Output 셋팅

        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        System.out.println("Genesis Block 생성 중...");

        Block genesis = new Block("0"); // 첫블럭의 이전블럭은 존재하지 않기에 0
        genesis.addTransaction(genesisTransaction); // 블럭에 Transaction 추가
        addBlock(genesis); // 체인에 블럭 추가
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0'); // 00000nnnnnnnnnnnnnnnn의 형태 (difficulty만큼)
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); // a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        // 해쉬를 이용한 전체 블록체인 검증 :
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            // 현재 블럭에 저장된 해쉬와 계산한 해쉬 비교 -> 값 변동성 확인
            // 기존에 저장된 해쉬는 그대로여도 내용이 변했다면 계산된 해쉬의 내용이 변함
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return false;
            }
            // 이전 블럭에 저장된 해쉬와 계산한 해쉬 비교 -> 값 변동성 확인
            // 기존에 저장된 해쉬는 그대로여도 내용이 변했다면 계산된 해쉬의 내용이 변함
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            // 앞자리 0 x difficulty 만족하지 않을경우
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            // 블록 내 트랜잭션 검증 :
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }
            }
        }
        System.out.println("Blockchain is valid");
        return true;
    }

    // 체인에 블럭추가
    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
