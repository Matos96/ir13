.PHONY: build, run
default: build


build:
	javac -Xlint:none -cp .:pdfbox:megamap -d . ir/*.java

run: build
	java -Xmx1024m -cp .:pdfbox:megamap ir.SearchGUI -d 1000 -m

runOLD: build
	java -Xmx1024m -cp .:pdfbox:megamap ir.SearchGUI -i index_301748499 -m

buildPagerank:
	javac pagerank/PageRank.java

runPagerank:
	java pagerank.PageRank svwiki_links/links.txt