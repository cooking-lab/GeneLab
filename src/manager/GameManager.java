package manager;

import java.util.HashMap;
import java.util.Random;

import org.json.JSONObject;

import com.google.gson.GsonBuilder;
import com.mongodb.util.JSON;

import character.Character;
import character.CharacterChain;
import coin.Block;
import coin.BlockChain;
import coin.Player;
import gene.geneScience;

public class GameManager {

	private static DatabaseManager DM;
	private static GameManager GM;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		// db manager 호출
//		init();
//		GM.sellCharacter("hyunny", "yoonoh123", 200.0f, "17963e11ac3");
//		GM.makeCoinPool();
//		DM.signUpAdmin("YumManager", "YumBarkingAtTheMoon", "Musk", "We Can go to Mars", true);
//		DM.loadTransactionChain();
		
//		GM.signUp("yoonoh123", "yoonoh", "yoonoh");
//		GM.signUp("hyunny", "hyunny", "hyunny");		
		
//		Player p1 = DM.findPlayer("t1"); // DB에서 load
//		Player p2 = DM.findPlayer("t4");
		
//		Block temp = DM.sendCoin("adminId", p1.id, 300);
//		DM.addTransaction(temp);
//		p1.getBalance();
//		DM.updatePlayerCoin(p1);
		
//		BlockChain.sendCoin(p1, p2, 500);
//		DM.insertTransactionChain();

//		System.out.println(p1.getBalance());
//		System.out.println(p2.getBalance());
		// status 200
		
		// status 504 : 성별 같음
		//doBreeding("0000088c108ad5762f360c9fef58422ee47fc3e045d649cc7e1909e1609005fe","00000544f4743855726d27179184e1b3bbb9ded18db72ac526da6c4dac9d72f0");
		
		// status 504 : 종족 다름
		
		// status 505 : 근친
	}
	
	public void makeCoinPool() {
		BlockChain.initialSetting();
		DM.insertPlayer(BlockChain.admin);
		DM.insertTransactionChain();
	}
	
	public static void init() {
		BlockChain.onBC();
//		BlockChain.init();
		DM = new DatabaseManager("Game", "ChainList");
		GM = new GameManager();
	}
	
//	public static String testMakeCharacter(String DNA) {
//		init();
//		DM.loadCharacterChain();
//		Character newCharacter = CharacterChain.makeCharacter(DNA);
//		DM.insertCharacterChain();
//		if(DM.hasData)
//			DM.deleteCharacterChain();
//		return newCharacterJson;
//	}
	
	public boolean sendCharacter(String from, String to, String registerId) {
		init();
		
		DM.loadCharacterChain();
		DM.loadTransactionChain();
		
		Player seller = DM.findPlayer(from);
		Player buyer = DM.findPlayer(to);		
		
		// 캐릭터 역전송
		DM.sendCharacterToBuyer(seller, buyer, registerId);
		
		return true;
	}
		
	public boolean sellCharacter(String from, String to, float price, String registerId) {
		init();
		
		DM.loadCharacterChain();
		DM.loadTransactionChain();
		
		Player seller = DM.findPlayer(from);
		Player buyer = DM.findPlayer(to);
		
		// 1. 트랜잭션 발생 (코인 전송)
		Block temp = BlockChain.sendCoin(seller, buyer, price);		
		DM.addTransaction(temp);
		
		// 2. 체인 내 output 기반 금액 갱신
		seller.getBalance();
		buyer.getBalance();
		
		// 3. coin 갱신
        DM.updatePlayerCoin(seller);
        DM.updatePlayerCoin(buyer);
		
		// 4. 캐릭터 전송 (USER DB + CharacterChain Map 수정)
		DM.sendCharacterToBuyer(seller, buyer, registerId);
		
		return true;
	}
	
	public void signUp(String id, String password, String nickname) {
		init();
		DM.loadTransactionChain();		
		
		DM.signUp(id, password, nickname);
		
		Player newPlayer = DM.findPlayer(id);
        Player admin = DM.findPlayer("adminId");
        
        // 초기 생성 고객에게 500 코인 배정
        Block temp = BlockChain.sendCoin(admin, newPlayer, 500f);
                
        DM.addTransaction(temp);
        
        admin.getBalance();
        newPlayer.getBalance();
        
        DM.updatePlayerCoin(newPlayer);
        DM.updatePlayerCoin(admin);
        
        GM.makeCharacter(id);
        
		System.out.println("GOOD");
	}
	
	// 유저에게 캐릭터 5개 생성
	public void makeCharacter(String playerId) {
		init();
		
		DM.loadCharacterChain();
		Player p = DM.findPlayer(playerId);
		
		for(int i = 0; i < 5; i++) {
			long seed = System.currentTimeMillis(); // 1970년 1월 1일부터 현재까지 타임스템프를 가져옵니다.
			Random random = new Random(seed);
			geneScience gene = new geneScience();
			String[] species = { "100", "010", "001" };

			String DNA = gene.makeGene(species[random.nextInt(3)]);
		
			Character newCharacter = CharacterChain.makeCharacter(playerId, DNA);
		
			if(DM.dbHasData) DM.addCharacterChain(newCharacter);
			else DM.insertCharacterChain();
			String newCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(newCharacter);
		
			// modify Player DB
			p.setCharacter(newCharacter);
			DM.setCharacterToPlayer(p, newCharacter);
		
			// insert newCharacter toys DB
			DM.addNewCharacter(newCharacter);
			System.out.println(newCharacterString);
		}		
	}	

	public String doBreeding(String playerId, String mamaId, String papaId) {
		init();
		DM.loadCharacterChain();
		DM.loadTransactionChain();
		
		System.out.println("****************************************");
		// 교배 가능 여부 판단
		// 성별, 종족, 근친 여부
		// 교배 ㅇ안됨 + 이유
		geneScience gene = new geneScience();
		String ret = gene.checkBreedingAvailable(mamaId, papaId);
		JSONObject response = new JSONObject(ret);
		JSONObject response2 = (JSONObject) response.get("map");
		if(response2.getInt("status") != 200) {
			return new GsonBuilder().setPrettyPrinting().create().toJson(response2);
		}
		
		Character baby = CharacterChain.breeding(mamaId, papaId);
		baby._ownerId = playerId;
		if(DM.dbHasData) DM.addCharacterChain(baby);
		else DM.insertCharacterChain();
		DM.addNewCharacter(baby);
		
		// transaction 추가하기 (user가 admin에게 send : 수수료)
		Player admin = DM.findPlayer("adminId");
		Player p = DM.findPlayer(playerId);
		
		// update admin's info
		Block block = DM.sendCoin(playerId, "adminId", 0.1f);
		if(block != null) DM.addTransaction(block);
		else return null;
		admin.getBalance();
		DM.updatePlayerCoin(admin);
		
		// update Player's info
		p.characterList.add(baby);
		DM.setCharacterToPlayer(p, baby);
		p.getBalance();
		DM.updatePlayerCoin(p);

		response2.put("baby", baby);		
		
		return new GsonBuilder().setPrettyPrinting().create().toJson(response2);
	}
	
	public String getCharacter(String id) {
		init();
		if(CharacterChain.blockchain.size() == 0) DM.loadCharacterChain();
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.findCharacter.get(id));
		return json;
	}
}
