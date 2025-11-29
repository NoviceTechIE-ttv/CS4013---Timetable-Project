import java.util.ArrayList;

public class Module {
    // first what we read in from modules.csv
    private String moduleID;
    private int[] hoursPerWeek; // the hours per week a student will spend in lecture/lab/tutorial
    // then what we read in from our sessions.csv
    // they will be used to initialize Lectures, Labs, and Tutorials
    private Lecturer[] lecturers; // the lecturer for the lectures/labs/tutorials
    private int[] studentCaps; // the most students that can be in a group for lectures/labs/tutorials
    private String[] roomReqs; // the required room type for the lectures/labs/tutorials
    // when a student is added to the timetable, they also get added to every module they are in
    private ArrayList<Student> students; // all students taking this module
    // the Lecture, Lab, and Tutorial objects that are part of the module
    private Lecture lecture; // the Lecture object for this module
    private ArrayList<Lab> labGroups; // the Lab objects for this module
    private ArrayList<Tutorial> tutGroups; // the Tutorial objects for this module

    // initial constructor, fills out info from modules.csv
    public Module(String moduleID, int[] hoursPerWeek){

    }
    // fills out the rest of the information from sessions.csv
    public void completeModule(Lecturer[] lecturers, int[] studentCaps, String[] roomReqs){

    }

    // boring adders

    public void addStudent(Student student){

    }

    public void addLecture(Lecture lecture){

    }

    public void addLab(Lab lab){

    }

    public void addTutorial(Tutorial tutorial){

    }

    // boring getters

    public String getModuleID(){

    }

    public int[] getHoursPerWeek(){

    }

    public Lecturer[] getLecturers(){

    }

    public ArrayList<Student> getStudents(){

    }

    public Lecture getLecture(){

    }

    public ArrayList<Lab> getLabs(){

    }

    public ArrayList<Tutorial> getTutorials(){

    }

    // and just in case we need to reset
    // empties lectures, labs, and tutorials
    public void resetModule(){

    }

}
