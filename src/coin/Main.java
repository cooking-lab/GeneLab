package coin;

import character.CharacterChain;

public class Main {
    public static void main(String[] args) {
	// write your code here
        BlockChain.init(); // BC Library를 사용하기 위한 절차
//        Player p1 = new Player("testId1","testPw1","testName1");
//        Player p2 = new Player("testId2","testPw2","testName2");
//
//        BlockChain.setCoinToPlayer(p1); // 기본적으로 500코인을 배부하기로 결정
//        BlockChain.setCoinToPlayer(p2);
//        
//        System.out.println("\nPlayer1의 잔액 : " + p1.wallet.getBalance());
//        System.out.println("\nPlayer2의 잔액 : " + p2.wallet.getBalance());
//        
//        p1.sendCoinTo(p2,300f);
//        p2.sendCoinTo(p1,500f);
//        
//        System.out.println("\nPlayer1의 잔액 : " + p1.wallet.getBalance());
//        System.out.println("\nPlayer2의 잔액 : " + p2.wallet.getBalance());
//        
//        System.out.println(p1.wallet.publicKey);
//        System.out.println(p1.wallet.privateKey);
//        
        CharacterChain.test();
        CharacterChain.breedTest();
    }
}