package com.zoe.framework.regex;

import java.lang.ref.WeakReference;

/*
 * Used to cache a weak reference in a threadsafe way
 */
public final class SharedReference
{
	private WeakReference _ref = new WeakReference(null);
	private int _locked;

	/*
	 * Return an object from a weakref, protected by a lock.
	 *
	 * If the exclusive lock can't be obtained, null is returned;
	 *
	 * Note that _ref.Target is referenced only under the protection
	 * of the lock. (Is this necessary?)
	 */
	public Object Get()
	{
		RefObject<Integer> tempRef__locked = new RefObject<Integer>(_locked);
		//@czc:原子替换
		//boolean tempVar = 0 == Interlocked.Exchange(tempRef__locked, 1);
		Integer preVal;
		synchronized (tempRef__locked){
			preVal = tempRef__locked.argValue;
			tempRef__locked.argValue = 1;
		}
		boolean tempVar = 0 == preVal;
			_locked = tempRef__locked.argValue;
		if (tempVar)
		{
			Object obj = _ref.get();
			_locked = 0;
			return obj;
		}

		return null;
	}

	/*
	 * Suggest an object into a weakref, protected by a lock.
	 *
	 * Note that _ref.Target is referenced only under the protection
	 * of the lock. (Is this necessary?)
	 */
	public void Cache(Object obj)
	{
		RefObject<Integer> tempRef__locked = new RefObject<Integer>(_locked);
		//@czc:原子替换
		//boolean tempVar = 0 == Interlocked.Exchange(tempRef__locked, 1);
		Integer preVal;
		synchronized (tempRef__locked){
			preVal = tempRef__locked.argValue;
			tempRef__locked.argValue = 1;
		}
		boolean tempVar = 0 == preVal;
			_locked = tempRef__locked.argValue;
		if (tempVar)
		{
			_ref = new WeakReference(obj);
			_locked = 0;
		}
	}
}