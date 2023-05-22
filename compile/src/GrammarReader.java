import java.io.*;
import java.util.*;

public class GrammarReader {
    List<Grammar> grammarList;
    Set<String> vnSet;
    Set<String> vtSet;
    Map<String,Set<String>> FIRST;
    Map<String,Set<String>> FOLLOW;

    public GrammarReader(String filepath) {
        this.grammarList = new ArrayList<>() ;
        this.vnSet=new HashSet<>();
        this.vtSet=new HashSet<>();
        this.readtoList(filepath);
        this.FIRST=new HashMap<>();
        this.calFIRST();
        this.FOLLOW=new HashMap<>();
        this.calFOLLOW();
    }

    private void readtoList(String filepath){
        File file = new File(filepath);

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                Grammar grammar = this.readLinetoGrammar(tempString);
                this.grammarList.add(grammar);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Grammar readLinetoGrammar(String line){
        String str=line.trim();
        String left_ = str.split("→")[0].trim();
        Grammar grammar = new Grammar(left_);
        this.vnSet.add(left_);
        String right=str.split("→")[1];
        int i=0;
        int length=right.length();
        while(true){
            if(i>=length){
                break;
            }
            char ch=right.charAt(i);
            if(ch==' '){
                i++;
                continue;
            }
            else if(ch=='<'){
                int la = right.indexOf('>', i);
                String right_=right.substring(i,la+1);
                grammar.addRight_(right_);
                this.vnSet.add(right_);
                i=la+1;
            }
            else if((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')){
                String right_="";
                while((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')){
                    right_=right_+ch;
                    i++;
                    if(i<length){
                        ch=right.charAt(i);
                    }
                    else {
                        break;
                    }
                }
                grammar.addRight_(right_);
                vtSet.add(right_);
            }
            else if(ch=='$'){
                String right_=""+ch;
                i++;
                ch=right.charAt(i);
                while((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')){
                    right_=right_+ch;
                    i++;
                    if(i<length){
                        ch=right.charAt(i);
                    }
                    else {
                        break;
                    }
                }
                grammar.addRight_(right_);
                vtSet.add(right_);
            }
            else{
                if(ch==':'){
                    i++;
                    grammar.addRight_(ch+"=");
                    vtSet.add(ch+"=");
                }
                else{
                    grammar.addRight_(ch+"");
                    vtSet.add(ch+"");
                }
                i++;
            }
        }
        return grammar;
    }

    //计算FIRST
    public void calFIRST(){
        int preSize=-1;
        int currentSize=0;
        // 初始化
        for (String s : vnSet) {
            this.FIRST.put(s,new HashSet<>());
        }
        // 当集合有变化时
        while(preSize!=currentSize){
            preSize=currentSize;
            // 遍历grammarList
            for (Grammar grammar : grammarList) {
                String left_=grammar.left_; //左部
                String first=grammar.right_.get(0); //右部第一个字符
                // 如果右部第一个字符是非终结符
                if(isVn(first)){
                    int size=grammar.right_.size();
                    // 遍历grammar的右部
                    for(int i=0;i<size;i++){
                        String righti=grammar.right_.get(i);
                        // 如果是终结符，就退出
                        if(!isVn(righti)){
                            break;
                        }
                        // 如果是终结符，就把righti的FIRST（除了ε）添加进left_
                        for (String s : this.FIRST.get(righti)) {
                            if(!"ε".equals(s)){
                                this.FIRST.get(left_).add(s);
                            }else if (i==size){// 如果所有右部的都能推出ε，就把ε添加进left_
                                this.FIRST.get(first).add("ε");
                            }
                        }
                        // 如果righti的FIRST没有ε，就退出
                        if(!this.FIRST.get(righti).contains("ε")){
                            break;
                        }
                    }

                }
                else {//如果右部第一个字符是终结符,直接加入
                    this.FIRST.get(left_).add(first);
                }
            }
            // 计算现在FIRST集大小
            currentSize=this.calFIRSTSize();
        }
    }
    private void calFOLLOW(){
        int preSize=-1;
        int currentSize=0;
        // 初始化
        for (String s : vnSet) {
            this.FOLLOW.put(s,new HashSet<>());
        }
        // 对于文法的开始符号，把ε加进去
        this.FOLLOW.get(this.grammarList.get(0).left_).add("ε");
        while(preSize!=currentSize){// 当集合有变化时
            preSize=currentSize;
            for (Grammar grammar : grammarList) {
                String left_=grammar.left_;
                List<String> rights=grammar.right_;
                int rightsize=rights.size();
                for (int i=0;i<rightsize;i++){
                    if(isVn(rights.get(i))){// 如果当前这个是非终结符
                        if(i+1<rightsize && isVn(rights.get(i+1))){// 下一个也是非终结符
                            // 把除了ε 下一个所有FIRST元素都加进去
                            for (String s : this.FIRST.get(rights.get(i+1))) {
                                if(!"ε".equals(s)){
                                    this.FOLLOW.get(rights.get(i)).add(s);
                                }
                            }
                        }
                        else if(i+1>=rightsize){
                            this.FOLLOW.get(rights.get(i)).add("ε");
                        }
                        else{
                            this.FOLLOW.get(rights.get(i)).add(rights.get(i+1));
                        }
                    }
                }
                for(int i=rightsize-1;i>=0;i--){
                    // 如果是非终结符，就把左部的follow集加入
                    if(isVn(rights.get(i))){
                        for (String s : this.FOLLOW.get(left_)) {
                            this.FOLLOW.get(rights.get(i)).add(s);
                        }
                    }
                    else {
                        break;
                    }

                    if(!this.FIRST.get(rights.get(i)).contains("ε")){
                        break;
                    }
                }
            }
            currentSize=this.calFOLLOWSize();
        }
    }
    private int calFIRSTSize(){
        int sum=0;
        for (Set<String> value : FIRST.values()) {
            sum+=value.size();
        }
        return sum;
    }
    private int calFOLLOWSize(){
        int sum=0;
        for (Set<String> value : FOLLOW.values()) {
            sum+=value.size();
        }
        return sum;
    }
    private boolean isVn(String s){
        return this.vnSet.contains(s);
    }

    // 判断语句是否在grammarList里
    public int inGrammarList(Grammar grammar){
        for(int i=0;i<this.grammarList.size();i++){
            if(grammar.similar(this.grammarList.get(i))){
                return i;
            }
        }
        return -1;
    }


    @Override
    public String toString() {
        String rs="";
        for (Grammar grammar : grammarList) {
            rs=rs+grammar.toString();
        }
        return rs;
    }

}
