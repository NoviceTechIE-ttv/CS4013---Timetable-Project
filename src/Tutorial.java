import java.util.ArrayList;

public class Tutorial {
    private ArrayList<Session> sessions; // see bottom of Timetable for explanation of sessions

    // basic constructor
    public Tutorial(ArrayList<Session> sessions){
        this.sessions = new ArrayList<>(sessions);
    }

    // represents all tutorial sessions for all students in the module
    // will only ever make the tutorial if the sessions are already figured out
    // no need for setters as nothing will ever change

    // boring getters

    public ArrayList<Session> getSessions(){
        return sessions;
    }
}
