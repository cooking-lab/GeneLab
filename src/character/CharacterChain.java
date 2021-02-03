package character;

import com.google.gson.GsonBuilder;
import gene.geneScience;

import java.util.Arrays;
import java.util.Random;


import java.util.ArrayList;
import java.util.HashMap;

public class CharacterChain {
    public static ArrayList<Character> blockchain = new ArrayList<>();
    public static int totalCharacterNum = 0;
    public static int difficulty = 5;

    // 캐릭터 id / User Id 쌍
    // 캐릭터 id를 넣어서 이게 특정 유저의 캐릭터인지 확인할 수 있도록
    public static HashMap<String, String> characterToOwner = new HashMap<String, String>();

    // User Id에 상응하는 캐릭터 수가 몇개인지
    // User Id / user가 보유하고 있는 캐릭터 수 쌍
    public static HashMap<String, Integer> ownerCharacterCount = new HashMap<String, Integer>();

    
    // id & DNA -> 전체를 담고있는거 순서대로    
    // 특정 객체의 id를 알고싶을 땐 부/모의 id + 총 교배 횟수 X     
    // nonce == 총 교배 횟수로 값을 계속 늘림    
    // 부/모 id + 총 교배 횟수 + 본인 id + 이전 해쉬
    // 겹칠 가능성 x 
    
    // id & id[] -> 
    public static HashMap<String, String[]> parents = new HashMap<String, String[]>();

    // id & Character .
    public static HashMap<String, Character> findCharacter = new HashMap<String, Character>();
    
    // 특정 캐릭터의 id를 넣고, 그게 특정 유저의 캐릭터가 맞으면, 그 유저가 가지고 있는 캐릭터의 idx 증가 등?
    public static void test() {
        // DNA를 돌려줍니다
        // string[] x 3 -> 테스트 mom / dad x
        // key : mom0 1 2 3
        // 2n, 2n+1 참조 -> string[] -> 이거 3개를 못보냄
        // request : Character(본인 id, 본인 세대) 1세대 엄마 + 5세대 아빠 = 6세대 자식.
        // 세대 : 큰 값 + 1
        // 캐릭터의 부모 2쌍을 찾아서 DNA 추출 -> 엄마 6세대, 아빠 10세대 -> 유전 결과는 11세대
        // 조상을 봐야함
        // 몇대를 선택할건지.. 이거 그냥 확률로 먼저 정해 어떤애를 볼건지를 딱 정해
    	// 테스트 테스트 테스트 테스트 테스트 테스트 테스트 테스트 테스트 테스트 테스트 테스트 테스트
    	makeCharacter("111110011111111111111111111111111111111111111111111111111111111111111111111");
    	makeCharacter("111010011101110111011011111110111111101111111011011111110111111101111111010");
    	makeCharacter("101110011001100110010011111100111111001111110010011111100111111001111110011");
    	makeCharacter("101010010001000100000011111000111110001111100000011111000111110001111100010");
    	makeCharacter("110110011011101110110111110000111100001111000010111110000111100001111000001");
    	makeCharacter("110010010011001100100111100000111000001110000000111100000111000001110000010");
    	makeCharacter("100110010111011101101111000000110000001100000001111000000110000001100000000");
    	makeCharacter("100010001110111011101010000000100000001000000001010000000100000001000000011");
    }
    
    public static void breedTest() {
       for(int i = 0; i < 4; i++) {
    	   breeding(blockchain.get(2*i)._id,blockchain.get(2*i+1)._id);
       }
       for(int i = 4; i < 6; i++) {
    	   breeding(blockchain.get(2*i)._id,blockchain.get(2*i+1)._id);
       }
       for(int i = 6; i < 7; i++) {
    	   breeding(blockchain.get(2*i)._id,blockchain.get(2*i+1)._id);
       }
//       for(int i = 14; i < 15; i++) {
//    	   breeding(blockchain.get(2*i)._Id,blockchain.get(2*i+1)._Id);
//       }
    }
    
    public static String makeCharacter(String dna) {
    	
    	// 캐릭터 생성시 1루틴
        //ownerCharacterCount.put(userId, 1); // 1대신 user.number 등 .. 혹은 탐색해서 카운트할 수 있게        
        // 클라에서 요청 온 교배 정보의 부모도 추가해야함 parents
        //String[] breedingNow = {"결과물의 아버지", "결과물의 어머니"};
        //parents.put("결과물의 id", breedingNow);
    	totalCharacterNum++;
        Character newCharacter;
        if(blockchain.size() == 0)
        	newCharacter = new Character(dna,"0"); // 초기 캐릭터일 경우
        else 
        	newCharacter = new Character(dna, blockchain.get(blockchain.size() - 1)._hash); // 아닐경우              
        
        blockchain.add(newCharacter);
        blockchain.get(blockchain.size() - 1).generateCharacter(difficulty); // id 생성 시점 
        System.out.println(totalCharacterNum +"번째 캐릭터 생성 중...");
        findCharacter.put(blockchain.get(blockchain.size() - 1)._id, newCharacter); // 이게 겹칠 수 있음.
        System.out.println("\nBlockchain is Valid: " + isChainValid());
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\nThe Character Chain: ");
        System.out.println(blockchainJson);
        System.out.println("MakeCharacter is Finished!");
        
        return blockchainJson;
    }
    
    // 엄마, 아빠 순으로 id를 받아옴
    public static String breeding(String female, String male) {
        geneScience gene = new geneScience();
        System.out.println(findCharacter.get(female)._DNA);
        System.out.println(findCharacter.get(male)._DNA);
        String babyDna = gene.geneMix(findCharacter.get(female)._id, findCharacter.get(male)._id);
    	Character newCharacter = new Character(babyDna, blockchain.get(blockchain.size() - 1)._hash); // 새 캐릭터 생성
    	newCharacter.setParents(female, male);
    	String[] tempParent = {female, male}; // 엄마, 아빠 순    	
        blockchain.add(newCharacter); //체인에 부착
        blockchain.get(blockchain.size() - 1).generateCharacter(difficulty); // 해쉬값 생성 (블록에 붙은 시점
        totalCharacterNum++;       
        // characterToOwner.put(blockchain.get(blockchain.size() - 1).hash, userId);
        System.out.println("Breeding now...");
        findCharacter.put(blockchain.get(blockchain.size() - 1)._id, newCharacter);
        parents.put(blockchain.get(blockchain.size() - 1)._id, tempParent);
        System.out.println("\nBlockchain is Valid: " + isChainValid());
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\nThe Character Chain: ");
        System.out.println(blockchainJson);
        System.out.println("Breeding is Finished!");

        return blockchainJson;
    }

    // 이 녀석의 부모를 찾아주세요!
    // 찾고싶은 녀석의 id, male || 찾고싶은 녀석의 id, female -> 아버지, 어머니 찾기
    // 세대가 아니고 어디를 찾을건지 ex) 엄마 엄마 아빠 등
    // 0이면 엄마쪽으로 올라가고, 1이면 아빠쪽으로
    public static String findParentsDna(int[] direction, String targetId) {
        // target을 기준으로 부모를 찾읍시다
        String findDna = "";
        String findId = "";
        // direction[0]의 값은 현재의 부 or 모 
        Character tempCharacter;
        for(int i = 0; i < direction.length; i++){
            if(direction[i] != -1) {
                String[] tempParents = parents.get(targetId); // 참조
                if(tempParents == null) //  확인                	
                	break;
                System.out.println(direction[i]);
                findId = tempParents[direction[i]]; //
                tempCharacter = findCharacter.get(findId);                
                findDna = getDna(tempCharacter._id);
            }
        }
        
        return findDna;
    }
    
    public static String getDna(String ID) {
    	return findCharacter.get(ID)._DNA;
    }
   

    public static Boolean isChainValid() {
        Character currentBlock;
        Character previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0','0');

        for(int i=1; i< blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);

            if(!currentBlock._hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }

            if(!previousBlock._hash.equals(currentBlock._previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            if(!currentBlock._hash.substring(0,difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't ben mined");
                return false;
            }
        }
        return true;
    }
}
