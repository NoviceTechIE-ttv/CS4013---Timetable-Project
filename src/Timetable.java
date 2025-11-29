import java.util.ArrayList;

public class Timetable {
    private ArrayList<Room> facilities;
    private ArrayList<Programme> programmes;
    private ArrayList<Module> bookOfModules;
    private ArrayList<Lecturer> lecturerBody;
    private ArrayList<Student> studentBody;

    // first readCSVs
    // then addSessions
    // then getMasterTimetable
    // if getMasterTimetable returns false, resetTimetable and try again like 3 times
    // then allow for queries
    public static void main(String args[]){

    }

    // METHODS FOR READING IN DATA

    // reads in all csvs and passes data to the functions immediately below
    // so everything ends up in the right place
    // order to read in: rooms.csv, programmes.csv, modules.csv, sessions.csv, students.csv
    public void readCSVs(){

    }

    public void addRoom(){

    }

    public void addProgramme(){

    }

    // creates a module using the information from modules.csv
    // and adds it to bookOfModules
    public void addModule(){

    }

    // adds the information from sessions.csv to the relevant module
    public void completeModule(){

    }

    // helper method for completeModule which
    public void addLecturer(String lecturerID){

    }

    // adds a student to studentBody using information from students.csv
    // also adds that student to all of their modules
    public void addStudent(String studentID, String programmeID, int year, int semester){

    }

    // METHODS FOR SEARCHING BY IDS

    public Module getModuleByID(String moduleID){

    }

    public Programme getProgrammeByID(String programmeID){

    }

    public Student getStudentByID(String studentID){

    }

    public Lecturer getLecturerByID(String lecturerID){

    }

    public Room getRoomByID(String roomID){

    }

    // METHODS FOR SESSION GENERATION

    // Here's the hard stuff
    // Now we have to populate all the Lecture, Lab, and Tutorial Sessions
    // and disseminate that information to the relevant Modules, Lecturers, Students, and Rooms

    // goes through every Module and creates their Lectures, Labs, and Tutorials
    // if any return false, return false
    // return true after finished adding Lectures, Labs, and Tutorials for all Modules
    public boolean addSessions(){

    }

    // grabs the Students and relevant Lecturer from a module as well as any room reqs or student caps
    // then gets a random room, checks if they're compatible, grabs a time slot from that room
    // and sees if the time slot works for the Students and Lecturer
    // if it does, it makes a Lecture, gives it a Session, and adds the Lecture to the relevant objects
    // as outlined above and return true
    // if it doesn't, grab a new room and a new time and try again. If it doesn't work after like 5-10
    // "good" attempts (the room was an adequate capacity and type), split the x-hour long lecture into
    // x, hour long lectures and try each one individually. They don't all have to be in the same room
    public boolean addLectures(Module module){

    }
    // pretty similar to addLectures but for Labs
    // but instead of adding one Lab for the Module, split the Students into n groups of m Students where
    // n = (#students in module/student cap) rounded up and m = (#students/n)
    // then do what we did with addLectures but only with the first m Students in the module's student array
    // then the next m etc etc
    // only return true after all Labs are added, if any fail after 5-10 good attempts, return false
    public boolean addLabs(Module module){

    }
    // almost identical to addLabs but for Tutorials
    public boolean addTutorials(Module module){

    }

    // deletes all Lectures, Labs, Tutorials, and Sessions in every Module, Student, Lecturer, and Room
    // so that we can populate again
    public void resetTimetable(){

    }

    // METHODS FOR ANSWERING QUERIES AND GENERATING TIMETABLES
    // they all take in a two dimensional array of Session ArrayLists
    // these represent the day and then the hour and then a list of all the sessions happening then
    // even though a Student and a Lecturer only have at most 1 session in any given time slot
    // we still use an ArrayList of Sessions instead of a Session for consistency

    // generates a timetable for a student
    public ArrayList<Session>[][] getStudentTimetable(String studentID){

    }

    // generates a timetable for a lecturer
    public ArrayList<Session>[][] getLecturerTimetable(String lecturerID){

    }

    // generates a timetable for a module
    public ArrayList<Session>[][] getModuleTimetable(String moduleID){

    }

    // generates the timetable for a given year and semester of a programme
    public ArrayList<Session>[][] getGroupTimetable(String programmeID, int year, int semester){

    }

    // generates the timetable for every session being offered
    public ArrayList<Session>[][] getMasterTimetable(){

    }

    // takes in a timetable and turns it into a string
    public String toString(ArrayList<Session>[][] sessions){

    }
}




// ORDER IS ALWAYS LECTURE, LAB, TUTORIAL
// we will assume for the moment that every module has >= 1hr/wk of lectures, labs, and tutorials

// What exactly are Lectures, Labs, Tutorials, and Sessions?
// A Session is an instance of a Lecture, Lab, or Tutorial, with a specific room and a start and end time
// For example, we have a weekly OOD Lecture on Wednesdays at 11-12 in CSG001. This would be a session.
// The Lecture in this case would include both the session on Wednesdays in CSG001 and the session in ERB on Thursdays
// Labs and Tutorials are a bit more complicated - much like Lectures they can have multiple sessions
// But a Module will also often have multiple Labs and multiple Tutorials
// These are easiest to equate with Lab "groups", i.e. if you are in Lab A then you will be with the same
// subset of the class for both sessions of that lab and those in Lab B will have completely different sessions
// with different students at a different time and likely in a different room
// All of this to say, it is important to distinguish between Lecture/Lab/Tutorial objects and Session objects
// Even if a Lecture only has one Session