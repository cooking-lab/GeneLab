package manager;

import java.io.IOException;
import static com.mongodb.client.model.Projections.*;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import character.Character;
import character.CharacterChain;
import coin.Block;
import coin.BlockChain;
import coin.Player;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.util.JSON;


public class DatabaseManager {
	
	private static MongoClientURI geneLabDatabaseUri; // 접근
	private static MongoClient mongoClient; // client
	public boolean dbHasData = false;
	public boolean firstCall = true;
	
	DatabaseManager() {
		geneLabDatabaseUri = new MongoClientURI(
        		"mongodb://GeneLab:GeneLabPw@lab-shard-00-00.q3vtm.mongodb.net:27017,lab-shard-00-01.q3vtm.mongodb.net:27017,lab-shard-00-02.q3vtm.mongodb.net:27017/Lab?ssl=true&replicaSet=atlas-p8q81q-shard-0&authSource=admin&retryWrites=true&w=majority");
        mongoClient = new MongoClient(geneLabDatabaseUri);
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> chainListCollection = database.getCollection("ChainList"); // get Collection
        dbHasData = chainListCollection.count() != 0 ? true : false;
	}
	
	public String checkBreedingAvailable(String mamaId, String papaId) {
		JSONObject res = new JSONObject();
		// 성별 다른지
		String mamaGene = CharacterChain.findCharacter.get(mamaId)._DNA;
		String papaGene = CharacterChain.findCharacter.get(papaId)._DNA;
		
		if(mamaGene.charAt(2) == papaGene.charAt(2)) {
			res.put("status", 504);
			res.put("error", "같은 성별은 교배 대상이 아닙니다.");
			return new GsonBuilder().setPrettyPrinting().create().toJson(res);
		}
		// 같은 종족인지
		String mamaSpecies = mamaGene.substring(4,7);
		String papaSpecies = papaGene.substring(4,7);

		if(!mamaSpecies.equals(papaSpecies)){
			res.put("status", 504);
			res.put("error", "다른 종족은 교배 대상이 아닙니다.");
			return new GsonBuilder().setPrettyPrinting().create().toJson(res);
		}
		
		// 근친 인지
		JSONObject mamaInit = new JSONObject();
		mamaInit.put("depth", 0);
		mamaInit.put("mama", mamaId);
		mamaInit.put("papa", "");
		StringBuffer mInit = new StringBuffer(new GsonBuilder().setPrettyPrinting().create().toJson(mamaInit));
		mInit.replace(0, 10, "");
		mInit.replace(mInit.length()-2, mInit.length(), ",\n");
		
		
		JSONObject papaInit = new JSONObject();
		papaInit.put("depth", 0);
		papaInit.put("mama", "");
		papaInit.put("papa", papaId);
		StringBuffer pInit = new StringBuffer(new GsonBuilder().setPrettyPrinting().create().toJson(papaInit));
		pInit.replace(0, 10, "");
		pInit.replace(pInit.length()-2, pInit.length(), ",\n");
		
		JSONArray mamaArray = new JSONArray("[\n"+mInit.toString()+checkCloseFamily(mamaId, 1)+"]");
		JSONArray papaArray = new JSONArray("[\n"+pInit.toString()+checkCloseFamily(papaId, 1)+"]");

		for(int i=0; i<mamaArray.length(); i++) {
			JSONObject mamaObj = mamaArray.getJSONObject(i);
			String mamaOfMama = mamaObj.getString("mama");
			String papaOfPapa = mamaObj.getString("papa");
			for(int j=0; j<papaArray.length(); j++) {
				JSONObject papaObj = papaArray.getJSONObject(j);
				
				if(mamaOfMama.equals(papaObj.getString("mama")) || papaOfPapa.equals(papaObj.getString("papa"))) {
					// 5촌 이내 근촌
					if(mamaObj.getInt("depth") + papaObj.getInt("depth") < 6) {
						res.put("status", 505);
						res.put("error", "근친은 교배 대상이 아닙니다.");
						res.put("mom_depth", mamaObj.getInt("depth"));
						res.put("papa_depth", papaObj.getInt("depth"));
						return new GsonBuilder().setPrettyPrinting().create().toJson(res);
					}
				}
			}
			
		}
		res.put("status", 200);
		
		return new GsonBuilder().setPrettyPrinting().create().toJson(res);
	}
	
	public String checkCloseFamily(String characterId, int depth) {
		
		// gen == 0
		if(CharacterChain.findCharacter.get(characterId)._gen == 0) {
			return "";
		}
		if(depth == 6) {
			return "";
		}
		JSONObject ancestors = new JSONObject();
		ancestors.put("depth", depth); // depth : 본인 depth
		
		// 엄빠 있으면 찾아서 재귀
		String mamaId = CharacterChain.findCharacter.get(characterId)._mamaId;
		String papaId = CharacterChain.findCharacter.get(characterId)._papaId;
		if(mamaId == null) {
			return "";
		}
		ancestors.put("mama", mamaId);
		ancestors.put("papa", papaId);
		
		StringBuffer ret = new StringBuffer(new GsonBuilder().setPrettyPrinting().create().toJson(ancestors));
		ret.replace(0, 10, "");
		ret.replace(ret.length()-2, ret.length(), ",\n");
		String result = ret.toString();

		result += checkCloseFamily(mamaId, depth+1);
		result += checkCloseFamily(papaId, depth+1);
		
		return result;
	}
	
	// DB로부터 로딩 -> 알고리즘 진행 -> DB 업데이트.
	public void loadChain() {
		// CharacterChain		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> chainListCollection = database.getCollection("ChainList"); // get Collection        
       
       if(!dbHasData || CharacterChain.blockchain.size() != 0) return;
        
       		// ---------------------
    		// about CharacterChain & Map
    		// ---------------------
       
		FindIterable<Document> iterDocChain = chainListCollection.find().projection(fields(include("CharacterChain"), excludeId()));
	    MongoCursor<Document> mapCursorChain = iterDocChain.iterator();        
	        
	    JSONObject jObjectChain = new JSONObject(mapCursorChain.next().toJson());
	    JSONArray jArrayChain = jObjectChain.getJSONArray("CharacterChain");
	    //Type typeList = new TypeToken<ArrayList<Character>>(){}.getType();
	    
	    for(int i = 0; i < jArrayChain.length(); i++) {
	        	JSONObject obj = jArrayChain.getJSONObject(i);
	        	String hash = obj.getString("_hash");
	        	String previousHash = obj.getString("_previousHash");
	        	long timeStamp = obj.getJSONObject("_timeStamp").getLong("$numberLong");
	        	int nonce = obj.getInt("_nonce");
	        	String DNA = obj.getString("_DNA");
	        	String mamaId = obj.getString("_mamaId");
	        	String papaId = obj.getString("_papaId");
	        	String id = obj.getString("_id");
	        	int gen = obj.getInt("_gen");
	        	int ownerId = obj.getInt("_ownerId");
	        	Character temp = new Character(hash,previousHash,timeStamp,nonce,DNA,mamaId,papaId,id,gen,ownerId);
	        	CharacterChain.blockchain.add(temp);
	        	CharacterChain.findCharacter.put(id, temp);
	        	String[] parentsId = {mamaId, papaId};
	        	if(mamaId != "" && papaId != "")
	        		CharacterChain.parents.put(id, parentsId);
//	        	System.out.println(hash);
//	        	System.out.println(previousHash);
//	        	System.out.println(timeStamp);
//	        	System.out.println(nonce);
//	        	System.out.println(DNA);
//	        	System.out.println(mamaId);
//	        	System.out.println(papaId);
//	        	System.out.println(id);
//	        	System.out.println(gen);
//	        	System.out.println(ownerId);
	    }
	}
	
	public void addChain(Character newCharacter) {
		
		// --------------------------------------------------
		// about CharacterChain Database
		// --------------------------------------------------
		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB
        MongoCollection<Document> chainListCollection = database.getCollection("ChainList");
        
        String newCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(newCharacter);
        Object newCharacterJson = JSON.parse(newCharacterString);
        
        chainListCollection.updateOne(Filters.eq("ChainFilter","CharacterChain"),
        		Updates.addToSet("CharacterChain", newCharacterJson));
        
        // --------------------------------------------------
      	// about mapList(finding parent for breeding) 
      	// -------------------------------------------------- 
        
        // findCharacter
        MongoCollection<Document> mapListCollection = database.getCollection("MapList");        
        
        JSONObject findCharacter = new JSONObject();
        findCharacter.put("_id", newCharacter._id);
        findCharacter.put("_character", newCharacter);
        
        String findCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(findCharacter);
        Object findCharacterJson = JSON.parse(findCharacterString);
        
        mapListCollection.updateOne(Filters.eq("findCharacterFilter","findCharacterMap"),
        		Updates.addToSet("findCharacterMap.myArrayList", findCharacterJson));
        
        // parents        
        if(newCharacter._mamaId != "" && newCharacter._papaId != "") {
            JSONObject findParents = new JSONObject();
        	findParents.put("_babyid", newCharacter._id);
        	String[] Parents = {newCharacter._mamaId, newCharacter._papaId};
        	findParents.put("_parents", Parents);
        
        	String findParentsString = new GsonBuilder().setPrettyPrinting().create().toJson(findParents);
        	Object findParentsJson = JSON.parse(findParentsString);

        	mapListCollection.updateOne(Filters.eq("findParentsFilter","findParentsMap"),
        			Updates.addToSet("findParentsMap.myArrayList", findParentsJson));
        }
                
	}
	
	public void deleteChain() {
		// 필터 사용시 deleteOne을 하게되면 필터에 해당하는 가장 앞쪽 Data가 지워진다.
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> chainListCollection = database.getCollection("ChainList"); // get Collection
        
		chainListCollection.deleteOne(new Document("ChainFilter","CharacterChain"));
	    
    	// --------------------------------------------------
 		// about mapList(finding parent for breeding) 
 		// --------------------------------------------------    
    
		MongoCollection<Document> mapListCollection = database.getCollection("MapList"); // get Collection
		if(mapListCollection.count() == 0) return;
		mapListCollection.deleteOne(new Document("findCharacterFilter", "findCharacterMap"));
		mapListCollection.deleteOne(new Document("findParentsFilter", "findParentsMap"));        
	}
	
	
	public void insertChain() {
		
		// --------------------------------------------------
		// about CharacterChain Database
		// --------------------------------------------------
		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> chainListCollection = database.getCollection("ChainList"); // get Collection
                
        String characterChainString = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.blockchain);
        Object characterChainJson = JSON.parse(characterChainString);

        Document characterChainDocument = new Document("CharacterChain", characterChainJson);        
        characterChainDocument.append("ChainFilter", "CharacterChain");
        
        Date forRecord = new Date();

        chainListCollection.insertOne(characterChainDocument);
             
		// --------------------------------------------------
		// about mapList(finding parent for breeding) Database
		// --------------------------------------------------        

        MongoCollection<Document> mapListCollection = database.getCollection("MapList"); 

        // Save MapList 
        List<Document> mapLists= new ArrayList<>();        
        
        // insert findCharacter Map
        int i = 0;
    	JSONArray allCharacter = new JSONArray();
        for(Entry<String, Character> kv : CharacterChain.findCharacter.entrySet()) {
        	JSONObject inputData = new JSONObject();
        	inputData.put("_id", kv.getKey());
        	inputData.put("_character", kv.getValue());
        	allCharacter.put(i, inputData);
        	i++;
        }
        String findCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(allCharacter);
        Object findCharacterJson= JSON.parse(findCharacterString);
        Document findCharacterDoc = new Document("findCharacterMap", findCharacterJson);
        findCharacterDoc.append("findCharacterFilter", "findCharacterMap");
        mapLists.add(findCharacterDoc);
        
        // insert findParents Map
        int j = 0;
        JSONArray allParents = new JSONArray();
        for(Entry<String, String[]> kv : CharacterChain.parents.entrySet()) {
        	JSONObject inputData = new JSONObject();
        	inputData.put("_babyId", kv.getKey());
        	inputData.put("_parents", kv.getValue());
        	allParents.put(j,inputData);
        	j++;
        }
        
        String findParentsString = new GsonBuilder().setPrettyPrinting().create().toJson(allParents);
        Object findParentsJson = JSON.parse(findParentsString);
        Document findParentsDoc = new Document("findParentsMap",findParentsJson);
        findParentsDoc.append("findParentsFilter", "findParentsMap");
        mapLists.add(findParentsDoc);
                
        // save List
        mapListCollection.insertMany(mapLists);
        
        firstCall = false;

        // 큰 루틴
        // 1. DB에서 불러오기 (필터) o
        // 2. Chain 변수에 저장해 
        // 3. 새 캐릭터 만들어서 체인에 추가해
        // 4. DB에 갱신해                
	}
	
	public void insertObject(Object key, Object value) {
		
		if(key instanceof Character) {
			
			return;
		} else if (key instanceof Block) {
			
			return;
		}
		
	}
	
	public void deleteObject() {
		
	}
	
	public void findObject() {
		
	}
	
	public void initObject() {
		
	}

	public void test() {
		// update를 만들수 있으면 최고
        loadChain();
        CharacterChain.test();
        //CharacterChain.breedTest();
        insertChain();
        deleteChain();
	}	
}