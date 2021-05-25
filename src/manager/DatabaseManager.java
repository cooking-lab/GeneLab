package manager;

import java.io.IOException;
import static com.mongodb.client.model.Projections.*;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.PublicKey;
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
import coin.StringUtil;
import coin.Transaction;
import coin.TransactionInput;
import coin.TransactionOutput;

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
import org.bson.Document; 
import com.mongodb.client.FindIterable; 
import static com.mongodb.client.model.Filters.*; 
import static com.mongodb.client.model.Sorts.ascending; 
import static java.util.Arrays.asList;

import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseManager {
	
	private static MongoClientURI geneLabDatabaseUri; // 접근
	private static MongoClient mongoClient; // client
	public boolean dbHasData = false;
	public boolean dbHasTransaction = false;
	
	DatabaseManager(String dbName, String collection) {
		geneLabDatabaseUri = new MongoClientURI(
        		"mongodb://GeneLab:GeneLabPw@lab-shard-00-00.q3vtm.mongodb.net:27017,lab-shard-00-01.q3vtm.mongodb.net:27017,lab-shard-00-02.q3vtm.mongodb.net:27017/Lab?ssl=true&replicaSet=atlas-p8q81q-shard-0&authSource=admin&retryWrites=true&w=majority");
        mongoClient = new MongoClient(geneLabDatabaseUri);

        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );

        mongoLogger.setLevel(Level.SEVERE);
        
		MongoDatabase database = mongoClient.getDatabase(dbName); // get DB			   
        MongoCollection<Document> chainListCollection = database.getCollection(collection); // get Collection
        dbHasData = chainListCollection.count() != 0 ? true : false;        
        
        MongoCollection<Document> transactionChainListCollection = database.getCollection("TransactionChainList"); // get Collection
        dbHasTransaction = transactionChainListCollection.count() != 0 ? true : false;
        System.out.println("TransactionChainList 보유 여부 : " + dbHasTransaction);
	}	
	
	public void checkDBState() {
		geneLabDatabaseUri = new MongoClientURI(
        		"mongodb://GeneLab:GeneLabPw@lab-shard-00-00.q3vtm.mongodb.net:27017,lab-shard-00-01.q3vtm.mongodb.net:27017,lab-shard-00-02.q3vtm.mongodb.net:27017/Lab?ssl=true&replicaSet=atlas-p8q81q-shard-0&authSource=admin&retryWrites=true&w=majority");
        mongoClient = new MongoClient(geneLabDatabaseUri);
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB		
        MongoCollection<Document> chainListCollection = database.getCollection("ChainList"); // get Collection
        dbHasData = chainListCollection.count() != 0 ? true : false;        
        
        MongoCollection<Document> transactionChainListCollection = database.getCollection("TransactionChainList"); // get Collection
        dbHasTransaction = transactionChainListCollection.count() != 0 ? true : false;
	}
	
	// DB로부터 로딩 -> 알고리즘 진행 -> DB 업데이트.
	public void loadCharacterChain() {
		checkDBState();
		// CharacterChain		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> chainListCollection = database.getCollection("ChainList"); // get Collection        
       System.out.println("first : " + CharacterChain.blockchain.size());
       if(!dbHasData || CharacterChain.blockchain.size() != 0) return;
       System.out.println("second : " + CharacterChain.blockchain.size());
       		// ---------------------
    		// about CharacterChain & Map
    		// ---------------------
       
		FindIterable<Document> iterDocChain = chainListCollection.find().projection(fields(include("CharacterChain"), excludeId()));
	    MongoCursor<Document> mapCursorChain = iterDocChain.iterator();        
	        
	    JSONObject jObjectChain = new JSONObject(mapCursorChain.next().toJson());
	    JSONArray jArrayChain = jObjectChain.getJSONArray("CharacterChain");
	    
	    for(int i = 0; i < jArrayChain.length(); i++) {
	        	JSONObject obj = jArrayChain.getJSONObject(i);
	        	
	        	// get Character value
	        	String hash = obj.getString("_hash");
	        	String previousHash = obj.getString("_previousHash");
	        	long timeStamp = obj.getJSONObject("_timeStamp").getLong("$numberLong");
	        	int nonce = obj.getInt("_nonce");
	        	String DNA = obj.getString("_DNA");
	        	String mamaId = obj.getString("_mamaId");
	        	String papaId = obj.getString("_papaId");
	        	String id = obj.getString("_id");
	        	int gen = obj.getInt("_gen");
	        	String ownerId = obj.getString("_ownerId");
	        	
	        	// add Block
	        	Character temp = new Character(hash,previousHash,timeStamp,nonce,DNA,mamaId,papaId,id,gen,ownerId);	        	
	        	CharacterChain.blockchain.add(temp);

	        	// set Map to breeding
	        	CharacterChain.findCharacter.put(id, temp);	       	        	
	        	String[] parentsId = {mamaId, papaId};
	        	if(mamaId != "" && papaId != "")
	        		CharacterChain.parents.put(id, parentsId);
	        	
	        	// set Map character To Owner
	        	CharacterChain.characterToOwner.put(id, ownerId);
	    }
	}
	
	public String checkSpecies(String s_dna) {
		switch(s_dna) {
		case "100" : return "doll";
		case "010" : return "robot";
		case "001" : return "car";
		}
		return "";
	}
	
	public void addNewCharacter(Character newCharacter) {
		MongoDatabase database = mongoClient.getDatabase("Toy"); // get DB
        MongoCollection<Document> toyCollection = database.getCollection("toys");
        
        String newCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(newCharacter);
    
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(newCharacterString);
       
        Document doc = new Document();
        doc.append("id", element.getAsJsonObject().get("_id").getAsString());
        doc.append("species", checkSpecies((element.getAsJsonObject().get("_DNA").getAsString()).substring(4, 7)));
        doc.append("name", "testName");
        doc.append("gender", (element.getAsJsonObject().get("_DNA").getAsString()).charAt(2) == '0' ? "male" : "female");
        doc.append("generation",element.getAsJsonObject().get("_gen").getAsInt());
        doc.append("dna", element.getAsJsonObject().get("_DNA").getAsString());
        doc.append("mamaId", element.getAsJsonObject().get("_mamaId").getAsString());
        doc.append("papaId", element.getAsJsonObject().get("_papaId").getAsString());
        doc.append("market", null);
        doc.append("adventure", false);
        doc.append("ownerId", element.getAsJsonObject().get("_ownerId").getAsString());
        doc.append("cooltime", 0);

        toyCollection.insertOne(doc);
        
        System.out.println("new character insert!!");
	}
	
	public void addCharacterChain(Character newCharacter) {
		
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
        
        // ----------------------------------------------------
        // findCharacter(find Character Object using character's ID)
        // ----------------------------------------------------
        MongoCollection<Document> mapListCollection = database.getCollection("MapList");        
        
        JSONObject findCharacter = new JSONObject();
        findCharacter.put("_id", newCharacter._id);
        findCharacter.put("_character", newCharacter);
        
        String findCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(findCharacter);
        Object findCharacterJson = JSON.parse(findCharacterString);
        
        mapListCollection.updateOne(Filters.eq("findCharacterFilter","findCharacterMap"),
        		Updates.addToSet("findCharacterMap.myArrayList", findCharacterJson));
        
        // ----------------------------------------------------
        // about parents info(findParentsMap)
        // ----------------------------------------------------
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
        
        // ----------------------------------------------------
        // find Owner using character's id (characterToOwner)
        // ----------------------------------------------------
        
        JSONObject characterToOwner = new JSONObject();
        characterToOwner.put("_characterId", newCharacter._id);
        characterToOwner.put("_playerId", newCharacter._ownerId);
        
        String characterToOwnerString = new GsonBuilder().setPrettyPrinting().create().toJson(characterToOwner);
        Object characterToOwnerJson = JSON.parse(characterToOwnerString);
        
        mapListCollection.updateOne(Filters.eq("characterToOwnerFilter","characterToOwnerMap"),
        		Updates.addToSet("characterToOwnerMap.myArrayList", characterToOwnerJson));
        
	}
	
//	public void deleteChain() {
//		// 필터 사용시 deleteOne을 하게되면 필터에 해당하는 가장 앞쪽 Data가 지워진다.
//		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
//      MongoCollection<Document> chainListCollection = database.getCollection("ChainList"); // get Collection
//        
//		chainListCollection.deleteOne(new Document("ChainFilter","CharacterChain"));
//	    
//    	// --------------------------------------------------
// 		// about mapList(finding parent for breeding) 
// 		// --------------------------------------------------    
//    
//		MongoCollection<Document> mapListCollection = database.getCollection("MapList"); // get Collection
//		if(mapListCollection.count() == 0) return;
//		mapListCollection.deleteOne(new Document("findCharacterFilter", "findCharacterMap"));
//		mapListCollection.deleteOne(new Document("findParentsFilter", "findParentsMap"));        
//	}
	
	
	public void insertCharacterChain() {
		
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
        
        // insert CharacterToOwner Map
        int k = 0;
        JSONArray characterToOwnerList = new JSONArray();
        for(Entry<String, String> kv : CharacterChain.characterToOwner.entrySet()) {
        	JSONObject inputData = new JSONObject();
        	inputData.put("_characterId", kv.getKey());
        	inputData.put("_playerId", kv.getValue());
        	characterToOwnerList.put(k,inputData);
        	k++;
        }
        
        String characterToOwnerString = new GsonBuilder().setPrettyPrinting().create().toJson(characterToOwnerList);
        Object characterToOwnerJson = JSON.parse(characterToOwnerString);
        Document characterToOwnerDoc = new Document("characterToOwnerMap",characterToOwnerJson);
        characterToOwnerDoc.append("characterToOwnerFilter", "characterToOwnerMap");
        mapLists.add(characterToOwnerDoc);
                
        // save List
        mapListCollection.insertMany(mapLists); 
      
        dbHasData = true;
        // 큰 루틴
        // 1. DB에서 불러오기 (필터) o
        // 2. Chain 변수에 저장해 
        // 3. 새 캐릭터 만들어서 체인에 추가해
        // 4. DB에 갱신해                
	}
	
	// load TransactionChain & Chain's UTXOs 
	public void loadTransactionChain() {
		checkDBState();
		
	    // ---------------------
	 	// load TransactionChain
	 	// ---------------------
		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB
        MongoCollection<Document> transactionChainListCollection = database.getCollection("TransactionChainList"); // get Collection
        
		if(!dbHasTransaction || BlockChain.blockchain.size() != 0) return;
		
		FindIterable<Document> iterDocChain = transactionChainListCollection.find().projection(fields(include("TransactionChain"), excludeId()));
	    MongoCursor<Document> mapCursorChain = iterDocChain.iterator();
	    
	    JSONObject jObjectChain = new JSONObject(mapCursorChain.next().toJson());
	    JSONArray jArrayChain = jObjectChain.getJSONArray("TransactionChain");	    

	    for(int i = 0; i < jArrayChain.length(); i++) {
	        	JSONObject obj = jArrayChain.getJSONObject(i);
	        	
	        	// Block
	        	String hash = obj.getString("_hash");
	        	String previousHash = obj.getString("_previousHash");
	        	String merkleRoot = obj.getString("_merkleRoot");
	        	
	        	// Transaction
	        	JSONArray jArrayTransaction = obj.getJSONArray("_transactions");
	        	ArrayList<Transaction> transactionsLists = new ArrayList<Transaction>();
	        	for(int j = 0; j < jArrayTransaction.length(); j++) {
	        		JSONObject transObj = jArrayTransaction.getJSONObject(j);
	        		
	        		// transactionId
	        		String transactionId = transObj.getString("transactionId");

	        		// sender
	        		String senderHash = transObj.getString("senderHash");
	        		PublicKey senderKey = StringUtil.getPublicKeyFromString(senderHash);
	        		
	        		// reciepient
	        		String reciepientHash = transObj.getString("reciepientHash");
	        		PublicKey recipeintKey = StringUtil.getPublicKeyFromString(reciepientHash);
	        		
	        		// value
	        		float value = transObj.getFloat("value");
	        		
	        		// signature (sender with recipeient)
		        	JSONArray jArraySignature = transObj.getJSONArray("signature");
		        	byte[] signature = new byte[jArraySignature.length()];
		        	for(int k = 0; k < jArraySignature.length(); k++) 
		        		signature[k] = (byte) jArraySignature.getInt(k);
		        		        			        	
		        	// inputs (List) 
		        	// 여기는 반드시 TransactionInputList가 NULL일 가능성 고려해주어야함!! (추후 수정)
		            ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		        	if(i != 0) {
			        	JSONArray jArrayInput = transObj.getJSONArray("inputs");
		        	for(int k = 0; k < jArrayInput.length(); k++) {
		        		JSONObject inputObj = jArrayInput.getJSONObject(k);
		        		
		        		// id
		        		String transactionOutputId = inputObj.getString("transactionOutputId");
		        		
		        		// UTXO (TransactionOutput)
		        		JSONObject UTXOObj = inputObj.getJSONObject("UTXO");
		        		String UTXOId = UTXOObj.getString("id");
		        		
		        		// Key
		        		String reciepientHashOfOutput = UTXOObj.getString("reciepientHash");
		        		PublicKey reciepientKeyOfOutput = StringUtil.getPublicKeyFromString(reciepientHashOfOutput); 
		        		
		        		float valueOfOutput = UTXOObj.getFloat("value");
		        		String parentTrasnactionId = UTXOObj.getString("parentTransactionId");
		        		
		        		TransactionOutput UTXO = new TransactionOutput(
		        				UTXOId, 
		        				reciepientKeyOfOutput,
		        				reciepientHashOfOutput,
		        				valueOfOutput,
		        				parentTrasnactionId
		        				);
		        		
		        		// add
		        		TransactionInput inputElement = new TransactionInput(transactionOutputId, UTXO);
		        		inputs.add(inputElement);
		        	}
		        	}
		        	
		        	// outputs (List)
		        	JSONArray jArrayOutput = transObj.getJSONArray("outputs");
		            ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
		        	for(int k = 0; k < jArrayOutput.length(); k++) {
		        		JSONObject outputObj = jArrayOutput.getJSONObject(k);

		        		String outputId = outputObj.getString("id");
		        		
		        		// Key
		        		String reciepientHashOfOutput = outputObj.getString("reciepientHash");
		        		PublicKey reciepientKeyOfOutput = StringUtil.getPublicKeyFromString(reciepientHashOfOutput); 
		        		
		        		float valueOfOutput = outputObj.getFloat("value");
		        		String parentTrasnactionId = outputObj.getString("parentTransactionId");
		        		
		        		TransactionOutput UTXO = new TransactionOutput(
		        				outputId, 
		        				reciepientKeyOfOutput,
		        				reciepientHashOfOutput,
		        				valueOfOutput,
		        				parentTrasnactionId
		        				);
		        		
		        		// add
		        		outputs.add(UTXO);
		        	}
		        	
		        	// add
		        	Transaction transaction = new Transaction(
		        			transactionId,
		        			senderKey,
		        			recipeintKey,
		        			senderHash,
		        			reciepientHash,
		        			value,
		        			signature,
		        			inputs,
		        			outputs
		        			);
		        	
		        	transactionsLists.add(transaction);
	        	}
	        	
	        	// for making hash
	        	long timeStamp = obj.getJSONObject("_timeStamp").getLong("$numberLong");
	        	int nonce = obj.getInt("_nonce");
	        	Block temp = new Block(hash,previousHash,merkleRoot,transactionsLists,timeStamp,nonce);
	        	BlockChain.blockchain.add(temp);
	    }
	    	    
	    
	    // -----------------------
	 	// load BlockChain's UTXOs
	 	// -----------------------
	    
	    MongoCollection<Document> UTXOsCollection = database.getCollection("UTXOs"); // get Collection
		
		FindIterable<Document> iterUTXOs = UTXOsCollection.find().projection(fields(include("UTXOs"), excludeId()));
	    MongoCursor<Document> mapCursorUTXOs = iterUTXOs.iterator();
	    
	    JSONObject UTXOsDoc = new JSONObject(mapCursorUTXOs.next().toJson());
	    JSONObject UTXOsObject = UTXOsDoc.getJSONObject("UTXOs");
	    JSONArray UTXOsArray = UTXOsObject.getJSONArray("myArrayList");
	    for(int i = 0; i < UTXOsArray.length(); i++) {
	    	JSONObject UTXO = UTXOsArray.getJSONObject(i);
	    	
	    	JSONObject UTXOMap = UTXO.getJSONObject("map");
	    	
	    	// Key of Map (ID)
	    	String transactionIdOfUTXO = UTXOMap.getString("_transactionId");
	    	
	    	// Value of Map (transactionOutput)
	    	JSONObject transactionOutputOfUTXO = UTXOMap.getJSONObject("_transactionOutput");
	    	
	    	String outputId = transactionOutputOfUTXO.getString("id");

    		String reciepientHashOfOutput = transactionOutputOfUTXO.getString("reciepientHash");
    		PublicKey reciepientKeyOfOutput = StringUtil.getPublicKeyFromString(reciepientHashOfOutput); 
    		
    		float valueOfOutput = transactionOutputOfUTXO.getFloat("value");
    		String parentTrasnactionId = transactionOutputOfUTXO.getString("parentTransactionId");
    		
    		TransactionOutput tempUTXO = new TransactionOutput(
    				outputId, 
    				reciepientKeyOfOutput,
    				reciepientHashOfOutput,
    				valueOfOutput,
    				parentTrasnactionId
    				);
    		
    		BlockChain.UTXOs.put(transactionIdOfUTXO, tempUTXO);	    	
	    }
	    
	    System.out.println("Transaction Load Fin");
	    
	    String a = new GsonBuilder().setPrettyPrinting().create().toJson(BlockChain.blockchain);
	    System.out.println(a);
	}
	
	public void addTransaction(Block newBlock) {
		
		// --------------------------------------------------
		// about TransactionChain Database
		// --------------------------------------------------
				
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB
		MongoCollection<Document> transactionChainListCollection = database.getCollection("TransactionChainList");
		        
		String newBlockString = new GsonBuilder().setPrettyPrinting().create().toJson(newBlock);
		Object newBlockJson = JSON.parse(newBlockString);
		        
		transactionChainListCollection.updateOne(Filters.eq("ChainFilter","TransactionChain"),
		        Updates.addToSet("TransactionChain", newBlockJson));
		        
		// --------------------------------------------------
		// about UTXOs
		// -------------------------------------------------- 
		        
		// UTXO
		MongoCollection<Document> UTXOsCollection = database.getCollection("UTXOs");
						
//		for(int i = 0; i < newBlock._transactions.size(); i++) {
//			String transactionString = 
//					new GsonBuilder().setPrettyPrinting().create().toJson(newBlock._transactions.get(i));
//			System.out.println(transactionString);
//			Object transactionJson = JSON.parse(transactionString);
//			UTXOsCollection.updateOne(Filters.eq("ChainFilter", "TransactionChain"),
//					Updates.addToSet("UTXOs.myArrayList", transactionJson));			
//		}
		
		// update UTXOs
		int i = 0;
		JSONArray UTXO = new JSONArray();
		for(Entry<String, TransactionOutput> kv : BlockChain.UTXOs.entrySet()) {
			JSONObject inputData = new JSONObject();
			inputData.put("_transactionId", kv.getKey());
			inputData.put("_transactionOutput", kv.getValue());
			UTXO.put(i, inputData);
			i++;
		}
		
		String UTXOsString = new GsonBuilder().setPrettyPrinting().create().toJson(UTXO);
		Object UTXOsJson= JSON.parse(UTXOsString);

		UTXOsCollection.updateOne(Filters.eq("UTXOsFilter", "UTXOs"),
				Updates.set("UTXOs", UTXOsJson));
		
		System.out.println("UTXOs add Fin!!");
	}
	
	public void insertTransactionChain() {
		
		// --------------------------------------------------
		// about trasnactionChain Database (BlockChain)
		// --------------------------------------------------

		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
		MongoCollection<Document> transactionChainListCollection = database.getCollection("TransactionChainList"); 

		String trasnactionChainString = new GsonBuilder().setPrettyPrinting().create().toJson(BlockChain.blockchain);
		Object trasnactionChainJson = JSON.parse(trasnactionChainString);

		Document characterChainDocument = new Document("TransactionChain", trasnactionChainJson);        
		characterChainDocument.append("ChainFilter", "TransactionChain");

		transactionChainListCollection.insertOne(characterChainDocument);
		
		System.out.println("Transaction Insert Fin!!");
		
		
		// --------------------------------------------------
		// about UTXOsList (Exchange or sendcoin List ) Database
		// --------------------------------------------------        
		
		MongoCollection<Document> UTXOsCollection = database.getCollection("UTXOs"); 
				
		// Save UTXOs 
//		List<Document> UTXOsList= new ArrayList<>();        

		// insert UTXOs
		int i = 0;
		JSONArray UTXO = new JSONArray();
		for(Entry<String, TransactionOutput> kv : BlockChain.UTXOs.entrySet()) {
			JSONObject inputData = new JSONObject();
			inputData.put("_transactionId", kv.getKey());
			inputData.put("_transactionOutput", kv.getValue());
			UTXO.put(i, inputData);
			i++;
		}
		
		String UTXOsString = new GsonBuilder().setPrettyPrinting().create().toJson(UTXO);
		Object UTXOsJson= JSON.parse(UTXOsString);
		
		Document UTXOsDoc = new Document("UTXOs", UTXOsJson);
		UTXOsDoc.append("UTXOsFilter", "UTXOs");

		UTXOsCollection.insertOne(UTXOsDoc);
		
		System.out.println("UTXOs Insert Fin!!");
	}
	
	public void updatePlayerCoin(Player p) {
		// player coin 개수 업데이트
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB
        MongoCollection<Document> playersCollection = database.getCollection("players");        
        
        playersCollection.updateOne(eq("Players.id", p.id),
        			Updates.set("Players.coin", p.coin));
	}
	
	public void setCharacterToPlayer(Player p, Character newCharacter) {
		// 새 캐릭터 플레이어 할당
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB
        MongoCollection<Document> playersCollection = database.getCollection("players");        
        
        String newCharacterString = new GsonBuilder().setPrettyPrinting().create().toJson(newCharacter);
        Object newCharacterJson = JSON.parse(newCharacterString);
        
        p.hasCharacterNum = p.characterList.size();
        
        playersCollection.updateOne(eq("Players.id", p.id),
        		Updates.combine(
        				Updates.set("Players.coin", p.coin),
        				Updates.addToSet("Players.characterList", newCharacterJson),
        				Updates.set("Players.hasCharacterNum", p.hasCharacterNum)
        				));
        
        playersCollection.updateOne(
        		new Document("id", p.id), 
        		new Document("$set", 
        				new Document("cuisine", "American (New)"))
        		.append("$currentDate", new Document("lastModified", true)));
        
        System.out.println("Player Info Update Fin!!");
	}
	
	public void signUpAdmin(String id, String password, String nickname, String introduction, boolean isAdmin) {
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> playerCollection = database.getCollection("players"); // get Collection  

        Player newPlayer = new Player(id, password, nickname, "", true);

		String newPlayerString = new GsonBuilder().setPrettyPrinting().create().toJson(newPlayer);
		Object newPlayerJson = JSON.parse(newPlayerString);

		Document characterChainDocument = new Document("Players", newPlayerJson);        
		characterChainDocument.append("PlayerFilter", "player");

		playerCollection.insertOne(characterChainDocument);
		
		System.out.println("Admin SignUp Fin!!");
	}
	
	public void signUp(String id, String password, String nickname) {
		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> playerCollection = database.getCollection("players"); // get Collection  

        Player newPlayer = new Player(id, password, nickname, "");

		String newPlayerString = new GsonBuilder().setPrettyPrinting().create().toJson(newPlayer);
		Object newPlayerJson = JSON.parse(newPlayerString);

		Document characterChainDocument = new Document("Players", newPlayerJson);        
		characterChainDocument.append("PlayerFilter", "player");

		playerCollection.insertOne(characterChainDocument);
		
		System.out.println("SignUp Fin!!");
	}
	
	
	public void insertPlayer(Player p) {
		
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB			   
        MongoCollection<Document> playerCollection = database.getCollection("players"); // get Collection  

        //Player newPlayer = new Player(id, password, nickname, "");

		String newPlayerString = new GsonBuilder().setPrettyPrinting().create().toJson(p);
		Object newPlayerJson = JSON.parse(newPlayerString);

		Document characterChainDocument = new Document("Players", newPlayerJson);        
		characterChainDocument.append("ChainFilter", "player");

		playerCollection.insertOne(characterChainDocument);
		
		System.out.println("Player Insert Fin!!");
	}
	
	
	public Player findPlayer(String playerId) {		
		// findPlayer
		MongoDatabase database = mongoClient.getDatabase("Game"); // get DB
        MongoCollection<Document> playerCollection = database.getCollection("players");        
        
        Document playerDoc = playerCollection.find(
        		eq("Players.id", playerId)).
        		projection(fields(include("Players"), excludeId())).
        		first();
        
	    JSONObject jObjectPlayer = new JSONObject(playerDoc).getJSONObject("Players");
	    
	    // Object 여러개의 구조로 저장됨
	    JSONArray jArrayChain = jObjectPlayer.getJSONArray("characterList");
	    
	    // load CharacterList of Player
	    ArrayList<Character> characterList = new ArrayList<Character>();	    
	    for(int i = 0; i < jArrayChain.length(); i++) {
	        	JSONObject obj = jArrayChain.getJSONObject(i);
	        	
	        	// get Character value
	        	String hash = obj.getString("_hash");
	        	String previousHash = obj.getString("_previousHash");
	        	long timeStamp = obj.getLong("_timeStamp");
	        	int nonce = obj.getInt("_nonce");
	        	String DNA = obj.getString("_DNA");
	        	String mamaId = obj.getString("_mamaId");
	        	String papaId = obj.getString("_papaId");
	        	String id = obj.getString("_id");
	        	int gen = obj.getInt("_gen");
	        	String ownerId = obj.getString("_ownerId");
	        	
	        	// add CharacterList of Player
	        	Character temp = new Character(hash,previousHash,timeStamp,nonce,DNA,mamaId,papaId,id,gen,ownerId);	        	
	        	characterList.add(temp);
	    }
	    
		Player findOne = new Player(
				jObjectPlayer.getString("id"),
				jObjectPlayer.getString("password"),
				jObjectPlayer.getString("nickname"),
				jObjectPlayer.getString("introduction"),
				jObjectPlayer.getString("publicKey"),
				jObjectPlayer.getString("privateKey"),
				jObjectPlayer.getFloat("coin"),
				jObjectPlayer.getFloat("stone"),
				jObjectPlayer.getInt("hasCharacterNum"),
				characterList
			);
		
		return findOne;
	}
	
	
	public Block sendCoin(String send, String to, float value) {
		// get Sender Infomation
		Player S = findPlayer(send);
//		BlockChain.setCoinToPlayer(S); // test Code
		
		// get recipient Infomation
		Player T = findPlayer(to);
		Block ret = BlockChain.sendCoin(S, T, value);
		if(ret != null) return ret;
		else return null;
	}
	
	public void sendCharacterToBuyer(Player from, Player to, String registerId) {
		// findAuction
		MongoDatabase database = mongoClient.getDatabase("Toy"); // get DB
		MongoCollection<Document> playerCollection = database.getCollection("auctions");        
		        
		Document auctionDoc = playerCollection.find(
		        eq("regiNum", registerId)).first();
		
	    JSONObject jObjectAuction = new JSONObject(auctionDoc);

	    String toyId = jObjectAuction.getString("toyId");
	    
	    // CharacterChain에서 findCharacter에서 CharacterOwner 정보 변경하기
	    Character movedObj = CharacterChain.findCharacter.get(toyId);	    
	    movedObj._ownerId = to.id;	    
	    CharacterChain.findCharacter.remove(toyId);
	    CharacterChain.findCharacter.put(toyId, movedObj);
	    
	    // CharacterChain에서 characterToOwner 변경으로 캐릭터의 주인 변경하기
	    CharacterChain.characterToOwner.replace(toyId, to.id);
	    
	    // to의 CharacterList에서 캐릭터 추가하기
	    to.characterList.add(movedObj);
	    to.hasCharacterNum = to.characterList.size();
	    
	    // from의 CharacterList에서 캐릭터 삭제하기
	    for(int i = 0; i < from.characterList.size(); i++) {
	    	if(from.characterList.get(i)._id.equals(toyId)) {
	    		from.characterList.remove(i);
	    		break;
	    	}
	    }
	    from.hasCharacterNum = from.characterList.size();
	   	    
	    // update Player's database
	    MongoDatabase gameDatabase = mongoClient.getDatabase("Game"); // get DB
		MongoCollection<Document> playersCollection = gameDatabase.getCollection("players");  
		
		// 두 플레이어의 characterList Update해주기
		// SELLER
		JSONArray fromJArrayChain = new JSONArray();
		for(int i = 0; i < from.characterList.size(); i++) {
			String characterStr = new GsonBuilder().setPrettyPrinting().create().toJson(from.characterList.get(i));
			Object obj = JSON.parse(characterStr);
			fromJArrayChain.put(i, obj);			
		}
				
        playersCollection.updateOne(eq("Players.id", from.id),
        		Updates.combine(
        				Updates.set("Players.characterList", fromJArrayChain),
        				Updates.set("Players.hasCharacterNum", from.hasCharacterNum)
        				));
        
        // BUYER
        JSONArray toJArrayChain = new JSONArray();
		for(int i = 0; i < to.characterList.size(); i++) {
			String characterStr = new GsonBuilder().setPrettyPrinting().create().toJson(to.characterList.get(i));
			Object obj = JSON.parse(characterStr);
			toJArrayChain.put(i, obj);			
		}

        playersCollection.updateOne(eq("Players.id", to.id),
        		Updates.combine(
        				Updates.set("Players.characterList", toJArrayChain),
        				Updates.set("Players.hasCharacterNum", to.hasCharacterNum)
        				));
                
        // mapList 정보 갱신하기
        MongoCollection<Document> mapListCollection = gameDatabase.getCollection("MapList");  
        
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
        mapListCollection.updateOne(eq("findCharacterFilter", "findCharacterMap"),
        		Updates.set("findCharacterMap.myArrayList", findCharacterJson));

        
        // insert CharacterToOwner Map
        int k = 0;
        JSONArray characterToOwnerList = new JSONArray();
        for(Entry<String, String> kv : CharacterChain.characterToOwner.entrySet()) {
        	JSONObject inputData = new JSONObject();
        	inputData.put("_characterId", kv.getKey());
        	inputData.put("_playerId", kv.getValue());
        	characterToOwnerList.put(k,inputData);
        	k++;
        }
        
        String characterToOwnerString = new GsonBuilder().setPrettyPrinting().create().toJson(characterToOwnerList);
        Object characterToOwnerJson = JSON.parse(characterToOwnerString);
        mapListCollection.updateOne(eq("characterToOwnerFilter", "characterToOwnerMap"),
        		Updates.set("characterToOwnerMap.myArrayList", characterToOwnerJson));
               		
        
		MongoDatabase toyDatabase = mongoClient.getDatabase("Toy"); // get DB
        MongoCollection<Document> toyCollection = toyDatabase.getCollection("toys");    
        
        toyCollection.updateOne(eq("id", movedObj._id),
        		Updates.set("ownerId", movedObj._ownerId));
        
	}
	
	public void rentCharacter() {
		
	}

	public void test() {
		
	}	
}