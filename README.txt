The BSF4ooRexx project started its life in 2000/01 as a proof of concept. In 2001 the project "BSF4Rexx" was created, which indicates that the package was meant and available for all Rexx interpreters, from IBM's Object REXX to Mark Hessling's Regina.

In the fall of 2009 a version was developed that exploits the new kernel APIs introduced with ooRexx 4.0. As a result the kernel got rewritten as well, and the project got renamed to "BSF4ooRexx".

As the author I have been constantly under time pressure (too much work, too much ideas, ...), such that I never really cleaned the source code from outdated comments (this is especially true for bsf4oorexx.dev/source_cc/BSF4ooRexx.cc). Also the build scripts reflect the personal environment (directory structures changed over time, yet the main system to build was Windows), hence the Windows batch files.

Here are some ideas about improving the package as it is:

- create Unix-shell-script counterparts of the Windows batch files, such that creating the packages can be done there as well,

- better, yet, create an Apache Maven project, cf. <http://maven.apache.org/>:

  - allows compiling, testing and packaging the jar-files easily (baseline for BSF4ooRexx is Java 1.4, i.e. anyone having Java 1.4 deployed or higher can take advantage of BSF4ooRexx, can be easily defined in the POM)

  - allows compiling also the BSF4ooRexx.cc-file (there are Maven plugins that allow for that in a platform independent manner, replacing Makefiles, if I understood that correctly!)

- eventually remove outdated comments from all programs

---

Ad possible new features:

- whenever ooRexx-RFEs linked to BSF4ooRexx get realized by the ooRexx team, apply them in the BSF4ooRexx package (especially the feature where one could learn that the Rexx interpreter is in shutdown mode, such that no callbacks from Java to Rexx are passed on).

- whenever ooRexx gets debug and profile APIs, make them available, such that Java based SDKs for ooRexx can be created,

- maybe add JSR-223 support (can be implemented using a generic BSF 2.4 wrapper), if users would need it.

Pleae feel free to add to this document.

2011-04-05, Rony G. Flatscher


For debugging JavaFX (rgf, 20230930):

    set BSF4Rexx_JavaStartupOptions=-Djavafx.verbose=true

