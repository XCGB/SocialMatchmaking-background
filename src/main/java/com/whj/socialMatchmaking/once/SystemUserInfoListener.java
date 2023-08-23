package com.whj.socialMatchmaking.once;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: Baldwin
 * @createTime: 2023-07-17 19:44
 * @description:
 */
@Slf4j
public class SystemUserInfoListener implements ReadListener<SystemUserInfo> {
    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context 文本内容
     */
    @Override
    public void invoke(SystemUserInfo data, AnalysisContext context) {
        log.info("" + data);
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context 文本内容
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        log.info("所有数据解析完成！");
    }

}