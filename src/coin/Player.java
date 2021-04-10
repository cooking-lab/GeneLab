package coin;

import java.util.ArrayList;

public class Player {

    public Wallet _wallet; // wallet of player
    private float _coin; // coin amount for trading (blockchain)
    public String _nickname; // nickname
    private String _id; // id for login
    private String _password; // pw for login
    private float _stone; // game money for reinforce (?)
    private String _introduction;
    private String publicKey;
    private String privateKey;
    public int _hasCharacterNum;
    public ArrayList<Character> _characterList = new ArrayList<Character>();

    private boolean _isAdmin = false;
    
    // 기존 플레이어 load
    public Player(String id, String password, String nickname, String introduction, String publicKey, String privateKey) {
        this._id = id;
        this._password = password;
        this._nickname = nickname;
        this._wallet = new Wallet(); 
        this._wallet.setPublicKeyFromString(publicKey);
        this._wallet.setPrivateKeyFromString(privateKey);
        this._stone = 0;
        this._coin = 0;
        this._introduction = introduction;
        this._hasCharacterNum = 0;
    }
    
    // 새 흘레이어
    public Player(String id, String password, String nickname, String introduction) {
        this._id = id;
        this._password = password;
        this._nickname = nickname;
        this._wallet = new Wallet(); // 지갑 생성
        this._stone = 0;
        this._coin = 0;
        this._introduction = introduction;
        this._hasCharacterNum = 0;
    }

    // 관리자 생성
    public Player(String id, String password, String nickname, String introduction, boolean isAdmin) {
        this._id = id;
        this._password = password;
        this._nickname = nickname;
        this._wallet = new Wallet(); // 지갑 생성
        this._stone = 0;
        this._coin = 0;
        this._introduction = introduction;
        this._hasCharacterNum = 0;
        this._isAdmin = isAdmin;
    }


    // 유저간 거래용 함수
    public void sendCoinTo(Player to, float value){
        System.out.println("\n이전 블럭의 해쉬값 : "+ BlockChain.blockchain.get(BlockChain.blockchain.size() - 1)._hash);
        Block block = new Block(BlockChain.blockchain.get(BlockChain.blockchain.size() - 1)._hash);
        System.out.println("\n"+ this._nickname+ "의 코인 총량 : " + this._wallet.getBalance());
        System.out.println("\n"+ this._nickname+ "의 지갑에서 "+ to._nickname+" 지갑으로 "+value+" 코인 전송 시도 중...");
        block.addTransaction(_wallet.sendFunds(to._wallet.publicKey, value)); // 송금
        BlockChain.addBlock(block);
        System.out.println("\n"+ this._nickname + "의 코인 총량 : " + this._wallet.getBalance());
        System.out.println("\n"+ to._nickname +"의 코인 총량 : " + to._wallet.getBalance());
        BlockChain.isChainValid();
    }
    
    public void setCharacter() {
    	
    }

    public float getBalance(){
        _coin = _wallet.getBalance();
        return _coin;
    }
}
