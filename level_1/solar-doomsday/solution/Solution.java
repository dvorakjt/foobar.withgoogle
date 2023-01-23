import java.util.ArrayList;

public class Solution {
    public static int[] solution(int area) {
        int areaLeft = area;
        ArrayList<Integer> squares = new ArrayList<Integer>();
        do {
            //get the square root of the remaining area and round it down to the nearest integer
            int side = (int)Math.floor(Math.sqrt(areaLeft));

            //make a square from this side length and subtract it from areaLeft
	        int square = side * side;
            squares.add(square);
            areaLeft -= square;
        } while (areaLeft > 0); //repeat until all of the remaining area has been used
        
        //create an array and populate it with the values contained in squares
	    int[] s = new int[squares.size()];
        for(int i = 0; i < s.length; i++) {
            s[i] = squares.get(i);
        }
	    return s;
    }
}