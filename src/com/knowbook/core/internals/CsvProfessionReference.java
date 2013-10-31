package com.knowbook.core.internals;

import com.csvreader.CsvReader;
import com.knowbook.core.ProfessionReference;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class CsvProfessionReference implements ProfessionReference {

    private CsvReader csvReader;

    @Override
    public String getCurrentFieldName() {
        return read(0);
    }

    @Override
    public String getCurrentBranchName() {
        return read(1);
    }

    @Override
    public String getCurrentProfessionName() {
        return read(2);
    }

    private String read(int i) {
        try {
            return csvReader.get(i).replace((char)160, ' ').trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean readNextProfession() {
        try {
            return csvReader != null && csvReader.readRecord();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("professions.csv");
        csvReader = new CsvReader(inputStream, Charset.forName("utf-8"));
    }

    @Override
    public void finish() {
        csvReader.close();
    }

}
