// This is a generated file. Not intended for manual editing.
package org.oorexx.ide.lang;

import org.oorexx.ide.lexer.token.*;

public interface OoRexxTokenElementTypes {

  //

  OoRexxTokenType ENVIRONMENT = new OoRexxEnvironmentSymbolTokenType("ENVIRONMENT");
  OoRexxTokenType FALSE = new OoRexxEnvironmentSymbolTokenType("FALSE");
  OoRexxTokenType LOCAL = new OoRexxEnvironmentSymbolTokenType("LOCAL");
  OoRexxTokenType NIL = new OoRexxEnvironmentSymbolTokenType("NIL");
  OoRexxTokenType TRUE = new OoRexxEnvironmentSymbolTokenType("TRUE");

  // Directive
  OoRexxTokenType ATTRIBUTE = new OoRexxDirectiveTokenType("ATTRIBUTE");
  OoRexxTokenType CLASS = new OoRexxDirectiveTokenType("CLASS");
  OoRexxTokenType CONSTANT = new OoRexxDirectiveTokenType("CONSTANT");
  OoRexxTokenType METHOD = new OoRexxDirectiveTokenType("METHOD");
  OoRexxTokenType OPTIONS = new OoRexxDirectiveTokenType("OPTIONS");
  OoRexxTokenType REQUIRES = new OoRexxDirectiveTokenType("REQUIRES");
  OoRexxTokenType ROUTINE = new OoRexxDirectiveTokenType("ROUTINE");

  // Directive Options

  OoRexxTokenType DIRO_ABSTRACT = new OoRexxKeywordTokenType("ABSTRACT");
  OoRexxTokenType DIRO_ATTRIBUTE = new OoRexxKeywordTokenType("ATTRIBUTE");
  OoRexxTokenType DIRO_CLASS = new OoRexxKeywordTokenType("CLASS");
  OoRexxTokenType DIRO_DIGITS = new OoRexxKeywordTokenType("DIGITS");
  OoRexxTokenType DIRO_EXTERNAL = new OoRexxKeywordTokenType("EXTERNAL");
  OoRexxTokenType DIRO_FORM = new OoRexxKeywordTokenType("FORM");
  OoRexxTokenType DIRO_FUZZ = new OoRexxKeywordTokenType("FUZZ");
  OoRexxTokenType DIRO_GET = new OoRexxKeywordTokenType("GET");
  OoRexxTokenType DIRO_GUARDED = new OoRexxKeywordTokenType("GUARDED");
  OoRexxTokenType DIRO_INHERIT = new OoRexxKeywordTokenType("INHERIT");
  OoRexxTokenType DIRO_LIBRARY = new OoRexxKeywordTokenType("LIBRARY");
  OoRexxTokenType DIRO_METACLASS = new OoRexxKeywordTokenType("METACLASS");
  OoRexxTokenType DIRO_MIXINCLASS = new OoRexxKeywordTokenType("MIXINCLASS");
  OoRexxTokenType DIRO_PRIVATE = new OoRexxKeywordTokenType("PRIVATE");
  OoRexxTokenType DIRO_PROTECTED = new OoRexxKeywordTokenType("PROTECTED");
  OoRexxTokenType DIRO_PUBLIC = new OoRexxKeywordTokenType("PUBLIC");
  OoRexxTokenType DIRO_SET = new OoRexxKeywordTokenType("SET");
  OoRexxTokenType DIRO_SUBCLASS = new OoRexxKeywordTokenType("SUBCLASS");
  OoRexxTokenType DIRO_TRACE = new OoRexxKeywordTokenType("TRACE");
  OoRexxTokenType DIRO_UNGUARDED = new OoRexxKeywordTokenType("UNGUARDED");
  OoRexxTokenType DIRO_UNPROTECTED = new OoRexxKeywordTokenType("UNPROTECTED");

  // Whitespace

  OoRexxTokenType EOL_WHITE_SPACE = new OoRexxTokenType("EOL_WHITE_SPACE");

  // Class Keywords


  OoRexxTokenType CKW_SELF = new OoRexxKeywordTokenType("SELF");


  // Keyword

  OoRexxTokenType KW_ADDRESS = new OoRexxKeywordTokenType("ADDRESS");
  OoRexxTokenType KW_ARG = new OoRexxKeywordTokenType("ARG");
  OoRexxTokenType KW_CALL = new OoRexxKeywordTokenType("CALL");
  OoRexxTokenType KW_DO = new OoRexxKeywordTokenType("DO");
  OoRexxTokenType KW_DROP = new OoRexxKeywordTokenType("DROP");
  OoRexxTokenType KW_EXIT = new OoRexxKeywordTokenType("EXIT");
  OoRexxTokenType KW_EXPOSE = new OoRexxKeywordTokenType("EXPOSE");
  OoRexxTokenType KW_FORWARD = new OoRexxKeywordTokenType("FORWARD");
  OoRexxTokenType KW_GUARD = new OoRexxKeywordTokenType("GUARD");
  OoRexxTokenType KW_IF = new OoRexxKeywordTokenType("IF");
  OoRexxTokenType KW_INTERPRET = new OoRexxKeywordTokenType("INTERPRET");
  OoRexxTokenType KW_ITERATE = new OoRexxKeywordTokenType("ITERATE");
  OoRexxTokenType KW_LEAVE = new OoRexxKeywordTokenType("LEAVE");
  OoRexxTokenType KW_LOOP = new OoRexxKeywordTokenType("LOOP");
  OoRexxTokenType KW_NOP = new OoRexxKeywordTokenType("NOP");
  OoRexxTokenType KW_NUMERIC = new OoRexxKeywordTokenType("NUMERIC");
  OoRexxTokenType KW_OPTIONS = new OoRexxKeywordTokenType("OPTIONS");
  OoRexxTokenType KW_PARSE = new OoRexxKeywordTokenType("PARSE");
  OoRexxTokenType KW_PROCEDURE = new OoRexxKeywordTokenType("PROCEDURE");
  OoRexxTokenType KW_PULL = new OoRexxKeywordTokenType("PULL");
  OoRexxTokenType KW_PUSH = new OoRexxKeywordTokenType("PUSH");
  OoRexxTokenType KW_QUEUE = new OoRexxKeywordTokenType("QUEUE");
  OoRexxTokenType KW_RAISE = new OoRexxKeywordTokenType("RAISE");
  OoRexxTokenType KW_REPLY = new OoRexxKeywordTokenType("REPLY");
  OoRexxTokenType KW_RETURN = new OoRexxKeywordTokenType("RETURN");
  OoRexxTokenType KW_SAY = new OoRexxKeywordTokenType("SAY");
  OoRexxTokenType KW_SELECT = new OoRexxKeywordTokenType("SELECT");
  OoRexxTokenType KW_SIGNAL = new OoRexxKeywordTokenType("SIGNAL");
  OoRexxTokenType KW_TRACE = new OoRexxKeywordTokenType("TRACE");
  OoRexxTokenType KW_USE = new OoRexxKeywordTokenType("USE");

  // Keyword Options
  OoRexxTokenType KWO_ADDITIONAL = new OoRexxKeywordTokenType("ADDITIONAL");
  OoRexxTokenType KWO_ANY = new OoRexxKeywordTokenType("ANY");
  OoRexxTokenType KWO_ARG = new OoRexxKeywordTokenType("ARG");
  OoRexxTokenType KWO_ARGUMENTS = new OoRexxKeywordTokenType("ARGUMENTS");
  OoRexxTokenType KWO_ARRAY = new OoRexxKeywordTokenType("ARRAY");
  OoRexxTokenType KWO_BY = new OoRexxKeywordTokenType("BY");
  OoRexxTokenType KWO_CASELESS = new OoRexxKeywordTokenType("CASELESS");
  OoRexxTokenType KWO_CLASS = new OoRexxKeywordTokenType("CLASS");
  OoRexxTokenType KWO_COMMANDS = new OoRexxKeywordTokenType("COMMANDS");
  OoRexxTokenType KWO_CONTINUE = new OoRexxKeywordTokenType("CONTINUE");
  OoRexxTokenType KWO_DESCRIPTION = new OoRexxKeywordTokenType("DESCRIPTION");
  OoRexxTokenType KWO_DIGIT = new OoRexxKeywordTokenType("DIGIT");
  OoRexxTokenType KWO_DROP = new OoRexxKeywordTokenType("DROP");
  OoRexxTokenType KWO_ELSE = new OoRexxKeywordTokenType("ELSE");
  OoRexxTokenType KWO_END = new OoRexxKeywordTokenType("END");
  OoRexxTokenType KWO_ENGINEERING = new OoRexxKeywordTokenType("ENGINEERING");
  OoRexxTokenType KWO_ERROR = new OoRexxKeywordTokenType("ERROR");
  OoRexxTokenType KWO_EXIT = new OoRexxKeywordTokenType("EXIT");
  OoRexxTokenType KWO_EXPOSE = new OoRexxKeywordTokenType("EXPOSE");
  OoRexxTokenType KWO_FAILURE = new OoRexxKeywordTokenType("FAILURE");
  OoRexxTokenType KWO_FOR = new OoRexxKeywordTokenType("FOR");
  OoRexxTokenType KWO_FOREVER = new OoRexxKeywordTokenType("FOREVER");
  OoRexxTokenType KWO_FORM = new OoRexxKeywordTokenType("FORM");
  OoRexxTokenType KWO_FUZZ = new OoRexxKeywordTokenType("FUZZ");
  OoRexxTokenType KWO_HALT = new OoRexxKeywordTokenType("HALT");
  OoRexxTokenType KWO_INTERMEDIATES = new OoRexxKeywordTokenType("INTERMEDIATES");
  OoRexxTokenType KWO_LABEL = new OoRexxKeywordTokenType("LABEL");
  OoRexxTokenType KWO_LABELS = new OoRexxKeywordTokenType("LABELS");
  OoRexxTokenType KWO_LINEIN = new OoRexxKeywordTokenType("LINEIN");
  OoRexxTokenType KWO_LINEOUT = new OoRexxKeywordTokenType("LINEOUT");
  OoRexxTokenType KWO_LOSTDIGITS = new OoRexxKeywordTokenType("LOSTDIGITS");
  OoRexxTokenType KWO_LOWER = new OoRexxKeywordTokenType("LOWER");
  OoRexxTokenType KWO_MESSAGE = new OoRexxKeywordTokenType("MESSAGE");
  OoRexxTokenType KWO_NAME = new OoRexxKeywordTokenType("NAME");
  OoRexxTokenType KWO_NOMETHOD = new OoRexxKeywordTokenType("NOMETHOD");
  OoRexxTokenType KWO_NORMAL = new OoRexxKeywordTokenType("NORMAL");
  OoRexxTokenType KWO_NOSTRING = new OoRexxKeywordTokenType("NOSTRING");
  OoRexxTokenType KWO_NOTREADY = new OoRexxKeywordTokenType("NOTREADY");
  OoRexxTokenType KWO_NOVALUE = new OoRexxKeywordTokenType("NOVALUE");
  OoRexxTokenType KWO_OFF = new OoRexxKeywordTokenType("OFF");
  OoRexxTokenType KWO_ON = new OoRexxKeywordTokenType("ON");
  OoRexxTokenType KWO_OTHERWISE = new OoRexxKeywordTokenType("OTHERWISE");
  OoRexxTokenType KWO_OVER = new OoRexxKeywordTokenType("OVER");
  OoRexxTokenType KWO_PROPAGATE = new OoRexxKeywordTokenType("PROPAGATE");
  OoRexxTokenType KWO_PULL = new OoRexxKeywordTokenType("PULL");
  OoRexxTokenType KWO_RESULTS = new OoRexxKeywordTokenType("RESULTS");
  OoRexxTokenType KWO_RETURN = new OoRexxKeywordTokenType("RETURN");
  OoRexxTokenType KWO_SCIENTIFIC = new OoRexxKeywordTokenType("SCIENTIFIC");
  OoRexxTokenType KWO_SOURCE = new OoRexxKeywordTokenType("SOURCE");
  OoRexxTokenType KWO_STRICT = new OoRexxKeywordTokenType("STRICT");
  OoRexxTokenType KWO_SYNTAX = new OoRexxKeywordTokenType("SYNTAX");
  OoRexxTokenType KWO_THEN = new OoRexxKeywordTokenType("THEN");
  OoRexxTokenType KWO_TO = new OoRexxKeywordTokenType("TO");
  OoRexxTokenType KWO_UNTIL = new OoRexxKeywordTokenType("UNTIL");
  OoRexxTokenType KWO_UPPER = new OoRexxKeywordTokenType("UPPER");
  OoRexxTokenType KWO_USER = new OoRexxKeywordTokenType("USER");
  OoRexxTokenType KWO_VALUE = new OoRexxKeywordTokenType("NUMBER");
  OoRexxTokenType KWO_VAR = new OoRexxKeywordTokenType("VAR");
  OoRexxTokenType KWO_VERSION = new OoRexxKeywordTokenType("VERSION");
  OoRexxTokenType KWO_WHEN = new OoRexxKeywordTokenType("WHEN");
  OoRexxTokenType KWO_WHILE = new OoRexxKeywordTokenType("WHILE");
  OoRexxTokenType KWO_WITH = new OoRexxKeywordTokenType("WITH");



  // Identifiers

  OoRexxTokenType IDENTIFIER = new OoRexxTokenType("<IDENTIFIER>");

  // Literals

  OoRexxTokenType INTEGER_LITERAL = new OoRexxLiteralTokenType("<INTEGER>");
  OoRexxTokenType FLOAT_LITERAL = new OoRexxLiteralTokenType("<FLOAT>");
  OoRexxTokenType STRING_LITERAL = new OoRexxLiteralTokenType("<STRING>");
  OoRexxTokenType BYTE_STRING_LITERAL = new OoRexxLiteralTokenType("<BYTE_STRING>");
  OoRexxTokenType HEX_STRING_LITERAL = new OoRexxLiteralTokenType("<HEX_STRING>");
  OoRexxTokenType EMPTY_STRING_LITERAL = new OoRexxLiteralTokenType("<EMPTY_STRING>");


  // Comments

  OoRexxTokenType BLOCK_COMMENT = new OoRexxCommentTokenType("<BLOCK_COMMENT>");
  OoRexxTokenType EOL_COMMENT = new OoRexxCommentTokenType("<EOL_COMMENT>");

  OoRexxTokenType INNER_BLOCK_DOC_COMMENT = new OoRexxCommentTokenType("<INNER_BLOCK_DOC_COMMENT>");
  OoRexxTokenType OUTER_BLOCK_DOC_COMMENT = new OoRexxCommentTokenType("<OUTER_BLOCK_DOC_COMMENT>");

  OoRexxTokenType SHEBANG_LINE = new OoRexxTokenType("<SHEBANG_LINE>");

  // Operators

  OoRexxTokenType TILDETILDE = new OoRexxOperatorTokenType("~~");
  OoRexxTokenType TILDE = new OoRexxOperatorTokenType("~");

  OoRexxTokenType PLUS = new OoRexxOperatorTokenType("+");
  OoRexxTokenType MINUS = new OoRexxOperatorTokenType("-");
  OoRexxTokenType NEGATION = new OoRexxOperatorTokenType("\\");

  OoRexxTokenType POWER = new OoRexxOperatorTokenType("**");

  OoRexxTokenType MUL = new OoRexxOperatorTokenType("*");
  OoRexxTokenType DIV = new OoRexxOperatorTokenType("/");
  OoRexxTokenType INTDIV = new OoRexxOperatorTokenType("%");

  OoRexxTokenType STRINGCONCAT = new OoRexxOperatorTokenType("||");

  OoRexxTokenType EQ = new OoRexxOperatorTokenType("=");


  OoRexxTokenType EQEQ = new OoRexxOperatorTokenType("==");
  OoRexxTokenType STIRCTLT = new OoRexxOperatorTokenType("<<");
  OoRexxTokenType STIRCTGT = new OoRexxOperatorTokenType(">>");

  OoRexxTokenType NOTEQ = new OoRexxOperatorTokenType("\\=");
  OoRexxTokenType STRICTNOTGT = new OoRexxOperatorTokenType("\\>>");
  OoRexxTokenType STRICTNOTLT = new OoRexxOperatorTokenType("\\<<");

  OoRexxTokenType STRICTNOTEQ = new OoRexxOperatorTokenType("\\==");

  OoRexxTokenType GTEQ = new OoRexxOperatorTokenType(">=");
  OoRexxTokenType STRICTGTEQ = new OoRexxOperatorTokenType(">>=");
  OoRexxTokenType LTEQ = new OoRexxOperatorTokenType("<=");
  OoRexxTokenType STRICTLTEQ = new OoRexxOperatorTokenType("<<=");

  OoRexxTokenType AND = new OoRexxOperatorTokenType("&");
  OoRexxTokenType XOR = new OoRexxOperatorTokenType("&&");
  OoRexxTokenType OR = new OoRexxOperatorTokenType("|");

  OoRexxTokenType COLON = new OoRexxOperatorTokenType(":");
  OoRexxTokenType COLONCOLON = new OoRexxOperatorTokenType("::");
  OoRexxTokenType COMMA = new OoRexxOperatorTokenType(",");


  OoRexxTokenType DOT = new OoRexxOperatorTokenType(".");
  OoRexxTokenType GT = new OoRexxOperatorTokenType(">");
  OoRexxTokenType LT = new OoRexxOperatorTokenType("<");

  OoRexxTokenType LPAREN = new OoRexxOperatorTokenType("(");
  OoRexxTokenType RPAREN = new OoRexxOperatorTokenType(")");
  OoRexxTokenType LBRACK = new OoRexxOperatorTokenType("[");
  OoRexxTokenType RBRACK = new OoRexxOperatorTokenType("]");
  OoRexxTokenType SEMICOLON = new OoRexxOperatorTokenType(";");

}

