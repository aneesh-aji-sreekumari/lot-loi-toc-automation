package com.example.lotloi_test_app.multithreading;
import com.example.lotloi_test_app.models.Tuple;
import com.example.lotloi_test_app.services.FrontMatterComparisonService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class TupleThread implements Callable<Optional<Tuple>> {
    public FrontMatterComparisonService frontMatterComparisonService;
    public ExecutorService es;
    public MultipartFile file;
    public TupleThread(
            FrontMatterComparisonService frontMatterComparisonService,
            ExecutorService es,
            MultipartFile file){
        this.es = es;
        this.frontMatterComparisonService = frontMatterComparisonService;
        this.file = file;
    }
    @Override
    public Optional<Tuple> call() throws Exception {
        System.out.println("Current Thread:" + Thread.currentThread().getName());
        return frontMatterComparisonService.readPDF(file);
    }
}
