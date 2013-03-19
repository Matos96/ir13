/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Christian Wemstad 2013
 */

package ir;

import java.util.*;

public class DualIndex implements Index {
    /** The index as a hashtable. */
    private Index megaIndex;
    private Index biwordIndex;

    public DualIndex(LinkedList<String> indexfiles) {
        megaIndex = new MegaIndex(indexfiles);
        biwordIndex = new BiwordIndex();
    }

    public void insert( String token, int docID, int offset ) {
        megaIndex.insert(token, docID, offset);
        biwordIndex.insert(token, docID, offset);
    }
    public PostingsList getPostings( String token ) {
        System.out.println("Wrong getPostings()");
        return null;
    }
    public PostingsList search( Query query, int queryType, int rankingType, boolean sort){
        System.out.println("Wrong call search in dual");
        return null;
    }
    public PostingsList search( Query query, int queryType, int rankingType ) {
        PostingsList megaIndexPL = megaIndex.search(query, queryType, rankingType, false);
        PostingsList biwordIndexPL = biwordIndex.search(query, queryType, rankingType, false);
        megaIndexPL.merge(biwordIndexPL);
        Collections.sort(megaIndexPL.list);
        return megaIndexPL;
    }
    public void cleanup() {
        megaIndex.cleanup();
        biwordIndex.cleanup();
    }
    public int getNumberOfDocs() {
        return megaIndex.getNumberOfDocs();
    }
}