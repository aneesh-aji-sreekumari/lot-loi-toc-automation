package com.example.lotloi_test_app;

import com.example.lotloi_test_app.models.LoiItem;

public class Client {
    public static void main(String[] args) {
        String str = "IPL 2 Chassis Assembly A1 (CPN 676-3126-005) 10121";
        //System.out.println(testClass.matchesPattern(substring, "\\s\\d{4}"));
        LoiItem loiItem = testClass.getNumberTitleAndPageNumberofFirstLine(str);
        System.out.println(loiItem.getFigureNumber());
        System.out.println(loiItem.getFigureTitle());
        System.out.println(loiItem.getPageNumber());
        System.out.println(loiItem.getPageblock());
    }

    public static class testClass {
        private static boolean matchesPattern(String input, String patternToMatch) {
            // Check if the input matches the specified pattern
            return input.matches(patternToMatch);
        }

        public static String getPageBlock(String str){
            if(str.startsWith("IPL"))
                return "ILLUSTRATED PARTS LIST";
            int tableNumber = Integer.parseInt(str);
            if(tableNumber>=1 && tableNumber<=999)
                return "DESCRIPTION AND OPERATION";
            else if(tableNumber>=1001 && tableNumber<=1999)
                return "TESTING AND FAULT ISOLATION";
            else if(tableNumber>=2001 && tableNumber<=2999)
                return "SCHEMATIC AND WIRING DIAGRAMS";
            else if(tableNumber>=3001 && tableNumber<=3999)
                return "DISASSEMBLY";
            else if(tableNumber>=4001 && tableNumber<=4999)
                return "CLEANING";
            else if(tableNumber>=5001 && tableNumber<=5999)
                return "INSPECTION/CHECK";
            else if(tableNumber>=6001 && tableNumber<=6999)
                return "REPAIR";
            else if(tableNumber>=7001 && tableNumber<=7999)
                return "ASSEMBLY";
            else if(tableNumber>=8001 && tableNumber<=8999)
                return "FITS AND CLEARANCES";
            else if(tableNumber>=9001 && tableNumber<=9999)
                return "SPECIAL TOOLS, FIXTURES, EQUIPMENT, AND CONSUMABLES";
            else if(tableNumber>=10001 && tableNumber<=10999)
                return "ILLUSTRATED PARTS LIST";
            else if(tableNumber>=11001 && tableNumber<=11999)
                return "SPECIAL PROCEDURES";
            else if(tableNumber>=12001 && tableNumber<=12999)
                return "REMOVAL";
            else if(tableNumber>=13001 && tableNumber<=13999)
                return "INSTALLATION";
            else if(tableNumber>=14001 && tableNumber<=14999)
                return "SERVICING";
            else if(tableNumber>=15001 && tableNumber<=15999)
                return "STORAGE INCLUDING TRANSPORTATION";
            else if(tableNumber>=16001 && tableNumber<=16999)
                return "REWORK";
            return "Not valid";
        }
        public static LoiItem getNumberTitleAndPageNumberofFirstLine(String s){
            int N = s.length();
            LoiItem loiItem = new LoiItem();
            StringBuilder sb = new StringBuilder();
            boolean isTitleFound =  false;
            int i=0;
            if(s.startsWith("IPL")){
                if(s.charAt(3) == '*'){
                    i = 4;
                }
                else{
                    sb.append("IPL ");
                    i=4;
                }
            }
            while (i<N){
                if(s.charAt(i) == ' ' && (!sb.isEmpty())){
                    loiItem.setFigureNumber(sb.toString().trim());
                    loiItem.setPageblock(getPageBlock(loiItem.getFigureNumber()));
                    sb = new StringBuilder();
                    i= i+1;
                    break;
                }
                sb.append(s.charAt(i));
                i++;

            }
            while(i<N){
                char ch = s.charAt(i);
                if(ch == '.' && !sb.isEmpty()){
                    loiItem.setFigureTitle(sb.toString().trim());
                    sb = new StringBuilder();
                    isTitleFound = true;
                }
                else if(ch == '.'){
                    i++;
                    continue;
                }
                else {
                    sb.append(ch);
                }

                i++;
            }
            if(!sb.isEmpty()){
                if(isTitleFound == false){
                    loiItem.setFigureTitle(sb.toString().trim());
                }
                else{
                    loiItem.setPageNumber(sb.toString().trim());
                }
            }
        /* If loiItem.getPageNumber == null : Additional check to find whether the last characters of the String is
        pagenumber or is it part of table title  */

            if(loiItem.getPageNumber() == null){
                System.out.println("I am in");
                sb = new StringBuilder();
                String str = loiItem.getFigureTitle();
                N = str.length();
                int idx = N-1;
                for(i=N-1; i>=0; i--){
                    if(str.charAt(i)>='0' && str.charAt(i)<='9'){
                        sb.append(str.charAt(i));
                    }

                    else{
                        idx = i;
                        break;
                    }
                }
                if(sb.isEmpty())
                    return loiItem;
                try{
                    sb = sb.reverse();
                    int num = Integer.parseInt(sb.toString());
                    if(loiItem.getPageblock().equals(getPageBlock(sb.toString()))){
                        loiItem.setFigureTitle(loiItem.getFigureTitle().substring(0, idx+1).trim());
                        loiItem.setPageNumber(sb.toString());
                    }
                    else
                        return loiItem;
                } catch (NumberFormatException e) {
                    return loiItem;
                }
            }


            return loiItem;
        }
    }
}


