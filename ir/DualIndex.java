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
    private Index triwordIndex;
    private static final int K = 10;

    public DualIndex(LinkedList<String> indexfiles) {
        megaIndex = new MegaIndex(indexfiles);
        biwordIndex = new BiwordIndex();
        triwordIndex = new TriwordIndex();
    }

    public void insert( String token, int docID, int offset ) {
        megaIndex.insert(token, docID, offset);
        biwordIndex.insert(token, docID, offset);
        triwordIndex.insert(token, docID, offset);
    }
    public PostingsList getPostings( String token ) {
        System.out.println("Wrong getPostings()");
        return null;
    }
    public PostingsList search( Query query, int queryType, int rankingType, boolean sort) {
        System.out.println("Wrong call search in dual");
        return null;
    }
    public PostingsList search( Query query, int queryType, int rankingType ) {
        PostingsList triwordIndexPL = triwordIndex.search(query,queryType, rankingType,  false);
        System.out.println("Number of words in tri: " + triwordIndexPL.size());
        if(triwordIndexPL.size() >= K) {
            Collections.sort(triwordIndexPL.list);
            System.out.println("Used only tri");
            return triwordIndexPL;
        }
        PostingsList biwordIndexPL = biwordIndex.search(query, queryType, rankingType, false);
        biwordIndexPL.merge(triwordIndexPL, 1);
        System.out.println("Number of words in bi and tri: " + biwordIndexPL.size());
        if(biwordIndexPL.size() >= K) {
            Collections.sort(biwordIndexPL.list);
            System.out.println("Used only tri and bi");
            return biwordIndexPL;
        }
        // System.out.println("Used docID in biword: " + biwordIndexPL.toString());
        PostingsList megaIndexPL = megaIndex.search(query, queryType, rankingType, false);
        megaIndexPL.merge(biwordIndexPL, 1);
        Collections.sort(megaIndexPL.list);
        return megaIndexPL;
    }
    public void cleanup() {
        megaIndex.cleanup();
        biwordIndex.cleanup();
        triwordIndex.cleanup();
    }
    public int getNumberOfDocs() {
        return megaIndex.getNumberOfDocs();
    }
}