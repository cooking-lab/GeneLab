package manager;

import java.util.HashMap;

import org.json.JSONObject;

import com.google.gson.GsonBuilder;
import com.mongodb.util.JSON;

import character.Character;
import character.CharacterChain;
import coin.BlockChain;
import gene.geneScience;

public class GameManager {

	private static DatabaseManager DM;
	private static GameManager GM;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// db manager 호출
		init();
		GM = new GameManager();		
		DM.loadTransactionChain();
		DM.sendCoin("t1", "t4", 500);
		DM.insertTransactionChain();
//		GM.signUp("t1", "t2", "t3");
//		GM.signUp("t4", "t5", "t6");
//		doBreeding("00000ac9d93d8cc9a68f75714473b92876f55b4948c5cff9481cf0be6ed69dc1","000007fc85da58e279f4b911634614c3ac4d36dada2063233b13b198bffa49e9");
//		doBreeding("00000f1943cf20201ef5c9a74a0008a967a6d223f9cbd8e109e044d2589272d2","00000af89132d04b3fab56cfc07b03872e70366c7170816e40e70075d840ba79");
		
		// status 200
		
		// status 504 : 성별 같음
		//doBreeding("0000088c108ad5762f360c9fef58422ee47fc3e045d649cc7e1909e1609005fe","00000544f4743855726d27179184e1b3bbb9ded18db72ac526da6c4dac9d72f0");
		
		// status 504 : 종족 다름
		
		// status 505 : 근친
	}
	
	public static void init() {
		BlockChain.onBC();
		BlockChain.init(); // BC 모듈 활성화
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
	
	public void sendCoin() {
		DM.loadTransactionChain();
	}
	
	public void signUp(String id, String password, String nickname) {
		// init();
		// DM.loadCharacterChain();
		DM.signUp(id, password, nickname);
		System.out.println("good");
	}

	public String makeCharacter(String DNA) {
		init();
		DM.loadCharacterChain();
		Character newCharacter = CharacterChain.makeCharacter(DNA);
		if(DM.dbHasData) DM.addCharacterChain(newCharacter);
		else DM.insertCharacterChain();
		String newCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(newCharacter);
		
		// insert newCharacter toys DB
		DM.addNewCharacter(newCharacter);
		System.out.println(newCharacterString);
		return newCharacterString;
	}
	
	public static String doBreeding(String mamaId, String papaId) {
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
		if(DM.dbHasData) DM.addCharacterChain(baby);
		else DM.insertCharacterChain();
		DM.addNewCharacter(baby);
		
		String babyString = new GsonBuilder().setPrettyPrinting().create().toJson(baby);
		return babyString;
	}
	
	public String getCharacter(String id) {
		init();
		if(CharacterChain.blockchain.size() == 0) DM.loadCharacterChain();
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.findCharacter.get(id));
		return json;
	}
}
