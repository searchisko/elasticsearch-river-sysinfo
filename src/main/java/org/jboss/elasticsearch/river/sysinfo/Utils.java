/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.elasticsearch.river.sysinfo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.ElasticSearchParseException;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.support.XContentMapValues;

/**
 * Utility functions.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class Utils {

  /**
   * Trim String value, return null if empty after trim.
   * 
   * @param src value
   * @return trimmed value or null
   */
  public static String trimToNull(String src) {
    if (src == null || src.length() == 0) {
      return null;
    }
    src = src.trim();
    if (src.length() == 0) {
      return null;
    }
    return src;
  }

  /**
   * Check if String value is null or empty.
   * 
   * @param src value
   * @return <code>true</code> if value is null or empty
   */
  public static boolean isEmpty(String src) {
    return (src == null || src.length() == 0 || src.trim().length() == 0);
  }

  /**
   * Check if passed in value is null, or empty if it's String.
   * 
   * @param src value
   * @return <code>true</code> if value is null or empty String
   */
  public static boolean isEmpty(Object src) {
    return (src == null || (src instanceof String && isEmpty((String) src)));
  }

  /**
   * Parse time value from river settings/config map. Value must be number, which is normally in milliseconds, but you
   * can postfix it by one of next letters to set units
   * <ul>
   * <li><code>s</code> - seconds
   * <li><code>m</code> - minutes
   * <li><code>h</code> - hours
   * <li><code>d</code> - days
   * <li><code>w</code> - weeks
   * </ul>
   * 
   * @param settings map to get value from
   * @param key of config value in map
   * @param defaultDuration default duration used if no value in config
   * @param defaultTimeUnit time unit for default duration - if null no default is used, so return 0 as default in this
   *          case
   * @return time value in millis
   */
  protected static long parseTimeValue(Map<String, Object> settings, String key, long defaultDuration,
      TimeUnit defaultTimeUnit) {
    long ret = 0;
    if (settings == null || !settings.containsKey(key)) {
      if (defaultTimeUnit != null) {
        ret = new TimeValue(defaultDuration, defaultTimeUnit).millis();
      }
    } else {
      try {
        ret = TimeValue.parseTimeValue(XContentMapValues.nodeStringValue(settings.get(key), null),
            new TimeValue(defaultDuration, defaultTimeUnit)).millis();
      } catch (ElasticSearchParseException e) {
        throw new ElasticSearchParseException(e.getMessage() + " for setting: " + key);
      }
    }
    return ret;
  }

  /**
   * Get node value as {@link Integer} object instance if possible.
   * 
   * @param node to get value from
   * @return Integer value or null.
   * @throws NumberFormatException if value can't be converted to the int value
   * @see XContentMapValues#nodeIntegerValue(Object, int)
   */
  public static Integer nodeIntegerValue(Object node) throws NumberFormatException {
    if (node == null) {
      return null;
    }
    if (node instanceof Integer) {
      return (Integer) node;
    } else if (node instanceof Number) {
      return new Integer(((Number) node).intValue());
    }

    return Integer.parseInt(node.toString());
  }

  /**
   * Read JSON file from classpath into Map of Map structure.
   * 
   * @param filePath path inside jar/classpath pointing to JSON file to read
   * @return parsed JSON file
   * @throws SettingsException
   */
  public static Map<String, Object> loadJSONFromJarPackagedFile(String filePath) throws SettingsException {
    XContentParser parser = null;
    try {
      parser = XContentFactory.xContent(XContentType.JSON).createParser(Utils.class.getResourceAsStream(filePath));
      return parser.mapAndClose();
    } catch (IOException e) {
      throw new SettingsException(e.getMessage(), e);
    } finally {
      if (parser != null)
        parser.close();
    }
  }

}
