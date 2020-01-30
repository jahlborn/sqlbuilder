SqlBuilder

SqlBuilder is a library which attempts to take the pain out of generating SQL
queries within Java programs. Using one programming language (Java) to
generate code for another language (i.e. SQL) is always a challenge. There are
always issues with escaping characters within string literals, getting spaces
in the right place, and getting the parentheses to match up. And often, even
after the code is debugged and fully tested, it is still very fragile. The
slightest change will throw things out of balance and require another round of
testing and tweaking. 

SqlBuilder changes that whole scenario by wrapping the SQL syntax within very
lightweight and easy to use Java objects which follow the "builder" paradigm
(similar to StringBuilder). This changes many common SQL syntactical, runtime
errors into Java compile-time errors!


Please note that the GitHub repository is a mirror of the main project
repository which is hosted on SourceForge:

Homepage: https://openhms.sourceforge.io/sqlbuilder/

Project: https://sourceforge.net/projects/openhms/

