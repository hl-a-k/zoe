//	OuterJoin.java
//	Casper Datasets (R)
//

package com.zoe.framework.dataset.join;

//	Casper imports 
import com.zoe.framework.dataset.*;


/**
 * Performs an outer join.
 * THIS HAS NOT BEEN IMPLEMENTED YET
 * 
 * @since 1.0
 * @author Jonathan H. Liang
 * @version $Revision: 111 $ 
 */
public class OuterJoin 
	implements Join 
{

	/**
	 * Join two cache containers together.
	 * 
	 * @param cA
	 * @param cB
	 * @param joinColumns
	 * @return
	 * @throws CDataGridException
	 */
	public CDataCacheContainer join(CDataCacheContainer cA, CDataCacheContainer cB, String[] joinColumns)
		throws CDataGridException
	{
		return join(cA, cB, joinColumns, null);
	}
	
	
	/**
	 * Join two cache containers together.
	 * 
	 * @param cA
	 * @param cB
	 * @param joinColumns
	 * @return
	 * @throws CDataGridException
	 */
	public CDataCacheContainer join(CDataCacheContainer cA, CDataCacheContainer cB, String[] joinColumns, String[] selectedColumns)
		throws CDataGridException
	{
		return null;
	}
	

}
