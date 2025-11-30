public class Programme {
    private String programmeID;
    private String[][][] modulesBySemester; // what modules a student takes in what year and what semester

    // programmes exist to store what they are called
    // and a list of what modules they include and when they are taken
    // it does not change after creation and mostly serves to act as a key
    // for what modules are in what programme

    // basic constructor
    public Programme(String programmeID, String[][][] modulesBySemester){
        this.programmeID = programmeID;
        this.modulesBySemester = modulesBySemester;
    }

    // boring getters
    public String getProgrammeID(){
        return  this.programmeID;
    }

    // returns the list of modules a student would take if they were in this programme
    // in their yearth year and semesterth semester
    public String[] getModulesForSemester(int year, int semester){
        return  this.modulesBySemester[year][semester];
    }

}
