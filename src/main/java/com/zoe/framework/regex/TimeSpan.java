package com.zoe.framework.regex;

import java.util.Date;

public class TimeSpan {
	public final static TimeSpan ZERO = new TimeSpan(0);

	private long _ticks;

	private long _totalMilliSeconds = 0;

	public TimeSpan() {
	}

	public TimeSpan(long ticks) {
		this._ticks = ticks;
	}

	public TimeSpan(int hours, int minutes, int seconds) {
		this._ticks = TimeSpan.TimeToTicks(hours, minutes, seconds);
	}

	public TimeSpan(int days, int hours, int minutes, int seconds) {
		this(days, hours, minutes, seconds, 0);
	}

	public TimeSpan(int days, int hours, int minutes, int seconds,
					int milliseconds) {
		long num = ((long) days * 3600L * 24L + (long) hours * 3600L
				+ (long) minutes * 60L + (long) seconds)
				* 1000L + (long) milliseconds;
		if (num > 922337203685477L || num < -922337203685477L) {
			throw new IllegalArgumentException("Overflow_TimeSpanTooLong");
		}
		this._ticks = num * 10000L;
	}

	public static TimeSpan FromTicks(long value) {
		return new TimeSpan(value);
	}

	static long TimeToTicks(int hour, int minute, int second) {
		long num = (long) hour * 3600L + (long) minute * 60L + (long) second;
		if (num > 922337203685L || num < -922337203685L) {
			throw new IllegalArgumentException("Overflow_TimeSpanTooLong");
		}
		return num * 10000000L;
	}

	public long getTicks() {
		return _ticks;
	}

	public TimeSpan(Date afterDate, Date beforeDate) {
		this(afterDate.getTime() - beforeDate.getTime());
	}

	public long getMilliSeconds() {
		return (int) (this._ticks / 10000L % 1000L);
	}

	public long getSeconds() {
		return Math.round(_totalMilliSeconds / 1000);
	}

	public long getMinutes() {
		return Math.round(_totalMilliSeconds / (1000 * 60));
	}

	public long getHours() {
		return Math.round(_totalMilliSeconds / (1000 * 60 * 60));
	}

	public static TimeSpan FromMilliseconds(double value) {
		return Interval(value, 1);
	}

	private static TimeSpan Interval(double value, int scale) {
		if (Double.isNaN(value)) {
			throw new IllegalArgumentException("Arg_CannotBeNaN");
		}
		double num = value * (double) scale;
		double num2 = num + ((value >= 0.0) ? 0.5 : -0.5);
		if (num2 > 922337203685477.0 || num2 < -922337203685477.0) {
			try {
				throw new Exception("Overflow_TimeSpanTooLong");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new TimeSpan((long) num2 * 10000L);
	}

	public static boolean OpEquality(TimeSpan t1, TimeSpan t2) {
		if (t1 == null || t2 == null)
			return false;
		return t1.getTicks() == t2.getTicks();
	}

	public double getTotalMilliseconds()
	{
		double num = (double) this._ticks * 0.0001;
		if (num > 922337203685477.0)
			return 922337203685477.0;
		if (num < -922337203685477.0)
			return -922337203685477.0;
		else
			return num;
	}
}