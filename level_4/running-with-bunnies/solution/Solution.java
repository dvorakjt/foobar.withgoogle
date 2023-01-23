import java.util.Arrays;
import java.util.ArrayList;

class TimeAndLocation {
    int current_location;
    int time_remaining;
    ArrayList<Integer> bunnies_rescued;

    public void rescueBunny(int[][] times, int location) {
      if(location != 0 && location != times.length - 1 && !this.bunnies_rescued.contains(location)) {
        this.bunnies_rescued.add(location);
      }
    } 

    public int[] getRescuedBunnies() {
      int[] br = new int[this.bunnies_rescued.size()];
      for(int i = 0; i < this.bunnies_rescued.size(); i++) {
          br[i] = this.bunnies_rescued.get(i) - 1;
      }
      Arrays.sort(br);
      return br;
    }

    public ArrayList<Integer> getUnrescuedBunnyLocations(int[][] times) {
        ArrayList<Integer> unrescued_bunny_locations = new ArrayList<Integer>();
        int first_bunny = 1;
        int last_bunny = times.length - 2;
        for(int i = first_bunny; i <= last_bunny; i++) {
            if(!this.bunnies_rescued.contains(i)) {
                unrescued_bunny_locations.add(i);
            }
        }
        return unrescued_bunny_locations;
    }

    public TimeAndLocation moveTo(int[][] times, int new_location) {
        int new_time_remaining = this.time_remaining - times[this.current_location][new_location];
        TimeAndLocation new_time_and_location = new TimeAndLocation(new_location, new_time_remaining);
        for(int i = 0; i < this.bunnies_rescued.size(); i++) {
            new_time_and_location.bunnies_rescued.add(this.bunnies_rescued.get(i));
        }
        new_time_and_location.rescueBunny(times, new_location);
        return new_time_and_location;
    }

    public TimeAndLocation moveByShortestRoute(int[][] times, int goal, int[] history) {
        TimeAndLocation max_time_remaining_route = this.moveTo(times, goal);
        //if the route is circular, set the max_time_remaining to an extremely low number to guarantee
        //another option will be selected
        if(max_time_remaining_route.current_location == this.current_location) {
            max_time_remaining_route.time_remaining = -2147483647;
        }
        if(history == null) {
            history = new int[1];
            history[0] = this.current_location;
        }
        for(int i = 0; i < times.length; i++) {
            int[] new_history = new int[history.length + 1];
            boolean already_visited = false;
            for(int j = 0; j < history.length; j++) {
                new_history[j] = history[j];
                if(history[j] == i) already_visited = true;
            }
            new_history[new_history.length - 1] = i;
            if(!already_visited && i != goal) {
                TimeAndLocation new_time_and_location = this.moveTo(times, i);
                new_time_and_location = new_time_and_location.moveByShortestRoute(times, goal, new_history);
                if(new_time_and_location.time_remaining >= max_time_remaining_route.time_remaining) {
                    max_time_remaining_route = new_time_and_location;
                }
            }
        }
        return max_time_remaining_route;
    }

    TimeAndLocation(int location, int time) {
      this.time_remaining = time;
      this.current_location = location;
      this.bunnies_rescued = new ArrayList<Integer>();
    }
}

public class Solution {
    private static ArrayList<int[]> successful_rescues = new ArrayList<int[]>();

    private static void travelToBulkhead(TimeAndLocation t, int[][] times, int time_limit, ArrayList<int[]> successful_rescues) {
        //first determine if the current TimeAndLocation represents a successful rescue attempt
        TimeAndLocation time_at_bulkhead = t.moveByShortestRoute(times, times.length - 1, null);
        if(time_at_bulkhead.time_remaining >= 0) {
            int[] rescued_bunnies = time_at_bulkhead.getRescuedBunnies();
            if(rescued_bunnies.length > 0) {
                successful_rescues.add(rescued_bunnies);
            }
        }
        //now continue to travel to all of the bunnies
        ArrayList<Integer> bunnies_to_rescue = t.getUnrescuedBunnyLocations(times);
        for(int i = 0; i < bunnies_to_rescue.size(); i++) {
            int new_location = bunnies_to_rescue.get(i);
            TimeAndLocation new_t = t.moveByShortestRoute(times, new_location, null);
            travelToBulkhead(new_t, times, time_limit, successful_rescues);
        }
    }

    public static int[] solution(int[][] times, int time_limit) {
        //first determine if there are any positive feedback loops which could infinitely increase time
        boolean contains_feedback_loop = false;
        for(int i = 0; i < times.length; i++) {
            TimeAndLocation start_test = new TimeAndLocation(i, 0);
            TimeAndLocation end_test = start_test.moveByShortestRoute(times, i, null);
            if(end_test.time_remaining > 0) {
                contains_feedback_loop = true;
                break;
            }
        }
        if(contains_feedback_loop) {
          int[] all_bunnies = new int[times.length - 2];
          for(int i = 0; i < all_bunnies.length; i++) {
            all_bunnies[i] = i;
          }
          return all_bunnies;
        } else {
          //if there aren't, find the optimal possibility
          ArrayList<int[]> rescue_possibilities = new ArrayList<int[]>();
          TimeAndLocation start_t = new TimeAndLocation(0, time_limit);
          travelToBulkhead(start_t, times, time_limit, rescue_possibilities);
          int[] optimal_rescue = new int[0];
          for(int k = 0; k < rescue_possibilities.size(); k++) {
              int[] rescue = rescue_possibilities.get(k);
              if(rescue.length > optimal_rescue.length) optimal_rescue = rescue;
              else if(rescue.length == optimal_rescue.length) {
                  boolean contains_lower_bunny_ids = false;
                  for(int m = 0; m < rescue.length; m++) {
                      if(optimal_rescue[m] < rescue[m]) break;
                      else if(optimal_rescue[m] > rescue[m]) {
                          contains_lower_bunny_ids = true;
                          break;
                      } else continue;
                  }
                  if(contains_lower_bunny_ids) optimal_rescue = rescue;
              }
          }
          return optimal_rescue;
        }
    }
}