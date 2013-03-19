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
import java.util.Collections;
import java.util.LinkedList;

/**
 * A list of postings for a given word.
 */
public class PostingsList implements Serializable, Comparable<PostingsList> {

    private static final long serialVersionUID = 2230139515028354609L;

    /** The postings list as a linked list. */
    public LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

    public PostingsList() {

    }

    /*
     * public PostingsList(LinkedList<PostingsEntry> list) { this.list = list; }
     */

    /** Number of postings in this list */
    public int size() {
        return list.size();
    }

    /** Returns the ith posting */
    public PostingsEntry get(int i) {
        return list.get(i);
    }

    /** Removes the ith posting */
    private void remove(int i) {
        list.remove(i);
    }

    /** Returns true if the list contains entry with docID */
    private boolean contains(PostingsEntry postingsEntry) {
        for (PostingsEntry p : list)
            if (postingsEntry.equals(p))
                return true;
        return false;
    }

    /** Add docID to the list */
    public void add(int docID, int offset) {
        PostingsEntry pe = null;
        int i = 0;
        for (PostingsEntry p : list) {
            if (p.docID == docID) {
                pe = p;
                break;
            } else if (p.docID < docID)
                i++;
            else
                break;

        }
        if (pe == null)
            list.add(i, new PostingsEntry(docID, offset, 0));
        else
            pe.addOffset(offset);

    }
    public void addLast(int docID, int offset) {
        list.addLast(new PostingsEntry(docID, offset, 0));
    }

    public PostingsEntry getByDocID(int docID) {
        for (PostingsEntry p : list)
            if (p.docID == docID)
                return p;
        return null;
    }

    public static PostingsList removeAllNotIn(PostingsList firstList,
            PostingsList secondList) {
        int i = 0;
        int j = 0;
        PostingsList returnList = new PostingsList();
        while (i < secondList.size() && j < firstList.size()) {
            int secondDoc = secondList.get(i).docID;
            int firstDoc = firstList.get(j).docID;
            if (secondDoc == firstDoc) {
                returnList.addLast(firstDoc, 0); // Should merge offsets
                i++;
                j++;
            } else if (secondDoc > firstDoc) {
                j++;
            } else if (secondDoc < firstDoc) {
                i++;
            }
        }
        return returnList;
    }
    @SuppressWarnings("unchecked")
    @Override
    protected Object clone() {
        // super.clone();
        PostingsList returnList = new PostingsList();
        returnList.list = (LinkedList<PostingsEntry>) list.clone();
        return returnList;
    }

    public static PostingsList union(PostingsList firstList, PostingsList secondList) {
        PostingsList returnList = new PostingsList();
        int i = 0;
        int j = 0;
        if (secondList == null && firstList == null) {
            return returnList;
        }
        if (secondList == null) {
            return (PostingsList)firstList.clone();
        }
        if (firstList == null) {
            return (PostingsList) secondList.clone();
        }
        while (i < secondList.size() && j < firstList.size()) {
            int firstDoc = firstList.get(j).docID;
            int secondDoc = secondList.get(i).docID;
            if (secondDoc == firstDoc) {
                returnList.addLast(firstDoc, 0);
                j++;
                i++;
            } else if (secondDoc > firstDoc) {
                returnList.addLast(firstDoc, 0);
                j++;
            } else if (secondDoc < firstDoc) {
                returnList.addLast(secondDoc, 0);
                i++;
            }
        }
        while (i < secondList.size()) {
            int secondDoc = secondList.get(i).docID;
            returnList.addLast(secondDoc, 0);
            i++;
        }
        while (j < firstList.size()) {
            int firstDoc = firstList.get(j).docID;
            returnList.addLast(firstDoc, 0);
            j++;
        }
        return returnList;
    }


    public static PostingsList removeAllNotFollowedBy(PostingsList firstList,
            PostingsList secondList) {
        int i = 0;
        int j = 0;
        PostingsList returnList = new PostingsList();
        while (i < secondList.size() && j < firstList.size()) {
            int firstDoc = firstList.get(j).docID;
            int secondDoc = secondList.get(i).docID;
            if (secondDoc == firstDoc) {
                PostingsEntry first = firstList.get(j);
                PostingsEntry second = secondList.get(i);
                int ii = 0;
                int jj = 0;
                while (ii < second.offsets.size() && jj < first.offsets.size()) {
                    int firstOff = first.offsets.get(jj);
                    int secondOff = second.offsets.get(ii);
                    if (firstOff == (secondOff - 1)) {
                        returnList.addLast(firstDoc, secondOff);
                        ii++;
                        jj++;
                    } else if (secondOff > firstOff) // Only one separated is
                        // captured above
                    {
                        jj++;
                    } else
                        ii++;
                }
                i++;
                j++;
            } else if (secondDoc > firstDoc) {
                j++;
            } else if (secondDoc < firstDoc) {
                i++;
            }
        }
        return returnList;
    }
    public static PostingsList moveOffsets(PostingsList old) {
        PostingsList returnList = new PostingsList();
        int i = 0;
        for (PostingsEntry pe : old.list) {
            for (int offset : pe.offsets) {
                returnList.add(pe.docID, offset + 1);
            }
        }
        return returnList;
    }

    /*
     * @SuppressWarnings("unchecked") public PostingsList clone() { return new
     * PostingsList((LinkedList<PostingsEntry>) list.clone()); }
     */
    @Override
    public int compareTo(PostingsList o) {
        return (int) Math.signum(size() - o.size());
    }

    @Override
    public String toString() {
        String s = "[";
        for (PostingsEntry e : list) {
            s += e.docID + " ";
        }
        return s + "]";
        // return list.toString();
    }
    public void merge(PostingsList otherList) {
        merge(otherList, 0);
    }
    public void merge(PostingsList otherList, double addedScoreToOtherList) {
        int i = 0;
        int j = 0;
        while (i < size() && j < otherList.size()) {
            if (otherList.get(j).docID < get(i).docID) {
                otherList.get(j).score += addedScoreToOtherList;
                list.add(i, otherList.get(j));
                i++;
                j++;

            } else if (otherList.get(j).docID > get(i).docID) {
                i++;
            } else { //If the same document, merge scores.
                otherList.get(j).score += addedScoreToOtherList;
                get(i).score += otherList.get(j).score;
                i++;
                j++;
            }
        }
        while (j < otherList.size()) {
            list.add(i, otherList.get(j));
            j++;
            i++;
        }
    }
}
