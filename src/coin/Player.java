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
    public int _hasCharacterNum;
    public ArrayList<Character> _characterList = new ArrayList<Character>();

    private boolean _isAdmin = false;

    public Player(String id, String password, String nickname, String introduction) {
        this._id = id;
        this._password = password;
        this._nickname = nickname;
        this._wallet = new Wallet(true); // 지갑 생성
        this._stone = 0;
        this._coin = 0;
        this._introduction = introduction;
        this._hasCharacterNum = 0;
    }

    public Player(String id, String password, String nickname, String introduction, boolean isAdmin) {
        this._id = id;
        this._password = password;
        this._nickname = nickname;
        this._wallet = new Wallet(true); // 지갑 생성
        this._stone = 0;
        this._coin = 0;
        this._introduction = introduction;
        this._hasCharacterNum = 0;
        this._isAdmin = isAdmin;
    }


    // 유저간 거래용 함수
    public void sendCoinTo(Player to, float value){
        System.out.println("\n이전 블럭의 해쉬값 : "+ BlockChain.blockchain.get(BlockChain.blockchain.size() - 1).hash);
        Block block = new Block(BlockChain.blockchain.get(BlockChain.blockchain.size() - 1).hash);
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
