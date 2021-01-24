package com.gingerdroids.utils_java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Groups a list of objects into a hashmap of lists, using the <code>String</code> key computed from each object. 
 */
public class StringKeyedLists<ContentType> { 
	
	private final HashMap<String,ArrayList<ContentType>> listsByKey = new HashMap<String, ArrayList<ContentType>>(); 
	
	public StringKeyedLists(List<ContentType> contentList, StrGetter<ContentType> keyGetter) { 
		for (ContentType content : contentList) { 
			String key = keyGetter.getString(content); 
			ArrayList<ContentType> keyContentList = listsByKey.get(key); 
			if (keyContentList==null) { 
				keyContentList = new ArrayList<ContentType>();
				listsByKey.put(key, keyContentList); 
			}
			keyContentList.add(content); 
		}
	}
	
	public StringKeyedLists(ContentType[] contentArray, StrGetter<ContentType> keyGetter) { 
		this(Arrays.asList(contentArray), keyGetter); 
	}
	
	public ArrayList<ContentType> getList(String key) { 
		return listsByKey.get(key); 
	}

}

