package simulator2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMap {

	public static void main(String[] args) {
		Map<String,Integer> m = new HashMap<String,Integer>();
		
		m.put(new String("test"), 1);
		System.out.println(m.get(new String("test")));
		
		System.out.println(m.get("a") == m.get("test"));
		
		Integer i = Integer.MAX_VALUE;
		i = i ++;		
		System.out.println(i);
		i = i ++;		
		System.out.println(i);
		
		System.out.println((new String("test")).hashCode());
		
		List<String> l1 = new ArrayList<String>();
		l1.add("a");
		l1.add("b");
		List<String> l2 = new ArrayList<String>();
		l2.add("a");
		l2.add("b");
		System.out.println(l1.equals(l2));
	}

}
