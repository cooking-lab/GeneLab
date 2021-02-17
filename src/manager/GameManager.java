package manager;

import com.google.gson.GsonBuilder;
import com.mongodb.util.JSON;

import character.Character;
import character.CharacterChain;
import coin.BlockChain;

public class GameManager {

	private static DatabaseManager DM;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// db mavager 호출		
		GameManager gm = new GameManager();
//		gm.makeCharacter("111101001100010011000011111111111111111111111100011111111111111111111111111");
//		gm.makeCharacter("111001001110011011100111110000111100001111000000111110000111100001111000010");
//		gm.makeCharacter("101101010000100100001010000000100000001000000001010000000100000001000000001");
//		gm.makeCharacter("101001010010110100101111000000110000001100000010011000000110000001100000011");
//		gm.makeCharacter("110001010100111101010011100000111000001110000001111100000111000001110000001");
//		gm.makeCharacter("110101001100110100110111111000111110001111100011111111000111110001111100011");
//		gm.makeCharacter("100001010000011011011011111100111111001111110011011111100111111001111110000");
//		gm.makeCharacter("100101010010100011111100011111000011110000011010111110000000111110000011110");
//		doBreeding("000003f28daeaaf1b594a4101fd44292a6b56d3d2349808f5175fe0ae9fe9212","000002f604fef337e4d0300a47c2ee4f35be2e8d123142fad84e268e9dfcf25f");
	}
	
	public void init() {
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
	
	public String doBreeding(String mamaId, String papaId) {
		init();
		DM.loadChain();
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
