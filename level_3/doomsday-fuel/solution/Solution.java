import java.util.ArrayList;

public class Solution {
    public static int[] solution(int[][] m) {
        /*
            This was a tough one! I had to research Markov chains and matrices to be able to solve it

            Solution steps:
            1. Put the matrix into canonical order
            2. Find matrix Q, subtract it from an identity matrix the same size
            3. Find the inverse of I - Q, the result is F (the fundamental matrix)
            4. Get R (matrix containing probabilities of moving from a non-absorbing state to an absorbing state)
            5. Multiply F and R
            6. Find a common denominator for the fractions in the first row and format the first row as such:
            [numerator, numerator, ...numerators, denominator]
        */
        //if matter is already in a stable state, return 1/1
        if(m.length == 1) {
            int[] solved = {1,1};
            return solved;
        } else {
            FMatrix t = new FMatrix(m);
            //reorder the matrix to achieve canonical form (absorbing states first)
            FMatrix tC = FMatrix.toCanonicalForm(t);
            //get the result of an identity matrix the size of Q (probability of moving from one non-absorbing state to another) - Q
            FMatrix F = FMatrix.inverse(FMatrix.getIMinusQ(tC));
            FMatrix R = FMatrix.getR(tC);
            FMatrix FR = FMatrix.multiply(F, R);
            int[] formatted = FMatrix.format(FR);
            return formatted;
        }
    }
}

class Fraction {
  public long numerator;
  public long denominator;

  //private method used in simply to find the greatest common denominator
  private static long gcd(long a, long b) {
    return b == 0 ? a : gcd(b, a % b);
  }

  //function to find the gcd then divide by it to simplify an instance of the Fraction class
  public void simplify() {
    long gcd = Fraction.gcd(Math.abs(this.numerator), this.denominator);
    this.numerator /= gcd;
    this.denominator /= gcd;
  }

  public static Fraction add(Fraction f1, Fraction f2) {
        Fraction result = new Fraction(((f1.numerator * f2.denominator) + (f2.numerator * f1.denominator)), (f1.denominator * f2.denominator));
        result.simplify();
        return result;
  }

  public static Fraction subtract(Fraction f1, Fraction f2) {
    Fraction result = new Fraction(((f1.numerator * f2.denominator) - (f2.numerator * f1.denominator)), (f1.denominator * f2.denominator));
    result.simplify();
    return result;
  }

  public static Fraction multiply(Fraction f1, Fraction f2) {
        Fraction result = new Fraction((f1.numerator * f2.numerator), (f1.denominator * f2.denominator));
        result.simplify();
        return result;
    }

  public static Fraction divide(Fraction f1, Fraction f2) {
        Fraction reciprocal = new Fraction(f2.denominator, f2.numerator);
        Fraction result = multiply(f1, reciprocal);
        return result;
  }

  public Fraction(long n, long d) {
    numerator = n;
    denominator = d;
  }
}

class FMatrix { //a rational transition matrix
  public int height; //number of rows
  public int width; //number of columns
  public ArrayList<Integer> absorbingStates;
  public ArrayList<Integer> nonAbsorbingStates;
  public Fraction[][] matrix; //the actual matrix

  //constructors
  //construct a new FMatrix of a given size
  public FMatrix(int h, int w) {
    height = h;
    width = w;
    absorbingStates = new ArrayList<Integer>();
    nonAbsorbingStates = new ArrayList<Integer>();
    matrix = new Fraction[height][width];
  }

  //construct a FMatrix from a matrix of integers
  public FMatrix(int[][] m) {
    height = m.length;
    width = m[0].length;
    absorbingStates = new ArrayList<Integer>();
    nonAbsorbingStates = new ArrayList<Integer>();
    matrix = new Fraction[height][width];
    int denominator;
    for(int i = 0; i < height; i++) {
      denominator = 0;
      int j;
      for(j = 0; j < width; j++) {
        denominator += m[i][j];
      }
      if(denominator == 0) {
        absorbingStates.add(i);
        for(j = 0; j < width; j++) {
          if(j == i) {
            matrix[i][j] = new Fraction(1, 1);
          } else {
            matrix[i][j] = new Fraction(0, 1);
          }
        }
      } else {
        nonAbsorbingStates.add(i);
        for(j = 0; j < width; j++) {
          matrix[i][j] = new Fraction(m[i][j], denominator);
        }
      }
    } 
  }

  //construct a new matrix from a region of another FMatrix
  //i1, i2, j1, j2 are uninclusive
  public FMatrix(FMatrix f, int i1, int i2, int j1, int j2) {
    height = i2 - i1;
    width = j2 - j1;
    matrix = new Fraction[height][width];
    int a = 0;
    int b = 0;
    for(int i = i1; i < i2; i++) {
      for(int j = j1; j < j2; j++) {
        matrix[a][b] = f.matrix[i][j];
        b++;
      }
      a++;
      b=0;
    }
  }

  //returns a new FMatrix in canonical form
  public static FMatrix toCanonicalForm(FMatrix t) {
    FMatrix result = new FMatrix(t.height, t.width);
    //arraylist to hold the ints referring to the original state orders, but in canonical order
    ArrayList<Integer> reorderedStates = new ArrayList<Integer>();
    //add absorbing states first
    int s = 0; //keeps track of what state in the new matrix we are at
    for(int i = 0; i < t.absorbingStates.size(); i++) {
      reorderedStates.add(t.absorbingStates.get(i));
      result.absorbingStates.add(s);
      s++;
    }
    //then non absorbing states
    for(int i = 0; i < t.nonAbsorbingStates.size(); i++) {
      reorderedStates.add(t.nonAbsorbingStates.get(i));
      result.nonAbsorbingStates.add(s);
      s++;
    }
    for(int i = 0; i < reorderedStates.size(); i++) {
      for(int j = 0; j < reorderedStates.size(); j++) {
        result.matrix[i][j] = t.matrix[reorderedStates.get(i)][reorderedStates.get(j)];
      }
    }
    return result;
  }

  /*for a matrix in canoncial form, get matrix R, which contains the probabilities of moving from
  a non-absorbing state to an absorbing state
  */
  public static FMatrix getR(FMatrix t) {
    //R's rows span the first non-absorbing state, to the bottom of the matrix
    int i1 = t.nonAbsorbingStates.get(0);
    int i2 = t.matrix.length;
    //R's columns span the first absorbing state to the last absorbing state
    int j1 = t.absorbingStates.get(0);
    int j2 = t.nonAbsorbingStates.get(0); //j2 is uninclusive
    return new FMatrix(t, i1, i2, j1, j2);
  }

  /*for a matrix in canoncial form, get matrix Q, which contains the probabilities of moving from
  a non-absorbing state to another non-absorbing state
  */
  public static FMatrix getQ(FMatrix t) {
    //Q is a square so i1 = j1 and i2 = j2
    //Q spans the first non-absorbing state to the end of the matrix
    int i1 = t.nonAbsorbingStates.get(0);
    int i2 = t.matrix.length;
    return new FMatrix(t, i1, i2, i1, i2);
  }

  //used to get the fundamental matrix, in the equation F = (I - Q)^-1
  public static FMatrix getIMinusQ(FMatrix t) {
    //get Q
    FMatrix Q = FMatrix.getQ(t);

    //create an identity matrix of the same size
    FMatrix I = new FMatrix(Q.width, Q.height);
    //fill it with ones at points i,j=i and zeroes elsewhere
    for(int i = 0; i < I.width; i++) {
      for(int j = 0; j < I.height; j++) {
        if(i == j) I.matrix[i][j] = new Fraction(1, 1);
        else I.matrix[i][j] = new Fraction(0, 1);
      }
    }

    //subtract Q from I and return
    FMatrix result = new FMatrix(Q.width, Q.height);
    for(int i = 0; i < result.width; i++) {
      for(int j = 0; j < result.height; j++) {
        result.matrix[i][j] = Fraction.subtract(I.matrix[i][j], Q.matrix[i][j]);
      }
    }
    return result;
  }

  //inverse and multiplication
  private static FMatrix createAugmentedMatrix(FMatrix m) {
    FMatrix a = new FMatrix(m.height, m.width * 2);
    for(int i = 0; i < a.height; i++) {
        for(int j = 0; j < a.width; j++) {
            if (j < m.width) {
                a.matrix[i][j] = m.matrix[i][j];
            } else if (j == m.width + i){
                a.matrix[i][j] = new Fraction(1, 1);
            } else a.matrix[i][j] = new Fraction(0, 1);
        }
    }
    return a;
  }

//takes an augmented matrix and transforms it into an inverse of the original matrix
  public static FMatrix inverse(FMatrix m) {
      FMatrix augM = FMatrix.createAugmentedMatrix(m);
      //p is the pivot, which moves diagonally down to the bottom of the matrix
      for(int p = 0; p < m.height; p++) {
          //modify the rows of the matrix above p
          for(int i = p - 1; i >= 0; i--) {
              Fraction factor = Fraction.divide(augM.matrix[i][p], augM.matrix[p][p]); //determine what to multiply the row by to cancel out the digit above
              for(int j = 0; j < augM.width; j++) {
                augM.matrix[i][j] = Fraction.subtract(augM.matrix[i][j], Fraction.multiply(augM.matrix[p][j], factor));
              }
          }
          //modify the rows of the matrix below p
          for(int i = p + 1; i < augM.height; i++) {
              Fraction factor = Fraction.divide(augM.matrix[i][p], augM.matrix[p][p]); //determine what to multiply the row by to cancel out the digit above
              for(int j = 0; j < augM.width; j++) {
                augM.matrix[i][j] = Fraction.subtract(augM.matrix[i][j], Fraction.multiply(augM.matrix[p][j], factor));
              }
          }
      }
      //now for each row, divide the row by the element at the pivot
      for(int p = 0; p < augM.height; p++) {
        Fraction divisor = augM.matrix[p][p];
        for(int i = 0; i < augM.width; i++) {
            augM.matrix[p][i] = Fraction.divide(augM.matrix[p][i], divisor);
        }
    }

    //finally return the inverse of the matrix, which exists where the identity matrix existed formerly
    FMatrix inverse = new FMatrix(m.height, m.width);
    for(int i = 0; i < augM.height; i++) {
        for(int j = m.width; j < augM.width; j++) {
            inverse.matrix[i][j - m.width] = augM.matrix[i][j];
        }
    }
    return inverse;
  }

   public static FMatrix multiply(FMatrix t1, FMatrix t2) {
    FMatrix result = new FMatrix(t1.height, t2.width);
    for (int i = 0; i < t1.height; i++) {
      for (int j = 0; j < t2.width; j++) {
	      Fraction sum = new Fraction(0, 1);
	      for (int k = 0; k < t2.height; k++) {
		    sum = Fraction.add(sum, Fraction.multiply(t1.matrix[i][k], t2.matrix[k][j]));
	      }
	      result.matrix[i][j] = sum;
	    }
    }
    return result;
  }

  public static int[] format(FMatrix solutionMatrix) {
    long largestDenominator = 0;
    Fraction[] fractions = solutionMatrix.matrix[0]; //the first row of the solution matrix
    int[] result = new int[fractions.length + 1];
    //find the higest denominator
    for(int i = 0; i < fractions.length; i++) {
        if(fractions[i].denominator > largestDenominator) largestDenominator = fractions[i].denominator;
    }
    long lcm = largestDenominator;
    while(true) {
      boolean found = true;
      for(int i = 0; i < fractions.length; i++) {
        if(lcm % fractions[i].denominator != 0) {
          found = false;
          break;
        }
      }
      if(found) break;
      else lcm += (long)largestDenominator;
    }
    for(int i = 0; i < fractions.length; i++) {
      result[i] = (int)(fractions[i].numerator * (lcm / fractions[i].denominator));
    }
    result[result.length - 1] = (int)lcm;
    return result;
  }
}