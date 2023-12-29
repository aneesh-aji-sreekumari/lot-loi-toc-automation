package com.example.lotloi_test_app.services;
import com.example.lotloi_test_app.models.Tuple;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FrontMatterComparisonService {
    /* Reads the PDF document and stores TOC, LOI and LOT pages separately and returns a
    list of each one in a Tuple class*/
public  Optional<Tuple> readPDF(MultipartFile multipartFile) throws IOException {
    List<String> lines = new ArrayList<>();
    Tuple tuple = new Tuple();

    try{
        InputStream inputStream = multipartFile.getInputStream();

         PDDocument document = Loader.loadPDF(inputStream.readAllBytes());

        PDFTextStripper textStripper = new PDFTextStripper();

        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            textStripper.setStartPage(i);
            textStripper.setEndPage(i);

            String pageText = textStripper.getText(document);
            String[] pageLines = pageText.split(System.lineSeparator());
            boolean isToc = false;
            boolean isLoi = false;
            boolean isLot = false;
            for (String line : pageLines) {
                if(line.contains("TABLE OF CONTENTS"))
                    isToc = true;
                else if(line.contains("LIST OF ILLUSTRATIONS"))
                    isLoi = true;
                else if(line.contains("LIST OF TABLES"))
                    isLot = true;
                if(isToc){
                    tuple.getTocList().add(line);
                }
                else if(isLoi){
                    tuple.getLoiList().add(line);
                }
                else if(isLot){
                    tuple.getLotList().add(line);
                }
            }
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    return Optional.of(tuple);
}
}
