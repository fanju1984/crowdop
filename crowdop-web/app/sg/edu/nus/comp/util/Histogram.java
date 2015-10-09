package sg.edu.nus.comp.util;

import java.text.*;

public class Histogram {
	double minValue;
	double maxValue;
	int bckNum;
	
	double buckets[];
	
	public Histogram (double min, double max, int bn) {
		minValue = min;
		maxValue = max;
		bckNum = bn;
		
		buckets = new double[bckNum + 1];
	}
	
	public void addValue (double value, double freq) throws Exception {
		if (value < minValue || value > maxValue) {
			throw new Exception (value + 
					" is out of [" + minValue + "," + maxValue + "]");
		}
		DecimalFormat df=new DecimalFormat("#.000"); 
		double slot = (maxValue - minValue) / (double) bckNum;
		double division = value / slot;
		int index = (int) (Double.parseDouble(df.format(division)));
		buckets[index] += freq;
	}
	
	public String toString () {
		DecimalFormat df=new DecimalFormat("#.00"); 
		String str = "";
		double slot = (maxValue - minValue) / (double) bckNum;
		
		for (int i = 0; i < buckets.length; i ++) {
			double bmin = i * slot;
			double bmax = maxValue;
			if (bmin != maxValue) {
				bmax = bmin + slot;
			}
			if (bmin < bmax) {
				str += "[" + df.format(bmin) + "," + df.format(bmax) + ")";
			} else {
				str += df.format(bmin);
			}
			str += "\t" + df.format(buckets[i]);
			str += "\n";
		}
		return str;
	}
	
	public static void main (String args[]) throws Exception {
		Histogram hist = new Histogram (0, 1, 10);
		hist.addValue(0.25, 4);
		hist.addValue(0.2, 4);
		hist.addValue(0.3, 4);
		hist.addValue(0.61, 3);
		hist.addValue(0.60, 5);
		hist.addValue(1, 1.0);
		hist.addValue(0, 7);
		
		System.out.println (hist);
	}
}
