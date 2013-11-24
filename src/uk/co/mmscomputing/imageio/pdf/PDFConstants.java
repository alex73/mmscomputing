package uk.co.mmscomputing.imageio.pdf;

public interface PDFConstants{

  static public final int T_EOF       = -1;
  static public final int T_WHITE     = 0;


//  static public final int T_LPAREN    = 100;     // (
//  static public final int T_RPAREN    = 101;     // )
  static public final int T_BACKSLASH = 102;     // \
//  static public final int T_SLASH     = 105;     // /
//  static public final int T_PERCENT   = 106;     // %
//  static public final int T_LBRACKET  = 107;     // [
//  static public final int T_RBRACKET  = 108;     // ]
  static public final int T_LBRACE    = 109;     // }
  static public final int T_RBRACE    = 110;     // {

  static public final int T_LSS       = 103;     // <
  static public final int T_GTR       = 104;     // >


  static public final int T_CHAR      = 200;
  static public final int T_INTEGER   = 201;
  static public final int T_REAL      = 202;
  static public final int T_STRING    = 203;

  static public final int T_STRING_START     = 300;     // (
  static public final int T_STRING_END       = 301;     // )
  static public final int T_ARRAY_START      = 302;     // [
  static public final int T_ARRAY_END        = 303;     // ]
  static public final int T_DICTIONARY_START = 304;     // <<
  static public final int T_DICTIONARY_END   = 305;     // >>
  static public final int T_NAME             = 306;
  static public final int T_COMMENT          = 307;     // %

  static public final int T_NULL             = 400;     // null
  static public final int T_FALSE            = 401;     // false
  static public final int T_TRUE             = 402;     // true
  static public final int T_STREAM           = 403;     // stream
  static public final int T_ENDSTREAM        = 404;     // endstream
  static public final int T_OBJ              = 405;     // obj
  static public final int T_ENDOBJ           = 406;     // endobj
  static public final int T_R                = 407;     // R
  static public final int T_XREF             = 408;     // xref
  static public final int T_STARTXREF        = 409;     // startxref
  static public final int T_TRAILER          = 410;     // trailer
  static public final int T_N                = 411;     // n
  static public final int T_F                = 412;     // f
}