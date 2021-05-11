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

		init();
		GameManager gm = new GameManager();
		//gm.signUp("t1", "t2", "t3");
		// robot
//		gm.makeCharacter("t1", "110101001101010011000000011111111000000000000000000");
//		gm.makeCharacter("t1", "110001001111011011100100100000000000000000000000000");
//		gm.makeCharacter("t1", "100101010001100100001001011000000001010000000111000");
//		gm.makeCharacter("t1", "100001010011010100101101101110000000000100010010000");
//			
//		gm.makeCharacter("t1", "111101010101011101010010000000000000000001111111100");
//		gm.makeCharacter("t1", "111001001101100011010110100000000111111110000000000");
//		gm.makeCharacter("t1", "101101001111010011111011011111111111111111111111100");
//		gm.makeCharacter("t1", "111101001101010011010110011101001100111110000000000");
		gm.makeCharacter("t1");
		
	//	gm.doBreeding("t1", "00000eb88b1e9629b1889dc4d62eadf520bb1ba93094758482ed66d8c8a1e7e8", "0000042ef68cd2309261489220c56f50dfc12a3ea9d9a3a344d29fe3508583d2");
		
		//init();
//		DM.loadTransactionChain();
//		GM.makeCharacter("t1", "101001010001011100011111100000000000000000000000000");

		
		// 회원가입
//		DM.signUpAdmin("YumManager", "YumBarkingAtTheMoon", "Musk", "We Can go to Mars", true);
//		DM.loadTransactionChain();		
//		GM.signUp("t1", "t2", "t3");
//		GM.signUp("t4", "t5", "t6");
				
		// player Test		
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
//		DM.sendCoin("t1", "t4", 500);
//		doBreeding("00000ac9d93d8cc9a68f75714473b92876f55b4948c5cff9481cf0be6ed69dc1","000007fc85da58e279f4b911634614c3ac4d36dada2063233b13b198bffa49e9");
//		doBreeding("00000f1943cf20201ef5c9a74a0008a967a6d223f9cbd8e109e044d2589272d2","00000af89132d04b3fab56cfc07b03872e70366c7170816e40e70075d840ba79");
		
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
	
	
	public void sellCharacter(String registerId) {
		
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
        
		System.out.println("GOOD");
	}
	
	public String makeCharacter(String playerId) {
		init();
		
		DM.loadCharacterChain();
		long seed = System.currentTimeMillis(); // 1970년 1월 1일부터 현재까지 타임스템프를 가져옵니다.
		Random random = new Random(seed);
		geneScience gene = new geneScience();
		String[] species = { "100", "010", "001" };
		
		String DNA = gene.makeGene(species[random.nextInt(3)]);
		Character newCharacter = CharacterChain.makeCharacter(DNA);
		newCharacter._ownerId = playerId;
		
		if(DM.dbHasData) DM.addCharacterChain(newCharacter);
		else DM.insertCharacterChain();
		String newCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(newCharacter);
		
		// modify Player DB
		Player p = DM.findPlayer(playerId);
		p.setCharacter(newCharacter);
		DM.setCharacterToPlayer(p, newCharacter);
		
		// insert newCharacter toys DB
		DM.addNewCharacter(newCharacter);
		System.out.println(newCharacterString);
		
		return newCharacterString;
	}
	

	public String doBreeding(String playerId, String mamaId, String papaId) {
		init();
		DM.loadCharacterChain();
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
		Block block = DM.sendCoin(playerId, "adminId", 0.1f);
		if(block != null) DM.addTransaction(block);
		else return null;
		DM.updatePlayerCoin(admin);
		
		// update Player's info
		Player p = DM.findPlayer(playerId);
		p.characterList.add(baby);
		DM.setCharacterToPlayer(p, baby);

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
