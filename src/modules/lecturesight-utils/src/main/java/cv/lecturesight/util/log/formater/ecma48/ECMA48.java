package cv.lecturesight.util.log.formater.ecma48;

public class ECMA48 {

  final static String CMD_PREFIX = "\033[";
  final static String CMD_SUFFIX = "m";

  final static String RESET = CMD_PREFIX + "0" + CMD_SUFFIX;

  final static String BOLD = CMD_PREFIX + "1" + CMD_SUFFIX;
  final static String UNDERLINE = CMD_PREFIX + "4" + CMD_SUFFIX;
  final static String BLINK = CMD_PREFIX + "5" + CMD_SUFFIX;
  final static String REVERSE = CMD_PREFIX + "7" + CMD_SUFFIX;

  final static String RED = CMD_PREFIX + "31" + CMD_SUFFIX;
  final static String GREEN = CMD_PREFIX + "32" + CMD_SUFFIX;
  final static String ORANGE = CMD_PREFIX + "33" + CMD_SUFFIX;
  final static String BLUE = CMD_PREFIX + "34" + CMD_SUFFIX;
  final static String PURPLE = CMD_PREFIX + "35" + CMD_SUFFIX;
  final static String CYAN = CMD_PREFIX + "36" + CMD_SUFFIX;
  final static String GREY = CMD_PREFIX + "37" + CMD_SUFFIX;
  final static String DARKGREY = CMD_PREFIX + "90" + CMD_SUFFIX;
  final static String LIGHTRED = CMD_PREFIX + "91" + CMD_SUFFIX;
  final static String LIGHTGREEN = CMD_PREFIX + "92" + CMD_SUFFIX;
  final static String YELLOW = CMD_PREFIX + "93" + CMD_SUFFIX;
  final static String LIGHTBLUE = CMD_PREFIX + "94" + CMD_SUFFIX;
  final static String LIGHTPURPLE = CMD_PREFIX + "95" + CMD_SUFFIX;
  final static String TURQUISE = CMD_PREFIX + "96" + CMD_SUFFIX;

  final static String BG_BLACK = CMD_PREFIX + "40" + CMD_SUFFIX;
  final static String BG_RED = CMD_PREFIX + "41" + CMD_SUFFIX;
  final static String BG_GREEN = CMD_PREFIX + "42" + CMD_SUFFIX;
  final static String BG_ORANGE = CMD_PREFIX + "43" + CMD_SUFFIX;
  final static String BG_BLUE = CMD_PREFIX + "44" + CMD_SUFFIX;
  final static String BG_PURPLE = CMD_PREFIX + "45" + CMD_SUFFIX;
  final static String BG_CYAN = CMD_PREFIX + "46" + CMD_SUFFIX;
  final static String BG_GREY = CMD_PREFIX + "47" + CMD_SUFFIX;
  final static String BG_DARKGREY = CMD_PREFIX + "100" + CMD_SUFFIX;
  final static String BG_LIGHTRED = CMD_PREFIX + "101" + CMD_SUFFIX;
  final static String BG_LIGHTGREEN = CMD_PREFIX + "102" + CMD_SUFFIX;
  final static String BG_YELLOW = CMD_PREFIX + "103" + CMD_SUFFIX;
  final static String BG_LIGHTBLUE = CMD_PREFIX + "104" + CMD_SUFFIX;
  final static String BG_LIGHTPURPLE = CMD_PREFIX + "105" + CMD_SUFFIX;
  final static String BG_TURQUOISE = CMD_PREFIX + "106" + CMD_SUFFIX;
}
