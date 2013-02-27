/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012
 *   Finalized by: Christian Wemstad, 2013
 */

package ir;

import java.util.*;

/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

	/** The index as a hashtable. */
	private HashMap<String, PostingsList> index = new HashMap<String, PostingsList>();

	/**
	 * Inserts this token in the index.
	 */
	public void insert(String token, int docID, int offset) {
		PostingsList list = index.get(token);
		if (list == null) {
			list = new PostingsList();
			index.put(token, list);
		}
		list.add(docID, offset);
		HashSet<String> docSet = docTerms.get("" + docID);
		if(docSet == null)
			docSet = new HashSet<String>();
		docSet.add(token);
	}

	/**
	 * Returns the postings for a specific term, or null if the term is not in
	 * the index.
	 */
	public PostingsList getPostings(String token) {
		PostingsList list = index.get(token);
		if (list == null || list.size() == 0)
			return null;
		return list;
	}

	/**
	 * Searches the index for postings matching the query.
	 */
	public PostingsList search(Query query, int queryType, int rankingType) {
		if (queryType == Index.INTERSECTION_QUERY) {
			ArrayList<PostingsList> lists = new ArrayList<PostingsList>();
			for (int i = 0; i < query.terms.size(); i++) {
				PostingsList pl = getPostings(query.terms.get(i));
				if(pl == null)
					return new PostingsList();
				lists.add(pl);
			}

			Collections.sort(lists);

			PostingsList all = lists.get(0);
			lists.remove(0);
			for(PostingsList pl : lists) {
				all = PostingsList.removeAllNotIn(all, pl);
			}
			return all;
		}
		else if (queryType == Index.PHRASE_QUERY) {
			PostingsList all = getPostings(query.terms.getFirst());
			if(all == null)
				return null;
			for (int i = 1; i < query.terms.size(); i++) {
				PostingsList currentList = getPostings(query.terms.get(i));
				if(currentList == null)
					return null;
				all = PostingsList.removeAllNotFollowedBy(all, currentList);
			}
			return all;
		}
		return
			null;
	}

	/**
	 * No need for cleanup in a HashedIndex.
	 */
	public void cleanup() {
	}
}
