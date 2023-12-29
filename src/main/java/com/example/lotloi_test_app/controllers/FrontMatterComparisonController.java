package com.example.lotloi_test_app.controllers;
import com.example.lotloi_test_app.models.LoiPageblockItem;
import com.example.lotloi_test_app.models.LotPageblockItem;
import com.example.lotloi_test_app.models.Tuple;
import com.example.lotloi_test_app.multithreading.TupleThread;
import com.example.lotloi_test_app.services.FrontMatterComparisonService;
import com.example.lotloi_test_app.services.LOIComparisonService;
import com.example.lotloi_test_app.services.LOTComparisonService;
import com.example.lotloi_test_app.services.TOCComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/api")
public class FrontMatterComparisonController {
    @Autowired
    FrontMatterComparisonService frontMatterComparisonService;
    @Autowired
    TOCComparisonService tocComparisonService;
    @Autowired
    LOTComparisonService lotComparisonService;
    @Autowired
    LOIComparisonService loiComparisonService;
    @PostMapping("/comparator")
    public ResponseEntity<List<String>> processPDF(
            @RequestPart("oldRevisionFile") MultipartFile pdfFile1,
            @RequestPart("currentRevisionFile") MultipartFile pdfFile2) {
        try {
            /* The tuple will contain the raw lines from toc, lot and loi as arrayList */
//            Optional<Tuple> oldFile = frontMatterComparisonService.readPDF(pdfFile1);
//            Optional<Tuple> newFile = frontMatterComparisonService.readPDF(pdfFile2);
            ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            System.out.println(" Available Processors :" + Runtime.getRuntime().availableProcessors());
            TupleThread tupleThreadOld = new TupleThread(frontMatterComparisonService, es, pdfFile1);
            TupleThread tupleThreadNew = new TupleThread(frontMatterComparisonService, es, pdfFile2);
            Future<Optional<Tuple>> promiseOfOld = es.submit(tupleThreadOld);
            Future<Optional<Tuple>> promiseOfNew = es.submit(tupleThreadNew);
            Optional<Tuple> oldFile = promiseOfOld.get();
            Optional<Tuple> newFile = promiseOfNew.get();
            if(oldFile.isEmpty() || newFile.isEmpty())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            /* Raw lines extracted from PDF from TOC, LOI, LOT Respectively */
            Optional<List<String>> oldTocList = Optional.of(oldFile.get().getTocList());
            Optional<List<String>> newTocList = Optional.of(newFile.get().getTocList());
            Optional<List<String>> oldLoiList = Optional.of(oldFile.get().getLoiList());
            Optional<List<String>> newLoiList = Optional.of(newFile.get().getLoiList());
            Optional<List<String>> oldLotList = Optional.of(oldFile.get().getLotList());
            Optional<List<String>> newLotList = Optional.of(newFile.get().getLotList());

            /* Filtering Unwanted Files From the lines Extracted From TOC/LOT/LOI*/
            Optional<ArrayList<String>> filteredOldLoi = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLoiList.get());
            Optional<ArrayList<String>> filteredNewLoi = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(newLoiList.get());
            Optional<ArrayList<String>> filteredOldLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(oldLotList.get());
            Optional<ArrayList<String>> filteredNewLot = lotComparisonService.filterUnwantedLinesFromLatestLotLoi(newLotList.get());
            /* Checking if the filtered Arrays have null value or not*/
            if(filteredOldLoi.isEmpty() || filteredNewLoi.isEmpty() || filteredOldLot.isEmpty() || filteredNewLot.isEmpty())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            /* Merging multiline title to single line */
            Optional<ArrayList<String>> formattedOldLot = lotComparisonService.makeMultilineTitlesSingleline(filteredOldLot.get());
            Optional<ArrayList<String>> formattedNewLot = lotComparisonService.makeMultilineTitlesSingleline(filteredNewLot.get());
            Optional<ArrayList<String>> formattedOldLoi = loiComparisonService.makeMultilineTitlesSingleline(filteredOldLoi.get());
            Optional<ArrayList<String>> formattedNewLoi = loiComparisonService.makeMultilineTitlesSingleline(filteredNewLoi.get());
             if (formattedOldLot.isEmpty() || formattedNewLot.isEmpty() || formattedOldLoi.isEmpty() || formattedNewLoi.isEmpty())
                 return new ResponseEntity<>(HttpStatus.NOT_FOUND);

             Optional<HashMap<String, LoiPageblockItem>> oldPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedOldLoi.get());
            Optional<HashMap<String, LoiPageblockItem>> newPageblockWiseLoi = loiComparisonService.getPageblockwiseIllustrations(formattedNewLoi.get());
            Optional<ArrayList<String>> loiComparisonResult = loiComparisonService.compareLoi(oldPageblockWiseLoi.get(), newPageblockWiseLoi.get());

            Optional<HashMap<String, LotPageblockItem>> oldPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedOldLot.get());
            Optional<HashMap<String, LotPageblockItem>> newPageblockWiseLot = lotComparisonService.getPageblockwiseTables(formattedNewLot.get());
            Optional<ArrayList<String>> lotComparisonResult = lotComparisonService.compareLot(oldPageblockWiseLot.get(), newPageblockWiseLot.get());




            /* Getting the revision changes comparing old TOC and new TOC */
            Optional<ArrayList<String>> tocOutput
                    = tocComparisonService.getRevisionChangesInToc(oldTocList, newTocList);
            if(tocOutput.isEmpty())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
           //*********************NewCode Start **************************************

            //*********************NewCode End **************************************
            return new ResponseEntity<>(tocOutput.get(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }
    }
}
