package dev;

import java.util.ArrayList;
import java.util.HashMap;

public class Assignment2_4 {
	private HashMap<String, ArrayList<String>> mapOut;
	private HashMap<String, ArrayList<String>> mapRel;
    public void Precision(HashMap<String, ArrayList<String>> output, HashMap<String, ArrayList<String>> relevent)
    {
        mapOut = output;
        mapRel = relevent;
    }
    
    public float getPrecision(String docId ){
        ArrayList<String> arrOut = mapOut.get(docId);
        ArrayList<String> arrRel = mapRel.get(docId);
        int prec = 0;
        String paraId;
        
        for (int i = 0; i < arrOut.size(); i++)
        {
            paraId = arrRel.get(i);
            if ( arrRel.contains(paraId))
            {
                prec++;
            }
        }
        return (prec/arrOut.size());

    }
}
