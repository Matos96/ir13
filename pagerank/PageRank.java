package pagerank;

/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2012
 */

import java.util.*;
import java.io.*;

public class PageRank
{

    /**
     * Maximal number of documents. We're assuming here that we don't have more
     * docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    final static int NUMBER_OUT_PRINTS = 50;
    /**
     * Mapping from document names to document numbers.
     */
    Hashtable<String, Integer> docNumber = new Hashtable<String, Integer>();

    /**
     * Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     * A memory-efficient representation of the transition matrix. The outlinks
     * are represented as a Hashtable, whose keys are the numbers of the
     * documents linked from.
     * <p>
     *
     * The value corresponding to key i is a Hashtable whose keys are all the
     * numbers of documents j that i links to.
     * <p>
     *
     * If there are no outlinks from i, then the value corresponding key i is
     * null.
     */
    Hashtable<Integer, Hashtable<Integer, Boolean>> link = new Hashtable<Integer, Hashtable<Integer, Boolean>>();

    /**
     * The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     * The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     * The probability that the surfer will be bored, stop following links, and
     * take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     * Convergence criterion: Transition probabilities do not change more that
     * EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     * Never do more than this number of iterations regardless of whether the
     * transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    /* --------------------------------------------- */

    public PageRank(String filename)
    {
        int noOfDocs = readDocs(filename);
        computePagerank(noOfDocs);
    }

    /* --------------------------------------------- */

    /**
     * Reads the documents and creates the docs table. When this method finishes
     * executing then the @code{out} vector of outlinks is initialised for each
     * doc, and the @code{p} matrix is filled with zeroes (that indicate direct
     * links) and NO_LINK (if there is no direct link.
     * <p>
     *
     * @return the number of documents read.
     */
    int readDocs(String filename)
    {
        int fileIndex = 0;
        try
        {
            System.err.print("Reading file... ");
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null
                    && fileIndex < MAX_NUMBER_OF_DOCS)
            {
                int index = line.indexOf(";");
                String title = line.substring(0, index);
                Integer fromdoc = docNumber.get(title);
                // Have we seen this document before?
                if (fromdoc == null)
                {
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put(title, fromdoc);
                    docName[fromdoc] = title;
                }
                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer(
                    line.substring(index + 1), ",");
                while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS)
                {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get(otherTitle);
                    if (otherDoc == null)
                    {
                        // This is a previousy unseen doc, so add it to the
                        // table.
                        otherDoc = fileIndex++;
                        docNumber.put(otherTitle, otherDoc);
                        docName[otherDoc] = otherTitle;
                    }
                    // Set the probability to 0 for now, to indicate that there
                    // is
                    // a link from fromdoc to otherDoc.
                    if (link.get(fromdoc) == null)
                    {
                        link.put(fromdoc, new Hashtable<Integer, Boolean>());
                    }
                    if (link.get(fromdoc).get(otherDoc) == null)
                    {
                        link.get(fromdoc).put(otherDoc, true);
                        out[fromdoc]++;
                    }
                }
            }
            if (fileIndex >= MAX_NUMBER_OF_DOCS)
            {
                System.err
                .print("stopped reading since documents table is full. ");
            }
            else
            {
                System.err.print("done. ");
            }
            // Compute the number of sinks.
            for (int i = 0; i < fileIndex; i++)
            {
                if (out[i] == 0)
                    numberOfSinks++;
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("File " + filename + " not found!");
        }
        catch (IOException e)
        {
            System.err.println("Error reading file " + filename);
        }
        System.err.println("Read " + fileIndex + " number of documents");
        return fileIndex;
    }

    /* --------------------------------------------- */

    /*
     * Computes the pagerank of each document.
     */
    void computePagerank(int numberOfDocs)
    {

        //TODO: Compute P and J
        double J = 1 / (double) numberOfDocs;

        double[][] G = new double[numberOfDocs][numberOfDocs];
        for (int i = 0; i < numberOfDocs; i++)
        {
            int number_of_links = out[i];
            if (link.get(i) != null)
            {
                for (Integer j : link.get(i).keySet())
                {
                    if (link.get(i).get(j))
                    {
                        // System.out.println("Här är ett tal :) " + 1 / number_of_links);
                        G[i][j] = (1 / (double) number_of_links) * (1 - BORED) + BORED * J;
                    }
                }
            }
            if (number_of_links == 0)
            {
                for (int d = 0; d < numberOfDocs; d++ )
                {
                    if (d == i)
                        continue;
                    else
                        G[i][d] = (1 / (double) (numberOfDocs - 1)) * (1 - BORED) + BORED * J;
                    // G[i][d] = (1 / (double) (numberOfDocs - 1));
                }
            }

        }
        //           //Debugger
        // for (double[] i : G)
        // {
        //     System.out.print("[");
        //     for (double d : i)
        //     {
        //         System.out.print(" " + d);
        //     }
        //     System.out.println("]");
        // }
        // System.out.println(J);
        //

        double[] x = new double[numberOfDocs];
        double[] xprime = new double[numberOfDocs];
        x[0] = 1;

        //Approx pagerank
        for (int loop = 0; loop < 1000; loop++)
        {
            for (int i = 0; i < numberOfDocs; i++)
            {
                if (link.get(i) != null)
                {
                    for (Integer j : link.get(i).keySet())
                    {
                        xprime[j] += x[i] * (1 - BORED) / out[i];
                    }
                }
                xprime[i] += (BORED) / (double) numberOfDocs;
                xprime[i] += (double) numberOfSinks / (double)(numberOfDocs * numberOfDocs);
            }
            for (int i = 0; i < numberOfDocs; i++)
            {
                x[i] = xprime[i];
                xprime[i] = 0;
            }

        }
        ArrayList<IntDoub> m = new ArrayList<IntDoub>();
        for (int i = 0; i < numberOfDocs; i++)
        {
            m.add(new IntDoub(i, x[i]));
        }
        Collections.sort(m);
        for (int i = 0 ; i < NUMBER_OUT_PRINTS; i++)
        {
            System.out.println((1+i) + ". \t" + docName[m.get(i).key] + ". \t" + m.get(i).value);
        }
    }

    private class IntDoub implements Comparable
    {
        public int key;
        public double value;

        public IntDoub(int key, double value)
        {
            this.key = key;
            this.value = value;
        }
        public int compareTo(Object o)
        {
            if (o instanceof IntDoub)
            {
                if (value > ((IntDoub) o).value)
                    return -1;
                else if (value == ((IntDoub) o).value)
                    return 0;
                else
                    return 1;

            }
            else
                return 1;
        }
        public String toString()
        {
            return " " + key + ". " + value;
        }

    }

    /* --------------------------------------------- */

    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Please give the name of the link file");
        }
        else
        {
            new PageRank(args[0]);
        }
    }
}
