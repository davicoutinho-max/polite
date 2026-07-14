package dev.civicpulse.analytics.adapter.out.persistence;

import java.sql.Date;

/** Interface projection for the native daily-engagement-trend query (Spring Data maps native
 * query column aliases to getter names case-insensitively). */
public interface DailyCountProjection {

  Date getDay();

  Long getLikes();

  Long getComments();
}
