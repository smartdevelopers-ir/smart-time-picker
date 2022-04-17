package ir.smartdevelopers.smartwheeltimepicker;

public class Time {
    public static final String AM="am";
    public static final String PM="pm";
    public int hour;
    public int minute;
    public String am_pm;
    public int get24FormatHour(){
        int hour24Format=hour;
        if (Time.AM.equals(am_pm)){
            if (hour == 12){
                hour24Format=0;
            }
        }else {
            if (hour!=12){
                hour24Format=hour+12;
            }
        }
        return hour24Format;
    }
}
