package manager;

import com.google.gson.GsonBuilder;

import character.CharacterChain;
import coin.BlockChain;

public class GameManager {

	private static DatabaseManager DM;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// db manager 호출
		//init();
		doBreeding("000001ca7b646f56edc5f159ba19ac94ea7fd1bd583930344476fd6a63a9eeb9", "00000fe61ce2353d74da589d94602c9292cf9bc73166d24323357210b7ef516d");	
		//DM.test();
		//System.out.println(getCharacter("00000616bff9e9499044b283bc035fda0e03fadcdde3ebcdc827f71f5fd2329a"));
	}
	
	public static void init() {
		//BlockChain.init(); // BC 모듈 활성화
		DM = new DatabaseManager();
	}
	
	public static String makeCharacter(String DNA) {
		init();
		DM.loadChain();
		String newCharacterJson = CharacterChain.makeCharacter(DNA);
		DM.insertChain();
		if(DM.hasData)
			DM.deleteChain();
		return newCharacterJson;
	}

	
	public static void doBreeding(String mamaId, String papaId) {
		init();
		
		DM.loadChain();
		// 교배 가능 여부 판단
		// 성별, 종족, 근친 여부
		// 교배 ㅇ안됨 + 이유
		System.out.println("****************************************");
		System.out.println(DM.checkBreedingAvailable(mamaId, papaId));
//		String babyJson = CharacterChain.breeding(mamaId, papaId);
//		DM.insertCharacterChain();		
//		if(DM.hasData)
//			DM.deleteCharacterChain();
//		return babyJson;
	}
	
	public static String getCharacter(String id) {
		init();
		DM.loadChain();	
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.findCharacter.get(id));
		return json;
	}
}
