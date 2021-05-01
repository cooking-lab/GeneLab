package coin;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import character.Character;

public class Player {

    public String id; // id for login
    private String password; // pw for login
    public String nickname; // nickname
    private String introduction;
    private String publicKey;
    private String privateKey;
    public Wallet wallet; // wallet of player
    public float coin; // coin amount for trading (blockchain)
    public float stone; // game money for reinforce (?)
    public int hasCharacterNum;
    public ArrayList<Character> characterList = new ArrayList<Character>();

    private boolean isAdmin = false;
    
    // 기존 플레이어 load
    public Player(
    		String id, 
    		String password, 
    		String nickname, 
    		String introduction, 
    		String publicKey, 
    		String privateKey,
    		float coin,
    		float stone,
    		int hasCharacterNum,
    		ArrayList<Character> characterList
    		) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        PublicKey pub = StringUtil.getPublicKeyFromString(publicKey);
        PrivateKey pri = StringUtil.getPrivateKeyFromString(privateKey);
        this.wallet = new Wallet(pub, pri); 
        this.stone = stone;
        this.coin = coin;
        this.introduction = introduction;
        this.hasCharacterNum = hasCharacterNum;
        this.characterList = characterList;
    }
    
    // 새 흘레이어
    public Player(String id, String password, String nickname, String introduction) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.wallet = new Wallet(); // 지갑 생성
        this.publicKey = wallet.getStringFromPublicKey();
        this.privateKey = wallet.getStringFromPrivateKey();
        this.stone = 0;
        this.coin = 0;
        this.introduction = introduction;
        this.hasCharacterNum = 0;
    }

    // 관리자 생성
    public Player(String id, String password, String nickname, String introduction, boolean isAdmin) {
    	this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.wallet = new Wallet(); // 지갑 생성
        this.publicKey = wallet.getStringFromPublicKey();
        this.privateKey = wallet.getStringFromPrivateKey();
        this.stone = 0;
        this.coin = 0;
        this.introduction = introduction;
        this.hasCharacterNum = 0;
        this.isAdmin = isAdmin;
    }


    // 유저간 거래용 함수
    public void sendCoinTo(Player to, float value){
        System.out.println("\n이전 블럭의 해쉬값 : "+ BlockChain.blockchain.get(BlockChain.blockchain.size() - 1)._hash);
        Block block = new Block(BlockChain.blockchain.get(BlockChain.blockchain.size() - 1)._hash);
        System.out.println("\n"+ this.nickname+ "의 코인 총량 : " + this.wallet.getBalance());
        System.out.println("\n"+ this.nickname+ "의 지갑에서 "+ to.nickname+" 지갑으로 "+value+" 코인 전송 시도 중...");
        block.addTransaction(wallet.sendFunds(to.wallet.publicKey, value)); // 송금
        BlockChain.addBlock(block);
        System.out.println("\n"+ this.nickname + "의 코인 총량 : " + this.wallet.getBalance());
        System.out.println("\n"+ to.nickname +"의 코인 총량 : " + to.wallet.getBalance());
        BlockChain.isChainValid();
    }
    
    public void setCharacter(Character character) {
    	characterList.add(character);
    	hasCharacterNum = characterList.size();
    }

    public float getBalance(){
        coin = wallet.getBalance();
        return coin;
    }
}
