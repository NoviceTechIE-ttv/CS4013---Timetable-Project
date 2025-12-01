import java.util.ArrayList;

public class Student {
    // first what we read in from the file
    private String studentID;
    private String programmeID;
    private int year;
    private int semester;
    // the Lab, Lecture, and Tutorial objects that the student is in
    private ArrayList<Session> sessions;

    // Constructor for Student that takes in everything read in from CSV
    // and initializes its ArrayLists
    public Student(String studentID, String programmeID, int year, int semester){
        this.studentID = studentID;
        this.programmeID = programmeID;
        this.year = year;
        this.semester = semester;
        this.sessions = new ArrayList<>();
    }

    // boring adders

    public void addSession(Session session){
        sessions.add(session);
    }

    // boring getters

    public String getStudentID(){
        return studentID;
    }

    public ArrayList<Session> getSessions(){
        return sessions;
    }

    // and just in case we need to reset
    // empties lectures, labs, and tutorials
    public void resetStudent(){
        sessions.clear();
    }

    @Override
    public String toString(){
        return "Student ID :" + studentID + ", Programme ID :" + programmeID + ", Year :" + year + ", Semester :" + semester;
    }
}
