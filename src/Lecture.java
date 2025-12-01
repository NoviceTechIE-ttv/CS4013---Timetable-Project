import java.util.ArrayList;

public class Lecture {
    private ArrayList<Session> sessions; // see bottom of Timetable for explanation of sessions

    // basic constructor
    public Lecture(ArrayList<Session> sessions){
        this.sessions = new ArrayList<>(sessions);
    }

    // represents all lecture sessions for all students in the module
    // will only ever make the lecture if the sessions are already figured out
    // no need for setters as nothing will ever change

    // boring getters

    public ArrayList<Session> getSessions(){
        return sessions;
    }
}
