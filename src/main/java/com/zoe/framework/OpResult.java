package com.zoe.framework;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zoe.framework.util.BeanUtils;

import java.util.Map;

/**
 * 执行结果类（默认操作成功）
 *
 * @author caizhicong
 */
public class OpResult<T> extends JSONObject {

    private static final String messageSuccess = "操作成功";
    private static final String messageFailure = "操作失败";

    public OpResult() {
    }

    public OpResult(Map<String, Object> map) {
        super(map);
    }

    /**
     * 本方法只用于JSONObject转为其他类型
     */
    public <A> A getAs(String key, Class<A> clazz) {
        Object value = this.get(key);

        if(value != null && value.getClass() == clazz){
            //noinspection unchecked
            return (A) value;
        }

        JSONObject object = this.getJSONObject(key);
        if (object != null) {
            return object.toJavaObject(clazz);
        }
        return null;
    }

    /**
     * 本方法只用于JSONObject转为其他类型
     */
    public <A> A dataAs(Class<A> clazz) {
        return getAs("data", clazz);
    }

    public JSONObject data() {
        JSONObject data = getJSONObject("data");
        if(data == null) data = new JSONObject();
        return data;
    }

    public JSONArray dataArray() {
        JSONArray data = getJSONArray("data");
        if(data == null) data = new JSONArray();
        return data;
    }

    public OpResult<T> data(Object data) {
        this.put("data", data);
        return this;
    }

    public boolean success() {
        return getBooleanValue("success");
    }

    public OpResult<T> success(boolean success) {
        this.put("success",success);
        this.code(success ? 0 : 9999);
        if (msg() == null) {
            this.msg(success ? messageSuccess : messageFailure);
        }
        return this;
    }

    public String msg() {
        return getString("msg");
    }

    public OpResult<T> msg(String msg) {
        this.put("msg",msg);
        return this;
    }

    public int code() {
        return getIntValue("code");
    }

    public OpResult<T> code(int code) {
        this.put("code",code);
        return this;
    }

    public String url() {
        return getString("url");
    }

    public OpResult<T> url(String url) {
        this.put("url", url);
        return this;
    }

    public T tag() {
        return getAs("tag", BeanUtils.getParameterizedClass(getClass()));
    }

    public OpResult<T> tag(T tag) {
        this.put("tag", tag);
        return this;
    }

    public String callback() {
        return getString("callback");
    }

    public OpResult<T> callback(String callback) {
        this.put("callback", callback);
        return this;
    }

    public boolean dataEmpty(){
        return data().isEmpty();
    }

    public static <T> OpResult<T> result() {
        return new OpResult<>();
    }

    public static <T> OpResult<T> create() {
        return new OpResult<>();
    }

    public static <T> OpResult<T> parseResult(String text) {
        return new OpResult<>(JSONObject.parseObject(text));
    }

    public static <T> OpResult<T> succeedResult() {
        OpResult<T> opResult = new OpResult<>();
        opResult.success(true);
        opResult.msg(messageSuccess);
        return opResult;
    }

    public static <T> OpResult<T> succeedResult(String msg) {
        OpResult<T> opResult = new OpResult<>();
        opResult.success(true);
        opResult.msg(msg != null ? msg : messageSuccess);
        return opResult;
    }

    /**
     * 如果data不为null则为success否则为failed
     * @param data
     * @param <T>
     * @return
     */
    public static <T> OpResult<T> succeedResultIfNotNull(Object data) {
        OpResult<T> opResult = new OpResult<>();
        opResult.success(data != null);
        opResult.data(data);
        return opResult;
    }

    public static <T> OpResult<T> failedResult() {
        OpResult<T> opResult = new OpResult<>();
        opResult.success(false);
        return opResult;
    }

    public static <T> OpResult<T> failedResult(String msg) {
        OpResult<T> opResult = new OpResult<>();
        opResult.success(false);
        opResult.msg(msg != null ? msg : messageSuccess);
        return opResult;
    }

    public static <T> OpResult<T> failedResult(int code, String msg) {
        OpResult<T> opResult = new OpResult<>();
        opResult.code(code);
        opResult.success(false);
        opResult.msg(msg != null ? msg : messageSuccess);
        return opResult;
    }

    public OpResult<T> reset() {
        return this.success(true).msg(null).code(0).url(null);
    }

    /**
     * 输出JsonP格式数据
     * @return JsonP String
     */
    public String toJsonP() {
        StringBuilder sb = new StringBuilder();
        String callback = callback();
        if (callback != null) {
            sb.append(callback()).append("&&").append(callback()).append("(");
            sb.append(this.toJSONString());
            sb.append(")");
        }
        return sb.toString();
    }
}
