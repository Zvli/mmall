import java.util.*;

public class Test {

    public void number(int n){

        HashSet<Integer> redAarray = new HashSet<Integer>();
        Random random = new Random();

        for (;;) {
            redAarray.add(random.nextInt(33)+1);
            if(redAarray.size()==6) break;
        }
        List<Integer> redList=new ArrayList<Integer>(redAarray);
        Collections.sort(redList);
        //System.out.print(redAarray);
        for (Integer i:redList){
            System.out.print(i+" ");
        }
        System.out.println(random.nextInt(16)+1);
    }

    public static void main(String[] args) {
        Test test=new Test();
        test.number(0);
    }
}
