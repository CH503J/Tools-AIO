package com.ch503j.common.utils;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

public class IdGenerator {

    /**
     * 生成唯一 Long 类型 ID
     */
    public static Long nextId() {
        return IdWorker.getId();
    }

    /**
     * 生成字符串类型 ID（可选）
     */
    public static String nextIdStr() {
        return String.valueOf(IdWorker.getId());
    }
}
