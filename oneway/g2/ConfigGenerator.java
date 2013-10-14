package oneway.g2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class ConfigGenerator {
	
	public static void main(String arg[]) throws IOException
	{
		genTime(100);
		genConfig(20);
	}
	
	static void genConfig(int n) throws IOException
	{
//		float start=(float) 0.8;
		int timecount=0;
		int carscount=0;
		File timing=new File("config.txt");
		FileWriter fw=new FileWriter(timing);
		
		fw.write(n+"\n");
		
		Random random=new Random();
		int maxLen=3;
		for (int i = 0; i < n; i++) {
			int len=random.nextInt(maxLen)+2;
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
	
	static void genTime(int time) throws IOException
	{
//		float start=(float) 0.8;
		int timecount=0;
		int carscount=0;
		File timing=new File("timing.txt");
		FileWriter fw=new FileWriter(timing);
		
		while(timecount<=time)
		{
			timecount++;
			int random = (int) (Math.random()*100);
//			fw.write("Random: " +random+"\n");
			if(random<=30)
			{
				fw.write(timecount+"\n");
			}
			random = (int) (Math.random()*100);
			if(random<=30)
			{
				fw.write(-timecount+"\n");
			}
		}
		fw.close();
	}
}
