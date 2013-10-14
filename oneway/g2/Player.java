package oneway.g2;

import oneway.sim.Parking;

import java.util.*;

public class Player extends oneway.sim.Player {

	public Player() {
	}
	int left_entry_tick=0;
	int right_entry_tick=0;
	boolean flag =true;
//	boolean entry=false;
	int count=0;
	int tick = 0;
	int[] position;
	boolean canSync;

	public void init(int nsegments, int[] nblocks, int[] capacity) {
		this.nsegments = nsegments;
		this.nblocks = nblocks.clone();
		this.capacity = capacity.clone();
		position = capacity.clone();
		position[0] = 0;
		for (int i = 1; i < position.length; i++) {
			position[i] = position[i - 1] + nblocks[i - 1];
		}
		canSync=true;
		for(int i=1;i<nblocks.length;i++){
			if (nblocks[i]!=nblocks[0])
				canSync=false;
		}
		if(nblocks[0]>2)
			canSync=false;
		if (canSync) {
			tick=-2;
		}
	}

	public void setLights(oneway.sim.MovingCar[] old_movingCars, Parking[] left,
			Parking[] right, boolean[] llights, boolean[] rlights) {
		MovingCar[] movingCars = new MovingCar[old_movingCars.length];
		for (int i = 0; i < movingCars.length; i++) {
			movingCars[i] = new MovingCar(old_movingCars[i].segment,
					old_movingCars[i].block, old_movingCars[i].dir,
					old_movingCars[i].startTime);
		}
		tick++;
		
		if(canSync)
			synchronizedStrategy(movingCars, left, right, llights, rlights);
		else
			strategy0(movingCars, left, right, llights, rlights);
			//revertStrategy(movingCars, left, right, llights, rlights);
		
	}

	private void revertStrategy(MovingCar[] movingCars, Parking[] left,
			Parking[] right, boolean[] llights, boolean[] rlights) {
		/*if(stopDomination(movingCars, left, right, tick)==0){
			strategy0(movingCars, left, right, llights, rlights);
			return;
		}*/
		
		/***********KEVIN - IMPORTANT*****************/
		//TODO add fixed[] array and call strategy 0 after domination checks
		//noCrash always reverts back the changes we make, so it always goes to old lights
		//so domination isn't stopped
		//the method in TODO should solve the problem
		/*************************************/
		
		//default value of lights
		strategy0(movingCars, left, right, llights, rlights);
		int revert=DoWeNeedRevert(movingCars, left, right, tick);
		if(tick>0)
			System.out.println("revert is "+revert);
		//revert the cars from left to right
		int window=6;
		if(revert==1){
			//boolean[] old=new boolean[2];
			int count=0;
			int where=whereToRevert(movingCars, left, right,1);
			for (int i = where; i < where+window; i++) {
				if(i==nsegments) break;
				boolean old=llights[i];
				llights[i]=false;
				if(noCrash(movingCars, left, right, llights, rlights)==false){
					llights[i]=old;
					//break;
				}
			}
			for (int i = where; i < where+window; i++) {
				if(i==nsegments) break;
				boolean old=rlights[i];
				rlights[i]=true;
				if(noCrash(movingCars, left, right, llights, rlights)==false){
					rlights[i]=old;
				}
			}
		}
		
		//revert the cars from right to left
		else if(revert==-1){
			//boolean[] old=new boolean[2];
			//int count=0;
			int where=whereToRevert(movingCars, left, right,-1);
			for (int i = where; i > where-window; i--) {
				if(i==-1) break;
				boolean old=rlights[i];
				rlights[i]=false;
				if(noCrash(movingCars, left, right, llights, rlights)==false){
					rlights[i]=old;
					//break;
				}
			}
			for (int i = where; i > where-window; i--) {
				if(i==-1) break;
				boolean old=llights[i];
				llights[i]=true;
				if(noCrash(movingCars, left, right, llights, rlights)==false){
					llights[i]=old;
				}
			}

		}
		
	}

	//Finds the parking lot where the max penalty has accumulated
	//revert process starts from this lot
	private int whereToRevert(MovingCar[] movingCars, Parking[] left,
			Parking[] right, int direction) {
		
		float max_penalty=0;
		int where=0;
		if(direction==-1)
		{
			int i=0;
			for(i=0;i<nsegments;i++)
			{
				float lot_penalty=0;
				LinkedList<Integer> parking= left[i];
				if (parking!=null)
					for(int j=0;j<parking.size();j++)
					{	
								int temp=parking.get(j);
								int time_waited=tick-temp;
								lot_penalty+=(time_waited*Math.log(time_waited));
					}
				if(lot_penalty>max_penalty)
				{
					max_penalty=lot_penalty;
					where=i;
				}
			}
			return where;
		}
		else
		{
			int i=0;
			for(i=0;i<nsegments;i++)
			{
				float lot_penalty=0;
				LinkedList<Integer> parking= right[i];
				if(parking!=null)
					for(int j=0;j<parking.size();j++)
					{	
								int temp=parking.get(j);
								int time_waited=tick-temp;
								lot_penalty+=(time_waited*Math.log(time_waited));
					}
				if(lot_penalty>max_penalty)
				{
					max_penalty=lot_penalty;
					where=i;
				}
			}
			return where;
		}
		
	}

	private void synchronizedStrategy(MovingCar[] movingCars,
			Parking[] left, Parking[] right, boolean[] llights,
			boolean[] rlights) {
		for (int i = 0; i != nsegments; ++i) {
			llights[i] = true;
			rlights[i] = true;
		}
		llights[nsegments-1]=false;
		rlights[0]=false;
		if(nsegments%2==0){
			if(tick%(2*nblocks[0])==0){
				llights[nsegments-1]=true;
				rlights[0]=true;
			}	
		}
		else{
			if(tick%(2*nblocks[0])==0){
				llights[nsegments-1]=true;
			}
			if(tick % (2*nblocks[0]) == (nblocks[0]) ){
				rlights[0]=true;
			}
		}
	}

	private void strategy0(MovingCar[] old_movingCars,
			Parking[] left, Parking[] right, boolean[] llights,
			boolean[] rlights) {
		MovingCar[] movingCars = new MovingCar[old_movingCars.length];
		for (int i = 0; i < movingCars.length; i++) {
			movingCars[i] = new MovingCar(old_movingCars[i].segment,
					old_movingCars[i].block, old_movingCars[i].dir,
					old_movingCars[i].startTime);
		}
	
		for (int i = 0; i != nsegments; ++i) {
			llights[i] = false;
			rlights[i] = false;
		}
		// llights[1]=true;
		//llights[2] = true;
		if (tick == 4) {
			boolean t = noCrash(movingCars, left, right, llights, rlights);
			System.out.println(t);
		}
	
		for (int i = 0; i != nsegments; ++i) {
			llights[i] = false;
			rlights[i] = false;
		}
	
		for (int i = 1; i < nsegments ; i++) {
			if (left[i].size() + right[i].size() == capacity[i]) {
				if (isThereCar(movingCars, i - 1, nblocks[i - 1] - 1, 1))
					rlights[i] = true;
				if (isThereCar(movingCars, i, 0, -1))
					llights[i - 1] = true;
			}
		}
	
		for (int i = 0; i != nsegments; ++i) {
			if (i != 0) {
				rlights[i] = !rlights[i];
				if (!noCrash(movingCars, left, right, llights, rlights)) {
					rlights[i] = !rlights[i];
				}
			}
			if (i != nsegments - 1) {
				llights[i] = !llights[i];
				if (!noCrash(movingCars, left, right, llights, rlights)) {
					llights[i] = !llights[i];
				}
			}
		}
		int i = nsegments - 1;
		llights[i] = true;
		left[nsegments].add(0);
		if (!noCrash(movingCars, left, right, llights, rlights)) {
			llights[i] = false;
		}
		//left[nsegments].removeLast();
		i = 0;
		rlights[i] = true;
		right[0].add(0);
		if (!noCrash(movingCars, left, right, llights, rlights)) {
			rlights[i] = false;
		}
		left[nsegments].removeLast();
		right[0].removeLast();
		// rlights[0]=true;
		// System.err.println(tick+" -  "+noCrash(movingCars, left, right,
		// llights, rlights));
	}

	private void strategy1(MovingCar[] old_movingCars,
			Parking[] left, Parking[] right, boolean[] llights,
			boolean[] rlights) {
		MovingCar[] movingCars = new MovingCar[old_movingCars.length];
		for (int i = 0; i < movingCars.length; i++) {
			movingCars[i] = new MovingCar(old_movingCars[i].segment,
					old_movingCars[i].block, old_movingCars[i].dir,
					old_movingCars[i].startTime);
		}
		boolean old_llights[]=new boolean[llights.length];
		boolean old_rlights[]=new boolean[rlights.length];
		
		for(int i=0;i<llights.length;i++)
		{
			old_llights[i]=llights[i];
			old_rlights[i]=rlights[i];
		}
		
		for (int i = 0; i != nsegments; ++i) {
			llights[i] = false;
			rlights[i] = false;
		}
		
		int direction = stopDomination(old_movingCars, left, right, tick);
		
		//parking lot logic
		for (int i = 1; i < nsegments - 1; i++) {
			if (left[i].size() + right[i].size() >= capacity[i]-1) {
				if (isThereCar(movingCars, i - 1, nblocks[i - 1] - 1, 1))
					rlights[i] = true;
				if (isThereCar(movingCars, i, 0, -1))
					llights[i - 1] = true;
			}
		}
		
		
		if(direction == -1)
		{
			int i=0;
			if(!isThereCar2(movingCars, i, 1))
			{
				llights[i]=true;
				rlights[i]=false;
			}
			if(!noCrash(movingCars, left, right, llights, rlights))
			{
				llights[i]=old_llights[i];
				rlights[i]=old_rlights[i];
			}
			for(i=1;i<nsegments-1;i++)
			{
				if(!isThereCar2(movingCars, i, 1) )
				{
					if(left[i].size()+right[i].size()>=capacity[i])
					{
					llights[i]=true;
					rlights[i]=false;
					}
				}
				if(!noCrash(movingCars, left, right, llights, rlights))
				{
					llights[i]=old_llights[i];
					rlights[i]=old_rlights[i];
				}
				
			}
			i=nsegments-1;
			if(!isThereCar2(movingCars, i, 1))
			{
				llights[i]=true;
				rlights[i]=false;
			}
			if(!noCrash(movingCars, left, right, llights, rlights))
			{
				llights[i]=old_llights[i];
				rlights[i]=old_rlights[i];
			}	
		}
		else
		{
			for(int i=0;i<nsegments-1;i++)
			{
				if(!isThereCar2(movingCars, i, -1))
				{
					if(left[i+1].size()+right[i+1].size()>=capacity[i+1])
					{
						llights[i]=false;
						rlights[i]=true;
					}
				}
				if(!noCrash(movingCars, left, right, llights, rlights))
				{
					llights[i]=old_llights[i];
					rlights[i]=old_rlights[i];
				}
			}
			int i=nsegments-1;
			if(!isThereCar2(movingCars, i, 1))
			{
				llights[i]=false;
				rlights[i]=true;
			}
			if(!noCrash(movingCars, left, right, llights, rlights))
			{
				llights[i]=old_llights[i];
				rlights[i]=old_rlights[i];
			}
			
		}
	}

	
	private void strategy2(oneway.sim.MovingCar[] old_movingCars,
			Parking[] left, Parking[] right, boolean[] llights,
			boolean[] rlights) {
		MovingCar[] movingCars = new MovingCar[old_movingCars.length];
		for (int i = 0; i < movingCars.length; i++) {
			movingCars[i] = new MovingCar(old_movingCars[i].segment,
					old_movingCars[i].block, old_movingCars[i].dir,
					old_movingCars[i].startTime);
		}
		boolean old_llights[]=new boolean[llights.length];
		boolean old_rlights[]=new boolean[rlights.length];
		
		for(int i=0;i<llights.length;i++)
		{
			old_llights[i]=llights[i];
			old_rlights[i]=rlights[i];
		}
		
		for(int i=0;i<nsegments;i++)
		{
			llights[i]=false;
			rlights[i]=false;
		}
		
//		if(left_entry_tick+2==tick)
//		for(int i=0;i<nsegments;i++)
//		{
//			llights[i]=true;
//			rlights[i]=false;
//		}
//		
//		if(right_entry_tick+2==tick)
//			for(int i=0;i<nsegments;i++)
//			{
//				llights[i]=false;
//				rlights[i]=true;
//			}
		boolean entry=false;
		
			System.out.println("nblocks: "+nblocks[0]+";tick: "+tick+";flag: "+flag+";count: "+count);
			if((tick-1) % (nblocks[0]+1)==0 && flag)
			{
				entry=true;
				for (int i=0; i<nsegments;i++)
				{
				System.out.println("First if");
				llights[i]=false;
				rlights[i]=true;
				}
			}
//			else if((tick-1) % (nblocks[0]+3)==0 && !flag)
//			{
//				entry=true;
//				for (int i=0; i<nsegments;i++)
//				{
//				System.out.println("First if");
//				llights[i]=false;
//				rlights[i]=true;
//				}
//			}
			if((tick-1)%(nblocks[0]+1)==0 && !flag)
			{
				//count++;
				//right_entry_tick=tick;
				entry=true;
				for (int i=0; i<nsegments;i++)
				{
				System.out.println("Second if");
				llights[i]=true;
				rlights[i]=false;
			    }
//				count++;
			}
	/*		else if((tick-1) % (nblocks[0]+3)==0 && !flag)
			{
				entry=true;
				for (int i=0; i<nsegments;i++)
				{
				System.out.println("First if");
				llights[i]=true;
				rlights[i]=false;
				}
			}*/
			if(entry)
				flag=!flag;
	}
	
	private int DoWeNeedRevert(MovingCar[] movingCars,
			Parking[] left, Parking[] right, int tick) {
		float left_penalty = 0, right_penalty = 0;
		// process the left moving side
		for (int i = 0; i < left.length; i++) {
			if (left[i] != null) {
				for (int j = 0; j < left[i].size(); j++) {
					int temp = left[i].get(j);
					int time_waited = tick - temp;
					left_penalty += (1 + Math.log(time_waited));
				}
			}
		}
		for (int i = 0; i < right.length; i++) {
			if (right[i] != null) {
				for (int j = 0; j < right[i].size(); j++) {
					int temp = right[i].get(j);
					int time_waited = tick - temp;
					right_penalty += (1 + Math.log(time_waited));
				}
			}
		}
		for (int i = 0; i < movingCars.length; i++) {
			if (movingCars[i].dir==1) {
				right_penalty += (1 + Math.log(tick-movingCars[i].startTime));
			}
			else {
				left_penalty += (1 + Math.log(tick-movingCars[i].startTime));
			}
		}
		
		float difference=Math.abs(left_penalty-right_penalty);
		float sum=left_penalty+right_penalty;
		
		float ratio=difference/sum;
		float negligible_penalty=20; 
		
		if(ratio<=0.1 || sum<=negligible_penalty)
			return 0;
		
		if (left_penalty >= right_penalty)
			return -1;
		else
			return 1;
	}
	//returns the direction to favor if accumulation has occurred in one direction
	//returns zero if the penalties on both sides are similar or too small
	private int stopDomination(MovingCar[] old_movingCars,
			Parking[] left, Parking[] right, int tick) {
		float left_penalty = 0, right_penalty = 0;
		// process the left moving side
		for (int i = 0; i < left.length; i++) {
			if (left[i] != null) {
				for (int j = 0; j < left[i].size(); j++) {
					int temp = left[i].get(j);
					int time_waited = tick - temp;
					//System.out.println("LWait: " + time_waited);
					left_penalty += (time_waited * Math.log(time_waited));
				}
			}
		}
		for (int i = 0; i < right.length; i++) {
			if (right[i] != null) {
				for (int j = 0; j < right[i].size(); j++) {
					int temp = right[i].get(j);
					int time_waited = tick - temp;
					//System.out.println("RWait: " + time_waited);
					right_penalty += (time_waited * Math.log(time_waited));
				}
			}
		}
//		System.out.println(left_penalty);
//		System.out.println(right_penalty);
		float difference=Math.abs(left_penalty-right_penalty);
		float sum=left_penalty+right_penalty;
		
		float ratio=difference/sum;
		float negligible_penalty=300; 
		
		if(ratio<=0.15 || left_penalty+right_penalty<=negligible_penalty)
			return 0;
		
		if (left_penalty >= right_penalty)
			return -1;
		else
			return 1;
	}
	
	private int stopDomination2(oneway.sim.MovingCar[] old_movingCars,
            LinkedList<Integer> left,
            LinkedList<Integer> right, int tick)
	{
		float left_penalty=0,right_penalty=0;
		//process the left moving side
		if(left!=null)
		for(int i=0;i<left.size();i++)
		{	
					int temp=left.get(i);
					int time_waited=tick-temp;
					System.out.println("LWait: "+time_waited);
					left_penalty+=(time_waited*Math.log(time_waited));
		}
		if(right!=null)
		for(int i=0;i<right.size();i++)
		{
			int temp=right.get(i);
			int time_waited=tick-temp;
			System.out.println("LWait: "+time_waited);
			right_penalty+=(time_waited*Math.log(time_waited));
		}
		System.out.println(left_penalty);
		System.out.println(right_penalty);
		if(left_penalty>=right_penalty)
			return -1;
		else
			return 1;
	}

	// check if the segment has traffic
	private boolean hasTraffic(MovingCar[] cars, int seg, int dir) {
		for (MovingCar car : cars) {
			if (car.segment == seg && car.dir == dir)
				return true;
		}
		return false;
	}

	private boolean noCrash(MovingCar[] old_movingCars, Parking[] left,
			Parking[] right, boolean[] llights, boolean[] rlights) {
		// copy moving cars
		MovingCar[] movingCars = new MovingCar[old_movingCars.length];
		for (int i = 0; i < movingCars.length; i++) {
			movingCars[i] = new MovingCar(old_movingCars[i].segment,
					old_movingCars[i].block, old_movingCars[i].dir,
					old_movingCars[i].startTime);
		}
		// simple judge
		/*for (int i = 0; i < nsegments; i++) {
			if (rlights[i] && isThereCar(movingCars, i, 0))
				return false;
			if (llights[i] && isThereCar(movingCars, i, nblocks[i] - 1))
				return false;
		}*/
		// update cars
		left = copyList(left);
		right = copyList(right);
		// to right
		for (int i = 0; i < movingCars.length; i++) {
			if (movingCars[i].dir == 1) {
				if (movingCars[i].block == nblocks[movingCars[i].segment] - 1) {
					if (movingCars[i].segment != nsegments - 1)
						right[movingCars[i].segment + 1].add(0);
					movingCars[i] = null;
				} else {
					movingCars[i].block++;
				}
			}
		}
		// to left
		for (int i = 0; i < movingCars.length; i++) {
			if (movingCars[i] != null && movingCars[i].dir == -1) {
				if (movingCars[i].block == 0) {
					if (movingCars[i].segment != 0)
						left[movingCars[i].segment].add(0);
					movingCars[i] = null;
				} else {
					movingCars[i].block--;
				}
			}
		}
		// from parking lot
		// to right
		LinkedList<MovingCar> addedCars = new LinkedList<MovingCar>();
		for (int i = 0; i < right.length - 1; i++)
			if (right[i].size() > 0 && rlights[i]) {
				if (isThereCar(movingCars, i, 1))
					return false; // there will be crash
				MovingCar c = new MovingCar(i, 0, 1, 0);
				addedCars.add(c);
				right[i].remove();
			}
		// to left
		for (int i = 1; i < left.length; i++)
			if (left[i].size() > 0 && llights[i - 1]) {
				if (isThereCar(movingCars, i - 1, nblocks[i - 1] - 2))
					return false; // there will be crash
				MovingCar c = new MovingCar(i - 1, nblocks[i - 1] - 1, -1, 0);
				addedCars.add(c);
				left[i].remove();
			}
		for (int i = 0; i < movingCars.length; i++)
			if (movingCars[i] != null) {
				addedCars.add(movingCars[i]);
			}

		movingCars = addedCars.toArray(new MovingCar[0]);
		// sort cars
		for (int i = 0; i < movingCars.length - 1; i++) {
			for (int j = i + 1; j < movingCars.length; j++)
				if (movingCars[i].segment > movingCars[j].segment
						|| ((movingCars[i].segment == movingCars[j].segment) && (movingCars[i].block > movingCars[j].block))) {
					MovingCar t = movingCars[i];
					movingCars[i] = movingCars[j];
					movingCars[j] = t;
				}
		}
		// simple judge
		for (int i = 0; i < movingCars.length - 1; i++) {
			if (position[movingCars[i].segment] + movingCars[i].block == position[movingCars[i + 1].segment]
					+ movingCars[i + 1].block)
				return false;
		}
		// get park info
		int[] parked = new int[nsegments + 1];
		for (int i = 1; i < parked.length - 1; i++) {
			parked[i] = left[i].size() + right[i].size();
			if (parked[i] > capacity[i])
				return false;
		}
		// judge
		int[] blocking = new int[movingCars.length];

		for (int i = movingCars.length - 1; i >= 0; i--)
			if (movingCars[i].dir == 1) {
				MovingCar c = movingCars[i];
				for (int j = c.segment + 1; j < parked.length; j++)
					if (parked[j] < capacity[j]) {
						parked[j]++;
						blocking[i] = position[j]
								- (position[c.segment] + c.block);
						break;
					}
			}
		for (int i = 0; i < movingCars.length; i++)
			if (movingCars[i].dir == -1) {
				MovingCar c = movingCars[i];
				for (int j = c.segment; j >= 0; j--)
					if (parked[j] < capacity[j]) {
						parked[j]++;
						blocking[i] = (position[c.segment] + c.block)
								- position[j];
						break;
					}
			}
		int crash = 0;
		for (int i = 0; i < movingCars.length; i++) {
			for (int j = 0; j < movingCars.length; j++)
				if (movingCars[i].dir == 1 && movingCars[j].dir == -1) {
					int min = blocking[i] < blocking[j] ? blocking[i]
							: blocking[j];
					int posi = position[movingCars[i].segment]
							+ movingCars[i].block;
					int posj = position[movingCars[j].segment]
							+ movingCars[j].block;
					if (posj > posi && min + min > posj - posi) {
						crash++;
						int crash_posi = (posi + posj) / 2;
						int crash_posj = (posi + posj+1) / 2;
						MovingCar cari=getCarByPos(crash_posi);
						MovingCar carj=getCarByPos(crash_posj);
						if(cari.block==nblocks[cari.segment]-1 && carj.block==0 && capacity[carj.segment]==0){
						  crash--;
						}
					}
				}
			if (crash > 0)
				break;
		}
		if (crash == 0)
			return true;

		// do a reverse check again
		// TODO
		return false;
	}

	private MovingCar getCarByPos(int pos) {
		MovingCar car=new MovingCar(0, pos, 1, 0);
		while (nblocks[car.segment]<=car.block) {
			car.block-=nblocks[car.segment];
			car.segment++;
		}
		return car;
	}

	private boolean isThereCar(MovingCar[] movingCars, int s, int b) {
		for (int i = 0; i < movingCars.length; i++) {
			if (movingCars[i] == null)
				continue;
			if (movingCars[i].segment == s && movingCars[i].block == b) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isThereCar2(MovingCar[] movingCars, int s, int d) {
		for (int i = 0; i < movingCars.length; i++) {
			if (movingCars[i] == null)
				continue;
			if (movingCars[i].segment == s && movingCars[i].dir==d) {
				return true;
			}
		}
		return false;
	}

	private boolean isThereCar(MovingCar[] movingCars, int s, int b, int d) {
		for (int i = 0; i < movingCars.length; i++) {
			if (movingCars[i] == null)
				continue;
			if (movingCars[i].segment == s && movingCars[i].block == b
					&& movingCars[i].dir == d) {
				return true;
			}
		}
		return false;
	}

	static Parking[] copyList(Parking[] l) {
		if (l == null)
			return null;
		Parking[] copy = new Parking[l.length];
		for (int i = 0; i != l.length; ++i)
			if (l[i] != null)
				copy[i] = new Parking(l[i]);
		return copy;
	}

	private int nsegments;
	private int[] nblocks;
	private int[] capacity;
}
