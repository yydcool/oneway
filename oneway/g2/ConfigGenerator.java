package oneway.g2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ConfigGenerator {
	
	public static void main(String arg[]) throws IOException
	{
		genTime(100, 30, 30);
		genConfig(20,2,4);
	}
	
	//randomly generates a map
	// n - number of segments
	// minLen - minimum length of a segment
	// maxLen - maximum length of a segment
	static void genConfig(int n, int minLen, int maxLen) throws IOException
	{
//		float start=(float) 0.8;
		int timecount=0;
		int carscount=0;
		File timing=new File("config.txt");
		FileWriter fw=new FileWriter(timing);
		
		fw.write(n+"\n");
		
		Random random=new Random();
		maxLen=maxLen-minLen+1;
		for (int i = 0; i < n; i++) {
			int len=random.nextInt(maxLen)+minLen;
			fw.write(len+" ");
		}
		fw.write("\n");
		
		int maxCapacity=3;
		for (int i = 0; i < n-1; i++) {
			int cap=random.nextInt(maxCapacity);
			fw.write(cap+" ");
		}
		fw.write("\n");
		fw.close();
	}
	
	//Poisson timing distribution
	//time - all cars come before this time
	//rprobability - rate of the car gose to right side.
	//lprobability - rate of the car gose to left side.
	static void genTime(int time, double rprobability, double lprobability) throws IOException
	{
		int timecount=-1;
		File timing=new File("timing.txt");
		FileWriter fw=new FileWriter(timing);
		
		while(timecount<time)
		{
			timecount++;
			int random = (int) (Math.random()*100);
			if(random<=rprobability)
			{
				fw.write(timecount+"\n");
			}
			random = (int) (Math.random()*100);
			if(random<=lprobability)
			{
				fw.write(-timecount+"\n");
			}
		}
		fw.close();
	}
}
