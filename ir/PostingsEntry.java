/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Finalized by: Christian Wemstad, 2013
 */

package ir;

import java.io.Serializable;
import java.util.ArrayList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable
{

    private static final long serialVersionUID = 8415974529423644037L;
    public int docID;
    public double score;
    public ArrayList<Integer> offsets;

    public PostingsEntry(int docID, int offset, double score)
    {
        this.docID = docID;
        this.score = score;
        offsets = new ArrayList<Integer>();
        offsets.add(offset);
    }

    public PostingsEntry(int docID, ArrayList<Integer> offsets, double score)
    {
        this.docID = docID;
        this.score = score;
        this.offsets = offsets;
    }

    /**
     * PostingsEntries are compared by their score (only relevant in ranked
     * retrieval).
     *
     * The comparison is defined so that entries will be put in descending
     * order.
     */
    public int compareTo(PostingsEntry other)
    {
        return Double.compare(other.score, score);
    }

    /*
     * @Override public boolean equals(Object other) { if (other instanceof
     * PostingsEntry) return docID == ((PostingsEntry) other).docID; return
     * false; }
     */

    public void addOffset(int offset)
    {
        int index = 0;
        for (Integer i : offsets)
        {
            if (offset == i)
                return;
            else if (offset > i)
                index++;
            else
                break;
        }
        offsets.add(index, offset);
    }

    public Object clone() throws CloneNotSupportedException
    {
        // super.clone();
        @SuppressWarnings("unchecked")
        ArrayList<Integer> clone = (ArrayList<Integer>) offsets.clone();
        return new PostingsEntry(docID, clone, score);
    }

    @Override
    public String toString()
    {
        return  docID + ": " + offsets.toString();
    }

}
