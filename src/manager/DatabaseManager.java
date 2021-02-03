package manager;

import java.io.IOException;
import static com.mongodb.client.model.Projections.*;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.mongodb.util.JSON;

public class DatabaseManager {
	
	private MongoClientURI geneLabDatabaseUri; // 접근
	private MongoClient mongoClient; // client
	
	DatabaseManager(){
		geneLabDatabaseUri = new MongoClientURI(
        		"mongodb://GeneLab:GeneLabPw@lab-shard-00-00.q3vtm.mongodb.net:27017,lab-shard-00-01.q3vtm.mongodb.net:27017,lab-shard-00-02.q3vtm.mongodb.net:27017/Lab?ssl=true&replicaSet=atlas-p8q81q-shard-0&authSource=admin&retryWrites=true&w=majority");
        mongoClient = new MongoClient(geneLabDatabaseUri);
	}
	
	public void uploadCharacterChain() {
		// CharacterChain		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> collection = database.getCollection("ChainList"); // get Collection
        
       if(collection.count() == 0) return;
        
		FindIterable<Document> iterDoc = collection.find().projection(fields(include("CharacterChain"), excludeId()));
	    MongoCursor<Document> dbc = iterDoc.iterator();        
	        
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    JSONObject jObject = new JSONObject(dbc.next().toJson());
	    JSONArray jArray = jObject.getJSONArray("CharacterChain");
	    //Type typeList = new TypeToken<ArrayList<Character>>(){}.getType();
	        
	    for(int i = 0; i < jArray.length(); i++) {
	        	JSONObject obj = jArray.getJSONObject(i);
	            //Character array = gson.fromJson(obj.toString(), Character.class);
	        	System.out.println(i + "번째 : " + obj);
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
	        	System.out.println(hash);
	        	System.out.println(previousHash);
	        	System.out.println(timeStamp);
	        	System.out.println(nonce);
	        	System.out.println(DNA);
	        	System.out.println(mamaId);
	        	System.out.println(papaId);
	        	System.out.println(id);
	        	System.out.println(gen);
	        	System.out.println(ownerId);
	    }
	    collection.deleteOne(new Document("ChainFilter","ChainFilter"));
	}
	
	public void insertCharacterChain() {
		// CharacterChain		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> collection = database.getCollection("ChainList"); // get Collection
        
        System.out.println(collection.count());
        //if(collection.count() != 0)
        //	updateCharacterChain();
        
        String characterChainString = new GsonBuilder().setPrettyPrinting().create().toJson(CharacterChain.blockchain);
        Object characterChainJson = JSON.parse(characterChainString);

        Document characterChainDocument = new Document("CharacterChain", characterChainJson);        
        characterChainDocument.append("ChainFilter", "ChainFilter");

        collection.insertOne(characterChainDocument);       

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
	
	public void delete() {
		
	}
	
	public void find() {
		
	}
	
	public void init() {
		
	}

	public void test() {
	               
        uploadCharacterChain();
        CharacterChain.test();        
        insertCharacterChain();
	}	
}