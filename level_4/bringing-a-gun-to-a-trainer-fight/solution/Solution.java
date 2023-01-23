import java.util.Objects;
import java.util.HashSet;
import java.util.Iterator;

class BeamPosition {
  double x;
  double y;
  double distance_x;
  double distance_y;
  double direction_x;
  double direction_y;

  @Override 
  public int hashCode() {
    return(Objects.hash(this.x, this.y, this.distance_x, this.distance_y, this.direction_x, this.direction_y));
  }
  
  //this will allow me to collapse some paths
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BeamPosition other = (BeamPosition) obj;
    return this.x == other.x && this.y == other.y && this.distance_x == other.distance_x && this.distance_y == other.distance_y &&
    this.direction_x == other.direction_x && this.direction_y == other.direction_y;
  } 

  BeamPosition(double x, double y, double dis_x, double dis_y, double dir_x, double dir_y) {
    this.x = x;
    this.y = y;
    this.distance_x = dis_x;
    this.distance_y = dis_y;
    this.direction_x = dir_x;
    this.direction_y = dir_y;
  }

  //new_x and new_y refer to new x and y positions
  BeamPosition(BeamPosition s, double new_x, double new_y) {
    this.distance_x = s.distance_x;
    this.distance_y = s.distance_y;
    this.direction_x = s.direction_x;
    this.direction_y = s.direction_y;
    if(new_x != s.x) {
        double added_distance_x = Math.abs(new_x - s.x);
        if(this.distance_x == 0) {
            this.direction_x = (new_x - s.x) / added_distance_x;
        }
        this.distance_x += added_distance_x;
        this.x = new_x;
    } else this.x = s.x;
    if(new_y != s.y) {
        double added_distance_y = Math.abs(new_y - s.y);
        if(this.distance_y == 0) {
            this.direction_y = (new_y - s.y) / added_distance_y;
        }
        this.distance_y += added_distance_y;
        this.y = new_y;
    } else this.y = s.y;
  }
}

class Trajectory {
  double x;
  double y;
  int bounces;

  @Override 
  public int hashCode() {
    return(Objects.hash(this.x, this.y));
  }

  @Override
  public boolean equals(Object obj) {
	if (this == obj) return true;
	if (obj == null) return false;
	if (getClass() != obj.getClass()) return false;
	Trajectory other = (Trajectory) obj;
	return this.x == other.x && this.y == other.y;
  } 

  private static double gcd(double a, double b) {
    return b == 0 ? a : gcd(b, a % b);
  }

  public double getBeamLength() {
    return Math.sqrt((this.x * this.x) + (this.y * this.y));
  }

  // public void print() {
  //   System.out.println("Trajectory: [" + this.x + ", " + this.y + "] + | Bounces: " + this.bounces);
  // }

  public void simplify() {
    double g = Math.abs(gcd(this.x, this.y));
    if(g == 0) g = 1;
    this.x /= g;
    this.y /= g;
  }

  public boolean verify(double[] starting_position, double[] goal, double dim_x, double dim_y) {
    double[] current_position = new double[2];
    double[] previous_position = new double[2];
    double[] trajectory = new double[2];
  
    current_position[0] = starting_position[0];
    current_position[1] = starting_position[1];
    trajectory[0] = this.x;
    trajectory[1] = this.y;
    
    for(int i = 0; i <= this.bounces; i++) {
      do {
        current_position[0] += trajectory[0];
        current_position[1] += trajectory[1];
        //the shot hit a corner so return false
        if((current_position[0] == 0 || current_position[0] == dim_x) && (current_position[1] == 0 || current_position[1] == dim_y)) {
            return false;
        }
        //friendly fire! return false
        if(current_position[0] == starting_position[0] && current_position[1] == starting_position[1]) {
          return false;
        }
        if(current_position[0] == goal[0] && current_position[1] == goal[1]) {
          return true;
        }
      } while(current_position[0] > 0 && current_position[0] < dim_x && current_position[1] > 0 && current_position[1] < dim_y);
      double theta = Math.atan(trajectory[0] / trajectory[1]);
      //mirror the shot
      previous_position[0] = current_position[0] - trajectory[0];
      previous_position[1] = current_position[1] - trajectory[1];
      //determine distances to  boundaries
      double distance_to_boundary_x;
      double distance_to_boundary_y;
      if(trajectory[0] < 0) distance_to_boundary_x = previous_position[0]; //left and right walls
      else distance_to_boundary_x = dim_x - previous_position[0];
      if(trajectory[1] < 0) distance_to_boundary_y = previous_position[1]; //top and bottom walls
      else distance_to_boundary_y = dim_y - previous_position[1];
  
      //where is x when y has reached the boundary?
      double test_distance_to_boundary_y = Math.tan(theta) * distance_to_boundary_y;
  
      if(Math.abs(test_distance_to_boundary_y) > Math.abs(distance_to_boundary_x)) {
        //only flip the position if the boundary has been passed, not directly struck
        if(current_position[0] != 0 && current_position[0] != dim_x) {
          if(current_position[0] < 0) current_position[0] = -previous_position[0];
          if(current_position[0] > dim_x) current_position[0] = dim_x * 2 - previous_position[0];
          current_position[1] = previous_position[1];
        }
        trajectory[0] *= -1;
      } else {
        if(current_position[1] != 0 && current_position[1] != dim_y) {
          if(current_position[1] < 0) current_position[1] = -previous_position[1];
          if(current_position[1] > dim_y) current_position[1] = dim_y * 2 - previous_position[1];
          current_position[0] = previous_position[0];
        }
        trajectory[1] *= -1;
      }
    }
    return false;
  }


  Trajectory(BeamPosition s, double[] goal, int bounces) {
    double final_distance_x = Math.abs(goal[0] - s.x);
    double final_distance_y = Math.abs(goal[1] - s.y);
    double multiplier_x;
    double multiplier_y;
    if(s.direction_x != 0) multiplier_x = s.direction_x;
    else if(final_distance_x != 0) {
      multiplier_x = (goal[0] - s.x) / final_distance_x;
    } else multiplier_x = 0;
    if(s.direction_y != 0) multiplier_y = s.direction_y;
    else if(final_distance_y != 0) {
      multiplier_y = (goal[1] - s.y) / final_distance_y;
    } else multiplier_y = 0;
    double x =(s.distance_x + final_distance_x) * multiplier_x;
    double y = (s.distance_y + final_distance_y) * multiplier_y;
    this.x = x;
    this.y = y;
    this.bounces = bounces;
  }
}

public class Solution {
  public static int solution(int[] dimensions, int[] your_position, int[] trainer_position, int distance) {
    double dim_x = (double) dimensions[0];
    double dim_y = (double) dimensions[1];

    double[] yp = new double[2];
    double[] tp = new double[2];
    yp[0] = (double)your_position[0];
    yp[1] = (double)your_position[1];
    tp[0] = (double)trainer_position[0];
    tp[1] = (double)trainer_position[1];

    double max_beam_length = (double) distance;
    
    HashSet<Trajectory> trajectories = new HashSet<Trajectory>();
    HashSet<BeamPosition> previous_positions = new HashSet<BeamPosition>();
    BeamPosition starting_position = new BeamPosition(yp[0], yp[1], 0.0, 0.0, 0.0, 0.0);
    previous_positions.add(starting_position);

    for(int bounces = 0; ; bounces++) {
        Iterator itr = previous_positions.iterator();
        HashSet<BeamPosition> current_positions = new HashSet<BeamPosition>();
        double min_beam_length = Double.POSITIVE_INFINITY;
        while(itr.hasNext()) {
            BeamPosition starting_point = (BeamPosition) itr.next();
            Trajectory t = new Trajectory(starting_point, tp, bounces);
            double beam_length = t.getBeamLength();
            if(beam_length < min_beam_length) min_beam_length = beam_length;
            if(beam_length <= max_beam_length) {
              t.simplify();
              trajectories.add(t);
            }
            BeamPosition[] possibilities = new BeamPosition[4];
            possibilities[0] = new BeamPosition(starting_point, 0.0, starting_point.y);
            possibilities[1] = new BeamPosition(starting_point, dim_x, starting_point.y);
            possibilities[2] = new BeamPosition(starting_point, starting_point.x, 0.0);
            possibilities[3] = new BeamPosition(starting_point, starting_point.x, dim_y);
            for(BeamPosition possibility : possibilities) {
                if(possibility.x == starting_point.x && possibility.y == starting_point.y) continue;
                current_positions.add(possibility);
            }
        }
        if(min_beam_length > max_beam_length) break;
        previous_positions = current_positions;
    }

    int count = 0;
    Iterator tItr = trajectories.iterator();
    while (tItr.hasNext()) {
        Trajectory t = (Trajectory) tItr.next();
        //call verify method on each trajectory
        if(t.verify(yp, tp, dim_x, dim_y)) {
          // t.print();
          count++;
        }
    }
    return count;
  }
}