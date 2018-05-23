package org.beetl.sql.core.engine;

import java.util.Collection;
import java.util.List;

import org.beetl.core.Context;
import org.beetl.core.Function;
import org.beetl.sql.core.engine.SQLParameter;
/**
 * 
 * @author "Sean-[重庆]" 284771807@qq.com;
 *
 */
public class JoinFunction implements Function {
	
	public Object call(Object[] paras,Context ctx){
		Object[] objs = null;
		Object temp = paras[0];
		if(temp instanceof Object[]){
			objs = (Object[]) paras[0];
		}else if(temp instanceof Collection){
			objs = ((Collection) temp).toArray();
		}else if(temp instanceof String){
			objs = (((String) temp).split(","));
		}
		else{
			throw new RuntimeException("join.paras[0] must be a collection or an array!");
		}
		
		List<SQLParameter> dbParas = (List<SQLParameter>) ctx.getGlobal("_paras");
		
		try {
			ctx.byteWriter.writeString(join(objs,dbParas));
		} catch (Exception e) {
		}
		return null;
	}

	private static String join(Object[] array, List dbParas) {  
        if (array == null) {  
            return null;  
        }  
        int arraySize = array.length;  
        StringBuffer buf = new StringBuffer(arraySize*2-1);  
  
        for (int i = 0; i < arraySize; i++) {  
            if (i > 0) {  
                buf.append(',');  
            }  
            buf.append('?');  
            dbParas.add(new SQLParameter(null, array[i]));
        }  
        return buf.toString();  
    }  
}