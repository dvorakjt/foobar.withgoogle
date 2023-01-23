public class Solution {
     public static int solution(int x, int y) {
          //d represents the base of the diagonal line that contains the bunny's id
          int d = x + y - 1;

          //get the triangular number at the base of this diagonal
          int triangularNumber = (d * (d + 1)) / 2;

          //count up along the diagonal to get the id
          int id = triangularNumber - (y - 1);

          return id;
     }
}