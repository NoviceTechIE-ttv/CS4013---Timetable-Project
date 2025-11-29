public class Session {
    private int day; // what day of the week the session is on (m-f --> 0-4)
    private int startTime; // what hour the session starts (9a-6p --> 0-9)
    private int endTime; // what hour the session ends (9a-6p --> 0-9)
    private String moduleCode; // what module this session is a part of
    private String type; // "LEC" "LAB" or "TUT"
    private int group; // if the lab is split into groups 0, 1, 2, this is which of those groups are in this lab
    private Room room; // the room the session takes place in
    private Lecturer lecturer; // the lecturer teaching the session

    // need one constructor with a group
    public Session(int day, int startTime, int endTime, String moduleCode, String type, int group, Room room, Lecturer lecturer){

    }
    // and one without where it is set to null
    public Session(int day, int startTime, int endTime, String moduleCode, String type, Room room, Lecturer lecturer){

    }

    // checks if this session overlaps with a given duration
    // returns true if there is an overlap
    public boolean checkOverlap(int day, int startTime, int endTime){

    }

    // no setters or adders as the session will not change

    // boring getters
    public int getDay(){

    }

    public int getStartTime(){

    }

    public int getEndTime(){

    }

    public String getModuleCode(){

    }

    public String getType(){

    }

    public int getGroup(){

    }

    public Room getRoom(){

    }

    public Lecturer getLecturer(){

    }
}
