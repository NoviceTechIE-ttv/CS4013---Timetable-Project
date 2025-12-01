import java.util.ArrayList;

public class Lecturer {
    private String lecturerID;
    private ArrayList<Session> sessions;

    // constructor only needs the ID, sessions will be added later
    public Lecturer(String lecturerID){
        this.lecturerID = lecturerID;
        this.sessions = new ArrayList<>();
    }

    // boring adders

    public void addSession(Session session){
        this.sessions.add(session);
    }

    // boring getters

    public String getLecturerID(){
        return this.lecturerID;
    }

    public ArrayList<Session> getSessions(){
        return this.sessions;
    }

    // and in case we need to reset
    // empties our sessions ArrayList
    public void resetLecturer(){
        this.sessions.clear();
    }

    @Override
    public String toString(){
        return "Lecturer ID :" + lecturerID;
    }
}
