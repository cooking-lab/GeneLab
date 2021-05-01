package gene;

import character.Character;
import character.CharacterChain;
import coin.BlockChain;

import java.util.Arrays;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;

public class geneScience {
    public String[] momArray;
    public String[] papaArray;
    public int traitNum = 11;
    public int[] sliceArray = {4, 3, 4, 4, 4, 3, 3, 8, 8, 8, 2};

    private double mutationPer = 0.005;

    long seed = System.currentTimeMillis(); // 1970년 1월 1일부터 현재까지 타임스템프를 가져옵니다.
    Random random = new Random(seed);
    
    public String sliceTrait(String gene, int traitIndx) {
       // sliceArray 앞에서 traitIndx전까지 다 던한 값
       int start = arraySum(traitIndx, sliceArray);
        return gene.substring(start, start+sliceArray[traitIndx]);
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
    
    public String geneMix(String mamaId, String papaId) {
       String momGene = CharacterChain.getDna(mamaId);
       String papaGene = CharacterChain.getDna(papaId);
       String[] babyArray = new String[traitNum];
       
       for(int trait = 0; trait<traitNum; trait++) {
    	   System.out.println();
    	   System.out.println(trait + " 특성");
          // 성별 유전
          String babyGene = "";
          if(trait < 2) {
        	  String momTrait = sliceTrait(momGene, trait);
              String papaTrait = sliceTrait(papaGene, trait);
              System.out.println(trait + " 엄마 : " + momTrait);
              System.out.println(trait + " 아빠 : " + papaTrait);
              // 성별
              if(trait == 0) {
            	  String momSexGene[] = new String[2];
                  String papaSexGene[] = new String[2];
                  
                  for(int i=0; i<=2; i+=2) {
                     momSexGene[i/2] = momTrait.substring(i, i+2);
                     papaSexGene[i/2] = papaTrait.substring(i, i+2);
                  }
//                  if(momSexGene[1].charAt(0) == papaSexGene[1].charAt(0)) {
//                     System.out.println("성별 같음. 교배 불가");
//                     return "";
//                  }
                  // 유전 돌려~~
                  babyGene = momSexGene[random.nextInt(2)] + papaSexGene[random.nextInt(2)];
              }
              // 종족 (그대로 유전, 엄마/아빠 같은 종족임)
              else {
            	  if(!momTrait.equals(papaTrait)) {
                      System.out.println("종족 다름. 교배 불가");
                      return "";
                   }
                   babyGene = momTrait;
              }
          }
          else {
        	// 조상 살펴야지유
              String genes[] = new String[2];
              for(int j=0; j<2; j++){
                 String dna = findAncestors(j, j==0 ? mamaId : papaId, momGene, papaGene);
                 if(dna == "") {
                    j--;
                    continue;
                 }
                 genes[j] = sliceTrait(dna, trait);
                 System.out.println((j==0 ? "엄마" : "아빠")+"(조상) 유전자 : " + genes[j]);
                 }
              // 재료 유전
              if(trait >= 2 && trait <= 4) {
            	  babyGene = genes[random.nextInt(2)];
              }
              // 눈, 코/입, 열성
              else if(trait == 5 || trait == 6) {
            	  babyGene = crossover(genes[0], genes[1]);
              }
              // 히든
              // 유전X 돌연변이에 의해서만 생성
              else if(trait == 10) {
            	  String a = mutationPer >= random.nextDouble() ? "1" : "0";
            	  String b = mutationPer >= random.nextDouble() ? "1" : "0";
            	  babyGene = a + b;
              }
              // 색상 (몸통)
              else {
            	  int[] colorRange = new int[2];
                  for(int i=0; i<2; i++) {
                     colorRange[i] = Integer.valueOf(genes[i], 2);
                  }
                  Arrays.sort(colorRange); 
                  int colorGene = (colorRange[0] == colorRange[1]) ? colorRange[0] : random.nextInt(colorRange[1]-colorRange[0])+colorRange[0];
                  babyGene = String.format("%08d", Integer.parseInt(Integer.toBinaryString(colorGene)));
              }
          }
          System.out.println("아기 유전자 : " + babyGene);
          
          babyArray[trait] = babyGene;
       }
       
       String babyResult = decode(babyArray);
       return (mutationPer >= random.nextDouble() ? mutation(babyResult) : babyResult);
    }

    public String findAncestors(int who, String whoId, String momGene, String papaGene) {
       int ancestorRand = random.nextInt(7) % 8;
        ancestorRand = isExtended(ancestorRand); // -1 ~ 2
        System.out.println(who+"(0:엄마, 1:아빠) 조상 : " + ancestorRand );
        if(ancestorRand == -1) {
           if(who == 0) {return momGene;}
           else {return papaGene;}
        }
        // 조상 필요
        int[] anc = {-1, -1, -1};
        for(int i=0; i<ancestorRand+1; i++){
            anc[i] = random.nextInt(2);
        }
        // 블록체인에서 gene 불러옴
        return CharacterChain.findParentsDna(anc, whoId);
    }
    
    public String crossover(String trait1, String trait2){
        double crossPer = 0.5;
        String newGene = "";

        for(int i=0; i<trait1.length(); i++){
            double rand = random.nextDouble();
            // 엄마 유전 (gene1)
            if(rand < crossPer){
                System.out.print("엄마 ");
                newGene += trait1.charAt(i);
            }else{
                System.out.print("아빠 ");
                newGene += trait2.charAt(i);
            }
        }
        System.out.println();
        return newGene;

    }
    
    public String mutation(String gene) {
       StringBuilder mutationGene = new StringBuilder(gene);
       int start = sliceArray[0] + sliceArray[1];
       int rand = random.nextInt(gene.length()-start-sliceArray[10]) + start;
       char ch = mutationGene.charAt(rand);
       System.out.println("돌연변이 : "+rand+"번쨰 유전자 "+ch+"->반대로 바뀐다.");
       mutationGene.setCharAt(rand, (ch == '0') ? '1' : '0');
       
       return mutationGene.toString();
    }

    public String decode(String[] gene){
        String result = "";
        for(int i=0; i<gene.length; i++){
            result += gene[i];
        }
        return result;
    }

    public int isExtended(int rand){
        int num;
        switch(rand){
            case 7: num = 2; break;
            case 6: num = 1; break;
            case 5: ;
            case 4: num = 0; break;
            default: num = -1; break;
        }
        return num;
    }

    public int arraySum(int last, int[] arr){
        int[] temp = Arrays.copyOf(arr, last);
        int sum = 0;
        for(int i=0; i<temp.length; i++){
            sum += temp[i];
        }
        return sum;
    }
}