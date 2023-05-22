import java.util.*;

public class Closure {
    List<Grammar> status1;
    Map<String,Closure> statusMap;
    int index;

    public Closure(List<Grammar> status1, Map<String, Closure> status) {
        this.status1 = status1;
        this.statusMap = status;
        this.index=-1;
    }

    public Closure(List<Grammar> status1) {
        this.status1 = status1;
        this.statusMap = null;
        this.index=-1;
    }

    Closure moveDot(String left_){
        List<Grammar> newClosure=new ArrayList<>();
        List<Grammar> closure=this.status1;
        for (Grammar grammar : closure) {
            String item=grammar.getDotItem();
            if(left_.equals(item)){//如果·所在字符匹配，需要右移
                Grammar temp = grammar.moveDot();
                //这一步可能没用
                if(temp!=null){
                    newClosure.add(temp);
                }
            }
        }

        Closure status2 = new Closure(newClosure,null);
        return status2;
    }

    // closure指向newClosure
    void addNewClosure(Closure newClosure,String s){
        if(this.statusMap ==null){
            Map<String,Closure> map=new HashMap<>();
            map.put(s,newClosure);
            this.statusMap =map;
        }
        else{
            this.statusMap.put(s,newClosure);
        }
    }

    @Override
    public String toString() {
        Queue<Closure> closuresQ=new LinkedList<>();
        Queue<String> moveQ=new LinkedList<>();
        closuresQ.add(this);
        int idx=0;
        while(!closuresQ.isEmpty() && idx<50){
            Closure closure = closuresQ.remove();
            int i=closure.index;
            if(i!=0){
                System.out.print("-->"+moveQ.remove());
            }

            System.out.println("状态"+i);

            for (Grammar grammar : closure.status1) {
                System.out.println(grammar.toString());
            }
            i++;
            if(closure.statusMap !=null){
                for(Map.Entry<String,Closure> entry:closure.statusMap.entrySet()){
                    moveQ.add(entry.getKey());
                    closuresQ.add(entry.getValue());
                }
            }
            idx++;
        }
        return " ";
    }

    @Override
    public boolean equals(Object o) {
        Closure c=(Closure) o;
        boolean flag=true;
        if(!this.status1.equals(c.status1)){
            flag=false;
        }
        return flag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status1, statusMap, index);
    }

    public void toFile(){

    }
}
