package com.zoe.framework.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

public class TaskTest {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void run() {
        for (int i = 0; i < 1; i++) {
            logger.debug(i + " run......................................" + (new Date()));
        }
    }

    public void run1() {
        for (int i = 0; i < 1; i++) {
            logger.debug(i + " run1......................................" + (new Date()));
        }
    }

    public static void main(String[] args) {
        Map<String, Charset> charsets = Charset.availableCharsets();
        for (Map.Entry<String, Charset> entry : charsets.entrySet()) {
            System.out.println(entry.getKey());
        }
    }
}
