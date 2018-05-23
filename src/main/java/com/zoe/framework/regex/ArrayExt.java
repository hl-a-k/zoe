package com.zoe.framework.regex;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ArrayExt {

	public static void Reverse(Array array, int index, int length) {
		if (array == null) {
			throw new IllegalArgumentException("array");
		}
		int index1 = index;
		int index2 = index + length - 1;
		for (; index1 < index2; --index2) {
			Object obj = Array.get(array, index1);
			Array.set(array, index1, Array.get(array, index2));
			Array.set(array, index2, obj);
			++index1;
		}
	}

	public static void removeRange(ArrayList elementData, int fromIndex,
			int toIndex) {
		int size = elementData.size();

		int newSize = size - (toIndex - fromIndex);
		for (int i = toIndex - 1; i >= fromIndex; i--) {
			// elementData[i] = null;
			// elementData.set(i, null);
			elementData.remove(i);
		}
		size = newSize;
	}
}
