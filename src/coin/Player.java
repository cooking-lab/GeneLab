package coin;

public class Player {

    public Wallet wallet; // wallet of player
    private float coin; // coin amount for trading (blockchain)
    public String name; // nickname
    private String id; // id for login
    private String pw; // pw for login
    private float stone; // game money for reinforce (?)
    public int hasCharacterNum;

    private boolean _isAdmin = false;

    public Player(String id, String pw, String name) {
        this.id = id;
        this.pw = pw;
        this.name = name;
        this.wallet = new Wallet(); // 지갑 생성
        this.stone = 0;
        this.coin = 0;
        this.hasCharacterNum = 0;
    }

    public Player(String id, String pw, String name, boolean isAdmin) {
        this.id = id;
        this.pw = pw;
        this.name = name;
        this.wallet = new Wallet(); // 지갑 생성
        this.stone = 0;
        this.coin = 0;
        this.hasCharacterNum = 0;
        this._isAdmin = isAdmin;
    }


    // 유저간 거래용 함수
    public void sendCoinTo(Player to, float value){
        System.out.println("\n이전 블럭의 해쉬값 : "+ BlockChain.blockchain.get(BlockChain.blockchain.size() - 1).hash);
        Block block = new Block(BlockChain.blockchain.get(BlockChain.blockchain.size() - 1).hash);
        System.out.println("\n"+ this.name+ "의 코인 총량 : " + this.wallet.getBalance());
        System.out.println("\n"+ this.name+ "의 지갑에서 "+ to.name+" 지갑으로 "+value+"의 코인 전송 시도 중...");
        block.addTransaction(wallet.sendFunds(to.wallet.publicKey,value)); // 송금
        BlockChain.addBlock(block);
        System.out.println("\n"+ this.name + "의 코인 총량 : " + this.wallet.getBalance());
        System.out.println("\n"+ to.name +"의 코인 총량 : " + to.wallet.getBalance());
        BlockChain.isChainValid();
    }

    public float getBalance(){
        coin = wallet.getBalance();
        return coin;
    }
}
