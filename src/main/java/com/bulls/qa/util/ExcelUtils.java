package com.bulls.qa.util;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    public static List<Map<Integer, String>> readExcel(File file) {
        List<Map<Integer, String>> list = new ArrayList<Map<Integer, String>>();
        try {
            Workbook workbook = null;
            if (file.isFile() && file.exists()) {
                String[] split = file.getName().split("\\.");
                if ("xls".equals(split[1])) {
                    FileInputStream fis = new FileInputStream(file);
                    workbook = new HSSFWorkbook(fis);
                } else if ("xlsx".equals(split[1])) {
                    workbook = new XSSFWorkbook(file);
                }
                list = excelToList(workbook);

                return list;

            } else {
                logger.error("指定的文件不存在:{}", file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<Integer, String>> readExcel(InputStream fis) {
        List<Map<Integer, String>> list = new ArrayList<Map<Integer, String>>();
        try {
            Workbook workbook = new XSSFWorkbook(fis);
            list = excelToList(workbook);
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
        return list;
    }

    public static List<Map<Integer, String>> excelToList(Workbook workbook) {
        List<Map<Integer, String>> list = new ArrayList<Map<Integer, String>>();
        // 开始解析
        Sheet sheet = workbook.getSheetAt(0); // 读取sheet 0
        int firstRowIndex = sheet.getFirstRowNum() + 1; // 第一行是列名，所以不读
        int lastRowIndex = sheet.getLastRowNum();

        for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) { // 遍历行
            Row row = sheet.getRow(rIndex);
            Map<Integer, String> map = new HashMap<Integer, String>();
            if (row != null) {
                int firstCellIndex = row.getFirstCellNum();
                int lastCellIndex = row.getLastCellNum();
                for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) { // 遍历列
                    Cell cell = row.getCell(cIndex);
                    if (cell != null) {
                        map.put(cIndex, cell.toString());
                    }
                }
            }
            list.add(map);
        }
        return list;
    }

    public static void writeXLSX(String file, List<Map<String, Object>> list, String fields) {

        if (list.size() == 0) {
            return;
        }
        String[] strings;
        if (fields == null) {
            Map<String, Object> map = list.get(0);
            strings = new String[map.keySet().size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                strings[i++] = entry.getKey();
            }
        } else {
            strings = fields.split(",");
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet1");
        XSSFRow row0 = sheet.createRow(0);
        XSSFCell cell = row0.createCell(0);
        int i = 1;
        for (String string : strings) {
            cell.setCellValue(string);
            cell = row0.createCell(i++);
        }
        for (int rowCount = 0; rowCount < list.size(); rowCount++) {
            XSSFRow row = sheet.createRow(rowCount + 1);
            for (int column = 0; column < strings.length; column++) {
                cell = row.createCell(column);
                cell.setCellValue(list.get(rowCount).get(strings[column]) == null ? "" : list.get(rowCount).get(strings[column]).toString());
            }
        }
        steamToFile(workbook, new File(file));
    }

    public static void writeXLS(String file, List<Map<String, Object>> list, String fields) {

        if (list.size() == 0) {
            return;
        }
        String[] strings;
        if (fields == null) {
            Map<String, Object> map = list.get(0);
            strings = new String[map.keySet().size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                strings[i++] = entry.getKey();
            }
        } else {
            strings = fields.split(",");
        }

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("sheet1");
        HSSFRow row0 = sheet.createRow(0);
        HSSFCell cell = row0.createCell(0);
        int i = 1;
        for (String string : strings) {
            cell.setCellValue(string);
            cell = row0.createCell(i++);
        }
        for (int rowCount = 0; rowCount < list.size(); rowCount++) {
            HSSFRow row = sheet.createRow(rowCount + 1);
            for (int column = 0; column < strings.length; column++) {
                cell = row.createCell(column);
                cell.setCellValue(list.get(rowCount).get(strings[column]) == null ? "" : list.get(rowCount).get(strings[column]).toString());
            }
        }
        steamToFile(workbook, new File(file));
    }

    public static void steamToFile(Workbook workbook, File file) {
        try {
            FileOutputStream is = new FileOutputStream(file);
            workbook.write(is);
            is.close();
        } catch (IOException e) {
            logger.info("文件写入异常:{}", file);
            e.printStackTrace();
        }

        logger.info("文件写入完成:{}", file);
    }


}

