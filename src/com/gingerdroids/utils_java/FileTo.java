package com.gingerdroids.utils_java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class FileTo { 
	
	public static String string(File file) throws IOException { 
		Reader fileReader = new FileReader(file); 
		BufferedReader reader = new BufferedReader(fileReader); 
		StringBuffer sb = new StringBuffer(); 
		while (true) { 
			String line = reader.readLine(); 
			if (line ==null) break ; 
			if (sb.length()>0) sb.append(' '); 
			sb.append(line); 
		}
		return sb.toString(); 
	}
	
	public static String [] words(File file) throws IOException { 
		Reader fileReader = new FileReader(file); 
		BufferedReader reader = new BufferedReader(fileReader); 
		StringBuffer sb = new StringBuffer(); 
		while (true) { 
			String line = reader.readLine(); 
			if (line ==null) break ; 
			if (sb.length()>0) sb.append(' '); 
			sb.append(line); 
		}
		return sb.toString().split(" "); 
	}
	
	public static String [] lines(File file) throws IOException { 
		Reader fileReader = new FileReader(file); 
		BufferedReader reader = new BufferedReader(fileReader); 
		ArrayList<String> list = new ArrayList<>(); 
		while (true) { 
			String line = reader.readLine(); 
			if (line ==null) break ; 
			list.add(line); 
		}
		return Str.toArray(list); 
	}

}
