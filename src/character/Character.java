package character;

import java.util.Date;

public class Character {

    public String hash; // 현재 객체의 해쉬값
    public String previousHash; // 이전 객체의 해쉬값
    private long timeStamp; // 현재 시간
    private int nonce; // 새 해쉬값을 찾기 위해 증가하는 값

    // character 속성
    public String _DNA; // DNA
    public String _mamaId; // 엄마 id
    public String _papaId; // 아빠 id
    public String _Id; // 본인 ID == hash
    // public float _cooldown; // 쿨타임 (교배 횟수 많을수록 패널티)
    public int _gen; // 지금 몇세대인지 
    public int _ownerId; // 소유자 id
    
    
    // 0세대 캐릭터의 경우는 유전할때 본인만 참조가능하게.
    // mama, papa의 경우 빈칸으로 아니면 임의로 정한 값(관리자 지정)

    public Character(String DNA, String previousHash) { // 새 캐릭터 생성
        this._DNA = DNA;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
        this._mamaId = "";
        this._papaId = "";
    }
    
    public void setParents(String mama, String papa) {
    	this._mamaId = mama;
    	this._papaId = papa;
    }


    public String calculateHash() {
        // 이전해쉬 + 시간 + nonce + 우리 데이터의 형태
        // 부id + 모id + 토탈 교배 횟수 = 같을 일 없음
        String calculatedhash = StringUtil.applySha256(
        		previousHash + Long.toString(timeStamp) + 
        		Integer.toString(nonce) + _DNA + _mamaId + _papaId);

        return calculatedhash;
    }


    public void generateCharacter(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).contentEquals(target)) {
            nonce++;
            hash = calculateHash();
        }
        this._Id = hash; // id랑 hash랑 같은 값임
        System.out.println("(캐릭터 이름) 생성 완료 : " + hash);
    }
}
