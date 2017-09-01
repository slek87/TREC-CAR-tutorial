package dev;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import edu.unh.cs.treccar.Data;
import edu.unh.cs.treccar.read_data.DeserializeData;

public class dev {
	public static void main(String[] args) throws Exception{
        System.setProperty("file.encoding", "UTF-8");

        final FileInputStream fileInputStream2 = new FileInputStream(new File("train.test200.cbor.paragraphs"));

        for(Data.Paragraph p: DeserializeData.iterableParagraphs(fileInputStream2)) {
        	p.getParaId();
            p.getTextOnly();
            System.out.println();
        }

        System.out.println("\n\n");

    }
}
