import java.util.ArrayList;

public class Student {
    // first what we read in from the file
    private String studentID;
    private String programmeID;
    private int year;
    private int semester;
    // the Lab, Lecture, and Tutorial objects that the student is in
    private ArrayList<Lab> labs;
    private ArrayList<Lecture> lectures;
    private ArrayList<Tutorial> tutorials;

    // Constructor for Student that takes in everything read in from CSV
    // and initializes its ArrayLists
    public Student(String studentID, String programmeID, int year, int semester){

    }

    // boring adders

    public void addLecture(Lecture lecture){

    }

    public void addLab(Lab lab){

    }

    public void addTutorial(Tutorial tutorial){

    }

    // boring getters

    public String getStudentID(){

    }

    public ArrayList<Lecture> getLectures(){

    }

    public ArrayList<Lab> getLabs(){

    }

    public ArrayList<Tutorial> getTutorials(){

    }

    // and just in case we need to reset
    // empties lectures, labs, and tutorials
    public void resetStudent(){

    }
}
