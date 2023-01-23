// Whew! An INSANELY tough one. My initial approach was to solve it iteratively, which of course only worked for a small subset of 
// possible inputs as the time complexity for that solution grew very quickly. This approach proved useful for verifying later attempts and for informing 
// my creation of the generateCycleIndexTerms function. 
// The solution below makes use of Burnside's Lemma, which I had to research and learn how to apply. 

import java.util.ArrayList;
import java.math.BigInteger;

class CycleIndexTerm {
  public ArrayList<long[]> cycleList;
  public long coefficient;

  public static long factorial(long n) {
    long res = 1;
     for(long i = 1; i <= n; i++) {
      res *= i;
    }
    return res;
  }

  CycleIndexTerm(ArrayList<long[]> cycles, long vectorLength) {
    this.cycleList = new ArrayList<long[]>();
    long denominator = 1;

    for(long[] c : cycles) {
      this.cycleList.add(c);
      long numGroups = c[0];
      long groupLength = c[1];
      denominator *= (Math.pow(groupLength, numGroups) * factorial(numGroups));
    }
    this.coefficient = factorial(vectorLength) / denominator;
  }
}

class BurnsideLemmaTerm {
  private BigInteger coefficient;
  private long exponent;
  
  public BigInteger evaluateForStates(long states) {
    BigInteger s = BigInteger.valueOf(states);
    return this.coefficient.multiply(s.pow((int)this.exponent));
  }

  private static long lcm(long x, long y) {
    long greater;
    long leastCommonMultiple;
    if(x > y) greater = x;
    else greater = y;
    while(true) {
      if((greater % x == 0) && (greater % y == 0)) {
        leastCommonMultiple = greater;
        break;
      }
      greater++;
    }
    return leastCommonMultiple;
  }

  BurnsideLemmaTerm(CycleIndexTerm termA, CycleIndexTerm termB) {
    this.coefficient = BigInteger.valueOf(termA.coefficient * termB.coefficient);
    this.exponent = 0;
    for(long[] cycleA : termA.cycleList) {
      for(long[] cycleB : termB.cycleList) {
        long numGroupsA = cycleA[0];
        long numGroupsB = cycleB[0];
        long lengthA = cycleA[1];
        long lengthB = cycleB[1];
        long exp = (numGroupsA * numGroupsB * lengthA * lengthB) / lcm(lengthA, lengthB);
        this.exponent += exp;
      }
    }
  }
}

public class Solution {
    private static ArrayList<long[]> getCycleListFromVectorType(ArrayList<Long> vectorType) {
      ArrayList<long[]> groupCounts = new ArrayList<long[]>();
      ArrayList<long[]> cycleList = new ArrayList<long[]>();
      for(long vector : vectorType) {
        boolean counted = false;
        for(long[] g : groupCounts) {
          if(g[0] == vector) {
            counted = true;
            g[1]++;
          }
        }
        if(!counted) {
          long[] newCount = new long[2];
          newCount[0] = vector;
          newCount[1] = 1;
          groupCounts.add(newCount);
        }
      }
      for(long[] groupCount : groupCounts) {
        long groupLength = groupCount[0];
        long numOfGroups = groupCount[1];
        long[] cycle = new long[2];
        cycle[0] = numOfGroups;
        cycle[1] = groupLength;
        cycleList.add(cycle);
      }
      return cycleList;
    }

    private static void generateCycleIndexTerms(ArrayList<Long> vectorType, long initialVectorLength, long vectorLength, long subVectorLengthA, ArrayList<CycleIndexTerm> cycleIndexTerms) {
      for(long subVectorLengthB = 0; subVectorLengthB < vectorLength; subVectorLengthB++) {
        if((vectorLength - subVectorLengthB) > subVectorLengthA) continue;
        ArrayList<Long> newVectorType = new ArrayList<Long>();
        for(long el : vectorType) {
          newVectorType.add(el);
        }
        newVectorType.add(vectorLength - subVectorLengthB);
        if(subVectorLengthB == 0) {
          ArrayList<long[]> cycleList = getCycleListFromVectorType(newVectorType);
          CycleIndexTerm term = new CycleIndexTerm(cycleList, initialVectorLength);
          cycleIndexTerms.add(term);
        } else generateCycleIndexTerms(newVectorType, initialVectorLength, subVectorLengthB, vectorLength - subVectorLengthB, cycleIndexTerms);
      }
    }

    private static void seedAndGenerateCycleIndexterms(long vectorLength, ArrayList<CycleIndexTerm> cycleIndexTerms) {
      ArrayList<Long> vectorType = new ArrayList<Long>();
      generateCycleIndexTerms(vectorType, vectorLength, vectorLength, vectorLength, cycleIndexTerms);
    }

    private static BigInteger factorial(long N) {
      BigInteger f = BigInteger.ONE;
      for (long i = 2; i <= N; i++)
          f = f.multiply(BigInteger.valueOf(i));

      return f;
    }

    private static BigInteger combinationWithRepetition(long r, long n) {
      return factorial(r + n - 1).divide(factorial(r).multiply(factorial(n - 1)));
    }

    public static String solution(long h, long w, long s) {
      if(w == 1 && h == 1) return String.valueOf(s);
      if(h == 1) {
        return String.valueOf(combinationWithRepetition(w, s));
      } else if(w == 1) {
        return String.valueOf(combinationWithRepetition(h, s));
      }
      ArrayList<CycleIndexTerm> rowIndexTerms = new ArrayList<CycleIndexTerm>();
      ArrayList<CycleIndexTerm> colIndexTerms = new ArrayList<CycleIndexTerm>();
      seedAndGenerateCycleIndexterms(w, rowIndexTerms);
      seedAndGenerateCycleIndexterms(h, colIndexTerms);
      BigInteger total = BigInteger.ZERO;
      for(CycleIndexTerm rowIndexTerm : rowIndexTerms) {
        for(CycleIndexTerm colIndexTerm : colIndexTerms) {
          BurnsideLemmaTerm burnsideTerm = new BurnsideLemmaTerm(rowIndexTerm, colIndexTerm);
          total = total.add(burnsideTerm.evaluateForStates(s));
        }
      }
      BigInteger colPermutations = factorial(h);
      BigInteger rowPermutations = factorial(w);
      BigInteger denominator = colPermutations.multiply(rowPermutations);
      total = total.divide(denominator);
      return total.toString();
    }
}