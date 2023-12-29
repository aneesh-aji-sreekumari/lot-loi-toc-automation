package com.example.lotloi_test_app.services;
import com.example.lotloi_test_app.models.PageBlock;
import com.example.lotloi_test_app.models.SubSubTopic;
import com.example.lotloi_test_app.models.SubTopic;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TOCComparisonService {
    public Optional<ArrayList<String>> getRevisionChangesInToc(Optional<List<String>> OldLines, Optional<List<String>> newLines) throws IOException {
        if(OldLines.isEmpty() || newLines.isEmpty())
            throw new IOException();
        //Filtering the unwanted lines from the String array
        Optional<ArrayList<String>> formattedOldToc = filterUnwantedLinesFromLatestPdf(OldLines.get());
        Optional<ArrayList<String>> formattedNewToc = filterUnwantedLinesFromLatestPdf(newLines.get());
        //Checking whether the filterUnwantedLinesFromLatestPdf method return a null
        if (formattedOldToc.isEmpty() || formattedNewToc.isEmpty())
            throw new RuntimeException("There was some issue in reading the PDF");

        // segregating content page-block, subtopic and sub-subtopic wise
        Optional<ArrayList<PageBlock>> oldPageBlockArrayList = getPageblockWiseContent(formattedOldToc.get());
        Optional<ArrayList<PageBlock>> newPageBlockArrayList = getPageblockWiseContent(formattedNewToc.get());
        //Checking whether the filterUnwantedLinesFromLatestPdf method return a null
        if (oldPageBlockArrayList.isEmpty() || newPageBlockArrayList.isEmpty())
            throw new RuntimeException("There was some issue in reading the PDF");
        //Generating the output
        return compareOldAndNewToc(oldPageBlockArrayList.get(), newPageBlockArrayList.get());
    }

    public String[] extractTOCInfoFromPageblockString(String s) {
        String[] ans = new String[2];
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '.')
                sb.append(s.charAt(i));
            else if (s.charAt(i) == ' ' && !sb.isEmpty())
                sb.append(s.charAt(i));
            else if (s.charAt(i) == '.') {
                if (sb.isEmpty())
                    continue;
                ans[idx] = sb.toString().trim();
                idx++;
                sb = new StringBuilder();
            }

        }
        if (!sb.isEmpty())
            ans[idx] = sb.toString().trim();
        return ans;
    }

    public String[] extractTOCInfoFromSubOrSubsubTopicString(String s) {
        String[] ans = new String[3];
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ' && s.charAt(i) != '.')
                sb.append(s.charAt(i));
            else if (s.charAt(i) == ' ' && !sb.isEmpty())
                sb.append(s.charAt(i));
            else if (s.charAt(i) == '.') {
                if (sb.isEmpty())
                    continue;
                ans[idx] = sb.toString().trim();
                idx++;
                sb = new StringBuilder();
            }

        }
        if (!sb.isEmpty())
            ans[idx] = sb.toString().trim();
        return ans;
    }

    public int isPageblockTitle(String s) {
        String[] listOfPageBlocks = new String[]{"INTRODUCTION", "DESCRIPTION AND OPERATION", "TESTING AND FAULT",
                "SCHEMATIC AND WIRING DIAGRAMS", "DISASSEMBLY", "CLEANING", "INSPECTION", "CHECK", "REPAIR", "ASSEMBLY",
                "FITS AND CLEARANCES", "SPECIAL TOOLS", "ILLUSTRATED PARTS LIST", "SPECIAL PROCEDURES", "REMOVAL", "INSTALLATION",
                "STORAGE", "SERVICING", "REWORK", "APPENDIX"};
        int N = listOfPageBlocks.length;
        for (int i = 0; i < N; i++) {
            if (s.contains(listOfPageBlocks[i]))
                return 1;
        }
        return 0;
    }

    public Optional<ArrayList<String>> filterUnwantedLinesFromLatestPdf(List<String> lines) {
        int N = lines.size();
        ArrayList<String> finalList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            String s = lines.get(i);
            if (s.contains("cover page") || s.contains("TOC") || !s.contains("."))
                continue;
            finalList.add(s);
        }
        ArrayList<String> processed = new ArrayList<>();
        N = finalList.size();
        for (int i = 0; i < N; i++) {
            String s = finalList.get(i);
            if (isPageblockTitle(s) == 1 || s.substring(0, 3).contains("."))
                processed.add(s);
            else {
                int j = processed.size() - 1;
                processed.set(j, processed.get(j) + " " + s);
            }

        }
        return Optional.of(processed);
    }

    public Optional<ArrayList<PageBlock>> getPageblockWiseContent(ArrayList<String> listOfToc) {
        int N = listOfToc.size();
        ArrayList<PageBlock> ans = new ArrayList<>();
        PageBlock currentPageBlock = null;
        SubTopic currentSubTopic = null;
        for (int i = 0; i < N; i++) {
            String line = listOfToc.get(i);
            if (isPageblockTitle(line) == 1) {
                String[] info = extractTOCInfoFromPageblockString(line);
                PageBlock pageBlock = new PageBlock();
                pageBlock.pageBlockName = info[0];
                pageBlock.pageNumber = info[1];
                ans.add(pageBlock);
                currentPageBlock = pageBlock;
            } else if (line.charAt(0) >= 'A' && line.charAt(0) <= 'Z') {
                String[] info = extractTOCInfoFromSubOrSubsubTopicString(line);
                SubSubTopic subSubTopic = new SubSubTopic();
                subSubTopic.number = info[0];
                subSubTopic.subject = info[1];
                subSubTopic.pageNumber = info[2];
                currentSubTopic.subSubTopicList.add(subSubTopic);
            } else {
                String[] info = extractTOCInfoFromSubOrSubsubTopicString(line);
                SubTopic subTopic = new SubTopic();
                subTopic.number = info[0];
                subTopic.subject = info[1];
                subTopic.pageNumber = info[2];
                currentPageBlock.subTopicList.add(subTopic);
                currentSubTopic = subTopic;
            }
        }
        return Optional.of(ans);
    }

    //Comparing old and new TOC and getting the differences
    public Optional<ArrayList<String>> compareOldAndNewToc(ArrayList<PageBlock> oldToc, ArrayList<PageBlock> newToc) {
        ArrayList<String> ans = new ArrayList<>();
        int N = oldToc.size();
        for (int i = 0; i < N; i++) {
            PageBlock oldPageBlock = oldToc.get(i);
            PageBlock newPageBlock = newToc.get(i);
            ArrayList<SubTopic> oldSubTopicList = oldPageBlock.subTopicList;
            ArrayList<SubTopic> newSubTopicList = newPageBlock.subTopicList;
            int oldLen = oldSubTopicList.size();
            int newLen = newSubTopicList.size();
            int j = 0, k = 0;
            while (j < oldLen && k < newLen) {
                SubTopic oldSubTopic = oldSubTopicList.get(j);
                SubTopic newSubTopic = newSubTopicList.get(k);
                if (oldSubTopic.subject.equals(newSubTopic.subject)) {
                    if (!oldSubTopic.pageNumber.equals(newSubTopic.pageNumber)) {
                        ans.add("Add Revbar for : " + newPageBlock.pageBlockName + " " + newSubTopic.number + ". " + newSubTopic.subject
                                + " Page number got changed from " + oldSubTopic.pageNumber + " to " + newSubTopic.pageNumber);
                    }
                } else {
                    ans.add("Add Revbar for : " + newPageBlock.pageBlockName + ": Title Description changed from " + oldSubTopic.number + ". " +
                            oldSubTopic.subject + " to " + newSubTopic.number + ". " + newSubTopic.subject);
                }
                ArrayList<SubSubTopic> oldSubSubTopicList = oldSubTopic.subSubTopicList;
                ArrayList<SubSubTopic> newSubSubTopicList = newSubTopic.subSubTopicList;
                int x = oldSubSubTopicList.size();
                int y = newSubSubTopicList.size();
                int l = 0, m = 0;
                while (l < x && m < y) {
                    SubSubTopic oldSubSubTopic = oldSubSubTopicList.get(l);
                    SubSubTopic newSubSubTopic = newSubSubTopicList.get(m);
                    if (oldSubSubTopic.subject.equals(newSubSubTopic.subject)) {
                        if (!oldSubSubTopic.pageNumber.equals(newSubSubTopic.pageNumber)) {
                            ans.add("Add Revbar for : " + newPageBlock.pageBlockName + " " + newSubTopic.number + ". " +
                                    " " + newSubSubTopic.number + ". " + newSubSubTopic.subject
                                    + " : Page number got changed from " + oldSubSubTopic.pageNumber + " to " + newSubSubTopic.pageNumber);
                        }
                    } else {
                        ans.add("Add Revbar for : " + newPageBlock.pageBlockName + " " + newSubTopic.number + ". " +
                                " : Title Description changed from " + oldSubSubTopic.number + ". " +
                                oldSubSubTopic.subject + " to " + newSubSubTopic.number + ". " + newSubSubTopic.subject);
                    }
                    l++;
                    m++;
                }
                while (m < y) {
                    SubSubTopic newSubSubTopic = newSubSubTopicList.get(m);
                    ans.add("Add Revbar for : newly added title " + newPageBlock.pageBlockName + " " + newSubTopic.number + ". "
                            + newSubSubTopic.number + ". " + newSubSubTopic.subject);
                    m++;
                }
                j++;
                k++;
            }
            while (k < newLen) {
                SubTopic newSubTopic = newSubTopicList.get(k);
                ans.add("Add Revbar for : newly added title " + newPageBlock.pageBlockName + " " + newSubTopic.number + ". "
                        + newSubTopic.subject);
                ArrayList<SubSubTopic> newSubSubTopicList = newSubTopic.subSubTopicList;
                int len = newSubSubTopicList.size();
                for (int t = 0; t < len; t++) {
                    SubSubTopic newSubSubTopic = newSubSubTopicList.get(t);
                    ans.add("Add Revbar for : newly added title " + newPageBlock.pageBlockName + " " + newSubTopic.number + ". "
                            + newSubSubTopic.number + ". " + newSubSubTopic.subject);
                }
                k++;
            }

        }
        return Optional.of(ans);
    }
}
