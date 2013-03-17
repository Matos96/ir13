/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Hedvig Kjellström, 2012
 */

package ir;

import java.util.*;

public class Query
{
    private static final double ALPHA = 0.1;
    private static final double BETA = 0.9;
    public LinkedList<String> terms = new LinkedList<String>();
    public HashMap<String, Double> weights = new HashMap<String, Double>();

    /**
     *  Creates a new empty Query
     */
    public Query()
    {
    }

    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  )
    {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() )
        {
            String term = tok.nextToken();
            terms.add(term);
            Double weight = weights.get(term);
            if (weight == null)
                weight = new Double(0);
            weights.put(term, new Double( 1 + weight));
        }
        for (String term : terms)
        {
            weights.put(term, weights.get(term) / terms.size());
        }

    }

    /**
     *  Returns the number of terms
     */
    public int size()
    {
        return terms.size();
    }

    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy()
    {
        Query queryCopy = new Query();
        queryCopy.terms = (LinkedList<String>) terms.clone();
        queryCopy.weights = (HashMap<String, Double>) weights.clone();
        return queryCopy;
    }

    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer )
    {
        // results contain the ranked list from the current search
        // docIsRelevant contains the users feedback on which of the 10 first hits are relevant
        for (String term : terms)
        {
            weights.put(term, weights.get(term) * ALPHA);
        }
        double score = 0;
        for (int i = 0; i < docIsRelevant.length; i++)
        {
            if (docIsRelevant[i])
                score++;
        }
        score = 1 / score;
        for (int i = 0; i < docIsRelevant.length; i++)
        {
            if (docIsRelevant[i])
            {
                int docId = results.get(i).docID;
                HashSet<String> docTerms = indexer.index.docTerms.get("" + docId);
                // System.out.println(docTerms.size());
                double docSize = (double) indexer.index.docLengths.get("" + docId);
                for (String term : docTerms)
                {
                    PostingsList pl = indexer.index.getPostings(term);
                    if (!termIsBad(pl.size(), indexer.index.getNumberOfDocs()))
                    {
                        double tf = 0;
                        for (PostingsEntry pe : pl.list)
                        {
                            if (pe.docID == docId)
                            {
                                tf = pe.offsets.size();
                                break;
                            }
                        }
                        tf /= docSize;

                        if (!weights.containsKey(term))
                        {
                            weights.put(term, tf * BETA * score);
                            terms.add(term);
                        }
                        else
                        {
                            weights.put(term, weights.get(term) + tf * BETA * score);
                        }
                    }
                }
            }
        }

    }
    private boolean termIsBad(double size, double numberOfDocs)
    {
        // System.out.println("Size: " + size);
        // System.out.println("numberOfDocs: " + numberOfDocs);
        // System.out.println("Result: " + (size / numberOfDocs > Index.INDEX_ELIMINATON_CONSTANT));
        return size / numberOfDocs > Index.INDEX_ELIMINATON_CONSTANT;
    }
    public boolean queryTermIsBad(String term, Index index)
    {
        PostingsList pl = index.getPostings(term);
        if (pl == null)
            return false;
        return  termIsBad((double) pl.size(), (double) index.getNumberOfDocs());
    }
    public boolean queryTermIsBad(int position, Index index)
    {
        return queryTermIsBad(terms.get(position), index);
    }
}