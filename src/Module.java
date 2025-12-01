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
        this.moduleID = moduleID;
        this.hoursPerWeek = hoursPerWeek;

        this.students = new ArrayList<>();
        this.labGroups = new ArrayList<>();
        this.tutGroups = new ArrayList<>();

    }
    // fills out the rest of the information from sessions.csv
    public void completeModule(Lecturer[] lecturers, int[] studentCaps, String[] roomReqs){
        this.lecturers = lecturers;
        this.studentCaps = studentCaps;
        this.roomReqs = roomReqs;
    }

    // boring adders

    public void addStudent(Student student){
        students.add(student);
    }

    public void addLecture(Lecture lecture){
        this.lecture = lecture;
    }

    public void addLab(Lab lab){
        this.labGroups.add(lab);
    }

    public void addTutorial(Tutorial tutorial){
        this.tutGroups.add(tutorial);
    }

    // boring getters

    public String getModuleID(){
        return moduleID;
    }

    public int[] getHoursPerWeek(){
        return hoursPerWeek;
    }

    public Lecturer[] getLecturers(){
        return lecturers;
    }

    public ArrayList<Student> getStudents(){
        return students;
    }

    public Lecture getLecture(){
        return lecture;
    }

    public ArrayList<Lab> getLabs(){
        return labGroups;
    }

    public ArrayList<Tutorial> getTutorials(){
        return tutGroups;
    }

    // and just in case we need to reset
    // empties lectures, labs, and tutorials
    public void resetModule(){
        lecture = null;
        students = null;
        labGroups = null;
        tutGroups = null;
    }

}
