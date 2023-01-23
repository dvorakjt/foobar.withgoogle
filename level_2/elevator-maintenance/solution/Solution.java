import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class VersionComparator implements Comparator<String> {
    public int compare(String a, String b) {
        //split the version strings and then compare each number
        String[] versionStringsA = a.split("\\.");
        String[] versionStringsB = b.split("\\.");
        ArrayList<Integer> versionNumsA = new ArrayList<Integer>();
        ArrayList<Integer> versionNumsB = new ArrayList<Integer>();
        
        //prepare array lists
        for(String s : versionStringsA) {
            versionNumsA.add(Integer.valueOf(s));
        }
        for(String s : versionStringsB) {
            versionNumsB.add(Integer.valueOf(s));
        }
        int sizeA = versionNumsA.size();
        int sizeB = versionNumsB.size();

        //fill each arraylist with -1 in order to sort version numbers with more numbers higher than those with less
        for(int i = 0; i < (3 - sizeA); i++) {
            versionNumsA.add(-1);
        }
        for(int j = 0; j < (3 - sizeB); j++) {
            versionNumsB.add(-1);
        }
        
        int comparison = 0; //-1 if a < b, 0 if equal, 1 if a > b
        for(int k = 0; k < 3; k++) {
            int valueA = versionNumsA.get(k);
            int valueB = versionNumsB.get(k);
            if(valueA < valueB) {
                comparison = -1;
                break;
            }
            if(valueA > valueB) {
                comparison = 1;
                break;
            }
        }
        return comparison;
    }
}    

public class Solution {
    public static String[] solution(String[] l) {
        List<String> versionsList = Arrays.asList(l);
        Collections.sort(versionsList, new VersionComparator());
	    String[] sorted = new String[versionsList.size()];
	    for(int i = 0; i < sorted.length; i++) {
	        sorted[i] = versionsList.get(i);
	    }
	    return sorted;
    }
}