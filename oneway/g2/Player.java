package oneway.g2;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;

public class Player extends oneway.sim.Player
{

    public Player() {}

    public void init(int nsegments, int nblocks, int[] capacity)
    {
        this.nsegments = nsegments;
        this.nblocks = nblocks;
        this.capacity = capacity.clone();
    }


    public void setLights(MovingCar[] movingCars,
                          Parking[] left,
                          Parking[] right,
                          boolean[] llights,
                          boolean[] rlights)
    {
    	for (int i = 0; i != nsegments; ++i) {
            	llights[i] = false;
            	rlights[i] = false;
        }
        for (int i = 0; i != nsegments; ++i) {
        	if(i!=nsegments-1)
        		llights[i] = true;
            if (!noCrash(movingCars, left, right, llights, rlights)){
            	llights[i] = false;
            }
            
            if (i!=0)
            	rlights[i] = true;
            if (!noCrash(movingCars, left, right, llights, rlights)){
            	rlights[i] = false;
            }	
        }
        int i=nsegments-1;
        llights[i] = true;
        left[nsegments].add(0);
        if (!noCrash(movingCars, left, right, llights, rlights)){
        	llights[i] = false;
        }
        left[nsegments].removeLast();
        i=0;
        rlights[i] = true;
        right[0].add(0);
        if (!noCrash(movingCars, left, right, llights, rlights)){
        	rlights[i] = false;
        }	
    	right[0].removeLast();
        //rlights[0]=true;
        System.err.println(noCrash(movingCars, left, right, llights, rlights));
    }


    // check if the segment has traffic
    private boolean hasTraffic(MovingCar[] cars, int seg, int dir) {
        for (MovingCar car : cars) {
            if (car.segment == seg && car.dir == dir)
                return true;
        }
        return false;
    }

    private boolean noCrash(MovingCar[] movingCars,
            Parking[] left,
            Parking[] right,
            boolean[] llights,
            boolean[] rlights){
    	
    	//update cars
    	LinkedList<MovingCar> addedCars=new LinkedList<MovingCar>();
    	for (int i = 0; i < right.length-1; i++) 
    	if(right[i].size()>0 && rlights[i]){
    		MovingCar c=new MovingCar(i, -1, 1, 0);
			addedCars.add(c);
		}
    	for (int i = 1; i < left.length; i++) 
        	if(left[i].size()>0 && llights[i-1]){
        		MovingCar c=new MovingCar(i-1, nblocks+1, -1, 0);
    			addedCars.add(c);
    		}
    	for (int i = 0; i < movingCars.length; i++) {
			addedCars.add(movingCars[i]);
		}
    	movingCars=addedCars.toArray(new MovingCar[0]);
    	//sort cars
    	for (int i = 0; i < movingCars.length-1; i++) {
			for (int j = i+1; j < movingCars.length; j++) 
			if (movingCars[i].segment>movingCars[j].segment || ((movingCars[i].segment==movingCars[j].segment)&&(movingCars[i].block>movingCars[j].block))){
				MovingCar t=movingCars[i];
				movingCars[i]=movingCars[j];
				movingCars[j]=t;
			}
		}
    	//get park info
    	int[] parked=new int[nsegments+1];
    	for (int i = 1; i < parked.length-1; i++) {
			parked[i]=left[i].size()+right[i].size();
		}
    	//judge
    	int[] blocking=new int[movingCars.length];
    	
    	for (int i = movingCars.length-1; i >=0 ; i--) 
    	if (movingCars[i].dir==1){
    		MovingCar c=movingCars[i];
			for (int j = c.segment+1; j < parked.length; j++) 
			if (parked[j]<capacity[j]){
				parked[j]++;
				blocking[i]=j*nblocks-(c.segment*nblocks+c.block);
				break;
			}
		}
    	for (int i = 0; i <movingCars.length ; i++) 
        	if (movingCars[i].dir==-1){
        		MovingCar c=movingCars[i];
    			for (int j = c.segment; j >=0; j--) 
    			if (parked[j]<capacity[j]){
    				parked[j]++;
    				blocking[i]=(c.segment*nblocks+c.block)-j*nblocks;
    				break;
    			}
    		}
    	int crash=0;
    	for (int i = 0; i <movingCars.length ; i++){
    		for (int j = 0; j <movingCars.length ; j++)
    			if (movingCars[i].dir==1 && movingCars[j].dir==-1){
    				int min=blocking[i]<blocking[j]?blocking[i]:blocking[j];
    				int posi=movingCars[i].segment*nblocks+movingCars[i].block;
    				int posj=movingCars[j].segment*nblocks+movingCars[j].block;
    				if (min+min>posj-posi){
    					crash++;
    					int crash_pos=(posi+posj)/2;
    					if ((crash_pos+1)%nblocks==0 && crash_pos!=(posi+posj+1)/2 ) //maybe a bug? 
    						crash--;
    				}
    			}
    		if (crash>0) break;
    	}
    	if (crash==0) return true;
    	
    	//do a reverse check again
    	return false;
    }

    private int nsegments;
    private int nblocks;
    private int[] capacity;
}
