package cv.lecturesight.util.conf;

public interface Configuration {

  String get(String key);

  boolean getBoolean(String key);

  byte    getByte(String key);

  short   getShort(String key);

  int     getInt(String key);

  long    getLong(String key);

  float   getFloat(String key);

  double  getDouble(String key);

  String[] getList(String key);
}
