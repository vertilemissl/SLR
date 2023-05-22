import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GrammarAnalysisTable {
    GrammarReader grammarReader;
    List<String>  vtList;
    List<String> vnList;
    List<Closure> status;//状态转换图
    String[][] actionTable;
    String[][] gotoTable;

    // 判断是否是终结符
    int isVt(String item){
        for (int i=0;i<this.vtList.size();i++) {
            if(this.vtList.get(i).equals(item)){
                return i;
            }
        }
        return -1 ;
    }
    // 判断是否是非终结符
    int isVn(String item){
        for (int i=0;i<this.vnList.size();i++) {
            if(this.vnList.get(i).equals(item)){
                return i;
            }
        }
        return -1 ;
    }

    public GrammarAnalysisTable(String filepath) throws IOException {
        this.grammarReader=new GrammarReader(filepath);
        this.vnList=new ArrayList<>(this.grammarReader.vnSet);
        this.vtList=new ArrayList<>(this.grammarReader.vtSet);
        if(!this.vtList.contains("ε")){
            this.vtList.add("ε");
        }
        this.generateAnalysisTable(grammarReader.grammarList);

        int row=this.status.size();
        int col=this.vtList.size();
        this.actionTable=new String[row][col];
        this.gotoTable=new String[row][this.vnList.size()];
        //初始化
        for(int i=0;i<this.actionTable.length;i++){
            for(int j=0;j<this.actionTable[i].length;j++){
                this.actionTable[i][j]="";
            }
        }
        for(int i=0;i<gotoTable.length;i++){
            for(int j=0;j<gotoTable[i].length;j++){
                gotoTable[i][j]="";
            }
        }
        this.saveAnalysisTable();
        this.saveStatus();
    }

    //判断某语句是否在grammarList里
    boolean inStatus1(Grammar g,List<Grammar> status1){
        for (Grammar grammar : status1) {
            if(g.hashCode()==grammar.hashCode()){
                return true;
            }
        }
        return false;
    }


    // 计算闭包（文法集，前一个状态，新状态的·后的字符）
    Closure calClosure(List<Grammar> grammarList, List<Grammar> S, Set<String> set){
        List<Grammar> closure =new ArrayList<>();
        Queue<Grammar> queue=new LinkedList<>();
        for (Grammar grammar : S) {
            queue.add(grammar);
            closure.add(grammar);
        }
        while(!queue.isEmpty()){
            Grammar temp=queue.remove();//获取队首
            String item=temp.getDotItem();//获得·后面的字符
            if(item==null || item.equals("ε")){
                continue;
            }
            set.add(item);

            if(isVt(item)>=0){//是终结符
                continue;
            }
            //是非终结符Vn,遍历grammarList,寻找左部为Vt的表达式,加入closure
            for (Grammar grammar : grammarList) {
                String left_ = grammar.left_;
                if(item.equals(left_)){
                    // 如果语句在closure里就跳过
                    if(this.inStatus1(grammar,closure)){
                        continue;
                    }
                    // 不在closure里
                    else{
                        closure.add(grammar);
                        // 不是左递归
                        if(!temp.left_.equals(grammar.getDotItem())){
                            queue.add(grammar);
                        }
                    }
                }
            }

        }
        Closure status = new Closure(closure);
        return status;
    }

    //判断closure是否已在列表里
    int inClosureList(Closure c,List<Closure> q){
        if(q.size()<=0) return -1;
        for (Closure closure : q) {
            if (closure.equals(c)){
                return closure.index;
            }
        }
        return -1;
    }

    //生成分析表
    void generateAnalysisTable(List<Grammar> grammarList){
        List<Closure> analysisTable=new ArrayList<>();
        //将开始元素添加进来
        List<Grammar> start=new ArrayList<>();
        Grammar S = grammarList.get(0);
        start.add(S);
        //创建记录·后字符的集合
        Set<String> set=new HashSet<>();
        Queue<List<String>> itemQ=new LinkedList<>();
        Queue<Closure> closureQ=new LinkedList<>();//可能没有用！
        //计算第0个状态
        int i=0;
        Closure closure = this.calClosure(grammarList, start,set);
        closure.index=i++;
        List<String> itemL=new ArrayList<>(set);
        set.clear();
        //放到他们该在的地方
        itemQ.add(itemL);
        closureQ.add(closure);
        analysisTable.add(closure);
        // 深度优先
        while (!closureQ.isEmpty()){
            //弹出第一个状态
            closure=closureQ.remove();
            //弹出第一组该移动的
            itemL=itemQ.remove();

            for (String s : itemL) {
                //转移状态，对·移动
                Closure temp = closure.moveDot(s);
                //计算闭包
                Closure newClosure = this.calClosure(grammarList, temp.status1, set);
                int flag=this.inClosureList(newClosure,analysisTable);

                List<String> itemList=new ArrayList<>(set);
                set.clear();

                // 如果已经出现过
                if(flag>=0){
                    newClosure=analysisTable.get(flag);
                }
                // 如果没有出现过
                else{
                    newClosure.index=i++;
                    //放到他们该在的地方
                    itemQ.add(itemList);
                    closureQ.add(newClosure);
                    analysisTable.add(newClosure);
                }
                // closure指向newClosure
                closure.addNewClosure(newClosure,s);
            }
        }
        this.status =analysisTable;
    }

    // 移进
    void shift(int i,Map<String,Closure> tempMap){
        for(Map.Entry<String,Closure> entry:tempMap.entrySet()){
            String move=entry.getKey();
            int index=entry.getValue().index;
            // 如果是终结符,ACTION
            if(this.isVt(move)>=0){
                if(this.actionTable[i][this.isVt(move)].equals("")|| this.actionTable[i][this.isVt(move)].equals("s"+index)){
                    this.actionTable[i][this.isVt(move)]="s"+index;
                }else{
                    System.out.println("不是SLR");
                }

            }
            // 如果是非终结符,GOTO
            else{
                if(this.gotoTable[i][this.isVn(move)].equals("") || this.gotoTable[i][this.isVn(move)].equals(index+"")){
                    this.gotoTable[i][this.isVn(move)]=index+"";
                }else{
                    System.out.println("不是SLR");
                }

            }
        }
    }
    // 归约
    void reduce(int i,Grammar grammar) {
        // 查找语句所在编号
        int index = this.grammarReader.inGrammarList(grammar);
        // 查找左部的FOLLOW
        Set<String> tempFOLLOW = this.grammarReader.FOLLOW.get(grammar.left_);
        // 遍历FOLLOW
        for (String s : tempFOLLOW) {
            if (this.actionTable[i][this.isVt(s)].equals("") || this.actionTable[i][this.isVt(s)].equals("r"+index)) {
                this.actionTable[i][this.isVt(s)] = "r" + index;
            } else {
                System.out.println("不是SLR");
            }
        }
    }

    //输出分析表
    void saveAnalysisTable() throws IOException {
        int row=this.status.size();
        for(int i=0;i<row;i++){
            Closure tempClosure = this.status.get(i);
            Map<String,Closure> tempMap=tempClosure.statusMap;
            // 如果tempMap不为空，可以移进
            if(tempMap!=null) {
                this.shift(i,tempMap);
                // 遍历status1，找到需要规约的
                List<Grammar> grammarList = tempClosure.status1;
                for (Grammar grammar : grammarList) {
                    if(grammar.isReduced() || "ε".equals(grammar.right_.get(0))){
                        this.reduce(i,grammar);
                    }
                }
            }
            // 规约
            else{
                List<Grammar> grammarList = tempClosure.status1;
                for (Grammar grammar : grammarList) {
                    this.reduce(i,grammar);
                }
            }
        }

        // 输出,写入到csv文件里
        File file=new File("grammarAnalysisTable.csv");
        if(file.exists()) {file.delete();}
        file.createNewFile();

        FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write("\t");
        for (int i=0;i<this.vtList.size();i++){
            bufferedWriter.write(this.vtList.get(i)+"\t");

        }
        for (int i=0;i<this.vnList.size();i++){
            if(i==this.vnList.size()-1){
                bufferedWriter.write(this.vnList.get(i));
            }else{
                bufferedWriter.write(this.vnList.get(i)+"\t");
            }
        }
        bufferedWriter.write("\n");

        for(int i=0;i<row;i++){
            bufferedWriter.write(i+"\t");
            for (int j=0;j<this.actionTable[i].length;j++){
                bufferedWriter.write(this.actionTable[i][j]+"\t");
            }
            for (int j=0;j<this.gotoTable[i].length;j++){
                if(j==this.gotoTable[i].length-1){
                    bufferedWriter.write(this.gotoTable[i][j]);
                }else{
                    bufferedWriter.write(this.gotoTable[i][j]+"\t");
                }

            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.close();
        System.out.println("success");
    }

    // 状态转换图
    void  saveStatus() throws IOException {
        File file=new File("statusTable.txt");
        if(file.exists()) {file.delete();}
        file.createNewFile();

        FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        int i=0;
        for (Closure closure : status) {
            bufferedWriter.write("状态"+i+"\n");
            List<Grammar> status1 = closure.status1;
            bufferedWriter.write(status1.toString()+"\n");
            i++;
        }
        bufferedWriter.close();
        System.out.println("success");
    }

}
