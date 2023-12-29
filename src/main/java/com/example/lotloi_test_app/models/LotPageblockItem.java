package com.example.lotloi_test_app.models;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
@Getter
@Setter
public class LotPageblockItem {
   private ArrayList<LotItem> listOfTables;
   private String pageblockName;
   public LotPageblockItem(String pageblockName){
       this.pageblockName = pageblockName;
       listOfTables = new ArrayList<>();
   }

}
