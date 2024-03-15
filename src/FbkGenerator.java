/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Maxwell
 */
public class FbkGenerator {
    //Amend to Collections.synchronizedList if change to mutithreads
    private ArrayList<String> feedbacks = new ArrayList();
    private List<String> synFeedbacks = Collections.synchronizedList(feedbacks);
    private ArrayList<Map> qMaps = new ArrayList();
    private int fbkNum = 10;
    private String fileName = "Generated_Feedbacks";
    private boolean timeOut;

    public FbkGenerator(ArrayList<Map> qMaps, int fbkNum) {
        this.qMaps = qMaps;
        if (fbkNum != 0) {
            this.fbkNum = fbkNum;
        }
    }

    public boolean isDone() {
        return timeOut;
    }
    public void setFbkNum(int num) {
        this.fbkNum = num;
    }
    // gen and resurn feedbacks
    public ArrayList<String> getFeedbacks() {
        //Amend to add excutor if change to mutithreads
        feedbacks.clear();
        var executor = Executors.newCachedThreadPool();

        for (int i = 0; i < fbkNum; i++) {
            executor.execute(() -> {
                genFeedbacks();
            });
        }
        executor.shutdown();
        //insert the first row
        String firstRow = qMaps.stream().map(aMap -> (String) aMap.get("question"))
                .map(str ->str.replaceAll(":", "").trim()).collect(Collectors.joining(":"));
        synFeedbacks.add(0, "Num:"+firstRow);
        try {
            timeOut = executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        //insert number of feedbacks
        for(int i=1; i<feedbacks.size();i++){
            feedbacks.set(i, i+":"+feedbacks.get(i));
        }
        return feedbacks;
    }
    //gen excel file and save
    public String toExcel(String inputFileName) {
        //return file location or error msg String
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("Feedback_Data ");
        XSSFRow row;
        String returnMsg = "Excel File saved at:\n";
        int rowid = 0;
        //set excel file
        for (String feedback : feedbacks) {
            row = spreadsheet.createRow(rowid++);
            int cellid = 0;
            
            String[] cellValues = feedback.split(":");
            for (String value : cellValues) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue(value.trim());
            }
        }
        //check and set filename
        if (inputFileName != null && inputFileName.length() > 0) {
            this.fileName = inputFileName;
        }
        if (!fileName.contains("\\")) {
            String home = System.getProperty("user.home");
            fileName = home + "\\Downloads\\" + fileName;
        }
        File file = new File(fileName + ".xlsx");
        //save file
        try(FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
            out.close();
            returnMsg = returnMsg + file.getPath();
        } catch (FileNotFoundException fnfe) {
            System.out.println("File not found");
            returnMsg = "Unable to found file location, please select file path";
        } catch (IOException ioe) {
            System.out.println("toExcel(): IO problem");
            returnMsg = "Unknow IO problem";
        }
        return returnMsg;
    }
    //gen feedbacks
    //gen feedbacks
    private void genFeedbacks() {
        //assembly the data parts
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> qMap : qMaps) {
            //Amend to execute here if change to mutithreads
            String[] ans = qMap.get("answer").split("\n");
            int[] result = getResult(qMap, ans.length);
            for (int j = 0; j < result.length; j++) {
                int aResult = result[j];
                String anAns = ans[aResult].replaceAll(":", "");
                if (j == 0) {
                    sb.append(anAns);
                } else {
                    sb.append(",").append(anAns);
                }
            }
            //Amend here if change to mutithreads
            sb.append(":");
        }//Amend here if change to mutithreads
        synFeedbacks.add(sb.toString());
        //System.out.println("fb" + i + " gen scucess!");
    }
    //return the choosen answer location of a question
    private int[] getResult(Map<String, String> qMap, int numOfAns) {
        String ratio = qMap.get("ratio");
        int maxAns = 1;
        int maxRatio = 0;
        int[] ratioArray, result;
        //determine number of answers for the question
        if (!qMap.get("maxAns").isBlank()) {
            int input = Integer.parseInt(qMap.get("maxAns"));
            if (input > 1) {
                maxAns= (int) (input * Math.random()) + 1;
            }
        }//setup ratio array
        if (ratio.isEmpty()) {
            ratioArray = new int[numOfAns];
            for (int i = 0; i < numOfAns; i++) {
                ratioArray[i] = 1;
                maxRatio += 1;
            }
        } else {//for non default ratio
            String[] strArray = ratio.split(":");
            ratioArray = new int[strArray.length];
            for (int i = 0; i < strArray.length; i++) {
                int aRatio = Integer.parseInt(strArray[i].trim());
                ratioArray[i] = aRatio;
                maxRatio += aRatio;
            }
        }//setup result array
        result = new int[maxAns];
        for (int i = 0; i < maxAns; i++) {
            int num = (int) (maxRatio * Math.random());
            int checker = 0;
            //determine choosen answers according to ratio
            for (int j = 0; j < ratioArray.length; j++) {
                checker += ratioArray[j];
                if (num < checker) {
                    maxRatio -= ratioArray[j];
                    ratioArray[j] = 0;
                    result[i] = j;
                    break;
                }
            }
        }
        return result;
    }
}
