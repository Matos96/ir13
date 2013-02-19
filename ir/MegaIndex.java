/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 */

package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


import com.larvalabs.megamap.MegaMap;
import com.larvalabs.megamap.MegaMapException;
import com.larvalabs.megamap.MegaMapManager;

public class MegaIndex implements Index
{

    private static final double PAGERANK_MULTIPLYER = 8;
    /**
     * The index as a hash map that can also extend to secondary memory if
     * necessary.
     */
    private MegaMap index;

    /**
     * The MegaMapManager is the user's entry point for creating and saving
     * MegaMaps on disk.
     */
    private MegaMapManager manager;

    /**
     * Store the size of each.
     */
    private HashMap<Integer, Integer> docSizeTable = new HashMap<Integer, Integer>();

    private int numberOfDocs = -1;

    /** The directory where to place index files on disk. */
    private static final String path = ".";

    /**
     * Create a new index and invent a name for it.
     */
    public MegaIndex()
    {
        try
        {
            manager = MegaMapManager.getMegaMapManager();
            index = manager
                    .createMegaMap(generateFilename(), path, true, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create a MegaIndex, possibly from a list of smaller indexes.
     */
    public MegaIndex(LinkedList<String> indexfiles)
    {
        try
        {
            manager = MegaMapManager.getMegaMapManager();
            if (indexfiles.size() == 0)
            {
                // No index file names specified. Construct a new index and
                // invent a name for it.
                index = manager.createMegaMap(generateFilename(), path, true,
                                              false);
            }
            else if (indexfiles.size() == 1)
            {
                // Read the specified index from file
                index = manager.createMegaMap(indexfiles.get(0), path, true,
                                              false);
                HashMap<String, String> m = (HashMap<String, String>) index
                                            .get("..docIDs");
                if (m == null)
                {
                    System.err
                    .println("Couldn't retrieve the associations between docIDs and document names");
                }
                else
                {
                    docIDs.putAll(m);
                }

                HashMap<String, Integer> m2 = (HashMap<String, Integer>) index
                                              .get("..docLengths");
                if (m2 == null)
                {
                    System.err
                    .println("Couldn't retrieve the docLengths");
                }
                else
                {
                    docLengths.putAll(m2);
                }

                HashMap<String, Double> m3 = (HashMap<String, Double>) index
                                              .get("..pageRanking");
                if (m3 == null)
                {
                    System.err
                    .println("Couldn't retrieve the pageRanking");
                }
                else
                {
                    pageRanking.putAll(m3);
                }


            }
            else
            {
                // Merge the specified index files into a large index.
                MegaMap[] indexesToBeMerged = new MegaMap[indexfiles.size()];
                for (int k = 0; k < indexfiles.size(); k++)
                {
                    System.err.println(indexfiles.get(k));
                    indexesToBeMerged[k] = manager.createMegaMap(
                                               indexfiles.get(k), path, true, false);
                }
                index = merge(indexesToBeMerged);
                for (int k = 0; k < indexfiles.size(); k++)
                {
                    manager.removeMegaMap(indexfiles.get(k));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Generates unique names for index files
     */
    String generateFilename()
    {
        String s = "index_" + Math.abs((new java.util.Date()).hashCode());
        System.err.println(s);
        return s;
    }

    /**
     * It is ABSOLUTELY ESSENTIAL to run this method before terminating the JVM,
     * otherwise the index files might become corrupted.
     */
    public void cleanup()
    {
        // Save the docID-filename association list in the MegaMap as well
        index.put("..docIDs", docIDs);
        index.put("..docLengths", docLengths);
        index.put("..pageRanking", pageRanking);
        // Shutdown the MegaMap thread gracefully
        manager.shutdown();
    }

    /**
     * Returns the dictionary (the set of terms in the index) as a HashSet.
     */
    public Set<String> getDictionary()
    {
        return index.getKeys();
    }

    /**
     * Merges several indexes into one.
     */
    MegaMap merge(MegaMap[] indexes)
    {
        try
        {
            MegaMap res = manager.createMegaMap(generateFilename(), path, true,
                                                false);
            for (MegaMap index : indexes)
            {


                @SuppressWarnings("unchecked")
                Set<String> keys = (Set<String>) index.getKeys();
                for (String s : keys)
                {
                    /* Fixing names of files <-> docID */
                    if (s.equals("..docIDs"))
                    {
                        HashMap<String, String> m = (HashMap<String, String>) index
                                                    .get("..docIDs");
                        if (m == null)
                        {
                            System.err
                            .println("Couldn't retrieve the associations between docIDs and document names");
                        }
                        else
                        {
                            docIDs.putAll(m);
                        }
                        continue;
                    }
                    /* End of fixing name */

                    /* Finxing docLength */
                    if (s.equals("..docLengths"))
                    {
                        HashMap<String, Integer> m = (HashMap<String, Integer>) index
                                                     .get("..docLengths");
                        if (m == null)
                        {
                            System.err
                            .println("Couldn't retrieve the docLengths");
                        }
                        else
                        {
                            docLengths.putAll(m);
                        }
                        continue;
                    }
                    /* End of fixing docLength */


                    /* Finxing docLength */
                    if (s.equals("..pageRanking"))
                    {
                        HashMap<String, Double> m = (HashMap<String, Double>) index
                                                     .get("..pageRanking");
                        if (m == null)
                        {
                            System.err
                            .println("Couldn't retrieve the pageRanking");
                        }
                        else
                        {
                            pageRanking.putAll(m);
                        }
                        continue;
                    }
                    /* End of fixing docLength */

                    if (res.hasKey(s))
                    {
                        try
                        {
                            PostingsList pl = (PostingsList) res.get(s);
                            if (pl != null && index.get(s) != null)
                                pl.merge((PostingsList) index.get(s));
                            else if (index.get(s) != null)
                            {
                                pl = (PostingsList) index.get(s);
                            }
                        }
                        catch (ClassCastException e)
                        {
                            System.out.println("error");
                        }
                    }
                    else if (s != null && index.get(s) != null)
                        res.put(s, (PostingsList)((PostingsList) index.get(s)).clone());
                }
            }
            return res;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Inserts this token in the hashtable.
     */
    public void insert(String token, int docID, int offset)
    {
        PostingsList list = null;
        try
        {
            list = (PostingsList) index.get(token);
        }
        catch (MegaMapException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        if (list == null)
        {
            list = new PostingsList();
            index.put(token, list);
        }
        list.add(docID, offset);

    }

    /**
     * Returns the postings for a specific term, or null if the term is not in
     * the index.
     */
    public PostingsList getPostings(String token)
    {
        try
        {
            return (PostingsList) index.get(token);
        }
        catch (Exception e)
        {
            return new PostingsList();
        }
    }

    /**
     * Searches the index for postings matching the query.
     */
    public PostingsList search(Query query, int queryType, int rankingType)
    {
        if (queryType == Index.INTERSECTION_QUERY)
        {
            ArrayList<PostingsList> lists = new ArrayList<PostingsList>();
            for (int i = 0; i < query.terms.size(); i++)
            {
                PostingsList pl = getPostings(query.terms.get(i));
                if (pl == null)
                    return new PostingsList();
                lists.add(pl);
            }

            Collections.sort(lists);

            PostingsList all = lists.get(0);
            lists.remove(0);
            for (PostingsList pl : lists)
            {
                all = PostingsList.removeAllNotIn(all, pl);
            }
            return all;
        }
        else if (queryType == Index.PHRASE_QUERY)
        {
            PostingsList all = getPostings(query.terms.getFirst());
            if (all == null)
                return null;
            for (int i = 1; i < query.terms.size(); i++)
            {
                PostingsList currentList = getPostings(query.terms.get(i));
                if (currentList == null)
                    return null;
                all = PostingsList.removeAllNotFollowedBy(all, currentList);
            }
            return all;
        }
        else if (queryType == Index.RANKED_QUERY)
        {
            PostingsList all = new PostingsList();
            if (numberOfDocs < 0)
                numberOfDocs = docIDs.size();
            for (String term : query.terms)
            {
                all = PostingsList.union(all, getPostings(term));
            }

            if (rankingType == Index.TF_IDF || rankingType == Index.COMBINATION)
            {
                for (String term : query.terms)
                {
                    PostingsList pl = getPostings(term);
                    if (pl == null)
                        continue;
                    double idf_for_pl = Math.log10(numberOfDocs / pl.size());
                    double wtq = 1 + Math.log10(1); // log(1) should be number of occurenses
                    // wtq *= idf_for_pl;
                    for (PostingsEntry post : pl.list)
                    {
                        PostingsEntry scoreEntry = all.getByDocID(post.docID);
                        if (post.offsets.size() != 0)
                        {
                            // scoreEntry.score += (1 + Math.log10(post.offsets.size())) * idf_for_pl * wtq;
                            scoreEntry.score += (post.offsets.size()) * idf_for_pl;
                        }
                    }

                }
                for (PostingsEntry post : all.list)
                {
                    // System.out.println(docLengths.get("" + post.docID));
                    post.score /=   docLengths.get("" + post.docID);
                    // System.out.println(docLengths.get("" + post.docID));
                }
            }

            if (rankingType == Index.PAGERANK || rankingType == Index.COMBINATION)
            {
                if (pageRanking != null)
                    for (PostingsEntry pe : all.list)
                    {
                        int docID = pe.docID;
                        // System.out.println(docID);
                        // System.out.println(pageRanking.size());
                        double score = (double) pageRanking.get("" + docID);
                        // System.out.println(score);
                        pe.score += (score) * PAGERANK_MULTIPLYER;
                    }
            }
            Collections.sort(all.list);
            return all;
        }
        return null;
    }

}
