package com.whj.socialMatchmaking.once;

import com.alibaba.excel.EasyExcel;
import java.util.List;

/**
 * @author: Baldwin
 * @createTime: 2023-07-17 19:33
 * @description: 导入Excel
 */
public class ImportExcel {
    public static void main(String[] args) {
        String fileName = "C:\\Users\\WHJ\\Desktop\\userDates.xlsx";
//         listenerRead(fileName);
        synchronousRead(fileName);
    }

    /**
     * 监听器
     * @param fileName 文件名
     */
    public static void listenerRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        EasyExcel.read(fileName, SystemUserInfo.class, new SystemUserInfoListener()).sheet().doRead();
    }

    /**
     * 同步读
     * @param fileName 文件名
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<SystemUserInfo> list = EasyExcel.read(fileName).head(SystemUserInfo.class).sheet().doReadSync();
        for (SystemUserInfo data : list) {
            System.out.println(data);
        }
    }

}