package com.github.dkharrat.nexusdata.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;

public class DateUtil {
    public final static String DEFAULT              = "MM/dd/yyyy hh:mm:ss a Z";
    public final static String ISO8601              = "yyyy-MM-dd'T'HH:mm:ssZ";
    public final static String RFC822               = "EEE, dd MMM yyyy HH:mm:ss Z";
    public final static String UTC                  = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public final static String SIMPLE               = "MM/dd/yyyy hh:mm:ss a";
    public final static String ISO8601_NO_TIMEZONE  = "yyyy-MM-dd'T'HH:mm:ss";
    public final static String DATE_TO_STRING       = "EEE MMM dd HH:mm:ss zzz yyyy";

    private static float SECONDS_PER_MINUTE = 60.0f;
    private static float SECONDS_PER_HOUR   = 3600.0f;
    private static float SECONDS_PER_DAY    = 86400.0f;
    private static float SECONDS_PER_MONTH  = 2592000.0f;
    private static float SECONDS_PER_YEAR   = 31536000.0f;

    @SuppressLint("SimpleDateFormat")
    public static String format(String format, Date date, TimeZone timeZone) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(timeZone);
        return df.format(date);
    }

    public static String format(String format, Date date) {
        return format(format, date, TimeZone.getTimeZone("UTC"));
    }

    public static String formatISO8601(Date date) {
        return format(ISO8601, date);
    }

    public static String formatRFC822(Date date) {
        return format(RFC822, date);
    }

    public static String formatUTC(Date date) {
        return format(UTC, date);
    }

    public static Date parse(String format, String date, TimeZone timeZone) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);
        df.setTimeZone(timeZone);
        return df.parse(date);
    }

    public static Date parse(String format, String date) throws ParseException {
        return parse(format, date, TimeZone.getTimeZone("UTC"));
    }

    public static Date parseISO8601(String date) throws ParseException {
        return parse(ISO8601, date);
    }

    public static Date parseRFC822(String date) throws ParseException {
        return parse(RFC822, date);
    }

    public static Date parseUTC(String date) throws ParseException {
        return parse(UTC, date);
    }

    /**
     * Tries various date formats to parse the date until one succeeds.
     * The following date formats will be tried in order:
     *
     * <ol>
     *   <li> UTC </li>
     *   <li> RFC822 </li>
     *   <li> ISO8601 </li>
     *   <li> "MM/dd/yyyy hh:mm:ss a" </li>
     *   <li> "MM/dd/yyyy" </li>
     * </ol>
     *
     * An ParseException exception is thrown if unable to parse the string
     *
     * @param date  A string representing the date to parse
     * @return  Date representing the parsed date string
     * @throws ParseException
     */
    public static Date smartParse(String date) throws ParseException {
        // replace 'Z' with '+0000' to confirm to ISO8601 standard, as a workaround to limitations with SimpleDateFormat
        // not parsing date correctly
        String dateStr = date.replace("Z", "+0000");

        // parse different formats in turn, until one succeeds
        String dateFormats[] = {DateUtil.ISO8601, DateUtil.RFC822, DateUtil.UTC, DateUtil.SIMPLE, DateUtil.DATE_TO_STRING, "MM/dd/yyyy"};
        for (String format : dateFormats) {
            try {
                return DateUtil.parse(format, dateStr);
            } catch(ParseException e) {
                // ignore, try the next date format
            }
        }

        throw new ParseException("Unable to parse date " + date, 0);
    }

    public static Date now() {
        return new Date();
    }

    /**
     * Returns the amount of elapsed time from that date to now in full words
     */
    public static String getDistanceOfTimeInWords(Date date)
    {
        return getDistanceOfTimeInWords(date, true);
    }

    /**
     * Returns the amount of elapsed time from that date to now in shortened words
     */
    public static String getDistanceOfTimeInWords(Date date, boolean beConcise)
    {
        String ago      = "ago";
        String FromNow  = "from now";
        String JustNow  = "Just now";
        String LessThan = "Less than";
        String About    = "About";
        String Over     = "Over";
        String Almost   = "Almost";
        String Seconds  = beConcise ? "secs" : "seconds";
        String Minute   = beConcise ? "min" : "minute";
        String Minutes  = beConcise ? "mins" : "minutes";
        String Hour     = beConcise ? "hr" : "hour";
        String Hours    = beConcise ? "hrs" : "hours";
        String Day      = "day";
        String Days     = "days";
        String Month    = "month";
        String Months   = "months";
        String Year     = "year";
        String Years    = "years";

        long since = date.getTime() - (new Date()).getTime();

        String direction = since <= 0 ? ago : FromNow;

        int seconds     = (int) (Math.abs(since) / 1000);
        int minutes     = Math.round(seconds / SECONDS_PER_MINUTE);
        int hours       = Math.round(seconds / SECONDS_PER_HOUR);
        int days        = Math.round(seconds / SECONDS_PER_DAY);
        int months      = Math.round(seconds / SECONDS_PER_MONTH);
        int years       = Math.round(seconds / SECONDS_PER_YEAR);
        int offset      = (int) Math.round(Math.floor(years / 4.0) * 1440.0);
        int remainder   = (minutes - offset) % 525600;

        int number;
        String measure;
        String modifier = "";

        if (minutes <= 1) {
            measure = Seconds;
            if (seconds <= 4) {
                number = 5;
                modifier = beConcise? JustNow : LessThan;
            }
            else if (seconds <= 9) {
                number = 10;
                modifier = beConcise ? JustNow : LessThan;
            }
            else if (seconds <= 19) {
                number = 20;
                modifier = beConcise? JustNow : LessThan;
            }
            else if (seconds <= 39) {
                number = 30;
                modifier = beConcise ? JustNow : About;
            }
            else if (seconds <= 49) {
                number = 1;
                measure = Minute;
                modifier = beConcise ? JustNow : LessThan;
            }
            else if (seconds <= 59) {
                number = 1;
                measure = Minute;
                modifier = beConcise ? JustNow : LessThan;
            }
            else {
                number = 1;
                measure = Minute;
                modifier = beConcise ? JustNow : About;
            }
        }
        else if (minutes <= 44) {
            number = minutes;
            measure = Minutes;
        }
        else if (minutes <= 89) {
            number = 1;
            measure = Hour;
            modifier = beConcise ? modifier : About;;
        }
        else if (minutes <= 1439) {
            number = hours;
            measure = Hours;
            modifier = beConcise ? modifier : About;;
        }
        else if (minutes <= 2529) {
            number = 1;
            measure = Day;
        }
        else if (minutes <= 43199) {
            number = days;
            measure = Days;
        }
        else if (minutes <= 86399) {
            number = 1;
            measure = Month;
            modifier = beConcise ? modifier : About;
        }
        else if (minutes <= 525599) {
            number = months;
            measure = Months;
        }
        else {
            number = years;
            measure = number == 1 ? Year : Years;
            if (remainder < 131400) {
              modifier = beConcise ? modifier : About;;
            } else if (remainder < 394200) {
              modifier = Over;
            } else {
              ++number;
              measure = Years;
              modifier = Almost;
            }
        }
        if (modifier.equalsIgnoreCase(JustNow))
        {
            return modifier;
        } else
        {
            if (modifier.length() > 0) {
                modifier += " ";
            }
            return modifier + number + " " + measure + " " + direction;
        }
    }
}
