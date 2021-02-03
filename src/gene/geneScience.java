package gene;

import character.Character;
import character.CharacterChain;
import coin.BlockChain;

import java.util.Arrays;
import java.util.Random;

public class geneScience {
    public String[] momArray;
    public String[] papaArray;
    public int traitNum = 14;
    public int[] sliceArray = {4, 3, 4, 4, 4, 3, 8, 8, 8, 3, 8, 8, 8, 2};

    private double mutationPer = 0.005;

    long seed = System.currentTimeMillis(); // 1970년 1월 1일부터 현재까지 타임스템프를 가져옵니다.
    Random random = new Random(seed);
    
    public String sliceTrait(String gene, int traitIndx) {
       // sliceArray 앞에서 traitIndx전까지 다 던한 값
       int start = arraySum(traitIndx, sliceArray);
        return gene.substring(start, start+sliceArray[traitIndx]);
    }
    
    public String geneMix(String momId, String papaId) {
       String momGene = CharacterChain.getDna(momId);
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
                 String dna = findAncestors(j, j==0 ? momId : papaId, momGene, papaGene);
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
              else if(trait == 5 || trait == 9 || trait == 13) {
            	  babyGene = crossover(genes[0], genes[1]);
              }
              // 색상 (눈, 몸통)
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
          System.out.println("아기 유전자 : " +babyGene);
          
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
       int rand = random.nextInt(gene.length()-start) + start;
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