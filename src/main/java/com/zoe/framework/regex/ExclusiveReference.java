package com.zoe.framework.regex;

/*
 * Used to cache one exclusive runner reference
 */
public final class ExclusiveReference
{
	private RegexRunner _ref;
	private Object _obj;
	private int _locked;

	/*
	 * Return an object and grab an exclusive lock.
	 *
	 * If the exclusive lock can't be obtained, null is returned;
	 * if the object can't be returned, the lock is released.
	 *
	 */
	public Object Get()
	{
		// try to obtain the lock

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
			// grab reference


			Object obj = _ref;

			// release the lock and return null if no reference

			if (obj == null)
			{
				_locked = 0;
				return null;
			}

			// remember the reference and keep the lock

			_obj = obj;
			return obj;
		}

		return null;
	}

	/*
	 * Release an object back to the cache
	 *
	 * If the object is the one that's under lock, the lock
	 * is released.
	 *
	 * If there is no cached object, then the lock is obtained
	 * and the object is placed in the cache.
	 *
	 */
	public void Release(Object obj)
	{
		if (obj == null)
		{
			throw new IllegalArgumentException("obj");
		}

		// if this reference owns the lock, release it

		if (_obj == obj)
		{
			_obj = null;
			_locked = 0;
			return;
		}

		// if no reference owns the lock, try to cache this reference

		if (_obj == null)
		{
			// try to obtain the lock

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
				// if there's really no reference, cache this reference

				if (_ref == null)
				{
					_ref = (RegexRunner) obj;
				}

				// release the lock

				_locked = 0;
				return;
			}
		}
	}
}