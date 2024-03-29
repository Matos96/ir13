/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Christian Wemstad 2013
 */

package ir;

import java.util.*;

public class TriwordIndex implements Index {
    /** The index as a hashtable. */
    private HashMap<Biword, PostingsList> index = new HashMap<Biword, PostingsList>();


    private String previousToken = null;
    private String prevprevToken = null;
    private int previousDocID = -1;
    private int prevprevDocID = -1;
    private int numberOfDocs = -1;

    public void insert( String token, int docID, int offset ) {

        if (docID != previousDocID || docID != prevprevDocID) {
            prevprevToken = previousToken;
            prevprevDocID = previousDocID;
            previousToken = token;
            previousDocID = docID;
        } else {

            Biword bw = new Biword(prevprevToken, previousToken, token);
            PostingsList pl = index.get(bw);
            if (pl == null) {
                pl = new PostingsList();
                index.put(bw, pl);
            }
            pl.add(docID, offset);
            // HashSet<Biword> docSet = docTerms.get("" + docID);
            // if (docSet == null)
            // {
            //     docSet = new HashSet<Biword>();
            //     docTerms.put("" + docID, docSet),
            // }
            // docSet.add(bw);
            previousToken = token;
        }
    }
    public PostingsList getPostings( String token ) {
        System.out.println("Wrong getPostings()");
        return null;
    }
    public PostingsList getPostings(Biword biToken) {
        PostingsList list = index.get(biToken);
        if (list == null || list.size() == 0)
            return null;
        return list;
    }
    public PostingsList search(Query query, int queryType, int rankingType) {
        return search(query, queryType, rankingType, true);
    }
    public PostingsList search( Query query, int queryType, int rankingType, boolean sort ) {
        if (query.terms.size() <= 2)
            return null;
        ArrayList<Biword> biQuery = generateBiwords(query.terms);


        if (queryType == Index.INTERSECTION_QUERY) {
            ArrayList<PostingsList> lists = new ArrayList<PostingsList>();
            for (int i = 0; i < biQuery.size(); i++) {
                PostingsList pl = getPostings(biQuery.get(i));
                if (pl == null) {
                    System.out.println("found empty postinglist for BiTerm: " + biQuery.get(i));
                    // System.out.println(index.size());
                    for (Biword b : index.keySet()) {
                    //     Biword newB = new Biword(b.getfirstWord(), b.getSecondWord(), b.getThirdWord());
                        System.out.println(b);
                    }
                    return new PostingsList();
                }
                lists.add(pl);
            }

            Collections.sort(lists);

            PostingsList all = lists.get(0);
            lists.remove(0);
            for (PostingsList pl : lists) {
                all = PostingsList.removeAllNotIn(all, pl);
            }
            return all;

        } else if (queryType == Index.PHRASE_QUERY) {
            return null;
        } else if (queryType == Index.RANKED_QUERY) {
            long startTime = System.nanoTime();
            PostingsList all = new PostingsList();
            for (Biword biTerm : biQuery) {
                PostingsList pl = getPostings(biTerm);
                if (pl != null)
                    all = PostingsList.union(all, pl);
            }

            if (rankingType == Index.TF_IDF || rankingType == Index.COMBINATION) {
                for (Biword biTerm : biQuery) {
                    PostingsList pl = getPostings(biTerm);
                    if (pl == null) {
                        System.out.println("found empty postinglist for BiTerm: " + biTerm);
                        continue;
                    }
                    double idf_for_pl = Math.log10(getNumberOfDocs() / pl.size());
                    double wtq = 1 * idf_for_pl;
                    for (PostingsEntry post : pl.list) {
                        PostingsEntry scoreEntry = all.getByDocID(post.docID);
                        if (post.offsets.size() != 0) {
                            // scoreEntry.score += (1 + Math.log10(post.offsets.size())) * idf_for_pl * wtq;
                            // System.out.println(scoreEntry.score);
                            scoreEntry.score += (post.offsets.size()) * idf_for_pl * wtq;
                        }
                    }

                }
                for (PostingsEntry post : all.list) {
                    // System.out.println(docLengths.get("" + post.docID));
                    post.score /=   docLengths.get("" + post.docID);
                    // System.out.println(docLengths.get("" + post.docID));
                }
            }

            if (rankingType == Index.PAGERANK || rankingType == Index.COMBINATION) {
                if (pageRanking != null)
                    for (PostingsEntry pe : all.list) {
                        int docID = pe.docID;
                        // System.out.println(docID);
                        // System.out.println(pageRanking.size());
                        double score = (double) pageRanking.get("" + docID);
                        // System.out.println(score);
                        pe.score += (score) * PAGERANK_MULTIPLYER;
                    }
            }
            if (sort)
                Collections.sort(all.list);
            System.out.println("Time spent: " + (System.nanoTime() - startTime));
            return all;
        }
        return null;
    }
    public void cleanup() {

    }
    public int getNumberOfDocs() {
        if (numberOfDocs == -1) {
            numberOfDocs = docIDs.size();
        }
        return numberOfDocs;
    }
    private ArrayList<Biword> generateBiwords(LinkedList<String> terms) {
        String prev = null;
        String prevprev = null;
        ArrayList<Biword> returnList = new ArrayList<Biword>();
        for (String term : terms) {
            if (prev == null || prevprev == null) {
                prevprev = prev;
                prev = term;
            } else {
                Biword bw = new Biword(prevprev, prev, term);
                returnList.add(bw);
                prevprev = prev;
                prev = term;
            }
        }
        return returnList;
    }
    private class Biword {
        private String firstWord;
        private String secondWord;
        private String thirdWord;
        public Biword(String firstWord, String secondWord, String thirdWord) {
            this.firstWord = firstWord;
            this.secondWord = secondWord;
            this.thirdWord = thirdWord;
        }
        public String getfirstWord() {
            return firstWord;
        }
        public String getSecondWord() {
            return secondWord;
        }
        public String getThirdWord() {
            return thirdWord;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof Biword) {
                if (((Biword) o).firstWord.equals(firstWord) && ((Biword) o).secondWord.equals(secondWord) && ((Biword) o).thirdWord.equals(thirdWord)) {
                    return true;
                }
            }
            return false;
        }
        public int hashCode() {
            return firstWord.hashCode() + secondWord.hashCode() + thirdWord.hashCode();
        }
        public String toString() {
            return "[" + firstWord + ", " + secondWord + ", " + thirdWord + " ]";
        }
    }
}