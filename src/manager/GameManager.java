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
		// db manager 호출
		//init();
		GameManager gm = new GameManager();
//		gm.makeCharacter("111101001100010011000011111111111111111111111100011111111111111111111111111");
//		gm.makeCharacter("111001001110011011100111110000111100001111000000111110000111100001111000010");
//		gm.makeCharacter("101101010000100100001010000000100000001000000001010000000100000001000000001");
//		gm.makeCharacter("101001010010110100101111000000110000001100000010011000000110000001100000011");
//		
//		gm.makeCharacter("110001010100111101010011100000111000001110000001111100000111000001110000001");
//		gm.makeCharacter("110101001100110100110111111000111110001111100011111111000111110001111100011");
//		gm.makeCharacter("100001010000011011011011111100111111001111110011011111100111111001111110000");
//		gm.makeCharacter("100101010010100011111100011111000011110000011010111110000000111110000011110");
//		gm.doBreeding("000002814ed3c25e4e2056fd046512727b19b78708ec4051692b0044eac19af8", "00000fa6aced28b93a4796a272330e24e83cc1815b60de410ae768454143de38");
//		gm.doBreeding("00000ad6acfb91fed2f69d574199fa8114da1f72187458fa42364dc4779f9bd3", "0000035ac0cee966bda7d32ba79d62e7b5c3063a276057e2bc54abd2ae5432be");
//		gm.doBreeding("0000089bc76f07e4b319c3371ccf229c9e740d5155d74bb639d075717777d775", "00000a9062e808a47fd9d9474b2b2d77b36a91497cf9c7309ab81c8a46ae1587");
//		gm.doBreeding("0000019aab397ea177d5dc6edc6470c3933c7929e02b319f7f6a7164e5e20539", "00000ab90dce1fa95399834e2cfe9f27b81504bbc7258a707658abb3253e3685");
		
//		doBreeding("0000050ecd51a08c98fdd98bcd4d2fe3d0300321ffbcc7b71b8f414246c0f1a0","000000ab39e587b059074719ecb398a1ebd8349e8879125e5617b619f4fc7cfb");
//		doBreeding("00000e7bb4eff71bfe92d1f7d3cd2ffc610f018dae06c74604778e1d3774ade8","000006296c68d012dc90edf4236b1897ae8f47982f36c8c1045303fef8a219b2");
		doBreeding("0000070e441563a617e7b0a4576a2ad0a31ab9a1f0cb2f2b4184fadfdf044402","000005c9c65bf0837cd806674b06fd39c992abd798491be3f66515ffdb5c718c");
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
	
	public static void doBreeding(String mamaId, String papaId) {
		init();
		DM.loadChain();
		System.out.println("****************************************");
		// 교배 가능 여부 판단
		// 성별, 종족, 근친 여부
		// 교배 ㅇ안됨 + 이유
		String ret = DM.checkBreedingAvailable(mamaId, papaId);
		
//		Character baby = CharacterChain.breeding(mamaId, papaId);
//		if(DM.dbHasData) DM.addChain(baby);
//		else DM.insertChain();
//		String babyString = new GsonBuilder().setPrettyPrinting().create().toJson(baby);
//		return babyString;

	}
	
	public String getCharacter(String id) {
		init();
		if(CharacterChain.blockchain.size() == 0) DM.loadChain();
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.findCharacter.get(id));
		return json;
	}
}
