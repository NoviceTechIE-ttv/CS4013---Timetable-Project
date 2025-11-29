import java.util.ArrayList;

public class Lab {
    private ArrayList<Session> sessions; // see bottom of Timetable for explanation of sessions

    // basic constructor
    public Lab(ArrayList<Session> sessions){
        this.sessions = new ArrayList<>(sessions);
    }

    
    // represents all lab sessions for all students in the module
    // will only ever make the lab if the sessions are already figured out
    // no need for setters as nothing will ever change

    // boring getters

    public ArrayList<Session> getSessions(){
            return sessions;
    }
}
