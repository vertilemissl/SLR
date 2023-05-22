import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Grammar {
    public String left_;
    public List<String> right_;
    public int dot;
    // 构造方法
    public Grammar(String left_, List<String> right_) {
        this.left_ = left_;
        this.right_ = right_;
        this.dot=0;
    }
    public Grammar(String left_) {
        this.left_ = left_;
        this.right_ = new ArrayList<>();
        this.dot = 0;
    }
    public Grammar(){
        this.left_=null;
        this.right_=null;
        this.dot = 0;
    }
    String getDotItem(){
        if(this.dot>=this.right_.size()){
            return null;
        }
        return this.right_.get(this.dot);
    }

    void addRight_(String r){
        this.right_.add(r);
    }

    Grammar moveDot(){
        Grammar temp=new Grammar();

        if(this.dot+1<=this.right_.size()){
            //浅拷贝
            temp.left_=this.left_;
            temp.right_=this.right_;
            temp.dot=this.dot+1;
            return temp;
        }
        else{
            System.out.println("dot 越界");
            return null;
        }
    }
    // 判断两个语句是否一样(除了·的位置)
    public boolean similar(Grammar grammar){
        if(this.left_.equals(grammar.left_) && this.right_.equals(grammar.right_)){
            return true;
        }
        return false;
    }

    // 判断是否需要规约（·的位置是否等于right_的长度）
    public boolean isReduced(){
        return this.right_.size()==this.dot;
    }

    @Override
    public String toString() {
        String left="{"+this.left_;
        String right="";
        for (String s : right_) {
            right=right+s+",";
        }
        return left+":"+right+this.dot+"}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grammar grammar = (Grammar) o;
        return dot == grammar.dot && Objects.equals(left_, grammar.left_) && Objects.equals(right_, grammar.right_);
    }
    @Override
    public int hashCode() {
        return Objects.hash(left_, right_, dot);
    }
}
