/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */

package ir;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.*;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.HashMap;

/**
 * Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {

    /** The index to be built up by this indexer. */
    public Index index;

    /** The next docID to be generated. */
    private int lastDocID = 0;

    /* ----------------------------------------------- */

    /** Generates a new document identifier as an integer. */
    private int generateDocID() {
        return lastDocID++;
    }

    /** Generates a new document identifier based on the file name. */
    private int generateDocID(String s) {
        return s.hashCode();
    }

    /* ----------------------------------------------- */

    /**
     * Initializes the index as a HashedIndex.
     */
    public Indexer(int type) {
        if (type == Index.HASHED_INDEX)
            index = new HashedIndex();
        else if (type == Index.BIWORD_INDEX){
            index = new BiwordIndex();
            // System.out.println("Using biwordIndex");
        }
        else if(type == Index.TRIWORD_INDEX){
            index = new TriwordIndex();
        } else {System.out.println("ERROR: NO INDEX TO PICK");}
    }

    /**
     * Initializes the index as a MegaIndex.
     */
    public Indexer(int type, LinkedList<String> indexfiles) {
        if (type == Index.DUAL_INDEX)
            index = new DualIndex(indexfiles);
        else
            index = new MegaIndex(indexfiles);
    }

    /* ----------------------------------------------- */

    /**
     * Tokenizes and indexes the file @code{f}. If @code{f} is a directory, all
     * its files and subdirectories are recursively processed.
     */
    public void processFiles(File f) {
        // do not try to index fs that cannot be read
        if (f.canRead()) {
            if (f.isDirectory()) {
                String[] fs = f.list();
                // an IO error could occur
                if (fs != null) {
                    for (int i = 0; i < fs.length; i++) {
                        processFiles(new File(f, fs[i]));
                    }
                }
            } else {
                System.err.println("Indexing " + f.getPath());
                // First register the document and get a docID
                int docID;
                if (index instanceof HashedIndex || index instanceof BiwordIndex || index instanceof TriwordIndex) {
                    // For HashedIndex, use integers.
                    docID = generateDocID();
                } else {
                    // For MegaIndex, use hash codes based on file names.
                    try {
                        docID = generateDocID(f.getCanonicalPath());
                    } catch (IOException e) {
                        docID = generateDocID(f.getPath());
                    }
                }
                index.docIDs.put("" + docID, f.getPath());
                try {
                    // Read the first few bytes of the file to see if it is
                    // likely to be a PDF
                    Reader reader = new FileReader(f);
                    char[] buf = new char[4];
                    reader.read(buf, 0, 4);
                    if (buf[0] == '%' && buf[1] == 'P' && buf[2] == 'D'
                            && buf[3] == 'F') {
                        // We assume this is a PDF file
                        try {
                            String contents = extractPDFContents(f);
                            reader = new StringReader(contents);
                        } catch (IOException e) {
                            // Perhaps it wasn't a PDF file after all
                            reader = new FileReader(f);
                        }
                    } else {
                        // We hope this is ordinary text
                        reader = new FileReader(f);
                    }
                    SimpleTokenizer tok = new SimpleTokenizer(reader);
                    int offset = 0;
                    while (tok.hasMoreTokens()) {
                        insertIntoIndex(docID, tok.nextToken(), offset++);
                    }
                    index.docLengths.put("" + docID, offset);
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* ----------------------------------------------- */

    /**
     * Extracts the textual contents from a PDF file as one long string.
     */
    public String extractPDFContents(File f) throws IOException {
        FileInputStream fi = new FileInputStream(f);
        PDFParser parser = new PDFParser(fi);
        parser.parse();
        fi.close();
        COSDocument cd = parser.getDocument();
        PDFTextStripper stripper = new PDFTextStripper();
        String result = stripper.getText(new PDDocument(cd));
        cd.close();
        return result;
    }

    /* ----------------------------------------------- */

    /**
     * Indexes one token.
     */
    public void insertIntoIndex(int docID, String token, int offset) {
        index.insert(token, docID, offset);
    }

    public void addPageRank(HashMap<Integer, Double> al) {
        for (String docID : index.docIDs.keySet()) {
            int fileInt = -1;
            try {
                String value = index.docIDs.get(docID);
                int from = value.lastIndexOf('/');
                int to = value.lastIndexOf('.');
                fileInt = Integer.parseInt(value.substring(from + 1, to));
            } catch (Exception e) {
                System.err.println("Failed to parse!");
                System.exit(-1);
            }
            index.pageRanking.put(docID, al.get(fileInt));
        }

    }

}