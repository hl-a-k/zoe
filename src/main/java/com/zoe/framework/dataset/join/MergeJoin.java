//	MergeJoin.java
//	Casper Datasets (R) 
//

package com.zoe.framework.dataset.join;

//	Casper imports 
import com.zoe.framework.dataset.CDataCacheContainer;
import com.zoe.framework.dataset.CDataGridException;


/**
 * 	A merge-join is a very simplistic merge of two datasets. 
 * 	The more sophisticated joins are implemented with InnerJoin and OuterJoin.
 * 
 * @since 1.0
 * @author Jonathan H. Liang
 * @version $Revision: 111 $ 
 */
public class MergeJoin 
	implements Join 
{

	
	/**
	 * 
	 */
	public CDataCacheContainer join(CDataCacheContainer cA, CDataCacheContainer cB, String[] joinColumns)
		throws CDataGridException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public CDataCacheContainer join(CDataCacheContainer cA, CDataCacheContainer cB, String[] joinColumns, String[] selectedColumns) 
		throws CDataGridException 
	{
		// TODO Auto-generated method stub
		return null;
	}

}
