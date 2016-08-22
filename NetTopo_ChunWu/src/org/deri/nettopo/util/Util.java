package org.deri.nettopo.util;

import java.util.*;
import java.util.regex.Pattern;

public class Util {
	
	/**
	 * @param arg  such as: 3-56
	 * @return true if it is a string range
	 */
	public static boolean isStringRange(String arg){
		boolean result = false;
		String regex = "\\d+-\\d+";
		if(Pattern.matches(regex, arg)){
			int index= arg.indexOf('-');
			int first = Integer.parseInt(arg.substring(0, index));
			int last  = Integer.parseInt(arg.substring(index+1));
			if(last >= first){
				result = true;
			}else{
				result = false;
			}
		}else{
			result = false;
		}
		
		return result;
	}
	
	/**
	 * convert string range to int array
	 * @param arg
	 * @return
	 */
	public static int[] stringRange2IntArray(String arg){
		int[] result = null;
		if(isStringRange(arg)){
			int index = arg.indexOf('-');
			int first = Integer.parseInt(arg.substring(0, index));
			int last =  Integer.parseInt(arg.substring(index+1));
			int size = last - first + 1;
			result = new int[size];
			for(int i=0;i<size;i++){
				result[i] = first + i;
			}
		}else{
			result = null;
		}
		return result;
	}
	
	/**
	 * to check if the arg can be convert to Integer array
	 * if arg=="", return false
	 * @param arg
	 * @return
	 */
	public static boolean string2IntArrayBoolean(String arg){
		if(arg.trim().equals("")){
			return false;
		}
		StringTokenizer st = new StringTokenizer(arg, ",: ");
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			try {
				if(!FormatVerifier.isNotNegative(value)){
					return false;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * to check if the arg can be convert to Integer array
	 * if arg=="", return false
	 * @param arg
	 * @return
	 */
	public static boolean string2PositiveIntArrayBoolean(String arg){
		if(arg.trim().equals("")){
			return false;
		}
		StringTokenizer st = new StringTokenizer(arg, ",: ");
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			try {
				if(!FormatVerifier.isPositiveInteger(value)){
					return false;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * The token of the StringTokenizer is:",: ",that is , or : or blank space
	 * Converts a String to an int array.
	 * @return new int array
	 */
	public static int[] string2IntArray(String arg) {
		StringTokenizer st = new StringTokenizer(arg, ",: ");
		Vector<Integer> rs = new Vector<Integer>();
		while (st.hasMoreTokens()) {
			rs.addElement(new Integer(Integer.parseInt(st.nextToken())));
		}
		int[] result = new int[rs.size()];
		for (int j = 0; j < result.length; j++)
			result[j] = ((Integer)rs.elementAt(j)).intValue();
		return result;
	}

	/**
	 * to check if the arg can be convert to Double array
	 * if arg=="", return false
	 * @param arg
	 * @return
	 */
	public static boolean string2DoubleArrayBoolean(String arg){
		if(arg.trim().equals("")){
			return false;
		}
		StringTokenizer st = new StringTokenizer(arg, ",: ");
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			try {
				if(!FormatVerifier.isDouble(value)){
					return false;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * to check if the arg can be convert to Double array
	 * if arg=="", return false
	 * @param arg
	 * @return
	 */
	public static boolean string2PositiveDoubleArrayBoolean(String arg){
		if(arg.trim().equals("")){
			return false;
		}
		StringTokenizer st = new StringTokenizer(arg, ",: ");
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			try {
				if(!FormatVerifier.isPositiveDouble(value)){
					return false;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * The token of the StringTokenizer is:",: ". that is , or : or blank space
	 * Converts a String to a double array.
	 * @return new double array
	 */
	public static double[] string2DoubleArray(String arg) {
		StringTokenizer st = new StringTokenizer(arg, ",: ");
		Vector<Double> rs = new Vector<Double>();
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			try {
				if(FormatVerifier.isDouble(value)){
					rs.addElement(new Double(Double.parseDouble(value)));
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			
		}
		double[] result = new double[rs.size()];
		for (int j = 0; j < result.length; j++)
			result[j] = rs.elementAt(j).doubleValue();
		return result;
	}
	
	/**
	 * Concatenates two string arrays
	 * @param a first array
	 * @param b second array
	 * @return new and concated string array
	 */
	public static String[] stringArrayConcat(String[] a, String[] b) {
		String[] c = new String[((a != null) ? a.length : 0) + ((b != null) ? b.length : 0)];
		if (a != null)
			System.arraycopy(a, 0, c, 0, a.length);
		if (b != null)
			System.arraycopy(b, 0, c, (a != null) ? a.length : 0, b.length);
		return c;
	}

	/**
	 * 
	 * @param array the String[] to be processed
	 * @param n how many elements will be removed from array
	 * @return new String[]
	 */
	public static String[] removeFirstElements(String[] array, int n) {
		String[] r = new String[array.length - n];
		System.arraycopy(array, n, r, 0, r.length);
		return r;
	}
	
	/**
	 * to check if the nodeID is in the nodeIDs array
	 * @param nodeID given integer
	 * @param nodeIDs given integer array
	 * @return 
	 */
	public static boolean isIntegerInIntegerArray(int element, int[] array){
		boolean in = false;
		for(int i=0;i<array.length;i++){
			if(array[i] == element){
				in = true;
				break;
			}
		}
		return in;
	}
	
	/**
	 * to check if the array1's elements are all in the array2
	 * @param nodeIDs1 given integer array1
	 * @param nodeIDs2 given integer array2
	 * @return 
	 */
	public static boolean isIntegerArrayInIntegerArray(int[] array1, int[] array2){
		if(array1.length > array2.length){
			return false;
		}
		for(int i=0;i<array1.length;i++){
			boolean isIn = Util.isIntegerInIntegerArray(array1[i], array2);
			if(isIn == false){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This function return the 0-based element index of the attrName in the attrNames array\
	 * and -1 if the attrName is not found in the array 
	 * @param attrName
	 * @return 
	 */
	public static int indexOf(String[] attrNames, String attrName){
		for(int i=0;i<attrNames.length;i++){
			if(attrNames[i].equals(attrName))
				return i;
		}
		return -1;
	}
	
	/**
	 * check if the given boolean array does not contain false
	 * @param attrValid a boolean array
	 * @return
	 */
	public static boolean checkAllArgValid(boolean[] attrValid){
		for(int i=0;i<attrValid.length;i++){
			if(!attrValid[i])
				return false;
		}
		return true;
	}

	/**
	 * check if there is more than one num in the array
	 * @param num
	 * @param array
	 * @return
	 */
	public static boolean isDuplicatedInIntegerArray(int num, int[] array){
		int count = 0;
		for(int i=0;i<array.length;i++){
			if(array[i] == num){
				++count;
				if(count >= 2){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if there is only one element in the array of all the elements.
	 * @param array
	 * @return
	 */
	public static boolean isDuplicatedIntegerArray(int[] array){
		boolean duplicated = false;
		for(int i=0;i<array.length;i++){
			if(Util.isDuplicatedInIntegerArray(array[i], array)){
				duplicated = true;
				break;
			}
		}
		return duplicated;
	}
	
	/**
	 * test if Class c1 is derived from Class with name c2
	 * @param c1 
	 * @param c2
	 * @return
	 */
	public static boolean isDerivedClass(Class<?> c1, String c2){
		Class<?> superC = c1.getSuperclass();
		if(superC!=null){
			String superName = superC.getName();
			if(superName.equals(c2)){
				return true;
			}else{
				return isDerivedClass(superC, c2);
			}
		}
		return false;
	}

	/**
	 * give a possibility, then you can use this function for many times to 
	 * validate the possibility. It is meaningless if you just use it once.
	 * @param possibility
	 * @return
	 */
	public static boolean isDoneWithThePossibility(double possibility){
		boolean result = false;
		if(possibility > 1 || possibility < 0){
			System.err.println("The possibility should between 0 and 1");
			System.exit(0);
		}else{
			double temp = Math.random();
			if(temp <= possibility)
				result = true;
			else
				result = false;
		}
		
		return result;
	}
	
	/**
	 * to generate a no duplicated integer array with 1-based elements.
	 * @param size
	 * @return
	 */
	public static int[] generateNotDuplicatedIntArray(int size){
		int[] array = new int[size];
		for(int i=0;i<size;){
			int nextInt = 1 + new Random().nextInt(size);
			if(isIntegerInIntegerArray(nextInt,array)){
				continue;
			}else{
				array[i] = nextInt;
			}
			i++;
		}
		return array;
	}
	
	/**
	 * generate Disordered Integer Array With Existing Array.
	 * @param array
	 * @return
	 */
	public static int[] generateDisorderedIntArrayWithExistingArray(int[] array){
		int[] indexArray = generateNotDuplicatedIntArray(array.length);
		int[] resultArray = new int[array.length];
		for(int i=0;i<resultArray.length;i++){
			resultArray[i] = array[indexArray[i] - 1];
		}
		return resultArray;
	}
	
	/**
	 * @param first
	 * @param in
	 * @return the the elements in first array that alse in the in array
	 */
	public static int[] IntegerArrayInIntegerArray(int[] first, int[] in){
		LinkedList<Integer> array = new LinkedList<Integer>();
		for(int i=0;i<first.length;i++){
			if(isIntegerInIntegerArray(first[i],in)){
				array.add(first[i]);
			}
		}
		return IntegerArray2IntArray(array.toArray(new Integer[array.size()]));
	}
	
	public static int[] IntegerArray2IntArray(Integer[] array){
		int[] result = new int[array.length];
		for(int i=0;i<array.length;i++){
			result[i] = array[i].intValue();
		}
		return result;
	}
	
//	public static void main(String[] args){
//		int[] array1 = {1,2};
//		int[] array2 = {3,4,5};
//		Integer[] result = IntegerArrayInIntegerArray(array1,array2);
//		System.out.println(Arrays.toString(result));
//	}
}
