import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexicalAnalysis {
    private Map<String,Integer> keywotrds;
    private Map<String,Integer> operators;
    private Map<String,Integer> delimiters;
//    private ArrayList<String> constantsList;
    private List<String> variables;
    private List<Token> resultList;
    public LexicalAnalysis() {
        this.resultList=new ArrayList<>();
        // 关键字
        this.keywotrds = new HashMap<>();
        this.keywotrds.put("const",10);
        this.keywotrds.put("var",11);
        this.keywotrds.put("procedure",12);
        this.keywotrds.put("begin",13);
        this.keywotrds.put("end",14);
        this.keywotrds.put("odd",15);
        this.keywotrds.put("if",16);
        this.keywotrds.put("then",17);
        this.keywotrds.put("call",18);
        this.keywotrds.put("while",19);
        this.keywotrds.put("do",20);
        this.keywotrds.put("read",21);
        this.keywotrds.put("write",22);
        // 运算符
        this.operators=new HashMap<>();
        this.operators.put("+",30);
        this.operators.put("-",30);
        this.operators.put("*",31);
        this.operators.put("/",31);
        this.operators.put("<",32);
        this.operators.put("<=",32);
        this.operators.put(">",32);
        this.operators.put(">=",32);
        this.operators.put("#",32);
        this.operators.put("=",33);
        this.operators.put(":=",34);
        // 界限符
        this.delimiters=new HashMap<>();
        this.delimiters.put("{",50);
        this.delimiters.put("}",51);
        this.delimiters.put("(",52);
        this.delimiters.put(")",53);
        this.delimiters.put(";",54);
        // 变量
        this.variables=new ArrayList<>();
    }

    private int isKeyword(String word){
        for (String s : this.keywotrds.keySet()) {
            if(s.equals(word)){
                return this.keywotrds.get(word);
            }
        }
        return 1;
    }
    private int isOperator(String word){
        for (String s : this.operators.keySet()) {
            if(s.equals(word)){
                return this.operators.get(word);
            }
        }
        return -1;
    }
    private int isDelimiter(String word){
        for (String s : this.delimiters.keySet()) {
            if(s.equals(word)){
                return this.delimiters.get(word);
            }
        }
        return -1;
    }
    private char charAt(int num,String s,int l,char err){
        if(num<l){
            return s.charAt(num);
        }
        return err;
    }

    public void scan(String sentence) {
        int l=sentence.length();
        int i=0;
        while(true){
            char ch=this.charAt(i,sentence,l,' ');
            while(ch==' '){
                ch=this.charAt(++i,sentence,l,' ');
            }
            // 可能是关键字或变量名
            if((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')){
                String word="";
                while((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')){
                    word=word+ch;
                    ch=this.charAt(++i,sentence,l,' ');
                }
                Token token = new Token(this.isKeyword(word), word);
                this.resultList.add(token);
            }

            // 数字
            else if(ch>='0'&&ch<='9'){
                int sum =0;
                while(ch>='0'&&ch<='9'){
                    sum=sum*10+ch-'0';
                    ch=this.charAt(++i,sentence,l,' ');
                }
                Token token = new Token(2, ""+sum);
                this.resultList.add(token);
            }

            // 其他
            else{
                String word=""+ch;
                if((ch=='<' || ch=='>'|| ch==':')&&this.charAt(i+1,sentence,l,'0')=='='){
                    word=word+"=";
                    i++;
                }
                i++;
                int id=this.isOperator(word);
                if(id!=-1){
                    Token token = new Token(id, word);
                    this.resultList.add(token);
                }else{
                    id=this.isDelimiter(word);
                    if(id==-1){//非法字符
                        Token token = new Token(-1, word);
                        this.resultList.add(token);
                    }
                    else{
                        Token token = new Token(id, word);
                        this.resultList.add(token);
                    }
                }
            }
            if(i>=l){
                break;
            }
        }
    }

    public List<Token> getResultList() {
        return resultList;
    }
    public void showResultList() {
        for (Token t : this.resultList) {
            System.out.println(t.toString());
        }
    }
}
class Token{
    public int id;
    public String value;

    public Token() {
        this.id = -1;
        this.value = "";
    }

    public Token(int id, String value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        return "("+id +"," + value + ')';
    }
}

