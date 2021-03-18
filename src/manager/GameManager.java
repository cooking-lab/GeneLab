package manager;

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
//		gm.makeCharacter("111101001100010011000011111111111111111111111100011111111111111111111111111");
//		gm.makeCharacter("110001010100111101010011100000111000001110000001111100000111000001110000001");
//		
//		gm.makeCharacter("111001001110011011100111110000111100001111000000111110000111100001111000010");
//		gm.makeCharacter("110101001100110100110111111000111110001111100011111111000111110001111100011");
//		
//		gm.makeCharacter("101101010000100100001010000000100000001000000001010000000100000001000000001");
//		gm.makeCharacter("100001010000011011011011111100111111001111110011011111100111111001111110000");
//		
//		gm.makeCharacter("101001010010110100101111000000110000001100000010011000000110000001100000011");
//		gm.makeCharacter("100101010010100011111100011111000011110000011010111110000000111110000011110");
//	
//		doBreeding("0000019a716f5cdeedff47af6b4a40bffac6e36d14f962f63e43ef2f5a51ae1b","00000bc7f61388ecf6bec8dd363fe1e09947c2502f803a4b71210e9be870876c");
//		doBreeding("000009f44bab170c049cd8201720ae4e07455562eae606b1fc62d4a980d310f2","0000021d730b5dadc8da7666286f4a10dd26813f15f7a5433b172643d9268086");
		
		// status 200
		
		// status 504 : 성별 같음
		//doBreeding("0000088c108ad5762f360c9fef58422ee47fc3e045d649cc7e1909e1609005fe","00000544f4743855726d27179184e1b3bbb9ded18db72ac526da6c4dac9d72f0");
		
		// status 504 : 종족 다름
		
		// status 505 : 근친
		String ret = doBreeding("0000088c108ad5762f360c9fef58422ee47fc3e045d649cc7e1909e1609005fe", "000009888127d51ad0cde46da4057477b7f1c990ff923cb1a80240c2de68cd26");
		System.out.println(ret);
		//DM.test();
		//System.out.println(getCharacter("00000616bff9e9499044b283bc035fda0e03fadcdde3ebcdc827f71f5fd2329a"));
	}
	
	public static void init() {
		//BlockChain.init(); // BC 모듈 활성화
		DM = new DatabaseManager();
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
		return newCharacterString;
	}
	
	public static String doBreeding(String mamaId, String papaId) {
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
