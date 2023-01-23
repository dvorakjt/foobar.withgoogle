public class Solution {
    private static int sum;

    private static double max(double n, double l) {
        return Math.round((Math.round(n / 2) * 2) / l) - Math.floor(l / 2);
    }

    private static void count(double n, double l, double min, double max) {
        if(l == 2) {
            double total = (Math.round(n / 2) - min);
            if(total > 0) sum += (int)total;
        } else {
            for(double i = min; i <= max; i++) {
              double newN = n - i;
              double newL = l - 1;
              double newMin = i + 1;
              double newMax = max(newN, newL);
              count(newN, newL, newMin, newMax);
            }
        }
    }

    private static int solution(int N) {
        //loop through lengths
        double n = (double)N;
        double l = 2;
        double s = 1;
        double m = max(n, l);
        sum = 0;
        while(m > 0) {
            count(n, l, s, m);
            l++;
            m = max(n, l);
        }
        return sum;
    }
}
