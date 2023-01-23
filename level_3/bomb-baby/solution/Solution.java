//Note: this has undergone some minor refactoring to make the code more DRY since my submission. 
//The code that is now listed in calculateGenerationsToSkip was previously repeated in two places, so I just
//extracted it into a function.

import java.math.BigInteger;

public class Solution {
    public static String solution(String x, String y) {
        BigInteger m = new BigInteger(x);
        BigInteger f = new BigInteger(y);
        BigInteger minGenerations = BigInteger.valueOf(-1);
        BigInteger generations = BigInteger.valueOf(0);
        while(true) {
            //if either total has reached 0 or less, break from the loop
            if((m.compareTo(BigInteger.valueOf(1)) == -1) || 
                (f.compareTo(BigInteger.valueOf(1)) == - 1)) break;
            
            //if the number of each bomb type is the same, break from the loop
            //if the total is 1,1 -- the starting point -- set minGenerations to generations
            if(m.equals(f)) {
                if(m.equals(BigInteger.valueOf(1))) minGenerations = generations;
                break;
            } else if(m.compareTo(f) == 1) {
                //skip ahead
                BigInteger generationsToSkip = calculateGenerationsToSkip(m, f);
                m = m.subtract(f.multiply(generationsToSkip));
                generations = generations.add(generationsToSkip);
            } else {
                BigInteger generationsToSkip = calculateGenerationsToSkip(f, m);
                f = f.subtract(m.multiply(generationsToSkip));
                generations = generations.add(generationsToSkip);
            }
        }
        if(minGenerations.compareTo(BigInteger.valueOf(-1)) == 1) return minGenerations.toString();
        else return "impossible";
    }

    //when one bomb type count is greater than the other, count the number of generations to skip
    public static BigInteger calculateGenerationsToSkip(BigInteger largerBombCount, BigInteger smallerBombCount) {
        BigInteger generationsToSkip;
        //if the lower term is one, skip to 1,1
        if(smallerBombCount.equals(BigInteger.ONE)) {
            generationsToSkip = largerBombCount.subtract(BigInteger.ONE);
        } else { //otherwise, calculate how far to skip
            generationsToSkip = largerBombCount.divideAndRemainder(smallerBombCount)[0];
        }
        return generationsToSkip;
    }
}