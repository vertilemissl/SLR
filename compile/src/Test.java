import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException {
        Scanner scn=new Scanner(System.in);
        System.out.println("input sentence:");
        String s = scn.nextLine();
        LexicalAnalysis lexicalAnalysis = new LexicalAnalysis();
        lexicalAnalysis.scan(s);
        List<Token> resultList = lexicalAnalysis.getResultList();
        Queue<String> queue = new LinkedList<>();
        for (Token token : resultList) {
            if(token.id==2){
                queue.add("$uni");
            }
            else if(token.id==1){
                queue.add("$id");
            }
            else if(token.id==30){
                queue.add("$pm");
            }
            else if(token.id==31){
                queue.add("$md");
            }
            else if(token.id==32){
                queue.add("$ro");
            }
            else{
                queue.add(token.value);
            }
        }
        for (String s1 : queue) {
            System.out.print(s1+" ");
        }

        GrammarAnalysisTable grammarAnalysisTable = new GrammarAnalysisTable("grammar.txt");
//        System.out.println(grammarAnalysisTable.grammarReader.FOLLOW);

        GrammaticalAnalysis grammaticalAnalysis = new GrammaticalAnalysis(grammarAnalysisTable);
//
        grammaticalAnalysis.grammaticalAnalyze(queue,grammarAnalysisTable);
//
    }
    //const m=7, n=85; var x,y,z,q,r; procedure multiply; var a,b; begin  a:=x; b:=y; z:=0; while b>0 do begin if odd b then z:=z+a; a:=2*a; b:=b/2; end end;
//procedure divide; var w;
//var n,f;begin n:=0;f:=1; while n # 10 do begin n:=n+1; f:=f*n; end; call print; end.
}
