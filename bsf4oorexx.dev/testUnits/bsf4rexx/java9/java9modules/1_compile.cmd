@rem rgf, 2018-01-14, needs Java 9 compiler
@rem module-info.java is only compiled when using "-d" (or "-module-path")
rd out /s /q  && md out

@echo compiling module mod_A
dir      src\mod_A\*java /s /b > mod_A_source_files.txt
type mod_A_source_files.txt
javac -d out\mod_A @mod_A_source_files.txt

@echo compiling module mod_B
dir      src\mod_B\*java /s /b > mod_B_source_files.txt
type mod_B_source_files.txt
javac --module-path out -d out\mod_B @mod_B_source_files.txt

@echo compiling module mod_C
dir      src\mod_C\*java /s /b > mod_C_source_files.txt
type mod_C_source_files.txt
javac --module-path out -d out\mod_C @mod_C_source_files.txt




