package edu.cs.columbia.iesrcsel.utils;

import java.util.ArrayList;
import java.util.List;

public class Builder {

	protected static final String SEPARATOR = ",";
	protected static final String SEPARATOR_PAIR = "-";
	
	protected static Boolean[] ToBoolean(String[] split) {
		Boolean[] ret = new Boolean[split.length];
		
		for (int i = 0; i < split.length; i++) {
			ret[i] = Boolean.valueOf(split[i]);
		}
		
		return ret;
	}

	protected static Integer[] ToInteger(String[] split) {
		Integer[] ret = new Integer[split.length];
		
		for (int i = 0; i < split.length; i++) {
			ret[i] = Integer.valueOf(split[i]);
		}
		
		return ret;
	}
	
	protected static Long[] ToLong(String[] split) {
		Long[] ret = new Long[split.length];
		
		for (int i = 0; i < split.length; i++) {
			ret[i] = Long.valueOf(split[i]);
		}
		
		return ret;
	}
	
	protected static Float[] ToFloat(String[] split) {
		Float[] ret = new Float[split.length];
		
		for (int i = 0; i < split.length; i++) {
			ret[i] = Float.valueOf(split[i]);
		}
		
		return ret;
	}

	protected static Double[] ToDouble(String[] split) {
		Double[] ret = new Double[split.length];
		
		for (int i = 0; i < split.length; i++) {
			ret[i] = Double.valueOf(split[i]);
		}
		
		return ret;
	}
	
	protected static List<Integer> createList(Integer[] integers) {
		
		List<Integer> ret = new ArrayList<Integer>();
		
		for (int i = integers[0]; i <= integers[1]; i+=integers[2]) {
			ret.add(i);
		}
		
		return ret;
	}
	
	protected static List<Long> createList(Long[] longs) {
		
		List<Long> ret = new ArrayList<Long>();
		
		for (long i = longs[0]; i <= longs[1]; i+=longs[2]) {
			ret.add(i);
		}
		
		return ret;
	}
	
	protected static List<Double> createList(Double[] doubles) {
		
		List<Double> ret = new ArrayList<Double>();
		
		for (double i = doubles[0]; i <= doubles[1]; i+=doubles[2]) {
			ret.add(i);
		}
		
		return ret;
	}
	
}
