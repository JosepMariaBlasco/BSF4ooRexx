Nuthsell examples to drive OpenOffice.org by ooRexx in a Platform independent manner
------------------------------------------------------------------------------------

This directory contains the nutshell examples by Martin Burger, who documented them
in his work <http://wi.wu-wien.ac.at/rgf/diplomarbeiten/index.html#bakk_200601>.

Due to the prolog code of these examples, one can run them not only from the
command line, but also as macros from within OpenOffice.org, if one imports
these scripts as macros.

All scripts run on Linux and Windows (and other operating system platforms) unchanged.

To run all scripts invoke 'runAllExamples.cmd' (Windows) or 'runAllExamples.sh' (Unix).

The graphics ooRexx.png, BSF4ooRexx.png and the ooRexx4OOo.png were designed and
made available for free by Graham ("Gray") Wilson of South Africa (kudos to him!).

Rony G. Flatscher, WU, 2010-07-20

2006-05-29, Wirtschaftsuniversitaet Wien, Vienna, Austria
2009-10-18, renamed the scripts to "*.rxjo"
2009-10-20, renamed the scripts to "*.rxo"
2010-07 through 2010-07-14
            - tidied up the code a little bit/a *lot*,
            - extended functionalities to demonstrate additional (usually needed) features,
              which was at times extremely time consuming
            - changed it to allow running the macros/scripts stand-alone as well
            - supplied file names to be used, adapted code to find those files correctly
              independent of type of incocation (stand alone or via dispatch)
            - supplied hash-bang for Unix systems
            - Reformulated parcel-descriptor.xml, supplying meaningful description

