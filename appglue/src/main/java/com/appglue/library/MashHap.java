package com.appglue.library;

import java.lang.reflect.Array;
import java.util.ArrayList;



public class MashHap<A, C>
{
	private ArrayList<C>[] buckets;
	
	private static final int SIZE = 100;
	
	// reserve 0th index for output only services
	private static final int OUTPUT_ONLY = -1;
	
	@SuppressWarnings("unchecked")
	public MashHap()
	{
		buckets = (ArrayList<C>[]) Array.newInstance(ArrayList.class, SIZE + 1);
		
		for(int i = 0; i < SIZE + 1; i++)
		{
			buckets[i] = new ArrayList<C>();
		}
	}
	
	public void add(A key, C thing)
	{
		int pos = hash(key) + 1;
		buckets[pos].add(thing);
	}
	
	private int hash(A key)
	{
		if(key == null)
		{
			// Then it's an input only service
			return OUTPUT_ONLY;
		}
		
		int hash = key.hashCode() == Integer.MIN_VALUE ? 0 : key.hashCode();
		return Math.abs(hash) % SIZE;
	}
	
	public ArrayList<C> getBucket(A key)
	{
		int pos = hash(key) + 1;
		return buckets[pos];
	}
}
