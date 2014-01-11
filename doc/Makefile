# aptitude install texlive-lang-cyrillic txt

all: anwenderdoku.pdf anwenderdoku.html entwicklerdoku.pdf entwicklerdoku.html

%.html: %.txt bilder/*
	asciidoc -o $@ $*.txt

%.pdf: %.txt bilder/*
	a2x -v -a encoding=utf-8 -a lang=de --dblatex-opts='-Platex.output.revhistory=0 -P latex.encoding=utf8 -P latex.unicode.use=1' $*.txt


