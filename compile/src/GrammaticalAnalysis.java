import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class GrammaticalAnalysis {
    Stack<Integer> statusStack;// 状态栈
    Stack<String> wordStack;// 符号栈
    List<Grammar> grammarList;// 全部文法
    String[][] actionTable;
    String[][] gotoTable;

    public GrammaticalAnalysis(GrammarAnalysisTable grammarAnalysisTable){
        this.statusStack=new Stack<>();
        this.wordStack=new Stack<>();
        this.grammarList=grammarAnalysisTable.grammarReader.grammarList;
        this.actionTable=grammarAnalysisTable.actionTable;
        this.gotoTable=grammarAnalysisTable.gotoTable;
    }
    private void showRecord(BufferedWriter bufferedWriter,String input,int index,String action) throws IOException {
        String status="";//状态栈
        for (Integer integer : statusStack) {
            status+=integer+"";
        }
        String words="";
        for (String s : wordStack) {
            words+=s+"";
        }
        bufferedWriter.write(index+"\t"+status+"\t"+words+"\t"+input+"\t"+action+"\n");
    }
    // 出错
    private void error(Queue<String> inputQueue,String tempWord){
        //顺序输出符号栈
        for (String s : wordStack) {
            System.out.print(s+"  ");
        }
        System.out.print("※");
        System.out.print(tempWord+" ");
        for (String s : inputQueue) {
            System.out.print(s);
        }
    }
    private void shift(Queue<String> inputQueue,String newStatus,String tempWord){
        // 队首出队
        inputQueue.remove();
        // 把新状态push进状态栈
        this.statusStack.push(Integer.parseInt(newStatus.substring(1)));
        // 把tempWord push进符号栈
        this.wordStack.push(tempWord);
    }
    private Grammar reduce(String newStatus){
        // 状态栈、符号栈弹出对应数量的word
        // 获取规约的文法
        Grammar tempGrammar=this.grammarList.get(Integer.parseInt(newStatus.substring(1)));
        // 获取对应数量：grammarList的第newStatus个文法右部的size
        int num=0;
        if("ε".equals(tempGrammar.right_.get(0))){
            num=0;
        }else{
            num=tempGrammar.right_.size();
        }
        //弹出
        for(int i=0;i<num;i++){
            this.statusStack.pop();
            this.wordStack.pop();
        }
        // 把文法左部push进符号栈
        this.wordStack.push(tempGrammar.left_);
        return tempGrammar;
    }

    public void grammaticalAnalyze(Queue<String> inputQueue,GrammarAnalysisTable grammarAnalysisTable) throws IOException {
        File file=new File("grammarAnalysisRecord.csv");
        if(file.exists()) {file.delete();}
        file.createNewFile();

        FileWriter fileWriter=new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        bufferedWriter.write("步骤\t状态栈\t符号栈\t输入串\t动作\n");
        String input="";
        for (String s : inputQueue) {
            input+=s;
        }
        bufferedWriter.write("1\t0\t#\t"+input+"\t\n");

        int flag=1;
        int index=1;
        // 把状态0 push进状态栈
        this.statusStack.push(0);
        while(!inputQueue.isEmpty()){
            // 当前输入进的字符
            String tempWord=inputQueue.peek();
            // 当前状态栈的栈顶
            int currentStatus=this.statusStack.peek();
            // 查找tempWord对应的编号
            int vtIndex = grammarAnalysisTable.isVt(tempWord);
            // 获取对应状态
            String newStatus = this.actionTable[currentStatus][vtIndex];
            // 出错
            if(newStatus.equals("")){
                System.out.println("1出错");
                this.error(inputQueue,tempWord);
                flag=0;
                break;
            }

            // 移进
            if(newStatus.charAt(0)=='s'){
                shift(inputQueue,newStatus,tempWord);
            }
            // 规约
            else{
                Grammar tempGrammar=this.reduce(newStatus);
                // 获取新状态
                // 查找文法左部对应的编号
                int vnIndex = grammarAnalysisTable.isVn(tempGrammar.left_);
                currentStatus=this.statusStack.peek();
                String nextStatus=this.gotoTable[currentStatus][vnIndex];

                // 出错
                if(nextStatus.equals("")){
                    System.out.println("2出错");
                    this.error(inputQueue,tempWord);
                    flag=0;
                    break;
                }

                // 把新状态push进状态栈
                this.statusStack.push(Integer.parseInt(nextStatus));

            }
            index++;
            input="";
            for (String s : inputQueue) {
                input+=s+"";
            }
            this.showRecord(bufferedWriter,input,index,newStatus);
        }

        // 如果符号栈里还有
        String S=grammarList.get(0).left_;
        int vtIndex = grammarAnalysisTable.isVt("ε");
        while(wordStack.size()!=1 || !S.equals(wordStack.peek())){
            // 当前状态栈的栈顶
            int currentStatus=this.statusStack.peek();
            // 获取对应状态
            String newStatus = this.actionTable[currentStatus][vtIndex];
            // 报错
            if(newStatus.equals("")){
                //顺序输出符号栈
                error(inputQueue," ");
                flag=0;
                break;
            }

            // 规约
            Grammar tempGrammar=this.reduce(newStatus);

            if(S.equals(wordStack.peek())){
                index++;
                this.showRecord(bufferedWriter,"\t",index,newStatus);
                break;
            }

            // 获取新状态
            // 查找文法左部对应的编号
            int vnIndex = grammarAnalysisTable.isVn(tempGrammar.left_);
            currentStatus=this.statusStack.peek();
            String nextStatus=this.gotoTable[currentStatus][vnIndex];

            // 出错
            if(nextStatus.equals("")){
                System.out.println("4出错");
                this.error(inputQueue," ");
            }
            // 把新状态push进状态栈
            this.statusStack.push(Integer.parseInt(nextStatus));

            index++;
            this.showRecord(bufferedWriter,",",index,newStatus);
        }

        this.wordStack.clear();
        this.statusStack.clear();
        if(flag==1){
            System.out.println("accept");
        }
        bufferedWriter.close();
    }
}


