package com.zoe.framework.sql2o.data;

/**
 * Created by caizhicong on 2016/2/22.
 */
/*
public final class RowState {
    public static final int Detached = 1;
    public static final int Unchanged = 2;
    public static final int Added = 4;
    public static final int Deleted = 8;
    public static final int Modified = 16;
}
*/
public enum RowState {
    Detached(1), Unchanged(2), Added(4), Deleted(8), Modified(16);

    private int value = 0;

    RowState(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static RowState valueOf(int value) {
        switch (value) {
            case 1:
                return Detached;
            case 2:
                return Unchanged;
            case 4:
                return Added;
            case 8:
                return Deleted;
            case 16:
                return Modified;
            default:
                return null;
        }
    }
}

