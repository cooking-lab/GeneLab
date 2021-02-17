package manager;

import com.google.gson.GsonBuilder;

import character.CharacterChain;
import coin.BlockChain;

public class GameManager {

	private static DatabaseManager DM;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// db manager 호출
		init();
		//DM.test();
		//System.out.println(getCharacter("00000616bff9e9499044b283bc035fda0e03fadcdde3ebcdc827f71f5fd2329a"));
	}
	
	public static void init() {
		//BlockChain.init(); // BC 모듈 활성화
		DM = new DatabaseManager();
	}
	
	public static String makeCharacter(String DNA) {
		init();
		DM.loadCharacterChain();
		String newCharacterJson = CharacterChain.makeCharacter(DNA);
		DM.insertCharacterChain();
		if(DM.hasData)
			DM.deleteCharacterChain();
		return newCharacterJson;
	}
	
	public static String doBreeding(String mamaId, String papaId) {
		init();
		DM.loadCharacterChain();
		String babyJson = CharacterChain.breeding(mamaId, papaId);
		DM.insertCharacterChain();		
		if(DM.hasData)
			DM.deleteCharacterChain();
		return babyJson;
	}
	
	public static String getCharacter(String id) {
		init();
		DM.loadCharacterChain();	
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.findCharacter.get(id));
		return json;
	}
}
