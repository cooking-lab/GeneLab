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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// db manager 호출
		//init();
		GameManager gm = new GameManager();
		
		// robot
		gm.makeCharacter("110101001101010011000000000000000000000000000000000");
		gm.makeCharacter("110001001111011011100100100000000000000000000000000");
		gm.makeCharacter("100101010001100100001001000000000000000000000000000");
		gm.makeCharacter("100001010011010100101101100000000000000000000000000");
		
		gm.makeCharacter("111101010101011101010010000000000000000000000000000");
		gm.makeCharacter("111001001101100011010110100000000000000000000000000");
		gm.makeCharacter("101101001111010011111011000000000000000000000000000");
		gm.makeCharacter("101001010001011100011111100000000000000000000000000");
	
//		doBreeding("00000ac9d93d8cc9a68f75714473b92876f55b4948c5cff9481cf0be6ed69dc1","000007fc85da58e279f4b911634614c3ac4d36dada2063233b13b198bffa49e9");
//		doBreeding("00000f1943cf20201ef5c9a74a0008a967a6d223f9cbd8e109e044d2589272d2","00000af89132d04b3fab56cfc07b03872e70366c7170816e40e70075d840ba79");
		
		// status 200
		
		// status 504 : 성별 같음
		//doBreeding("0000088c108ad5762f360c9fef58422ee47fc3e045d649cc7e1909e1609005fe","00000544f4743855726d27179184e1b3bbb9ded18db72ac526da6c4dac9d72f0");
		
		// status 504 : 종족 다름
		
		// status 505 : 근친
//		String ret = doBreeding("0000088c108ad5762f360c9fef58422ee47fc3e045d649cc7e1909e1609005fe", "000009888127d51ad0cde46da4057477b7f1c990ff923cb1a80240c2de68cd26");
//		System.out.println(ret);
		//DM.test();
		//System.out.println(getCharacter("00000616bff9e9499044b283bc035fda0e03fadcdde3ebcdc827f71f5fd2329a"));
		//gm.makeCharacter("100101010010100011111100011111000011110000011010111110000000111110000011110");
	}
	
	public static void init() {
		//BlockChain.init(); // BC 모듈 활성화
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
	

	public String makeCharacter(String DNA) {
		init();
		DM.loadChain();
		Character newCharacter = CharacterChain.makeCharacter(DNA);
		if(DM.dbHasData) DM.addChain(newCharacter);
		else DM.insertChain();
		String newCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(newCharacter);
		// insert newCharacter toys DB
		DM.addNewCharacter(newCharacter);
		System.out.println(newCharacterString);
		return newCharacterString;
	}
	
	public String doBreeding(String mamaId, String papaId) {
		init();
		DM.loadChain();
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
		if(DM.dbHasData) DM.addChain(baby);
		else DM.insertChain();
		DM.addNewCharacter(baby);
		
		String babyString = new GsonBuilder().setPrettyPrinting().create().toJson(baby);
		return babyString;
	}
	
	public String getCharacter(String id) {
		init();
		if(CharacterChain.blockchain.size() == 0) DM.loadChain();
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.findCharacter.get(id));
		return json;
	}
}
