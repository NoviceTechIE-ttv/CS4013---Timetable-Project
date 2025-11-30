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
        this.studentID = studentID;
        this.programmeID = programmeID;
        this.year = year;
        this.semester = semester;
        this.labs = new ArrayList<>();
        this.lectures = new ArrayList<>();
        this.tutorials = new ArrayList<>();
    }

    // boring adders

    public void addLecture(Lecture lecture){
        lectures.add(lecture);
    }

    public void addLab(Lab lab){
        labs.add(lab);
    }

    public void addTutorial(Tutorial tutorial){
        tutorials.add(tutorial);
    }

    // boring getters

    public String getStudentID(){
        return studentID;
    }

    public ArrayList<Lecture> getLectures(){
        return lectures;
    }

    public ArrayList<Lab> getLabs(){
        return labs;
    }

    public ArrayList<Tutorial> getTutorials(){
        return tutorials;
    }

    // and just in case we need to reset
    // empties lectures, labs, and tutorials
    public void resetStudent(){
        lectures.clear();
        labs.clear();
        tutorials.clear();
    }

    @Override
    public String toString(){
        return "Student ID :" + studentID + ", Programme ID :" + programmeID + ", Year :" + year + ", Semester :" + semester;
    }
}
