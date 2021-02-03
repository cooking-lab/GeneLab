package manager;

import character.CharacterChain;
import coin.BlockChain;

public class GameManager {

	private static DatabaseManager DM;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// db manager 호출


	}
	
	public static void init() {
		BlockChain.init(); // BC 모듈 활성화
		DatabaseManager DM = new DatabaseManager();
	}
	
	public static String makeCharacter(String DNA) {
		init();
		DM.uploadCharacterChain();
		String newCharacterJson = CharacterChain.makeCharacter(DNA);
		DM.insertCharacterChain();
		return newCharacterJson;
	}
	
	public static String doBreeding(String mamaId, String papaId) {
		init();
		DM.uploadCharacterChain();
		String babyJson = CharacterChain.breeding(mamaId, papaId);
		DM.insertCharacterChain();
		return babyJson;
	}
}
