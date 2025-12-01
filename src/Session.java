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
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.moduleCode = moduleCode;
        this.type = type;
        this.group = group;
        this.room = room;
        this.lecturer = lecturer;
    }
    // and one without where it is set to null
    public Session(int day, int startTime, int endTime, String moduleCode, String type, Room room, Lecturer lecturer){
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.moduleCode = moduleCode;
        this.type = type;
        this.room = room;
        this.lecturer = lecturer;
    }

    // checks if this session overlaps with a given duration
    // returns true if there is an overlap
    public boolean checkOverlap(int day, int startTime, int endTime){
        if (this.day != day) {
            return false;
        }

        return this.startTime < endTime && startTime < this.endTime;
    }

    // no setters or adders as the session will not change

    // boring getters
    public int getDay(){
        return day;
    }

    public int getStartTime(){
        return startTime;
    }

    public int getEndTime(){
        return endTime;
    }

    public String getModuleCode(){
        return moduleCode;
    }

    public String getType(){
        return type;
    }

    public int getGroup(){
        return group;
    }

    public Room getRoom(){
        return room;
    }

    public Lecturer getLecturer(){
        return lecturer;
    }

    @Override
    public String toString(){
        return moduleCode + "\n" + type + "\n" + group + "\n" + startTime + " - " + endTime;
    }
}
